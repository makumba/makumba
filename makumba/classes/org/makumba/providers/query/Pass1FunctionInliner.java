package org.makumba.providers.query;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.makumba.DataDefinition;
import org.makumba.DataDefinitionNotFoundError;
import org.makumba.FieldDefinition;
import org.makumba.ProgrammerError;
import org.makumba.DataDefinition.QueryFragmentFunction;
import org.makumba.commons.ClassResource;
import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;
import org.makumba.providers.QueryAnalysisProvider.ASTTransformVisitor;
import org.makumba.providers.QueryAnalysisProvider.FromWhere;
import org.makumba.providers.datadefinition.mdd.MakumbaDumpASTVisitor;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.QueryAnalysis;
import org.makumba.providers.QueryAnalysisProvider;
import org.makumba.providers.QueryProvider;
import org.makumba.providers.query.mql.ASTUtil;
import org.makumba.providers.query.mql.HqlASTFactory;
import org.makumba.providers.query.mql.HqlTokenTypes;

import antlr.collections.AST;

public class Pass1FunctionInliner {

    static final HqlASTFactory fact = new HqlASTFactory();

    public static Logger logger = Logger.getLogger("org.makumba.db.query.inline");

    static int functionCache = NamedResources.makeStaticCache("function pass2 analyses", new NamedResourceFactory() {
        @Override
        protected Object makeResource(Object name) {
            String s = (String) name;
            DataDefinition calleeType = DataDefinitionProvider.getInstance().getDataDefinition(
                s.substring(0, s.indexOf(' ')));
            QueryFragmentFunction func = calleeType.getFunctions().getFunction(s.substring(s.indexOf(' ') + 1, s.lastIndexOf(' ')));
            AST pass1 = parseAndAddThis(calleeType, func);
            String provider= s.substring(s.lastIndexOf(' ')+1);
            
            try {
                // TODO: this should move to the MDD analyzer after solving chicken-egg
                // however, note that a new Query analysis method is needed (with known labels)
                // note also that this leads to calls of this inliner, which works.
                return QueryProvider.getQueryAnalzyer(provider).getQueryAnalysis(inlineAST(pass1, provider), func.getParameters());
            } catch (Throwable t) {
                return new ProgrammerError(t, "Error parsing function " + func.getName() + " from MDD " + calleeType
                        + "\n" + t.getMessage());
            }
        }
    });
    
    private static QueryAnalysis getInlinedFunctionAnalysis(String queryAnalysisProvider, DataDefinition calleeType,
            QueryFragmentFunction func) {
        
        Object ret= NamedResources.getStaticCache(functionCache).getResource(
                calleeType.getName() + " " + func.getName()+" "+queryAnalysisProvider);
        if (ret instanceof ProgrammerError)
            throw (ProgrammerError) ret;
        return (QueryAnalysis)ret;
    }


    /*
     * a beautiful but not so practical way of non-recursive tree traversal: it's not practical because it traverses the
     * tree but can't change it during traversal
     */
    // ArrayList<AST> stack= new ArrayList<AST>();
    // AST a= tree;
    // root: while(true) {
    // do{ stack.add(a); visit(a); } while((a=a.getFirstChild())!=null);
    // do{ if(stack.isEmpty()) break root; a= stack.remove( stack.size() - 1).getNextSibling(); } while(a==null);
    // }
    /** The core inliner method: inline functions in an AST tree 
     * @param provider */
    public static AST inlineAST(AST parsed, String provider) {
        // new MakumbaDumpASTVisitor(false).visit(parsed);
        InlineVisitor v = new InlineVisitor(provider);
        AST ret = v.inlineAST(parsed);
        
        if (logger.getLevel() != null && logger.getLevel().intValue() >= java.util.logging.Level.FINE.intValue())
            logger.fine(parsed.toStringList() + " \n-> " + ret.toStringList());

        return ret;
    }

    /** The traverse() visitor for inlining, inlines functions and actors */
    static class InlineVisitor extends ASTTransformVisitor {

        public InlineVisitor(String provider) {
           super(true);
           this.queryAnalysisProvider= provider;
        }

        private String queryAnalysisProvider;
        
        private FromWhere fromWhere= new FromWhere();

        private AST inlineAST(AST parsed) {
            // inlining is a simple question of traversal with the inliner visitor
            AST ret= traverse(parsed);
            // ... and of adding the from and where sections discovered during inlining
            // FIXME: for now they are added to the root query. adding them to other subqueries may be required
            
            // if we are in the middle of a parsing, (like when we force recursion to inline the callee)
            // we take the root from the bottom of the stack
            // otherwise we are a the end of a parsing since the stack is empty, so we got the real root
            fromWhere.addToTreeFromWhere(getPath().size()>0?getPath().get(0):ret);
            return ret;
        }

        public AST visit(AST current) {
            // the function signature is a method call which has a DOT as first child
            if (current.getType() != HqlTokenTypes.METHOD_CALL)
                return current;
            if (current.getFirstChild().getType() == HqlTokenTypes.IDENT
                    && current.getFirstChild().getText().equals("actor"))
                return treatActor(current);
            if (current.getFirstChild().getType() != HqlTokenTypes.DOT)
                return current;
            
            // we push both the method call and the dot into the stack
            // to have complete data there when we need to recurse
            // the dot is especially important for e.g. actors into the callee
            getPath().push(current);
            getPath().push(current.getFirstChild());
            
            AST callee = current.getFirstChild().getFirstChild();
            String methodName = callee.getNextSibling().getText();

            boolean isStatic = false;

            // determine whether the callee is a DataDefinition name, in which case we have a static function
            DataDefinition calleeType = null;
            if (callee.getType() == HqlTokenTypes.DOT)
                calleeType = getMdd(ASTUtil.getPath(callee));
            if (callee.getType() == HqlTokenTypes.IDENT)
                calleeType = getMdd(callee.getText());

            // if we're not an MDD name, we try to find a label type using the FROMs of the query
            if (calleeType == null) {
                // we force a recursion now because we might have a function or actor in the callee
                // FIXME: this probably would not be needed in case of a depth-first, post-order traversal
                // TODO: find type will be needed also to compute parameter types. the recursion is needed there too
                callee= inlineAST(callee);                                    
                FieldDefinition fd = findType(callee);
                if (fd.getType().startsWith("ptr"))
                    calleeType = fd.getPointedType();
                else
                    throw new ProgrammerError("Not a pointer type in call of " + methodName + ": "
                            + view(callee));
            } else
                isStatic = true;

            // now we can retrieve the function
            QueryFragmentFunction func = calleeType.getFunctions().getFunction(methodName);

            if (func == null) {
                throw new ProgrammerError("Unknown function '" + methodName + "' in type " + calleeType.getName());
            }

            // and its parsed form from the cache
            QueryAnalysis parsed = getInlinedFunctionAnalysis(queryAnalysisProvider, calleeType, func);

            /* FIXME: at this point, from the QueryAnalysis (pass2) we know the function return type!
             In order to see whether the function fits in the expression it is put in, 
             we could replace the function with a constant of that type (or an extra from for pointers)
             and analyze the resulting expression using e.g. findType().
             However, that might pose problems if we have functions in the rest of the expression, so 
             a simpler heuristic (looking at parent operators, parent MQL/SQL functions, etc) can help. 
             After all, this doesn't need to be 100% precise. 
             
             This would allow us to find an error in the orignal query, 
             on the original query text, and therefore with an accurate line-column indication
            */
            
            // we duplicate the tree as we are going to change it
            AST funcAST = new HqlASTFactory().dupTree(parsed.getPass1Tree());
            // QueryAnalysisProvider.parseQuery(queryFragment)

            AST from = funcAST.getFirstChild().getFirstChild().getFirstChild();
            // at this point, from should be "Type this"
            // but if that has siblings, we need to add them to the FROM of the outer query
            // since they come from from-where enrichment, probably from actors
            // TODO: maybe consider whether we should cache the function AST and the from-where enrichments separately
            from = from.getNextSibling();
            while (from != null) {
                fromWhere.addFrom(from);
                from = from.getNextSibling();
            }
            // similarly if we have a WHERE, that can only come from previous enrichment
            // (since we parsed SELECT functionExpr FROM Type this), so we propagate it
            AST where = funcAST.getFirstChild().getNextSibling();
            if (where != null && where.getType() == HqlTokenTypes.WHERE)
                fromWhere.addWhere(where.getFirstChild());

            // the function expr: query-> SELECT_FROM -> FROM-> SELELECT -> 1st projection
            funcAST = funcAST.getFirstChild().getFirstChild().getNextSibling().getFirstChild();

            DataDefinition para = func.getParameters();
            AST exprList = current.getFirstChild().getNextSibling();
            if (exprList.getNumberOfChildren() != para.getFieldNames().size())
                throw new ProgrammerError("Wrong number of parameters for call to " + func
                        + "\nRequired parameter number: " + para.getFieldNames().size() + "\nGiven parameters: "
                        + exprList.getNumberOfChildren() + "\nParameter values: " + view(exprList));

            // we copy the parameter expressions
            // FIXME: use findType(path, expr) for each parameter expression and check its type!
            final HashMap<String, AST> paramExpr = new HashMap<String, AST>();
            AST p = exprList.getFirstChild();
            getPath().push(exprList);
            for (String s : para.getFieldNames()) {
                paramExpr.put(s, inlineAST(p));
                p = p.getNextSibling();
            }
            getPath().pop();

            final AST calleeThis= callee;
            // now we visit the function AST and replace "this" with the callee tree
            AST ret = funcAST;
            if (!isStatic)
                ret = (new ASTTransformVisitor(false) {
                    public AST visit(AST node) {
                        // for each "this" node, we put the callee instead
                        if (node.getType() == HqlTokenTypes.IDENT && node.getText().equals("this"))
                            return calleeThis;
                        return node;
                    }

                }).traverse(funcAST);

            // then we visit the function more, and replace parameter names with parameter expressions
            ret = (new ASTTransformVisitor(false) {
                public AST visit(AST node) {
                    // for each parameter node, we put the param expression instead
                    if (node.getType() == HqlTokenTypes.IDENT && paramExpr.get(node.getText()) != null &&
                    // a field name from another table might have the same name as a param
                            // FIXME: there might be other cases where this is not the param but some field
                            getPath().peek().getType() != HqlTokenTypes.DOT
                    //        
                    )
                        return paramExpr.get(node.getText());
                    return node;
                }
            }).traverse(ret);
            
            getPath().pop(); // pop the dot
            getPath().pop(); // pop the method_call
            
            // new MakumbaDumpASTVisitor(false).visit(ret);

            return ret;

        }

        /** Actor processing. Two cases: simple insertion of attribute, or enriching the outer query 
         */
        private AST treatActor(AST current) {
            AST actorType = current.getFirstChild().getNextSibling();
            if (actorType.getNumberOfChildren() != 1)
                throw new ProgrammerError("actor(Type) must indicate precisely one type: "+view(current));
            actorType = actorType.getFirstChild();

            String rt= QueryAnalysisProvider.getGeneratedActorName(actorType);
            String parameterSyntax = QueryProvider.getQueryAnalzyer(queryAnalysisProvider).getParameterSyntax();
            
            // the more complicated case: we have a actor(Type).something
            int type = getPath().peek().getType();
            if (type == HqlTokenTypes.DOT) {
                fromWhere.addActor(actorType, QueryProvider.getQueryAnalzyer(queryAnalysisProvider).getParameterSyntax());
            } else
                // the simple case: we simply add the parameter
                // FIXME: in HQL the parameter syntax is a tree (: paamName) not an IDENT
                rt= QueryProvider.getQueryAnalzyer(queryAnalysisProvider).getParameterSyntax()+ rt ;

            return ASTUtil.makeNode(HqlTokenTypes.IDENT, rt);
        }

        /** Find the type of an expression AST, given the path to the query root. 
         * Makes a query treee and invokes the second pass on it. */
        private FieldDefinition findType(AST expr) {

            // we build a query tree
            AST query = ASTUtil.makeNode(HqlTokenTypes.QUERY, "query");
            AST selectFrom = ASTUtil.makeNode(HqlTokenTypes.SELECT_FROM, "SELECT_FROM");
            AST select = ASTUtil.makeNode(HqlTokenTypes.SELECT, "SELECT");
            AST from = ASTUtil.makeNode(HqlTokenTypes.FROM, "FROM");

            query.setFirstChild(selectFrom);
            selectFrom.setFirstChild(from);

            // we copy the FROM part of all the queries in our path
            AST lastAdded = null;
            for (AST a : getPath()) {
                // we find queriess
                if (a.getType() != HqlTokenTypes.QUERY)
                    continue;
                // we duplicate the FROM section of each query
                AST originalFrom = a.getFirstChild().getFirstChild().getFirstChild();
                AST toAdd = QueryAnalysisProvider.makeASTCopy(originalFrom);
                if (lastAdded == null) {
                    from.setFirstChild(toAdd);
                } else
                    lastAdded.setNextSibling(toAdd);
                lastAdded = toAdd;
                while (originalFrom.getNextSibling() != null) {
                    originalFrom = originalFrom.getNextSibling();
                    toAdd = QueryAnalysisProvider.makeASTCopy(originalFrom);
                    lastAdded.setNextSibling(toAdd);
                    lastAdded = toAdd;
                }
            }
            // TODO: also add the extraFrom (enrichment) to the new query FROM
            from.setNextSibling(select);
            
            // we select just the expression we want to determine the type of
            select.setFirstChild(QueryAnalysisProvider.makeASTCopy(expr));

            return  QueryProvider.getQueryAnalzyer(queryAnalysisProvider).getQueryAnalysis(query, null).getProjectionType().getFieldDefinition(0);

        }
    }





    /*
     * Parse the function and add this nodes where they are needed
     */
    static AST parseAndAddThis(final DataDefinition calleeType, final QueryFragmentFunction func) {
        boolean subquery= func.getQueryFragment().trim().toUpperCase().startsWith("SELECT");
        AST pass1 = QueryAnalysisProvider.parseQuery("SELECT " + (subquery?"(":"")+func.getQueryFragment() + " FROM "
                + calleeType.getName() + " this"+(subquery?")":""));

       ASTTransformVisitor thisVisitor = new ASTTransformVisitor(false) {

                public AST visit(AST a) {
                    // we are looking for non-this idents
                    if (a.getType() != HqlTokenTypes.IDENT || a.getText().equals("this"))
                        return a;

                    int top = getPath().size();
                    AST parent = getPath().get(top - 1);

                    // if we are part of a DOT tree, we must be the first
                    if (parent.getType() == HqlTokenTypes.DOT && ((getPath().get(top - 2).getType() == HqlTokenTypes.DOT)
                            || parent.getFirstChild() != a))
                        return a;

                    // a dot may mean an MDD name, no need for this-adding
                    if (parent.getType() == HqlTokenTypes.DOT && getMdd(parent) != null)
                        return a;
                    // the ident may be a MDD name or a paramter name
                    if (getMdd(a.getText()) != null || func.getParameters().getFieldDefinition(a.getText()) != null)
                        return a;
                    // the ident may be actor from actor(type)
                    if (a.getText().equals("actor") && getMdd(a.getNextSibling().getFirstChild()) != null)
                        return a;

                    // FIXME: at this point we might still be a label defined in a FROM
                    // which has the same name as the a MDD field.
                    // to check for that, we can invoke findType() for that label
                    // however i am not sure whether such a label is legal
                    if (calleeType.getFieldDefinition(a.getText()) != null
                            || calleeType.getFunctions().getFunction(a.getText()) != null) {
                        // make a dot and a this ident.
                        AST ret = ASTUtil.makeNode(HqlTokenTypes.DOT, ".");
                        AST thisAST = ASTUtil.makeNode(HqlTokenTypes.IDENT, "this");
                        ret.setFirstChild(thisAST);
                        // copy the ident so its next sibling doesn't get to the this tree
                        thisAST.setNextSibling(QueryAnalysisProvider.makeASTCopy(a));
                        return ret;
                    }

                    // not sure what the AST can be at this point. if this is an error, it will be found at pass2
                    return a;
                }

            };
        pass1= thisVisitor.traverse(pass1);
        // test code to compare with the matcher-based algorithm:

        // String queryAndThis= "SELECT " + FunctionInliner.addThisToFunction(calleeType, func)
        // + " FROM " + calleeType.getName() + " this";
        // AST paa= QueryAnalysisProvider.parseQuery(queryAndThis);
        //            
        // if(!compare(new ArrayList<AST>(), pass1, paa)){
        // System.out.println(paa.toStringList());
        // System.out.println(pass1.toStringList());
        // }
        return pass1;
    }

    /** View an AST e.g. in an error message */
    static String view(AST a){
        return a.toStringList();
    }
    static DataDefinition getMdd(AST callee) {
        return getMdd(ASTUtil.getPath(callee));
    }

    static DataDefinition getMdd(String path) {
        try {
            return DataDefinitionProvider.getInstance().getDataDefinition(path);
        } catch (DataDefinitionNotFoundError e) { /* ignore */ }
        return null;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader((InputStream) ClassResource.get(
                "org/makumba/providers/query/inlinerCorpus.txt").getContent()));
            String query = null;
            int line = 0;

            while ((query = rd.readLine()) != null) {
                line++;
                if (query.trim().startsWith("#"))
                    continue;

                AST firstAST = QueryAnalysisProvider.parseQuery(QueryAnalysisProvider.checkForFrom(query));
                AST f= QueryAnalysisProvider.parseQuery(QueryAnalysisProvider.checkForFrom(query));
                AST processedAST = inlineAST(firstAST, "oql");
                QueryAnalysisProvider.reduceDummyFrom(processedAST);

                Throwable oldError = null;
                AST compAST = null;
                String oldInline = null;
                
                try {
                    oldInline= FunctionInliner.inline(query, QueryProvider.getQueryAnalzyer("oql"));
                    compAST = QueryAnalysisProvider.parseQuery(oldInline);
                } catch (Throwable t) {
                    oldError = t;
                }

                // new MakumbaDumpASTVisitor(false).visit(processedAST);

                // AST a= flatten(processedAST);
                // new MakumbaDumpASTVisitor(false).visit(a);

                // new MakumbaDumpASTVisitor(false).visit(a);

                // if(line==1)
                // new MakumbaDumpASTVisitor(false).visit(new MqlQueryAnalysis(oldInline, false,
                // false).getAnalyserTree());
                // System.out.println(oldInline);
                
                if (!QueryAnalysisProvider.compare(new ArrayList<AST>(), processedAST, compAST)) {
                    System.err.println(line + ": " + query);
                    if (oldError != null){
                        System.err.println(line + ": old inliner failed! " + oldError + " query was: " + oldInline);
                        System.out.println(line + ": " +Pass1ASTPrinter.printAST(processedAST));
                        //new MakumbaDumpASTVisitor(false).visit(processedAST);

                    }
                    else {
                        System.out.println(line+": pass1: " +Pass1ASTPrinter.printAST(processedAST));
                        System.out.println(line+": old:   " +Pass1ASTPrinter.printAST(compAST));
                    }
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
