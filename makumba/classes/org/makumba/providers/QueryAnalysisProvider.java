///////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2008  http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//  -------------
//  $Id$
//  $Name$
/////////////////////////////////////

package org.makumba.providers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.makumba.DataDefinition;
import org.makumba.DataDefinitionNotFoundError;
import org.makumba.FieldDefinition;
import org.makumba.InvalidFieldTypeException;
import org.makumba.OQLParseError;
import org.makumba.commons.RegExpUtils;
import org.makumba.providers.query.FunctionInliner;
import org.makumba.providers.query.Pass1FunctionInliner;
import org.makumba.providers.query.mql.ASTUtil;
import org.makumba.providers.query.mql.HqlParser;
import org.makumba.providers.query.mql.HqlTokenTypes;
import org.makumba.providers.query.mql.MqlQueryAnalysis;
import org.makumba.providers.query.mql.Node;

import antlr.RecognitionException;
import antlr.collections.AST;

/**
 * @author
 * @version $Id$
 */
public abstract class QueryAnalysisProvider implements Serializable {
    private static final long serialVersionUID = 1L;

    public abstract String getName();

    protected abstract QueryAnalysis getRawQueryAnalysis(String query);

    protected QueryAnalysis getRawQueryAnalysis(String query, String insertIn) {
        return getRawQueryAnalysis(query, null);
    }

    public QueryAnalysis getQueryAnalysis(String query) {
        return getRawQueryAnalysis(query);
    }

    public abstract QueryAnalysis getQueryAnalysis(AST query, DataDefinition knownLabels);

    public QueryAnalysis getQueryAnalysis(String query, String insertIn) {
        return getRawQueryAnalysis(query, insertIn);
    }

    // public abstract AST inlineFunctions(AST query);

    /**
     * Returns a possible alternative field to the one indicated.
     */
    public abstract FieldDefinition getAlternativeField(DataDefinition dd, String fn);

    /**
     * Returns the notation of the primary key in the query language
     * 
     * @param label
     *            the label of the object
     * @return the notation for the primary key of the object
     */
    public abstract String getPrimaryKeyNotation(String label);

    /**
     * Checks if an expression is valid, nullable or set
     * 
     * @param expr
     *            the expression
     * @return The path to the null pointer (if the object is nullable), <code>null</code> otherwise
     */
    public Object checkExprSetOrNullable(String from, String expr) {

        if (from == null) {
            // wtf?
            return null;
        }

        String query = "select " + expr + " from " + from;

        AST parsed = inlineFunctions(query);

        AST fromAST = parsed.getFirstChild().getFirstChild().getFirstChild();

        // we re-construct the from after inlining
        // we assume that we have ranges like a.b.c x
        from = "";
        String separator = "";

        while (fromAST != null) {
            // fromAST is RANGE or JOIN, its first child is expected to be the type
            AST type = fromAST.getFirstChild();

            // but sometimes its first child is "INNER" or "OUTER" or whatever
            // so we jump that because here we don't care what kind of join it is
            if (fromAST.getFirstChild().getType() != HqlTokenTypes.DOT
                    && fromAST.getFirstChild().getType() != HqlTokenTypes.IDENT) {
                type = type.getNextSibling();
            }

            // now we write the type (path) and the label, which is its next sibling
            String tp = type.getType() == HqlTokenTypes.DOT ? ASTUtil.constructPath(type) : type.getText();
            from += separator + tp + " " + type.getNextSibling();
            separator = ", ";
            fromAST = fromAST.getNextSibling();
        }

        return checkASTSetOrNullable(from, parsed.getFirstChild().getFirstChild().getNextSibling().getFirstChild());
    }

    private Object checkASTSetOrNullable(String from, AST ast) {
        if (ast == null) {
            return null;
        }
        if (ast.getType() == HqlTokenTypes.QUERY) {
            // we don't go into subqueries
            return null;
        }

        if (ast.getType() == HqlTokenTypes.DOT) {
            Object o = checkLabelSetOrNullable(from, ASTUtil.constructPath(ast));
            if (o != null) {
                return o;
            }
        }
        Object o = checkASTSetOrNullable(from, ast.getFirstChild());
        if (o != null) {
            return o;
        }
        return checkASTSetOrNullable(from, ast.getNextSibling());
    }

    /**
     * Checks if a character can be part of a makumba identifier
     * 
     * @param c
     *            the character to check
     * @return <code>true</code> if the character can be part of a makumba identifier, <code>false</code> otherwise
     */
    static boolean isMakId(char c) {
        return Character.isJavaIdentifierPart(c) || c == '.';
    }

    /**
     * Checks if an id is nullable, and if so, return the path to the null pointer
     * 
     * @param referenceSequence
     *            a sequence like field1.field2.field3
     * @return The path to the null pointer (if the object is nullable), <code>null</code> otherwise
     */
    public Object checkLabelSetOrNullable(String from, String referenceSequence) {
        int dot = referenceSequence.indexOf(".");
        if (dot == -1) {
            return null;
        }
        String substring = referenceSequence.substring(0, dot);
        try { // if the "label" is actually a real number as 3.0
            Integer.parseInt(substring);
            return null; // if so, just return
        } catch (NumberFormatException e) {
        }
        DataDefinition dd = getQueryAnalysis("SELECT 1 FROM " + from).getLabelType(substring);
        if (dd == null) {
            System.out.println(from);
            System.out.println(referenceSequence);
            throw new org.makumba.NoSuchLabelException("no such label '" + substring + "'.");
        }
        while (true) {
            int dot1 = referenceSequence.indexOf(".", dot + 1);
            if (dot1 == -1) {
                String fn = referenceSequence.substring(dot + 1);
                FieldDefinition fd = dd.getFieldDefinition(fn);
                if (fd == null && (fd = getAlternativeField(dd, fn)) == null) {
                    throw new org.makumba.NoSuchFieldException(dd, fn);
                }

                if (fd.getType().equals("set") || fd.getType().equals("setintEnum")
                        || fd.getType().equals("setcharEnum")) {
                    return fd;
                }
                return null;
            }
            FieldDefinition fd = dd.getFieldDefinition(referenceSequence.substring(dot + 1, dot1));
            if (fd == null) {
                throw new org.makumba.NoSuchFieldException(dd, referenceSequence.substring(dot + 1, dot1));
            }
            if (!fd.getType().startsWith("ptr")) {
                throw new InvalidFieldTypeException(fd, "pointer");
            }
            if (!fd.isNotNull()) {
                return referenceSequence.substring(0, dot1);
            }
            dd = fd.getPointedType();
            dot = dot1;
        }
    }

    /** return the first character(s) in a parameter designator */
    public abstract String getParameterSyntax();

    public static String getGeneratedActorName(AST actorType) {
        return "actor_" + ASTUtil.constructPath(actorType).replace('.', '_');
    }

    public AST inlineFunctions(String query) {
        if (!Configuration.getQueryInliner().equals("pass1")) {
            return parseQuery(FunctionInliner.inline(query, this));
        }
        return Pass1FunctionInliner.inlineAST(parseQuery(query), getName());
    }

    // TODO: this generates left join by default, which is useful for mak:list
    /**
     * HQL does not accept things like p.language without a JOIN before
     * 
     * @param query
     *            the original pass1 query tree
     * @return the transformed tree
     */
    public AST range2Join(AST query) {
        return new ASTTransformVisitor(false) {
            @Override
            public AST visit(AST a) {
                if (a.getType() != HqlTokenTypes.RANGE) {
                    return a;
                }
                AST parent = getPath().get(getPath().size() - 1);
                // we don't transform for the first range
                if (parent.getFirstChild() == a) {
                    return a;
                }
                try {
                    DataDefinitionProvider.getInstance().getDataDefinition(ASTUtil.constructPath(a.getFirstChild()));
                    return a;
                } catch (DataDefinitionNotFoundError de) {
                }
                AST ret = ASTUtil.makeNode(HqlTokenTypes.JOIN, "join");
                ret.setFirstChild(a.getFirstChild());
                ret.setNextSibling(a.getNextSibling());
                return ret;

            }
        }.traverse(query);
    }

    /**
     * add .id to pointer projections, for legacy queries to work
     * 
     * @param query
     *            the original pass1 query tree
     * @return the transformed tree
     */
    public AST addIdsToPointerProjections(AST query) {
        return new ASTTransformVisitor(false) {
            @Override
            public AST visit(AST a) {
                if (a.getText().startsWith("$") || a.getText().startsWith("?") || a.getText().startsWith(":")) {
                    return a;
                }
                if (getPath().size() == 0) {
                    return a;
                }
                AST parent = getPath().get(getPath().size() - 1);
                if (a.getType() == HqlTokenTypes.AS) {
                    return a;
                }
                if (parent.getType() != HqlTokenTypes.SELECT && parent.getType() != HqlTokenTypes.AS
                        && parent.getType() != HqlTokenTypes.ORDER && parent.getType() != HqlTokenTypes.GROUP
                        && parent.getType() != HqlTokenTypes.IN && parent.getType() != HqlTokenTypes.NOT_IN) {
                    return a;
                }
                if (parent.getType() == HqlTokenTypes.AS && parent.getFirstChild() != a) {
                    return a;
                }
                if ((parent.getType() == HqlTokenTypes.IN || parent.getType() == HqlTokenTypes.NOT_IN)
                        && parent.getFirstChild() != a) {
                    return a;
                }
                // maybe there is an id already
                if (a.getFirstChild() != null
                        && a.getFirstChild().getNextSibling() != null
                        && a.getType() == HqlTokenTypes.DOT
                        && ("id".equals(a.getFirstChild().getNextSibling().getText()) || "enum_".equals(a.getFirstChild().getNextSibling().getText()))) {
                    return a;
                }
                FieldDefinition type = findType(a, getName());
                if (type.getType().equals("boolean") && parent.getType() != HqlTokenTypes.CASE) {
                    AST ret = ASTUtil.makeNode(HqlTokenTypes.CASE, "case");
                    AST when = ASTUtil.makeNode(HqlTokenTypes.WHEN, "when");
                    AST elze = ASTUtil.makeNode(HqlTokenTypes.ELSE, "else");
                    elze.setFirstChild(ASTUtil.makeNode(HqlTokenTypes.NUM_INT, "0"));
                    when.setFirstChild(QueryAnalysisProvider.makeASTCopy(a));
                    when.getFirstChild().setNextSibling(ASTUtil.makeNode(HqlTokenTypes.NUM_INT, "1"));
                    ret.setFirstChild(when);
                    when.setNextSibling(elze);
                    ret.setNextSibling(a.getNextSibling());
                    return ret;

                }

                String toAdd = null;
                if (type.getType().startsWith("ptr")) {
                    toAdd = "id";
                }
                if (type.getType().endsWith("Enum") && type.getOriginalFieldDefinition() != null
                        && type.getOriginalFieldDefinition().getName().equals("enum")) {
                    toAdd = "enum_";
                }
                if (toAdd == null) {
                    return a;
                }
                AST ret = ASTUtil.makeNode(HqlTokenTypes.DOT, ".");
                AST idAST = ASTUtil.makeNode(HqlTokenTypes.IDENT, toAdd);
                AST a1 = QueryAnalysisProvider.makeASTCopy(a);
                ret.setFirstChild(a1);
                ret.setNextSibling(a.getNextSibling());
                // copy the ident so its next sibling doesn't get to the this tree
                a1.setNextSibling(idAST);
                return ret;
            }

        }.traverse(query);
    }

    /** make a copy of an AST, node with the same first child, but with no next sibling */
    public static AST makeASTCopy(AST current1) {
        Node current2 = ASTUtil.makeNode(current1.getType(), current1.getText());
        // we connect the new node to the rest of the structure
        current2.setFirstChild(current1.getFirstChild());
        current2.setLine(current1.getLine());
        current2.setCol(current1.getColumn());

        // FIXME: how about the text which the line and column refer to?

        return current2;
    }

    public static abstract class ASTTransformVisitor {

        public boolean repetitive;

        private Stack<AST> path = new Stack<AST>();

        public ASTTransformVisitor(boolean repetitive) {
            this.repetitive = repetitive;
        }

        public boolean isRepetitive() {
            return repetitive;
        }

        public Stack<AST> getPath() {
            return path;
        }

        public abstract AST visit(AST a);

        /**
         * recursively traverse the tree, and transform it via a visitor. keep a traversal state, which includes the
         * path from the tree root to the current node
         */
        public AST traverse(AST current) {
            if (current == null) {
                return null;
            }

            boolean wasReplaced = false;
            // while traversal returns new stuff (e.g. an inlined function is still a function to inline) we repeat
            while (true) {
                AST current1 = visit(current);
                if (current1 != current) {
                    // we copy the root node of the structure we get
                    AST current2 = makeASTcopy(current1);
                    // and link it to the rest of our tree
                    current2.setNextSibling(current.getNextSibling());
                    current = current2;
                    if (!repetitive) {
                        wasReplaced = true;
                        // if we do not need to traverse what we replaced we go on
                        break;
                    }
                } else {
                    break;
                }
            }
            // if we replaced already, and we are not repetitive, we do't go deep
            if (!wasReplaced) {
                getPath().push(current);
                current.setFirstChild(traverse(current.getFirstChild()));
                getPath().pop();
            }

            // but we go wide
            current.setNextSibling(traverse(current.getNextSibling()));

            return current;
        }

        protected AST makeASTcopy(AST current1) {
            return QueryAnalysisProvider.makeASTCopy(current1);
        }

        /**
         * Find the type of an expression AST, given the path to the query root. Makes a query treee and invokes the
         * second pass on it.
         */
        protected FieldDefinition findType(AST expr, String qap) {

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
                if (a.getType() != HqlTokenTypes.QUERY) {
                    continue;
                }
                // we duplicate the FROM section of each query
                AST originalFrom = a.getFirstChild().getFirstChild().getFirstChild();
                AST toAdd = makeASTcopy(originalFrom);
                if (lastAdded == null) {
                    from.setFirstChild(toAdd);
                } else {
                    lastAdded.setNextSibling(toAdd);
                }
                lastAdded = toAdd;
                while (originalFrom.getNextSibling() != null) {
                    originalFrom = originalFrom.getNextSibling();
                    toAdd = makeASTcopy(originalFrom);
                    lastAdded.setNextSibling(toAdd);
                    lastAdded = toAdd;
                }
            }
            // TODO: also add the extraFrom (enrichment) to the new query FROM
            from.setNextSibling(select);

            // we select just the expression we want to determine the type of
            select.setFirstChild(makeASTcopy(expr));

            return QueryProvider.getQueryAnalzyer(qap).getQueryAnalysis(query, null).getProjectionType().getFieldDefinition(
                0);

        }
    }

    /**
     * A holder for FROM and WHERE sections to enrich a query, and the query enrichment code. Can be called repeatedly
     * on the same query. FIXME: for now only adding to the root query is performed
     * 
     * @author cristi
     * @version $Id$
     */
    public static class FromWhere {

        private List<AST> extraFrom = new ArrayList<AST>();

        private List<AST> extraWhere = new ArrayList<AST>();

        public void addFromWhere(AST from, AST where) {
            extraFrom.add(from);
            extraWhere.add(where);
        }

        public void addFrom(AST from) {
            extraFrom.add(from);

        }

        public void addWhere(AST where) {
            extraWhere.add(where);
        }

        Set<String> actors = new HashSet<String>();

        public void addActor(AST actorType, String paramSyntax) {
            if (actors.contains(actorType)) {
                return;
            }
            String act = getGeneratedActorName(actorType);
            // make an extra FROM
            AST range = ASTUtil.makeNode(HqlTokenTypes.RANGE, "RANGE");
            range.setFirstChild(makeASTCopy(actorType));
            range.getFirstChild().setNextSibling(ASTUtil.makeNode(HqlTokenTypes.ALIAS, act));

            // make an extra where
            AST equal = ASTUtil.makeNode(HqlTokenTypes.EQ, "=");
            equal.setFirstChild(ASTUtil.makeNode(HqlTokenTypes.IDENT, act));
            // FIXME: in HQL the parameter syntax is a tree (: paamName) not an IDENT
            equal.getFirstChild().setNextSibling(ASTUtil.makeNode(HqlTokenTypes.IDENT, paramSyntax + act));

            addFromWhere(range, equal);
        }

        /**
         * Add to a query the ranges and the where conditions that were found. The AST is assumed to be the root of a
         * query
         */
        public void addToTreeFromWhere(AST query) {
            // FIXME: it may be that a from and a where condition are already in here!
            // in that case, nothing needs to be done!
            // this is the case if actor( ...).field is used multiple times in the same query

            AST firstFrom = query.getFirstChild().getFirstChild().getFirstChild();
            for (AST range : extraFrom) {
                ASTUtil.appendSibling(firstFrom, range);
            }
            // the FROM ranges are in the FROM now, we clear it so it won't be added again
            extraFrom.clear();
            for (AST condition : extraWhere) {
                if (condition == null) {
                    continue;
                }

                AST where = query.getFirstChild().getNextSibling();

                if (where != null && where.getType() == HqlTokenTypes.WHERE) {
                    AST and = ASTUtil.makeNode(HqlTokenTypes.AND, "AND");
                    and.setFirstChild(condition);
                    condition.setNextSibling(where.getFirstChild());
                    where.setFirstChild(and);
                } else {
                    AST where1 = ASTUtil.makeNode(HqlTokenTypes.WHERE, "WHERE");
                    // where is not really a where here, can be null or can be somethig else, like orderby
                    where1.setNextSibling(where);
                    query.getFirstChild().setNextSibling(where1);
                    where1.setFirstChild(condition);
                }
            }
            // the where conditions are in the WHERE now, we clear them so they won't be added again
            extraWhere.clear();
        }

    }

    /**
     * Test method to compare two AST trees
     * 
     * @param path
     *            the path from the root to the compared nodes
     * @param t1
     *            current node in the first tree
     * @param t2
     *            current node in second tree
     * @return whether the trees are identical or not
     */
    public static boolean compare(List<AST> path, AST t1, AST t2) {
        if (t1 == null) {
            if (t2 != null) {
                System.out.println(path + " t1 null, t2 not null");
                return false;
            } else {
                return true;
            }
        }
        if (!t1.equals(t2)) {
            System.out.print(path + " [" + t1.getType() + " " + t1 + "] <> ");
            if (t2 == null) {
                System.out.println("null");
            } else {
                System.out.println("[" + t2.getType() + " " + t2 + "]");
            }

            return false;
        }
        if (!compare(path, t1.getNextSibling(), t2.getNextSibling())) {
            return false;
        }
        path.add(t1);
        try {
            return compare(path, t1.getFirstChild(), t2.getFirstChild());
        } finally {
            path.remove(path.size() - 1);
        }
    }

    public static void doThrow(String query, Throwable t, AST debugTree) {
        if (t == null) {
            return;
        }
        if (t instanceof RuntimeException) {
            t.printStackTrace();
            throw (RuntimeException) t;
        }
        String errorLocation = "";
        String errorLocationNumber = "";
        if (t instanceof RecognitionException) {
            RecognitionException re = (RecognitionException) t;
            if (re.getColumn() > 0) {
                errorLocationNumber = " column " + re.getColumn() + " of ";
                StringBuffer sb = new StringBuffer();
                sb.append("\r\n");

                for (int i = 0; i < re.getColumn(); i++) {
                    sb.append(' ');
                }
                sb.append('^');
                errorLocation = sb.toString();
            }
        }
        throw new OQLParseError("\r\n\r\nin " + errorLocationNumber + " query:\r\n\r\n" + query + errorLocation
                + errorLocation + errorLocation, t);
    }

    /*
     * 
     * This is code from the old HQL pass1 parser invocation
     * 
     *      HqlParser parser = HqlParser.getInstance(query1);

        // Parse the input expression
        try {
            parser.statement();
            ParseErrorHandler parseErrorHandler = parser.getParseErrorHandler();
            if(parseErrorHandler.getErrorCount()>0)
                parseErrorHandler.throwQueryException();
            
            
            //if(t1!=null){ ASTFrame frame = new ASTFrame("normal", t1);
            //frame.setVisible(true); }
            
            
            //here I can display the tree and look at the tokens, then find them in the grammar and implement the function type detection

            // Print the resulting tree out in LISP notation
            
        } catch(QuerySyntaxException g){
            throw new OQLParseError("during analysis of query: " + query1, g);           
        }
        catch (antlr.ANTLRException f) {
            throw new OQLParseError("during analysis of query: " + query1, f);
        }
      
     */

    public static AST parseQuery(String query) {
        query = preProcess(query);
        HqlParser parser = HqlParser.getInstance(query);
        try {
            parser.statement();
        } catch (Exception e) {
            if (parser.getError() == null) {
                doThrow(query, e, parser.getAST());
            }
        }
        doThrow(query, parser.getError(), parser.getAST());
        return parser.getAST();
    }

    /**
     * Add a dummy FROM section to the query if it doesn't have one, in order for it to conform to the grammars.
     * 
     * @param query
     * @return the new query
     */
    public static String checkForFrom(String query) {
        // first pass won't work without a FROM section, so we add a dummy catalog
        if (query.toLowerCase().indexOf("from") == -1) {
            return query + " FROM org.makumba.db.makumba.Catalog " + DUMMY_PROJECTION;
        }
        return query;
    }

    public static final String DUMMY_PROJECTION = "mak_dummy_projection";

    /**
     * Attempt to reduce the dummy FROM from the AST after inlining. Some inlining processes will add a from section, so
     * the dummy FROM is not needed any longer.
     * 
     * @param parsed
     *            the AST
     * @return whether the query still needs a dummy from after inlining
     */
    public static boolean reduceDummyFrom(AST parsed) {
        AST from = parsed.getFirstChild().getFirstChild();
        if (from.getFirstChild().getFirstChild().getNextSibling().getText().equals(DUMMY_PROJECTION)) {
            if (from.getFirstChild().getNextSibling() != null) {
                // the query got a new FROM section after inlining, so we can remove our dummy catalog
                from.setFirstChild(from.getFirstChild().getNextSibling());
                return false;
            } else {
                // there is no from even after inlining,
                // so we leave the catalog hanged here, otherwise the second pass will flop
                return true;
            }
        }
        return false;
    }

    public static final String regExpInSET = "in" + RegExpUtils.minOneWhitespace + "set" + RegExpUtils.whitespace
            + "\\(";

    public static final Pattern patternInSet = Pattern.compile(regExpInSET);

    public static String preProcess(String query) {
        // replace -> (subset separators) with __
        query = query.replaceAll("->", "__");

        // replace IN SET with IN.
        Matcher m = patternInSet.matcher(query.toLowerCase()); // find all occurrences of lower-case "in set"
        while (m.find()) {
            int start = m.start();
            int beginSet = m.group().indexOf("set"); // find location of "set" keyword
            // System.out.println(query);
            // composing query by concatenating the part before "set", 3 space and the part after "set"
            query = query.substring(0, start + beginSet) + "   " + query.substring(start + beginSet + 3);
            // System.out.println(query);
            // System.out.println();
        }
        query = query.replaceAll("IN SET", "IN    ");
        return query;
    }

    // this code is _experimental_ ---------------------------------------------------------------------
    static class SubqueryReductionVisitor extends ASTTransformVisitor {

        public SubqueryReductionVisitor() {
            super(true);
        }

        FromWhere fromWhere = new FromWhere();

        @Override
        public AST visit(AST current) {
            // we are after queries, but not the root query
            if (current.getType() != HqlTokenTypes.QUERY || getPath().size() == 0) {
                return current;
            }
            // the reduction is defensive (i.e. we do not reduce unless we are sure that there are no problems)
            AST parent = getPath().peek();

            // if we are the second child of the parent, then operator IN expects a multiple-result query
            if (parent.getFirstChild() != current) {
                if (parent.getType() == HqlTokenTypes.IN) {
                    return current;
                } else {
                    ;
                }
            } else {
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
            }

            // TODO: postorder, depth-first traversal!
            // TODO: currently we only add to the root query, maybe we should add to the enclosing query, and flatten
            // iteratively

            // TODO: left join when enriching the outer query in order not to mess up its result?

            // TODO: not inline queries with a groupBy, queries without a where
            // TODO: queries that have orderby, or more than one projection, should not be accepted here?

            // FIXME: i don't think that these copy more than the 1st child (FROM range, WHERE condition), got to check.
            fromWhere.addFrom(current.getFirstChild().getFirstChild().getFirstChild());

            if (current.getFirstChild().getNextSibling().getType() == HqlTokenTypes.WHERE) {
                fromWhere.addWhere(current.getFirstChild().getNextSibling().getFirstChild());
            }

            AST proj = current.getFirstChild().getFirstChild().getNextSibling().getFirstChild();

            // FIXME: the distinct should go to the decorated query
            if (proj.getType() == HqlTokenTypes.DISTINCT) {
                proj = proj.getNextSibling();
            }
            return proj;
        }
    }

    private static AST flatten(AST processedAST) {
        SubqueryReductionVisitor s = new SubqueryReductionVisitor();
        AST a = s.traverse(processedAST);
        s.fromWhere.addToTreeFromWhere(a);
        return a;
    }

    /** Transform OQL $x into :parameters, and record the parameter order */
    public static void transformOQLParameters(AST a, List<String> parameterOrder) {
        if (a == null) {
            return;
        }
        // MQL allows $some.param
        if (a.getType() == HqlTokenTypes.DOT && a.getFirstChild().getText().startsWith("$")) {
            a.setType(HqlTokenTypes.IDENT);
            a.setText(a.getFirstChild().getText() + "." + a.getFirstChild().getNextSibling().getText());
            a.setFirstChild(null);
        }
        if (a.getType() == HqlTokenTypes.IDENT && a.getText().startsWith("$") && a.getText().indexOf("###") < 0) {
            // replacement of $n with (: makumbaParam n)
            /*if (parameterOrder == null) {
                toOrdinalParam(a);
            } else {
            */
            a.setType(HqlTokenTypes.COLON);
            AST para = new Node();
            para.setType(HqlTokenTypes.IDENT);
            try {
                para.setText(MqlQueryAnalysis.MAKUMBA_PARAM + (Integer.parseInt(a.getText().substring(1)) - 1));
            } catch (NumberFormatException e) {
                // we probably are in some query analysis, so we ignore
                para.setText(a.getText().substring(1));
            }

            if (parameterOrder != null) {
                parameterOrder.add(para.getText());
                // we append in the tree to the parameter name the parameter position,
                // to be able to retrieve the position, and thus identify the parameter at type analysis
                para.setText(para.getText() + "###" + (parameterOrder.size() - 1));
            }
            a.setFirstChild(para);
            a.setText(":");
        } else if (a.getType() == HqlTokenTypes.COLON && a.getFirstChild() != null
                && a.getFirstChild().getType() == HqlTokenTypes.IDENT) {
            if (parameterOrder != null) {
                // we also accept : params though we might not know what to do with them later
                parameterOrder.add(a.getFirstChild().getText());
                // we append in the tree to the parameter name the parameter position,
                // to be able to retrieve the position, and thus identify the parameter at type analysis
                // unless this is a "valid" : param (result of function inlining)
                if (a.getFirstChild().getText().indexOf("###") < 0
                        && !a.getFirstChild().getText().startsWith(MqlQueryAnalysis.MAKUMBA_PARAM)) {
                    a.getFirstChild().setText(a.getFirstChild().getText() + "###" + (parameterOrder.size() - 1));
                }
            } else {
                // toOrdinalParam(a);
            }

        } else if (a.getType() == HqlTokenTypes.PARAM) {
            // we simulate $n

            a.setType(HqlTokenTypes.COLON);
            AST para = new Node();
            para.setType(HqlTokenTypes.IDENT);

            String s = "param" + parameterOrder.size();
            para.setText(s + "###" + parameterOrder.size());
            parameterOrder.add(s);
            a.setFirstChild(para);
            a.setText(":");
        }
        if (a.getType() == HqlTokenTypes.SELECT_FROM) {
            // first the SELECT part
            transformOQLParameters(a.getFirstChild().getNextSibling(), parameterOrder);
            // then the FROM part
            transformOQLParameters(a.getFirstChild(), parameterOrder);
            // then the rest
            transformOQLParameters(a.getNextSibling(), parameterOrder);

        } else {
            transformOQLParameters(a.getFirstChild(), parameterOrder);
            // we make sure we don't do "SELECT" again
            if (a.getType() != HqlTokenTypes.FROM) {
                transformOQLParameters(a.getNextSibling(), parameterOrder);
            }
        }
    }

    private static void toOrdinalParam(AST a) {
        a.setType(HqlTokenTypes.PARAM);
        a.setText("?");
        a.setFirstChild(null);
    }

    /**
     * Transform the tree so that various OQL notations are still accepted replacement of = or <> NIL with IS (NOT) NULL
     * OQL puts a 0.0+ in front of any AVG() expression This method also does various subquery transformations which are
     * not OQL-specific, to support: size(), elements(), firstElement()...
     */
    public static void transformOQL(AST a) {
        if (a == null) {
            return;
        }
        if (a.getType() == HqlTokenTypes.EQ || a.getType() == HqlTokenTypes.NE) {
            // replacement of = or <> NIL with IS (NOT) NULL
            if (isNil(a.getFirstChild())) {
                setNullTest(a);
                a.setFirstChild(a.getFirstChild().getNextSibling());
            } else if (isNil(a.getFirstChild().getNextSibling())) {
                setNullTest(a);
                a.getFirstChild().setNextSibling(null);
            }

        } else if (a.getType() == HqlTokenTypes.AGGREGATE && a.getText().toLowerCase().equals("avg")) {
            // OQL puts a 0.0+ in front of any AVG() expression probably to force the result to be floating point
            AST plus = new Node();
            plus.setType(HqlTokenTypes.PLUS);
            plus.setText("+");
            AST zero = new Node();
            zero.setType(HqlTokenTypes.NUM_DOUBLE);
            zero.setText("0.0");
            plus.setFirstChild(zero);
            zero.setNextSibling(a.getFirstChild());
            a.setFirstChild(plus);
        } else if (a.getType() == HqlTokenTypes.ELEMENTS) {
            makeSubquery(a, a.getFirstChild());
        } else if (a.getType() == HqlTokenTypes.METHOD_CALL && a.getFirstChild().getText().toLowerCase().equals("size")) {
            makeSelect(a, HqlTokenTypes.COUNT, "count");
        } else if (a.getType() == HqlTokenTypes.METHOD_CALL
                && a.getFirstChild().getText().toLowerCase().endsWith("element")) {
            makeSelect(a, HqlTokenTypes.AGGREGATE, a.getFirstChild().getText().substring(0, 3));
        }

        transformOQL(a.getFirstChild());
        transformOQL(a.getNextSibling());
    }

    static boolean isNil(AST a) {
        return a.getType() == HqlTokenTypes.IDENT && a.getText().toUpperCase().equals("NIL");
    }

    static void setNullTest(AST a) {
        if (a.getType() == HqlTokenTypes.EQ) {
            a.setType(HqlTokenTypes.IS_NULL);
            a.setText("is null");
        } else {
            a.setType(HqlTokenTypes.IS_NOT_NULL);
            a.setText("is not null");
        }
    }

    static void makeSelect(AST a, int type, String text) {
        makeSubquery(a, a.getFirstChild().getNextSibling().getFirstChild());
        AST from = a.getFirstChild().getFirstChild();
        from.setNextSibling(ASTUtil.makeNode(HqlTokenTypes.SELECT, "select"));
        from.getNextSibling().setFirstChild(ASTUtil.makeNode(type, text));
        from.getNextSibling().getFirstChild().setFirstChild(ASTUtil.makeNode(HqlTokenTypes.IDENT, "makElementsLabel"));
    }

    static void makeSubquery(AST a, AST type) {
        a.setType(HqlTokenTypes.QUERY);
        a.setFirstChild(ASTUtil.makeNode(HqlTokenTypes.SELECT_FROM, "select"));
        a.getFirstChild().setFirstChild(ASTUtil.makeNode(HqlTokenTypes.FROM, "from"));
        a.getFirstChild().getFirstChild().setFirstChild(ASTUtil.makeNode(HqlTokenTypes.RANGE, "range"));
        a.getFirstChild().getFirstChild().getFirstChild().setFirstChild(type);
        type.setNextSibling(ASTUtil.makeNode(HqlTokenTypes.ALIAS, "makElementsLabel"));
    }
}
