package org.makumba.providers.query;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.makumba.DataDefinition;
import org.makumba.DataDefinitionNotFoundError;
import org.makumba.FieldDefinition;
import org.makumba.InvalidValueException;
import org.makumba.ProgrammerError;
import org.makumba.DataDefinition.QueryFragmentFunction;
import org.makumba.commons.ClassResource;
import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;
import org.makumba.providers.datadefinition.mdd.MakumbaDumpASTVisitor;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.QueryAnalysisProvider;
import org.makumba.providers.QueryProvider;
import org.makumba.providers.query.mql.ASTUtil;
import org.makumba.providers.query.mql.HqlASTFactory;
import org.makumba.providers.query.mql.HqlTokenTypes;
import org.makumba.providers.query.mql.MqlQueryAnalysis;
import org.makumba.providers.query.mql.Node;

import antlr.collections.AST;

public class Pass1FunctionInliner {

    static final HqlASTFactory fact = new HqlASTFactory();

    public static Logger logger = Logger.getLogger("org.makumba.db.query.inline");

    static int functionCache = NamedResources.makeStaticCache("function pass2 analyses", new NamedResourceFactory() {
        @Override
        protected Object makeResource(Object name) {
            String s = (String) name;
            DataDefinition calleeType = DataDefinitionProvider.getInstance().getDataDefinition(
                s.substring(0, s.lastIndexOf(' ')));
            QueryFragmentFunction func = calleeType.getFunctions().getFunction(s.substring(s.lastIndexOf(' ') + 1));
            AST pass1 = parseAndAddThis(calleeType, func);
            
            try {
                // TODO: this should move to the MDD analyzer after solving chicken-egg
                // however, note that a new Query analysis method is needed (with known labels)
                // note also that this leads to calls of this inliner, which works.
                return new MqlQueryAnalysis(inlineAST(pass1), func.getParameters());
            } catch (Throwable t) {
                return new ProgrammerError(t, "Error parsing function " + func.getName() + " from MDD " + calleeType
                        + "\n" + t.getMessage());
            }
        }
    });

    // TODO: separate traverse() in a commons utility class
    public static interface ASTVisitor {
        public AST visit(TraverseState state, AST a);
    }

    static class TraverseState {
        boolean repetitive;

        TraverseState(boolean repetitive) {
            this.repetitive = repetitive;
        }

        List<AST> path = new ArrayList<AST>();

        List<AST> extraFrom = new ArrayList<AST>();

        List<AST> extraWhere = new ArrayList<AST>();

        // FIXME: extraFrom and extraWhere should also indicate to what query they should add
        // for now everything is added to the root query
    }

    /**
     * recursively traverse the tree, and transform it via a visitor. keep a traversal state, which includes the path
     * from the tree root to the current node
     */
    static AST traverse(TraverseState state, AST current, ASTVisitor v) {
        if (current == null)
            return null;

        boolean wasReplaced = false;
        // while traversal returns new stuff (e.g. an inlined function is still a function to inline) we repeat
        while (true) {
            AST current1 = v.visit(state, current);
            if (current1 != current) {
                // we copy the root node of the structure we get
                AST current2 = makeASTCopy(current1);
                // and link it to the rest of our tree
                current2.setNextSibling(current.getNextSibling());
                current = current2;
                if (!state.repetitive) {
                    wasReplaced = true;
                    // if we do not need to traverse what we replaced we go on
                    break;
                }
            } else
                break;
        }
        // if we replaced already, and we are not repetitive, we do't go deep
        if (!wasReplaced) {
            state.path.add(current);
            current.setFirstChild(traverse(state, current.getFirstChild(), v));
            state.path.remove(state.path.size() - 1);
        }

        // but we go wide
        current.setNextSibling(traverse(state, current.getNextSibling(), v));

        return current;
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
    /** The core inliner method: inline functions in an AST tree */
    public static AST inlineAST(AST parsed) {
        // new MakumbaDumpASTVisitor(false).visit(parsed);

        // inlining is a simple question of traversal with the inliner visitor
        TraverseState state = new TraverseState(true);
        AST ret = traverse(state, parsed, InlineVisitor.singleton);

        // and of adding the from and where sections discovered during inlining
        // FIXME: for now they are added to the root query. adding them to other subqueries may be required
        addFromWhere(ret, state);

        if (logger.getLevel() != null && logger.getLevel().intValue() >= java.util.logging.Level.FINE.intValue())
            logger.fine(parsed.toStringList() + " \n-> " + ret.toStringList());

        return ret;
    }

    /** The traverse() visitor for inlining, inlines functions and actors */
    static class InlineVisitor implements ASTVisitor {
        static ASTVisitor singleton = new InlineVisitor();

        public AST visit(TraverseState state, AST current) {
            // the function signature is a method call which has a DOT as first child
            if (current.getType() != HqlTokenTypes.METHOD_CALL)
                return current;
            if (current.getFirstChild().getType() == HqlTokenTypes.IDENT
                    && current.getFirstChild().getText().equals("actor"))
                return treatActor(state, current);
            if (current.getFirstChild().getType() != HqlTokenTypes.DOT)
                return current;

            final AST callee = current.getFirstChild().getFirstChild();
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
                FieldDefinition fd = findType(state.path, callee);
                if (fd.getType().startsWith("ptr"))
                    calleeType = fd.getPointedType();
                else
                    throw new ProgrammerError("Not a pointer type in call of " + methodName + ": "
                            + view(callee));
            } else
                isStatic = true;

            // now we can retrieve the function
            QueryFragmentFunction func = calleeType.getFunctions().getFunction(methodName);

            // and its parsed form from the cache
            Object parsed = NamedResources.getStaticCache(functionCache).getResource(
                calleeType.getName() + " " + func.getName());
            if (parsed instanceof ProgrammerError)
                throw (ProgrammerError) parsed;

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
            AST funcAST = new HqlASTFactory().dupTree(((MqlQueryAnalysis) parsed).getPass1Tree());
            // QueryAnalysisProvider.parseQuery(queryFragment)

            AST from = funcAST.getFirstChild().getFirstChild().getFirstChild();
            // at this point, from should be "Type this"
            // but if that has siblings, we need to add them to the FROM of the outer query
            // since they come from from-where enrichment, probably from actors
            // TODO: maybe consider whether we should cache the function AST and the from-where enrichments separately
            from = from.getNextSibling();
            while (from != null) {
                state.extraFrom.add(from);
                from = from.getNextSibling();
            }
            // similarly if we have a WHERE, that can only come from previous enrichment
            // (since we parsed SELECT functionExpr FROM Type this), so we propagate it
            AST where = funcAST.getFirstChild().getNextSibling();
            if (where != null && where.getType() == HqlTokenTypes.WHERE)
                state.extraWhere.add(where.getFirstChild());

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
            for (String s : para.getFieldNames()) {
                paramExpr.put(s, p);
                p = p.getNextSibling();
            }

            // now we visit the function AST and replace "this" with the callee tree
            AST ret = funcAST;
            if (!isStatic)
                ret = traverse(new TraverseState(false), funcAST, new ASTVisitor() {
                    public AST visit(TraverseState state, AST node) {
                        // for each "this" node, we put the callee instead
                        if (node.getType() == HqlTokenTypes.IDENT && node.getText().equals("this"))
                            return callee;
                        return node;
                    }

                });

            // then we visit the function more, and replace parameter names with parameter expressions
            ret = traverse(new TraverseState(false), ret, new ASTVisitor() {
                public AST visit(TraverseState state, AST node) {
                    // for each parameter node, we put the param expression instead
                    if (node.getType() == HqlTokenTypes.IDENT && paramExpr.get(node.getText()) != null &&
                    // a field name from another table might have the same name as a param
                            // FIXME: there might be other cases where this is not the param but some field
                            state.path.get(state.path.size() - 1).getType() != HqlTokenTypes.DOT
                    //        
                    )
                        return paramExpr.get(node.getText());
                    return node;
                }
            });
            // new MakumbaDumpASTVisitor(false).visit(ret);

            return ret;

        }

        /** Actor processing. Two cases: simple insertion of attribute, or enriching the outer query 
         */
        private AST treatActor(TraverseState state, AST current) {
            AST actorType = current.getFirstChild().getNextSibling();
            if (actorType.getNumberOfChildren() != 1)
                throw new ProgrammerError("actor(Type) must indicate precisely one type: "+view(current));
            actorType = actorType.getFirstChild();
            String rt = "actor_" + ASTUtil.constructPath(actorType).replace('.', '_');

            // the more complicated case: we have a actor(Type).something
            if (state.path.get(state.path.size() - 1).getType() == HqlTokenTypes.DOT) {
                // make an extra FROM
                AST range = ASTUtil.makeNode(HqlTokenTypes.RANGE, "RANGE");
                range.setFirstChild(makeASTCopy(actorType));
                range.getFirstChild().setNextSibling(ASTUtil.makeNode(HqlTokenTypes.ALIAS, rt));
                state.extraFrom.add(range);

                //make an extra where
                AST equal = ASTUtil.makeNode(HqlTokenTypes.EQ, "=");
                equal.setFirstChild(ASTUtil.makeNode(HqlTokenTypes.IDENT, rt));
                equal.getFirstChild().setNextSibling(ASTUtil.makeNode(HqlTokenTypes.IDENT, "$" + rt));
                state.extraWhere.add(equal);
            } else
                // the simple case: we simply add the parameter
                rt = "$" + rt;

            return ASTUtil.makeNode(HqlTokenTypes.IDENT, rt);
        }
    }

    /** Find the type of an expression AST, given the path to the query root. 
     * Makes a query treee and invokes the second pass on it. */
    private static FieldDefinition findType(List<AST> path, AST expr) {

        // we build a query tree
        Node query = ASTUtil.makeNode(HqlTokenTypes.QUERY, "query");
        Node selectFrom = ASTUtil.makeNode(HqlTokenTypes.SELECT_FROM, "SELECT_FROM");
        Node select = ASTUtil.makeNode(HqlTokenTypes.SELECT, "SELECT");
        Node from = ASTUtil.makeNode(HqlTokenTypes.FROM, "FROM");

        query.setFirstChild(selectFrom);
        selectFrom.setFirstChild(from);

        // we copy the FROM part of all the queries in our path
        AST lastAdded = null;
        for (AST a : path) {
            // we find queriess
            if (a.getType() != HqlTokenTypes.QUERY)
                continue;
            // we duplicate the FROM section of each query
            AST originalFrom = a.getFirstChild().getFirstChild().getFirstChild();
            AST toAdd = makeASTCopy(originalFrom);
            if (lastAdded == null) {
                from.setFirstChild(toAdd);
            } else
                lastAdded.setNextSibling(toAdd);
            lastAdded = toAdd;
            while (originalFrom.getNextSibling() != null) {
                originalFrom = originalFrom.getNextSibling();
                toAdd = makeASTCopy(originalFrom);
                lastAdded.setNextSibling(toAdd);
                lastAdded = toAdd;
            }
        }
        // TODO: also add the extraFrom (enrichment) to the new query FROM
        from.setNextSibling(select);
        
        // we select just the expression we want to determine the type of
        select.setFirstChild(makeASTCopy(expr));

        // FIXME: any query analysis provider that accepts a pass1 tree should be usable here.
        // there should be no direct dependence on Mql.
        return new MqlQueryAnalysis(query, null).getProjectionType().getFieldDefinition(0);

    }

    /** Add to a query the ranges and the where conditions that were found. The AST is assumed to be the root of a query */
    private static void addFromWhere(AST root, TraverseState state) {
        AST firstFrom = root.getFirstChild().getFirstChild().getFirstChild();
        for (AST range : state.extraFrom) {
            ASTUtil.appendSibling(firstFrom, range);
        }

        for (AST condition : state.extraWhere) {
            if (condition == null)
                continue;

            AST where = root.getFirstChild().getNextSibling();

            if (where != null && where.getType() == HqlTokenTypes.WHERE) {
                AST and = ASTUtil.makeNode(HqlTokenTypes.AND, "AND");
                and.setFirstChild(condition);
                condition.setNextSibling(where.getFirstChild());
                where.setFirstChild(and);
            } else {
                AST where1 = ASTUtil.makeNode(HqlTokenTypes.WHERE, "WHERE");
                where1.setNextSibling(where);
                root.getFirstChild().setNextSibling(where1);
                where1.setFirstChild(condition);
            }
        }
    }

    /*
     * Parse the function and add this nodes where they are needed
     */
    static AST parseAndAddThis(final DataDefinition calleeType, final QueryFragmentFunction func) {
        boolean subquery= func.getQueryFragment().trim().toUpperCase().startsWith("SELECT");
        AST pass1 = QueryAnalysisProvider.parseQuery("SELECT " + (subquery?"(":"")+func.getQueryFragment() + " FROM "
                + calleeType.getName() + " this"+(subquery?")":""));

        pass1 = traverse(new TraverseState(false), pass1, new ASTVisitor() {

            public AST visit(TraverseState state, AST a) {
                // we are looking for non-this idents
                if (a.getType() != HqlTokenTypes.IDENT || a.getText().equals("this"))
                    return a;

                int top = state.path.size();
                AST parent = state.path.get(top - 1);

                // if we are part of a DOT tree, we must be the first
                if (parent.getType() == HqlTokenTypes.DOT && ((state.path.get(top - 2).getType() == HqlTokenTypes.DOT)
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
                    thisAST.setNextSibling(makeASTCopy(a));
                    return ret;
                }

                // not sure what the AST can be at this point. if this is an error, it will be found at pass2
                return a;
            }

        });
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

    // this code is _experimental_
    static class SubqueryReductionVisitor implements ASTVisitor {

        public AST visit(TraverseState state, AST current) {
            // we are after queries, but not the root query
            if (current.getType() != HqlTokenTypes.QUERY || state.path.size() == 0)
                return current;
            // the reduction is defensive (i.e. we do not reduce unless we are sure that there are no problems)
            AST parent = state.path.get(state.path.size() - 1);

            // if we are the second child of the parent, then operator IN expects a multiple-result query
            if (parent.getFirstChild() != current)
                if (parent.getType() == HqlTokenTypes.IN)
                    return current;
                else
                    ;
            else
                // aggregate operators expect a multiple-result query
                switch (parent.getType()) {
                    case HqlTokenTypes.EXISTS:
                    case HqlTokenTypes.COUNT:
                    case HqlTokenTypes.MAX:
                    case HqlTokenTypes.MIN:
                    case HqlTokenTypes.AVG:
                    case HqlTokenTypes.INDICES:
                        return current;
                }

            // TODO: postorder, depth-first traversal!
            // TODO: currently we only add to the root query, maybe we should add to the enclosing query, and flatten
            // iteratively
            
            // TODO: left join when enriching the outer query in order not to mess up its result?

            // TODO: not inline queries with a groupBy, queries without a where
            // TODO: queries that have orderby, or more than one projection, should not be accepted here?

            // FIXME: i don't think that these copy more than the 1st child (FROM range, WHERE condition), got to check.
            state.extraFrom.add(current.getFirstChild().getFirstChild().getFirstChild());

            if (current.getFirstChild().getNextSibling().getType() == HqlTokenTypes.WHERE)
                state.extraWhere.add(current.getFirstChild().getNextSibling().getFirstChild());

            AST proj = current.getFirstChild().getFirstChild().getNextSibling().getFirstChild();

            // FIXME: the distinct should go to the decorated query
            if (proj.getType() == HqlTokenTypes.DISTINCT)
                proj = proj.getNextSibling();
            return proj;
        }
    }

    private static AST flatten(AST processedAST) {
        TraverseState s = new TraverseState(true);
        AST a = traverse(s, processedAST, new SubqueryReductionVisitor());
        addFromWhere(a, s);
        return a;
    }

    /** make a copy of an AST, node with the same first child, but with no next sibling */
    private static AST makeASTCopy(AST current1) {
        Node current2 = ASTUtil.makeNode(current1.getType(), current1.getText());
        // we connect the new node to the rest of the structure
        current2.setFirstChild(current1.getFirstChild());
        current2.setLine(current1.getLine());
        current2.setCol(current1.getColumn());

        // FIXME: how about the text which the line and column refer to?

        return current2;
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
                AST processedAST = inlineAST(firstAST);
                QueryAnalysisProvider.reduceDummyFrom(processedAST);

                String oldInline = FunctionInliner.inline(query, QueryProvider.getQueryAnalzyer("oql"));
                Throwable oldError = null;
                AST compAST = null;
                try {
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
                Pass1ASTPrinter.testPrinter(f, query);
                Pass1ASTPrinter.testPrinter(processedAST, oldInline);
                
                if (!QueryAnalysisProvider.compare(new ArrayList<AST>(), processedAST, compAST)) {
                    System.err.println(line + ": " + query);
                    if (oldError != null)
                        System.err.println(line + ": old inliner failed! " + oldError + " query was: " + oldInline);
                    else {
                        new MakumbaDumpASTVisitor(false).visit(processedAST);
                        new MakumbaDumpASTVisitor(false).visit(compAST);
                    }
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
