package org.makumba.providers.query.mql;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.hibernate.hql.antlr.HqlTokenTypes;
import org.makumba.FieldDefinition;
import org.makumba.OQLParseError;
import org.makumba.providers.QueryAnalysisProvider;
import org.makumba.providers.datadefinition.mdd.MakumbaDumpASTVisitor;

import antlr.RecognitionException;
import antlr.collections.AST;

/**
 * Function inliner based on the 2nd pass MQL query analysis. Performs tree transformation on the original AST necessary
 * to pass it on to the regular 2nd pass analysis.
 * 
 * @author Manuel Gay
 * @version $Id: FunctionInliner.java,v 1.1 Aug 3, 2009 1:06:27 PM manu Exp $
 */
public class FunctionInliner {
    
    private final boolean debug = true;

    private String query;
    
    private MqlTreePrinter printer;

    private HqlASTFactory fact = new HqlASTFactory();
    
    private List<String> parameterOrder = new ArrayList<String>();

    private MakumbaDumpASTVisitor v = new MakumbaDumpASTVisitor(false);
    
    public FunctionInliner(String query, QueryAnalysisProvider qp) {
        this.query = query;
        this.printer = new MqlTreePrinter(qp);
    }
    
    public static String inlineQuery(String query, QueryAnalysisProvider qp) {
        FunctionInliner inliner = new FunctionInliner(query, qp);
        return inliner.inline(query, qp);
    }
    
    private String inline(String query, QueryAnalysisProvider qp) {
        
        if(debug) {
            System.out.println("===== inlining query " + query);
        }
        
        String originalQuery = query;
        
        if(qp instanceof MqlQueryAnalysisProvider) {

            if(query.startsWith("###")) {
                query= query.substring(query.indexOf('#', 3)+3);
            }
            query = MqlQueryAnalysis.preProcess(query);
            
            if (query.toLowerCase().indexOf("from") == -1) {
                query += " FROM org.makumba.db.makumba.Catalog c";
            }
        }
        
        HqlParser parser=null;
        try{
            parser = HqlParser.getInstance(query);
            parser.statement();
        }catch(Throwable t) {
            doThrow(t, parser!=null?parser.getAST():null, query);
        }
        doThrow(parser.error, parser.getAST(), query);
        
        AST ast = parser.getAST();
        
        if(qp instanceof MqlQueryAnalysisProvider) {
            MqlQueryAnalysisProvider.transformOQLParameters(ast, parameterOrder);
            MqlQueryAnalysisProvider.transformOQL(ast);
        }
        
        boolean inlined = false;
        
        try {
            inlined = inline(ast, true);
        } catch(Throwable t) {
            doThrow(t, ast, query);
        }
        
        if(!inlined) {
            return originalQuery;
        }
        
        if(debug) {
            System.out.println("===== inlined ast");
            v.visit(ast);
        }
        
        String res = printer.printTree(ast);
        
        return res;
    }
    
    protected void doThrow(Throwable t, AST debugTree, String query) {
        if (t == null)
            return;
        if (t instanceof RuntimeException) {
            t.printStackTrace();
            throw (RuntimeException) t;
        }
        String errorLocation = "";
        String errorLocationNumber="";
        if (t instanceof RecognitionException) {
            RecognitionException re = (RecognitionException) t;
            if (re.getColumn() > 0) {
                errorLocationNumber= " column "+re.getColumn()+" of ";
                StringBuffer sb = new StringBuffer();
                sb.append("\r\n");

                for (int i = 0; i < re.getColumn(); i++) {
                    sb.append(' ');
                }
                sb.append('^');
                errorLocation = sb.toString();
            }
        }
        throw new OQLParseError("\r\nin "+errorLocationNumber+" query (during inlining of functions):\r\n" + query + errorLocation+errorLocation+errorLocation, t);
    }

    
    
    
    private boolean inline(AST ast, boolean root) throws Throwable {
        
        boolean inlined = false;

        // search all method calls in this AST
        ArrayList<MethodCall> methodCalls = findMethodCalls(ast, ast, null, null, null, false,
            new ArrayList<MethodCall>());
        
        if(methodCalls.size() == 0) {
            return false;
        }


        if(debug) {
            System.out.println("** inlining tree of " + (root?"query ":"function ") + printer.printTree(ast));
            v.visit(ast);
        }

        // let's analyze this function with a magic analyzer that accepts function calls
        // FIXME in case of SemanticExceptions, fetch them and append meaningful text
        MqlSqlWalker mqlAnalyzer = new MqlSqlWalker(printer.printTree(ast), null, true, true, true);

        try {
            mqlAnalyzer.statement(ast);
        } catch(Throwable t) {
            t.printStackTrace();
            System.out.println("in AST");
            v.visit(ast);
        }
        
        if(mqlAnalyzer.error != null) {
            mqlAnalyzer.error.printStackTrace();
            throw new Throwable(mqlAnalyzer.error);
        }

        //for(MethodCall m : methodCalls) {
        //    System.out.println(m);
        //}

        // now we traverse all the function calls
        if (mqlAnalyzer.orderedFunctionCalls.isEmpty()) {
            return false;
        } else {

            // the analyser inverts the function calls of functions that are arguments of another function
            // but we don't have this in the first-pass method calls
            // so we re-order these calls here
            LinkedHashMap<String, FunctionCall> orderedFunctionCalls = new LinkedHashMap<String, FunctionCall>();
            ArrayList<FunctionCall> f = new ArrayList<FunctionCall>();
            for (String key : mqlAnalyzer.orderedFunctionCalls.keySet()) {
                FunctionCall c = mqlAnalyzer.orderedFunctionCalls.get(key);
                if (c.isFunctionArgument()) {
                    f.add(c);
                    mqlAnalyzer.orderedFunctionCalls.remove(c);
                } else {
                    orderedFunctionCalls.put(key, c);
                    if (!f.isEmpty()) {
                        // add the function calls back again, in reverse order
                        for (int i = f.size() - 1; i >= 0; i--) {
                            orderedFunctionCalls.put(f.get(i).getKey(), f.get(i));
                        }
                    }
                }
            }

            mqlAnalyzer.orderedFunctionCalls = orderedFunctionCalls;
            
            //for(FunctionCall c : orderedFunctionCalls.values()) {
            //    System.out.println(c);
            //}

            int index = 0;
            for (FunctionCall c : mqlAnalyzer.orderedFunctionCalls.values()) {
                if (c.isFunctionArgument() || c.isMQLFunction()) {
                    // we don't do anything here because we either inline this function when we process the parent function,
                    // or don't care about it because it is a native MQL function
                    index++;
                    continue;
                }
                
                inlined = true;

                if(debug) {
                    System.out.println("*** Iterating over function call " + c);
                }
                
                // if this is an actor function, we need to inline it differently than other functions
                if (c.isActorFunction()) {
                    processActorFunction(c, ast, methodCalls.get(index));
                } else {
                    AST queryFragmentTree = fact.dupTree(c.getFunction().getParsedQueryFragment());
                    //v.visit(queryFragmentTree);
    
                    // apply oql-specific tree transformations on the function tree
                    List<String> parameterOrder = new ArrayList<String>();
                    MqlQueryAnalysisProvider.transformOQLParameters(queryFragmentTree, parameterOrder);
                    MqlQueryAnalysisProvider.transformOQL(queryFragmentTree);
    
                    if(debug) {
                        System.out.println("*** query fragment tree before replacing arguments");
                        v.visit(queryFragmentTree);
                    }
                    
                    replaceArgsAndThis(queryFragmentTree, queryFragmentTree, c, null, null, mqlAnalyzer, false, null, null);
                    
                    if(debug) {
                        System.out.println("*** query fragment after replacing arguments");
                        v.visit(queryFragmentTree);
                    }
    
                    // now we inline all the functions of the resulting tree
                    inline(queryFragmentTree, false);
    
                    // replace method call in original tree
                    // here we use the 1st pass method calls and rely on the fact that the order with the function calls is the same
                    replaceMethodCall(methodCalls.get(index), fact.dupTree(queryFragmentTree), methodCalls, c);
                    
                    if(debug) {
                        System.out.println("*** query tree after method call inlining");
                        v.visit(ast);
                    }

                    
                }

                //System.out.println("*** inlined query tree");
                //v.visit(ast);

                index++;
            }

            // finally, go over the tree and remove unused labels
            Hashtable<String, String> usedLabels = new Hashtable<String, String>();
            collectUsedLabels(ast, usedLabels);

            AST originalRange = findFrom(ast).getFirstChild();

            AST parent = null;
            while (originalRange != null) {
                String path = getPath(originalRange.getFirstChild().getNextSibling());
                if (!usedLabels.containsKey(path)) {
                    if (parent != null) {
                        parent.setNextSibling(originalRange.getNextSibling());
                    }
                } else {
                    parent = originalRange;
                }
                originalRange = originalRange.getNextSibling();
            }
            
            if(debug) {
                System.out.println("*** query tree after remvoing unused labels");
                v.visit(ast);

            }
            
            
            return inlined;
        }
        
        
    }

    /**
     * walks the query fragment tree and:
     * <ul>
     * <li>replaces "this" with the fitting label</li>
     * <li>replaces function arguments with their values</li>
     * <li>inlines arguments that are function calls</li>
     * <li>replaces arguments with parameters if the value is a parameter</li>
     * </ul>
     */
    private void replaceArgsAndThis(AST tree, AST root, FunctionCall c, FunctionCall parentCall, AST parent,
            MqlSqlWalker mqlAnalyzer, boolean firstChild, String lastAlias, AST currentFrom) throws Throwable {

        if (tree == null) {
            return;
        }

        // check if number of provided arguments is ok
        if (tree == root) {
            int expectedArgs = c.getFunction().getParameters().getFieldNames().size();
            int providedArgs = c.getOrderedArguments().size();
            if (expectedArgs != providedArgs) {
                throw new OQLParseError("Function '" + c.getFunction().toString() + "' is expecting " + expectedArgs
                        + " arguments, but " + providedArgs + " were provided");
            }
        }

        // we set the last alias of the FROM to be able to replace the "this" declarations in the query fragment
        if (lastAlias == null) {
            AST range = findFrom(tree).getFirstChild();

            // try to fetch the first FROM alias and if it's generated, replace it with something meaningful.
            // we get generated aliases because of the fake query we build in order to analyse the query fragments
            AST l = range.getFirstChild();
            AST a = l.getNextSibling();

            if (a != null && a.getText().equals("makumbaGeneratedAlias")) {
                String path = c.getPath();
                
                // we have a path that refers to pointed types so we have to expand the FROM section to account for those pointed types
                if (path.indexOf(".") > -1) {
                    // replace the RANGE elements: first the type, then the alias
                    l = ASTUtil.makeNode(HqlTokenTypes.IDENT, c.getParentType().getName());
                    a = ASTUtil.makeNode(HqlTokenTypes.ALIAS, path.substring(0, path.indexOf(".")));
                    range.setFirstChild(l);
                    l.setNextSibling(a);
                    
                    if (lastAlias == null) {
                        lastAlias = expandRangeElement(c.getPath(), a, range);
                    }
                    path = path.substring(0, path.indexOf("."));
                } else {
                    lastAlias = c.getPath();
                    a.setText(c.getPath());
                }
            } else {
                lastAlias = a.getText();
            }
        }

        switch (tree.getType()) {

            case HqlTokenTypes.FROM:
                currentFrom = tree;
                break;

            case HqlTokenTypes.IDENT:

                FieldDefinition argument = c.getFunction().getParameters().getFieldDefinition(tree.getText());

                if (tree.getText().equals("this")) {

                    if (c.getPath().indexOf(".") > -1 && lastAlias != null) {
                        tree.setText(lastAlias);
                    } else {
                        tree.setText(c.getPath());
                    }

                } else if (argument != null) {
                    int argumentIndex = c.getFunction().getParameters().getFieldNames().indexOf(tree.getText());

                    MqlNode arg = c.getOrderedArguments().get(argumentIndex);

                    boolean isParameter = arg.getType() == HqlSqlTokenTypes.NAMED_PARAM
                            || arg.getType() == HqlSqlTokenTypes.PARAM;

                    // if this is a function call itself we will need to inline it
                    if (arg.getText().startsWith("methodCallPlaceholder_")) {
                        
                        replaceFunctionArgument(root, c, parent, mqlAnalyzer, firstChild, currentFrom, argument, arg);
                    
                    } else {

                        Node n = null;

                        if (isParameter) {
                            // we can't replace the node with the parameter node because it is transformed by the 2nd pass analysis
                            // so we re-build our parameter node...
                            n = ASTUtil.makeNode(HqlTokenTypes.COLON, ":");
                            Node paramChild = ASTUtil.makeNode(HqlTokenTypes.IDENT, arg.getOriginalText());
                            n.setFirstChild(paramChild);
                            n.setNextSibling(tree.getNextSibling());
                            n.setCol(tree.getColumn());
                            n.setLine(tree.getLine());

                        } else if (!isParameter) {
                            // we have an argument of the function call that we need to replace with the value passed in the function call
                            
                            MqlNode type = arg;
                            checkArgumentType(argument, type, false);

                            // buid new argument node
                            n = ASTUtil.makeNode(arg.getType(), arg.getText());
                            n.setFirstChild(arg.getFirstChild());
                            n.setNextSibling(tree.getNextSibling());
                            n.setCol(tree.getColumn());
                            n.setLine(tree.getLine());

                        }

                        // we don't forget to replace the current tree for the recursive calls
                        tree = n;

                        if (parent != null) {

                            if (firstChild) {
                                parent.setFirstChild(n);
                            } else {
                                parent.setNextSibling(n);
                            }
                        }
                    }
                }

                break;

        }

        replaceArgsAndThis(tree.getFirstChild(), root, c, parentCall, tree, mqlAnalyzer, true, lastAlias, currentFrom);
        replaceArgsAndThis(tree.getNextSibling(), root, c, parentCall, tree, mqlAnalyzer, false, lastAlias, currentFrom);

    }

    /** replaces a function that is argument of another function in the query fragment tree */
    private void replaceFunctionArgument(AST root, FunctionCall c, AST parent, MqlSqlWalker mqlAnalyzer,
            boolean firstChild, AST currentFrom, FieldDefinition argument, MqlNode arg) throws Throwable {
        
        String key = arg.getText().substring("methodCallPlaceholder_".length());
        FunctionCall cArg = mqlAnalyzer.orderedFunctionCalls.get(key);
        //System.out.println("Argument is function call on " + cArg);

        AST inlinedFunction = inlineArgument(cArg, mqlAnalyzer, c);

        // now we check the type of this function
        // for this we need to call a dummy 2nd pass analyser on the tree
        AST inlinedFunctionType = fact.dupTree(inlinedFunction);
        AST select = findSelect(inlinedFunctionType);
        AST content = select.getFirstChild();
        AST last = null;
        String projectionLabel = "_mak_expr_";

        if (content.getType() != HqlTokenTypes.AS) {
            // add an "as" in order to be able to grab the expression later on
            AST as = ASTUtil.makeNode(HqlTokenTypes.AS, "as");
            as.setFirstChild(content);

            AST expr = ASTUtil.makeNode(HqlTokenTypes.IDENT, projectionLabel);
            last = ASTUtil.getLastChild(select);
            last.setNextSibling(expr);
            select.setFirstChild(as);
        } else {
            last = ASTUtil.getLastChild(content);
            projectionLabel = last.getText();
        }

        
        // FIXME we need much better error handling here
        MqlSqlWalker analyzer = new MqlSqlWalker(query, null, true, true, true);
        analyzer.statement(inlinedFunctionType);

        if(mqlAnalyzer.error != null) {
            throw new Throwable(mqlAnalyzer.error);
        }

        
        MqlNode t = analyzer.rootContext.projectionLabelSearch.get(projectionLabel);
        checkArgumentType(argument, t, true);

        // replace the method call the same way we do it in normal queries
        AST where = findWhere(root);
        MethodCall mc = new MethodCall(firstChild, root, parent, currentFrom, where);
        replaceMethodCall(mc, inlinedFunction, null, null);
    }

    /**
     * Checks if a function argument is of the right type
     */
    private void checkArgumentType(FieldDefinition argument, MqlNode type, boolean functionCall) throws OQLParseError {

        if (type.getType() == HqlSqlTokenTypes.COLON) {
            return;
        }
        if (!argument.isAssignableFrom(type.getMakType())) {

            // FIXME here we don't have the column of the argument
            // because we fetch it from the analyzer
            // but that requires quite some changes over there

            StringBuffer sb = new StringBuffer();
            sb.append("\r\n");

            for (int i = 0; i < type.getColumn(); i++) {
                sb.append(' ');
            }
            sb.append('^');
            String errorLocation = sb.toString();

            String error = "Invalid argument type: expecting " + argument.getType();
            if (functionCall) {
                error += ", but argument is of type " + type.getMakType().getType();
            } else {
                error += ", argument '" + type.getText() + "' is of type " + type.getMakType().getType();
            }

            throw new OQLParseError(error + "\r\nin query:\r\n" + query + errorLocation + errorLocation
                    + errorLocation);
        }
    }

    /**
     * expands the FROM section with a given path, a simple range element becomes
     * 
     * <pre>
     * RANGE [84]
     *   ParserTest [120]
     *   t [69]
     * RANGE [84]
     *   . [15]
     *     t [120]
     *     other [120]
     *   x0 [69]
     * </pre>
     */
    private String expandRangeElement(String path, AST tree, AST range) {

        String lastLabel = "";
        StringTokenizer tk = new StringTokenizer(path, ".");
        while (tk.hasMoreElements()) {

            String a = tk.nextToken();
            String b = tk.nextToken();
            String label = createLabel();

            Node r = ASTUtil.makeNode(HqlTokenTypes.RANGE, "RANGE");
            Node dot = ASTUtil.makeNode(HqlTokenTypes.DOT, ".");
            Node aNode = ASTUtil.makeNode(HqlTokenTypes.IDENT, a);
            Node bNode = ASTUtil.makeNode(HqlTokenTypes.IDENT, b);
            Node labelNode = ASTUtil.makeNode(HqlTokenTypes.ALIAS, label);

            r.setFirstChild(dot);
            dot.setFirstChild(aNode);
            aNode.setNextSibling(bNode);
            dot.setNextSibling(labelNode);
            range.setNextSibling(r);

            lastLabel = label;
        }

        return lastLabel;

    }

    /** inlines a function that is argument of another function */
    private AST inlineArgument(FunctionCall functionCall, MqlSqlWalker mqlAnalyzer, FunctionCall parentCall) throws Throwable {
        AST i = fact.dupTree(functionCall.getFunction().getParsedQueryFragment());
        replaceArgsAndThis(i, i, functionCall, parentCall, null, null, false, null, null);
        return i;
    }

    /**
     * Takes care of inlining actor functions, which work somewhat differently when inlined in a query
     */
    private void processActorFunction(FunctionCall c, AST queryFragmentTree, MethodCall methodCall) {
        // check whether we just call the actor function, or if we call a field of the actor
        
        // FIXME this does not work: the methodCall tree is not containing the .field !!
        // so maybe the HQL parser is to modify? aaah!
        if (methodCall.getParent().getType() == HqlTokenTypes.DOT && methodCall.isFirstChild()) {
            // we have actor(Type).field
            // SELECT actor(test.Person).field
            // --> x.field FROM test.Person x WHERE ... AND x = $actor_test_Person
            // we just build a query tree of the kind SELECT x from Type t WHERE t = $actor_Type
            // and we let the replaceMethodCall take care of the rest
            // in fact we cheat a bit here because we use the fact that the actor() method call is before the . to just replace this part of the select
            // and hence keep the .field of the original tree

            String type = getActorType(c);

            Node a = ASTUtil.makeNode(HqlTokenTypes.QUERY, "query");
            a.setFirstChild(ASTUtil.makeNode(HqlTokenTypes.SELECT_FROM, "SELECT_FROM"));
            a.getFirstChild().setFirstChild(ASTUtil.makeNode(HqlTokenTypes.FROM, "FROM"));
            a.getFirstChild().getFirstChild().setFirstChild(ASTUtil.makeNode(HqlTokenTypes.RANGE, "RANGE"));
            a.getFirstChild().getFirstChild().getFirstChild().setFirstChild(ASTUtil.makeNode(HqlTokenTypes.IDENT, type));
            String alias = createLabel();
            a.getFirstChild().getFirstChild().getFirstChild().getFirstChild().setNextSibling(
                ASTUtil.makeNode(HqlTokenTypes.ALIAS, alias));

            a.getFirstChild().getFirstChild().setNextSibling(ASTUtil.makeNode(HqlTokenTypes.SELECT, "SELECT"));
            a.getFirstChild().getFirstChild().getNextSibling().setFirstChild(ASTUtil.makeNode(HqlTokenTypes.IDENT, alias));

            a.getFirstChild().setNextSibling(ASTUtil.makeNode(HqlTokenTypes.WHERE, "WHERE"));
            a.getFirstChild().getNextSibling().setFirstChild(ASTUtil.makeNode(HqlTokenTypes.EQ, "="));
            a.getFirstChild().getNextSibling().getFirstChild().setFirstChild(ASTUtil.makeNode(HqlTokenTypes.IDENT, alias));
            a.getFirstChild().getNextSibling().getFirstChild().getFirstChild().setNextSibling(
                ASTUtil.makeNode(HqlTokenTypes.COLON, ":"));
            a.getFirstChild().getNextSibling().getFirstChild().getFirstChild().getNextSibling().setFirstChild(
                ASTUtil.makeNode(HqlTokenTypes.IDENT, "actor_" + type.replaceAll("\\.", "_") + "###"
                        + parameterNumber()));

            //v.visit(a);

            // finally we replace the actor method call with the new tree
            replaceMethodCall(methodCall, a, null, null);

        } else {
            v.visit(methodCall.getRoot());
            // we have actor(Type)
            // SELECT actor(test.Person) --> $actor_test_Person

            String type = getActorType(c);

            // param tree looks like
            // : [116]
            // parameter###0 [120]
            AST actorParamNode = ASTUtil.makeNode(HqlTokenTypes.COLON, ":");
            AST actorParamChild = ASTUtil.makeNode(HqlTokenTypes.IDENT, "actor_" + type.replaceAll("\\.", "_") + "###"
                    + parameterNumber());
            actorParamNode.setFirstChild(actorParamChild);
            methodCall.replace(actorParamNode);
        }

    }

    private String getActorType(FunctionCall c) {
        String type = "";
        if (c.getOrderedArguments().size() == 0) {
            // call on the actor of the current type, i.e. actor()
            type = c.getParentType().getName();
        } else {
            type = c.getOrderedArguments().firstElement().getText();
        }
        return type;
    }

    /**
     * Replaces a method call in the first-pass tree with an inlined function, also adding the necessary elements to the
     * FROM and WHERE
     */
    private void replaceMethodCall(MethodCall methodCall, AST inlinedFunction, List<MethodCall> methodCalls, FunctionCall functionCall) {

        AST select = findSelectContent(inlinedFunction);
        
        if (select.getType() != HqlTokenTypes.QUERY) {

            // we need to add the FROM...WHERE part to the existing tree
            AST additionalFrom = findFrom(inlinedFunction);
            
            if (additionalFrom != null) {
                
                if(debug) {
                    System.out.println("FROM tree to add");
                    v.visit(additionalFrom);
                }
                
                if(functionCall != null && functionCall.getPath() != null) {
                    
                    String label = functionCall.getPath().substring(0, functionCall.getPath().indexOf(".") > 0 ? functionCall.getPath().indexOf(".") : functionCall.getPath().length());
                    
                    // if we have a constraint on the label of the function, we will not re-use the label
                    // UNLESS the function call is part of the WHERE in which case we don't really care about having a constraint on the label
                    // e.g. SELECT p.indiv.name AS col1,character_length(p.indiv.name) AS col2 FROM test.Person p WHERE p.nameMin3CharsLong()
                    if(methodCall.getWhere() != null && !functionCall.isInWhere()) {
                        if(debug) {
                            System.out.println("Checking if label " + label + " has a constraint in WHERE tree");
                            v.visit(methodCall.getWhere());
                        }
                        Boolean hasConstraint = hasIdentifier(methodCall.getWhere(), label, false);
                        if(hasConstraint) {
                            join(methodCall.getFrom(), fact.dupTree(additionalFrom), inlinedFunction, null);
                        }
                    } else {
                        join(methodCall.getFrom(), fact.dupTree(additionalFrom), inlinedFunction, label);
                    }
                } else {
                    join(methodCall.getFrom(), fact.dupTree(additionalFrom), inlinedFunction, null);
                }
            }

            AST additionalWhere = findWhere(inlinedFunction);

            if (additionalWhere != null) {
                // cut off the ORDER, if any
                additionalWhere.setNextSibling(null);

                // do we already have a where?
                if (methodCall.getWhere() != null) {
                    // we need to generate an AND and append the existing WHERE
                    AST and = ASTUtil.makeNode(HqlTokenTypes.AND, "and");
                    and.setFirstChild(methodCall.getWhere().getFirstChild());
                    and.getFirstChild().setNextSibling(additionalWhere.getFirstChild());

                    methodCall.getWhere().setFirstChild(and);

                } else {
                    // append the additional WHERE element
                    ASTUtil.getLastChild(methodCall.getRoot()).setNextSibling(additionalWhere);
                }
            }

            // TODO also take care of ORDER BY
        }
        
        
        
        // if we have two method calls that are siblings in a tree, the second one will have as parent the first call
        // when the first call will get replaced, and then the second one will want to be replaced
        // the reference to the parent of the 2nd method call won't be valid anymore
        // hence before replacing a method call, we have to update all the parent references in all method calls to the new parent
        
        if(methodCalls != null) {
          for(MethodCall m : methodCalls) {
              
              AST currentCall;
              if(methodCall.isFirstChild()) {
                  currentCall = methodCall.getParent().getFirstChild();
              } else {
                  currentCall = methodCall.getParent().getNextSibling();
              }
              
              if(m.getParent().equals(currentCall)) {
                  m.parent = select;
              }
          }
            
        }
        
        methodCall.replace(select);
    }

    /**
     * joins the elements of the first FROM with the second one, avoiding label collision.
     * in some cases, we might not want to avoid label collision but instead have label re-usage
     * in that case we replace the to-be-reused label in the additionalFrom before joining
     */
    private void join(AST from, AST additionalFrom, AST tree, String labelToReuse) {
        
        Hashtable<String, AST> existingLabels = getLabels(from);
        Hashtable<String, AST> newLabels = getLabels(additionalFrom);

        // we need to avoid label collision, i.e. if the same label is used in the additional FROM
        // we will replace it, as well as at the calls to it
        // to do so, we need to
        // - replace the label itself in the RANGE element
        // - scan all the further range elements, if they contain a reference to that label, replace it too
        // - scan all the identifiers in the query (a.b.c), if they contain a reference to the label (a), replace it too
        Hashtable<String, String> collisions = new Hashtable<String, String>();
        for (String l : newLabels.keySet()) {
            if (existingLabels.containsKey(l) && !l.equals(labelToReuse)) {
                String r = createLabel();
                collisions.put(l, r);
                // replace the label in the tree we want to join the existing FROM with
                newLabels.get(l).getFirstChild().getNextSibling().setText(r);
            } else if(existingLabels.containsKey(l) && l.equals(labelToReuse)) {
                // remove the duplicate RANGE element from the additionalFrom so we don't have it twice in the end
                AST additionalRange = additionalFrom.getFirstChild();
                AST parent = additionalRange;
                while(!additionalRange.getFirstChild().getNextSibling().getText().equals(newLabels.get(l).getFirstChild().getNextSibling().getText())) {
                    additionalRange = additionalRange.getNextSibling();
                    parent = additionalRange;
                }
                if(additionalRange.getNextSibling() != null && additionalRange != parent) {
                    parent.setNextSibling(additionalRange.getNextSibling());
                } else if(additionalRange.getNextSibling() != null && additionalRange == parent) {
                    // we are at the beginning of the tree
                    additionalFrom.setFirstChild(additionalRange.getNextSibling());
                } else {
                    // we only have one RANGE element
                    // so we completely remove it from the FROM
                    additionalFrom.setFirstChild(null);
                }
                
            }

            // t.x.y.z (where t collides)
            for (String c : collisions.keySet()) {
                AST type = newLabels.get(l).getFirstChild();
                String path = getPath(type);
                if (path.startsWith(c + ".")) {
                    String t = path;
                    t = t.substring(c.length());
                    t = collisions.get(c) + t;
                    newLabels.get(l).setFirstChild(constructPath(t));
                    newLabels.get(l).getFirstChild().setNextSibling(type.getNextSibling());
                }
            }
        }

        // now that we are done with processing the RANGE elements, do the replacement in the rest of the tree
        replaceLabels(tree, collisions);

        // finally, append the RANGE elements from the new tree to the existing one, if there is one to append
        AST endFrom = ASTUtil.getLastChild(from);
        if(additionalFrom.getFirstChild() != null) {
            endFrom.setNextSibling(additionalFrom.getFirstChild());
        }
    }

    /**
     * Walks through the query function tree and replaces all colliding labels
     */
    private void replaceLabels(AST ast, Hashtable<String, String> collisions) {
        if (ast == null) {
            return;
        }

        // we skip the FROM tree since we already processed it
        if (ast.getType() == HqlTokenTypes.FROM) {
            replaceLabels(ast.getNextSibling(), collisions);
        }

        if (ast.getType() == HqlTokenTypes.IDENT) {

            for (String c : collisions.keySet()) {
                String path = getPath(ast);
                if (path.startsWith(c + ".") || path.equals(c)) {
                    String t = ast.getText();
                    t = t.substring(c.length());
                    t = collisions.get(c) + t;
                    ast = constructPath(t);
                }
            }

        }

        replaceLabels(ast.getFirstChild(), collisions);
        replaceLabels(ast.getNextSibling(), collisions);
    }

    /**
     * Walks the tree (except the FROM part) and collects all identifiers
     */
    private void collectUsedLabels(AST ast, Hashtable<String, String> usedLabels) {
        if (ast == null) {
            return;
        }

        // we skip the FROM tree since we process it afterwards
        if (ast.getType() == HqlTokenTypes.FROM) {
            collectUsedLabels(ast.getNextSibling(), usedLabels);
        }

        if (ast.getType() == HqlTokenTypes.IDENT) {
            usedLabels.put(ast.getText(), "");
        }

        collectUsedLabels(ast.getFirstChild(), usedLabels);
        collectUsedLabels(ast.getNextSibling(), usedLabels);
    }

    /**
     * Gets all the labels of a FROM element
     */
    private Hashtable<String, AST> getLabels(AST from) {
        Hashtable<String, AST> labels = new Hashtable<String, AST>();
        
        AST range = from.getFirstChild();
        while (range != null) {
            String b = range.getFirstChild().getNextSibling().getText();
            labels.put(b, range);
            range = range.getNextSibling();
        }

        return labels;
    }
    
    /**
     * Searches for a given identifier in a tree and returns true if it does
     */
    private Boolean hasIdentifier(AST tree, String identifier, Boolean found) {
        if(tree == null) {
            return found;
        }
        if(found) {
            return found;
        }
        
        if(tree.getType() == HqlTokenTypes.IDENT && tree.getText().equals(identifier)) {
            found = true;
            return true;
        } else {
            found = hasIdentifier(tree.getFirstChild(), identifier, found);
            if(found) {
                return found;
            }
            return hasIdentifier(tree.getNextSibling(), identifier, found);
        }
    }
    
    /**
     * Finds the content of the SELECT of a query tree
     * 
     * @param tree
     *            the tree to search
     * @return the first content node (without aliases) of the SELECT
     */
    private AST findSelectContent(AST tree) {
        AST c = findSelect(tree);
        if (c.getFirstChild().getType() == HqlTokenTypes.AS) {
            c = c.getFirstChild().getFirstChild();
        } else {
            c = c.getFirstChild();
        }
        c.setNextSibling(null);
        return c;
    }

    private AST findSelect(AST tree) {
        AST select_from = ASTUtil.findTypeInChildren(tree, HqlTokenTypes.SELECT_FROM);
        return ASTUtil.findTypeInChildren(select_from, HqlTokenTypes.SELECT);

    }

    /**
     * Finds the FROM in a query tree of the kind<br>
     * 
     * <pre>
     * query [83] 
     *            SELECT_FROM [86] 
     *               FROM [22]
     *            SELECT [45]
     *            WHERE [53]
     * </pre>
     * 
     * @param tree
     *            the tree to search
     * @return the AST to the FROM element, null if not found
     */
    private AST findFrom(AST tree) {
        AST select_from = ASTUtil.findTypeInChildren(tree, HqlTokenTypes.SELECT_FROM);
        if (select_from == null && tree.getType() == HqlTokenTypes.SELECT_FROM) {
            select_from = tree;
        } else if (select_from == null) {
            return null;
        }
        AST from = select_from.getFirstChild();
        return from;
    }

    private AST findWhere(AST tree) {
        return ASTUtil.findTypeInChildren(tree, HqlTokenTypes.WHERE);
    }

    

    /**
     * Finds all method calls in a query tree, and saves references to useful elements such as the query tree root, the
     * FROM element, the WHERE element and the method call parent node
     */
    private ArrayList<MethodCall> findMethodCalls(AST tree, AST root, AST parent, AST currentFrom, AST currentWhere,
            boolean firstChild, ArrayList<MethodCall> result) {

        if (tree == null) {
            return null;
        }

        // we fetch the WHERE first
        if (parent == null) {
            currentWhere = findWhere(tree);
            
            // we also fetch the FROM, in case we are traversing WHERE and have a function
            // because in that case we'll also need the reference to the FROM so we can join with the necessary tables
            currentFrom = findFrom(tree);
            
        }

        // in case we encounter another FROM on our way, it might be the FROM of a subquery
        // so we place it here
        if (tree.getType() == HqlTokenTypes.FROM) {
            currentFrom = tree;
        }

        // recursive-descent traversal of tree to fetch all method calls
        if (tree.getType() == HqlTokenTypes.METHOD_CALL) {
            result.add(new MethodCall(firstChild, root, parent, currentFrom, currentWhere));
        }

        parent = tree;

        findMethodCalls(tree.getFirstChild(), root, parent, currentFrom, currentWhere, true, result);
        findMethodCalls(tree.getNextSibling(), root, parent, currentFrom, currentWhere, false, result);

        return result;

    }

    /**
     * Given an AST, return the path, by concatenating all child ASTs that have dots
     */
    private String getPath(AST t) {
        if (t.getType() == HqlTokenTypes.DOT) {
            return constructPath(t);
        } else {
            return t.getText();
        }
    }

    /**
     * Given a DOT tree, construct the path as String
     */
    private String constructPath(AST type) {
        String a_text = "", b_text = "";
        AST a = type.getFirstChild();
        AST b = a.getNextSibling();
        if (a.getType() == HqlTokenTypes.DOT) {
            a_text = constructPath(a);
        } else {
            a_text = a.getText();
        }
        if (b.getType() == HqlTokenTypes.DOT) {
            b_text = constructPath(b);
        } else {
            b_text = b.getText();
        }
        return a_text + "." + b_text;
    }

    /**
     * Given a path of the kind a.b.c, constructs a DOT tree AST
     */
    private AST constructPath(String path) {
        if (path.indexOf(".") > -1) {
            String a = path.substring(0, path.indexOf("."));
            AST d = ASTUtil.create(fact, HqlTokenTypes.DOT, ".");
            AST i = ASTUtil.create(fact, HqlTokenTypes.IDENT, a);
            d.setFirstChild(i);
            i.setNextSibling(constructPath(path.substring(path.indexOf(".") + 1)));
            return d;

        } else {
            return ASTUtil.create(fact, HqlTokenTypes.IDENT, path);
        }
    }
    
    private int labelCounter = 0;
    
    private Vector<String> generatedLabels = new Vector<String>();
    
    public String createLabel() {
        String l = "_x_gen_" + labelCounter++;
        generatedLabels.add(l);
        return l;
    }
    
    private int parameterNumber = 0; 
    
    private int parameterNumber() {
        return parameterNumber++;
    }

    
    
    /**
     * class that holds information about a method call in the 1st pass tree
     */
    class MethodCall {

        private boolean firstChild;

        private AST root;

        private AST parent;

        private AST from;

        private AST where;

        public AST getRoot() {
            return root;
        }

        public AST getFrom() {
            return from;
        }

        public AST getWhere() {
            return where;
        }

        public AST getParent() {
            return parent;
        }

        public boolean isFirstChild() {
            return firstChild;
        }

        public MethodCall(boolean firstChild, AST root, AST parent, AST from, AST where) {
            super();
            this.firstChild = firstChild;
            this.parent = parent;
            this.from = from;
            this.where = where;
            this.root = root;
        }

        public void replace(AST node) {
            
            if (firstChild) {
                node.setNextSibling(parent.getFirstChild().getNextSibling());
                parent.setFirstChild(node);
            } else {
                node.setNextSibling(parent.getNextSibling().getNextSibling());
                parent.setNextSibling(node);
            }
        }

        @Override
        public String toString() {
            return "MethodCall [firstChild=" + firstChild + ", root=" + root + ", from=" + from + ", where=" + where
                    + ", parent=" + parent + "]";
        }

    }

}
