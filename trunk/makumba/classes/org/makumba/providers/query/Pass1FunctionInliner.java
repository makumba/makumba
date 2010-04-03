package org.makumba.providers.query;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.InvalidValueException;
import org.makumba.DataDefinition.QueryFragmentFunction;
import org.makumba.commons.ClassResource;
import org.makumba.providers.datadefinition.mdd.MakumbaDumpASTVisitor;
import org.makumba.providers.query.mql.ASTUtil;
import org.makumba.providers.query.mql.HqlParser;
import org.makumba.providers.query.mql.HqlSqlTokenTypes;
import org.makumba.providers.query.mql.MqlQueryAnalysis;
import org.makumba.providers.query.mql.Node;

import antlr.collections.AST;

public class Pass1FunctionInliner {

    public static interface ASTVisitor {
        public AST visit(TraverseState state, AST a);
    }

    static class TraverseState {
        List<AST> path = new ArrayList<AST>();

        List<AST> extraFrom = new ArrayList<AST>();

        List<AST> extraWhere = new ArrayList<AST>();
    }

    /**
     * recursively traverse the tree, and transform it via a visitor keep a path from the tree root to the current node
     */
    static AST traverse(TraverseState state, AST current, ASTVisitor v) {
        if (current == null)
            return null;

        // while traversal returns new stuff (e.g. an inlined function is still a function to inline) we repeat
        while (true) {
            AST current1 = v.visit(state, current);
            if (current1 != current) {
                // we copy the root node of the structure we get
                AST current2 = makeASTCopy(current1);
                // and link it to the rest of our tree
                current2.setNextSibling(current.getNextSibling());
                current = current2;
            } else
                break;
        }
        state.path.add(current);
        current.setFirstChild(traverse(state, current.getFirstChild(), v));
        state.path.remove(state.path.size() - 1);

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
    public static AST inlineQuery(String query) {
        HqlParser parser = HqlParser.getInstance(query);
        try {
            parser.statement();
        } catch (Throwable t) {
            // TODO: add a doThrow
            t.printStackTrace();
        }
        // FIXME: the parser may be in error, in that case we should throw the error further

        AST parsed = parser.getAST();
        return inlineAST(parsed);
    }

    public static AST inlineAST(AST parsed) {
        // new MakumbaDumpASTVisitor(false).visit(parsed);

        // inlining is a simple question of traversal with the inliner visitor
        TraverseState state = new TraverseState();
        AST ret = traverse(state, parsed, InlineVisitor.singleton);

        // and of adding the from and where sections discovered during inlining
        // FIXME: for now they are added to the root query. adding them to other subqueries may be required
        addFromWhere(ret, state);

        if (java.util.logging.Logger.getLogger("org.makumba.db.query.inline").getLevel().intValue() >= java.util.logging.Level.FINE.intValue())
            java.util.logging.Logger.getLogger("org.makumba.db.query.inline").fine(
                parsed.toStringList() + " \n-> " + ret.toStringList());

        return ret;
    }

    static class InlineVisitor implements ASTVisitor {
        static ASTVisitor singleton = new InlineVisitor();

        public AST visit(TraverseState state, AST current) {
            // the function signature is a method call which has a DOT as first child
            if (current.getType() != HqlSqlTokenTypes.METHOD_CALL)
                return current;
            if (current.getFirstChild().getType() == HqlSqlTokenTypes.IDENT
                    && current.getFirstChild().getText().equals("actor"))
                return treatActor(state, current);
            if (current.getFirstChild().getType() != HqlSqlTokenTypes.DOT)
                return current;

            final AST callee = current.getFirstChild().getFirstChild();
            String methodName = callee.getNextSibling().getText();

            // FIXME: this will blow if the expression is not a pointer. check that and throw exception
            DataDefinition calleeType = findType(state.path, callee).getPointedType();

            QueryFragmentFunction func = calleeType.getFunction(methodName);

            // we do a new function body parsing rather than using the AST stored in the MDD,
            // because we will heavily change the tree
            // FIXME: the MDD function body rewriting to add "this" should also be done with ASTs using traverse()
            // then the function tree should be duplicated here, also using traverse()
            // not sure whether MDDs need to return the rewritten function as text? 
            // TODO: separate traverse() in a commons utility class
            // TODO: use traversal in mql to rewrite parameters, IN SET, etc
            // the from FROM doesn't matter really
            HqlParser funcParser = HqlParser.getInstance("SELECT " + func.getQueryFragment() + "FROM "
                    + calleeType.getName() + " this");

            try {
                funcParser.statement();
            } catch (Throwable t) {
                // TODO: add a doThrow
                t.printStackTrace();
            }
            // here we should have no parser error since this function was parsed at MDD parse time

            // we go from query-> SELECT_FROM -> FROM-> SELELECT -> 1st projection
            AST funcAST = funcParser.getAST().getFirstChild().getFirstChild().getNextSibling().getFirstChild();

            DataDefinition para = func.getParameters();
            AST exprList = current.getFirstChild().getNextSibling();
            if (exprList.getNumberOfChildren() != para.getFieldNames().size())
                // FIXME: throw proper exception, refernce to original query
                throw new InvalidValueException("wrong number of parameters");

            // we copy the parameter expressions
            // FIXME: use findType(path, expr) for each parameter expression and check its type!
            final HashMap<String, AST> paramExpr = new HashMap<String, AST>();
            AST p = exprList.getFirstChild();
            for (String s : para.getFieldNames()) {
                paramExpr.put(s, p);
                p = p.getNextSibling();
            }

            // now we visit the function AST and replace this with the callee
            AST ret = traverse(new TraverseState(), funcAST, new ASTVisitor() {
                public AST visit(TraverseState state, AST node) {
                    // for each "this" node, we put the callee instead
                    if (node.getType() == HqlSqlTokenTypes.IDENT && node.getText().equals("this"))
                        return callee;
                    return node;
                }

            });

            // then we visit the function more, and replace parameter names with parameter expressions
            ret = traverse(new TraverseState(), ret, new ASTVisitor() {
                public AST visit(TraverseState state, AST node) {
                    // for each parameter node, we put the param expression instead
                    if (node.getType() == HqlSqlTokenTypes.IDENT && paramExpr.get(node.getText()) != null &&
                    // a field name from another table might have the same name as a param
                            // FIXME: there might be other cases where this is not the param but some field
                            state.path.get(state.path.size() - 1).getType() != HqlSqlTokenTypes.DOT
                    //        
                    )
                        return paramExpr.get(node.getText());
                    return node;
                }
            });
            // new MakumbaDumpASTVisitor(false).visit(ret);

            return ret;

        }

        private AST treatActor(TraverseState state, AST current) {
            AST actorType = current.getFirstChild().getNextSibling();
            if (actorType.getNumberOfChildren() != 1)
                // FIXME: throw the proper exception
                throw new InvalidValueException("actor(Type) must indicate precisely one type");
            actorType = actorType.getFirstChild();
            String rt = "actor_" + ASTUtil.constructPath(actorType).replace('.', '_');

            if (state.path.get(state.path.size() - 1).getType() == HqlSqlTokenTypes.DOT) {
                AST range = ASTUtil.makeNode(HqlSqlTokenTypes.RANGE, "RANGE");
                range.setFirstChild(makeASTCopy(actorType));
                range.getFirstChild().setNextSibling(ASTUtil.makeNode(HqlSqlTokenTypes.IDENT, rt));

                AST equal = ASTUtil.makeNode(HqlSqlTokenTypes.EQ, "=");
                equal.setFirstChild(ASTUtil.makeNode(HqlSqlTokenTypes.IDENT, rt));
                equal.getFirstChild().setNextSibling(ASTUtil.makeNode(HqlSqlTokenTypes.IDENT, "$" + rt));

                state.extraFrom.add(range);
                state.extraWhere.add(equal);
            } else
                // the simple case, we simply add the parameter
                rt = "$" + rt;

            return ASTUtil.makeNode(HqlSqlTokenTypes.IDENT, rt);
        }
    }

    /** Find the type of an expression AST, given the path to the query root */
    private static FieldDefinition findType(List<AST> path, AST expr) {

        // we build a query tree
        Node query = ASTUtil.makeNode(HqlSqlTokenTypes.QUERY, "query");
        Node selectFrom = ASTUtil.makeNode(HqlSqlTokenTypes.SELECT_FROM, "SELECT_FROM");
        Node select = ASTUtil.makeNode(HqlSqlTokenTypes.SELECT, "SELECT");
        Node from = ASTUtil.makeNode(HqlSqlTokenTypes.FROM, "FROM");

        query.setFirstChild(selectFrom);
        selectFrom.setFirstChild(from);

        // we copy the FROM part of all the queries in our path
        AST lastAdded = null;
        for (AST a : path) {
            // we find queriess
            if (a.getType() != HqlSqlTokenTypes.QUERY)
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

        from.setNextSibling(select);
        // we select just the expression we want to determine the type of
        select.setFirstChild(makeASTCopy(expr));

        // FIXME: any query analysis provider that accepts a pass1 tree should be usable here.
        // there should be no direct dependence on Mql.
        return new MqlQueryAnalysis(query).getProjectionType().getFieldDefinition(0);

    }

    /** Add to a query the ranges and the where conditions that were found. The AST is assumed to be the root of a query */
    private static void addFromWhere(AST root, TraverseState state) {
        for (AST range : state.extraFrom) {
            ASTUtil.appendSibling(root.getFirstChild().getFirstChild().getFirstChild(), range);
        }
        for (AST condition : state.extraWhere) {
            AST where = root.getFirstChild().getNextSibling();

            if (where.getType() == HqlSqlTokenTypes.WHERE) {
                AST and = ASTUtil.makeNode(HqlSqlTokenTypes.AND, "AND");
                and.setFirstChild(condition);
                condition.setNextSibling(where.getFirstChild());
                where.setFirstChild(and);
            } else {
                AST where1 = ASTUtil.makeNode(HqlSqlTokenTypes.WHERE, "WHERE");
                where1.setNextSibling(where);
                root.getFirstChild().setNextSibling(where1);
                where1.setFirstChild(condition);
            }
        }
    }

    /** make a copy of an AST, with the same first child, but with no next sibling */
    private static AST makeASTCopy(AST current1) {
        Node current2 = ASTUtil.makeNode(current1.getType(), current1.getText());
        // we connect the new node to the rest of the structure
        current2.setFirstChild(current1.getFirstChild());
        current2.setLine(current1.getLine());
        current2.setCol(current1.getColumn());

        // FIXME: how about the text which the line and column refer to?

        return current2;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader((InputStream) ClassResource.get(
                "org/makumba/providers/query/inlinerCorpus.txt").getContent()));
            String query = null;
            String lastQuery = null;
            String processedQuery = null;
            int line = 1;
            int lastQueryLine = -1;
            ArrayList<String> possible = new ArrayList<String>();

            while ((query = rd.readLine()) != null) {
                if (query.trim().startsWith("#"))
                    continue;
                if (query.trim().startsWith("->")) {
                    if (query.trim().substring(3).equals(processedQuery.trim()))
                        processedQuery = null;
                    else
                        possible.add(query);
                    // else maybe there is another version which matches
                    continue;
                }
                if (processedQuery != null) {
                    System.err.println(lastQueryLine + ": no match for:\n" + lastQuery + "\n->" + processedQuery);
                    for (String s : possible)
                        System.err.println(s);
                }
                possible.clear();
                lastQueryLine = line;
                lastQuery = query;
                processedQuery = inlineQuery(query).toStringList();
                line++;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
