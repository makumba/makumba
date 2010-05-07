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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.InvalidFieldTypeException;
import org.makumba.OQLParseError;
import org.makumba.commons.RegExpUtils;
import org.makumba.providers.datadefinition.mdd.MakumbaDumpASTVisitor;
import org.makumba.providers.query.FunctionInliner;
import org.makumba.providers.query.Pass1FunctionInliner;
import org.makumba.providers.query.mql.ASTUtil;
import org.makumba.providers.query.mql.HqlParser;
import org.makumba.providers.query.mql.HqlTokenTypes;
import org.makumba.providers.query.mql.Node;

import antlr.RecognitionException;
import antlr.collections.AST;

/**
 * @author
 * @version $Id$
 */
public abstract class QueryAnalysisProvider {

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

    /** Returns whether the GROUP BY or ORDER BY sections can include labels */
    public abstract boolean selectGroupOrOrderAsLabels();

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
                    && fromAST.getFirstChild().getType() != HqlTokenTypes.IDENT)
                type = type.getNextSibling();

            // now we write the type (path) and the label, which is its next sibling
            from += separator + ASTUtil.constructPath(type) + " " + type.getNextSibling();
            separator = ", ";
            fromAST = fromAST.getNextSibling();
        }

        return checkASTSetOrNullable(from, parsed.getFirstChild().getFirstChild().getNextSibling().getFirstChild());
    }

    public static AST inlineFunctions(String query) {
        if (!Configuration.getQueryInliner().equals("pass1"))
            // HQL doesn't use this anyway
            return parseQuery(FunctionInliner.inline(query, QueryProvider.getQueryAnalzyer("oql")));
        return Pass1FunctionInliner.inlineAST(parseQuery(query), "oql");
    }

    private Object checkASTSetOrNullable(String from, AST ast) {
        if (ast == null)
            return null;
        if (ast.getType() == HqlTokenTypes.QUERY)
            // we don't go into subqueries
            return null;

        if (ast.getType() == HqlTokenTypes.DOT) {
            Object o = checkLabelSetOrNullable(from, ASTUtil.constructPath(ast));
            if (o != null)
                return o;
        }
        Object o = checkASTSetOrNullable(from, ast.getFirstChild());
        if (o != null)
            return o;
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
        if (dot == -1)
            return null;
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
                if (fd == null && (fd = getAlternativeField(dd, fn)) == null)
                    throw new org.makumba.NoSuchFieldException(dd, fn);

                if (fd.getType().equals("set") || fd.getType().equals("setintEnum")
                        || fd.getType().equals("setcharEnum"))
                    return fd;
                return null;
            }
            FieldDefinition fd = dd.getFieldDefinition(referenceSequence.substring(dot + 1, dot1));
            if (fd == null)
                throw new org.makumba.NoSuchFieldException(dd, referenceSequence.substring(dot + 1, dot1));
            if (!fd.getType().startsWith("ptr"))
                throw new InvalidFieldTypeException(fd, "pointer");
            if (!fd.isNotNull())
                return referenceSequence.substring(0, dot1);
            dd = fd.getPointedType();
            dot = dot1;
        }
    }

    /** return the first character(s) in a parameter designator */
    public abstract String getParameterSyntax();

    public static String getGeneratedActorName(AST actorType){
        return "actor_" + ASTUtil.constructPath(actorType).replace('.', '_');
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
            this.repetitive= repetitive;
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
            if (current == null)
                return null;

            boolean wasReplaced = false;
            // while traversal returns new stuff (e.g. an inlined function is still a function to inline) we repeat
            while (true) {
                AST current1 = visit(current);
                if (current1 != current) {
                    // we copy the root node of the structure we get
                    AST current2 = QueryAnalysisProvider.makeASTCopy(current1);
                    // and link it to the rest of our tree
                    current2.setNextSibling(current.getNextSibling());
                    current = current2;
                    if (!repetitive) {
                        wasReplaced = true;
                        // if we do not need to traverse what we replaced we go on
                        break;
                    }
                } else
                    break;
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
    }

    /**
     * A holder for FROM and WHERE sections to enrich a query, and the query enrichment code.
     * Can be called repeatedly on the same query.
     * FIXME: for now only adding to the root query is performed
     * @author cristi
     * @version $Id$
     */
    public static class FromWhere{

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

        Set<String> actors= new HashSet<String>();
        public void addActor(AST actorType, String paramSyntax) {
            if(actors.contains(actorType))
                return;
            String act = getGeneratedActorName(actorType);
            // make an extra FROM
            AST range = ASTUtil.makeNode(HqlTokenTypes.RANGE, "RANGE");
            range.setFirstChild(makeASTCopy(actorType));
            range.getFirstChild().setNextSibling(ASTUtil.makeNode(HqlTokenTypes.ALIAS, act));

            //make an extra where
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
                if (condition == null)
                    continue;

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
        if (t1 == null)
            if (t2 != null) {
                System.out.println(path + " t1 null, t2 not null");
                return false;
            } else
                return true;
        if (!t1.equals(t2)) {
            System.out.print(path + " [" + t1.getType() + " " + t1 + "] <> ");
            if (t2 == null)
                System.out.println("null");
            else
                System.out.println("[" + t2.getType() + " " + t2 + "]");

            return false;
        }
        if (!compare(path, t1.getNextSibling(), t2.getNextSibling()))
            return false;
        path.add(t1);
        try {
            return compare(path, t1.getFirstChild(), t2.getFirstChild());
        } finally {
            path.remove(path.size() - 1);
        }
    }

    public static void doThrow(String query, Throwable t, AST debugTree) {
        if (t == null)
            return;
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
        throw new OQLParseError("\r\nin " + errorLocationNumber + " query:\r\n" + query + errorLocation + errorLocation
                + errorLocation, t);
    }

    public static AST parseQuery(String query) {
        query = preProcess(query);
        HqlParser parser = HqlParser.getInstance(query);
        try {
            parser.statement();
        } catch (Exception e) {
            if (parser.getError() == null)
                doThrow(query, e, parser.getAST());
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
        if (from.getFirstChild().getFirstChild().getNextSibling().getText().equals(DUMMY_PROJECTION))
            if (from.getFirstChild().getNextSibling() != null) {
                // the query got a new FROM section after inlining, so we can remove our dummy catalog
                from.setFirstChild(from.getFirstChild().getNextSibling());
                return false;
            } else
                // there is no from even after inlining,
                // so we leave the catalog hanged here, otherwise the second pass will flop
                return true;
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
        
        FromWhere fromWhere= new FromWhere();

        public AST visit(AST current) {
            // we are after queries, but not the root query
            if (current.getType() != HqlTokenTypes.QUERY || getPath().size() == 0)
                return current;
            // the reduction is defensive (i.e. we do not reduce unless we are sure that there are no problems)
            AST parent = getPath().peek();

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
            fromWhere.addFrom(current.getFirstChild().getFirstChild().getFirstChild());

            if (current.getFirstChild().getNextSibling().getType() == HqlTokenTypes.WHERE)
                fromWhere.addWhere(current.getFirstChild().getNextSibling().getFirstChild());

            AST proj = current.getFirstChild().getFirstChild().getNextSibling().getFirstChild();

            // FIXME: the distinct should go to the decorated query
            if (proj.getType() == HqlTokenTypes.DISTINCT)
                proj = proj.getNextSibling();
            return proj;
        }
    }

    private static AST flatten(AST processedAST) {
        SubqueryReductionVisitor s= new SubqueryReductionVisitor();
        AST a = s.traverse(processedAST);
        s.fromWhere.addToTreeFromWhere(a);
        return a;
    }
}
