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

import java.util.List;
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

import antlr.RecognitionException;
import antlr.collections.AST;

/**
 * @author
 * @version $Id$
 */
public abstract class QueryAnalysisProvider {
    public static final String DUMMY_PROJECTION= "mak_dummy_projection";

    protected abstract QueryAnalysis getRawQueryAnalysis(String query);

    protected QueryAnalysis getRawQueryAnalysis(String query, String insertIn){
        return getRawQueryAnalysis(query, null);
    }
    public QueryAnalysis getQueryAnalysis(String query) {
        return getRawQueryAnalysis(query);
    }

    public QueryAnalysis getQueryAnalysis(String query, String insertIn) {
        return getRawQueryAnalysis(query, insertIn);
    }

    //public abstract AST inlineFunctions(AST query);
    
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

        if(from == null) {
            // wtf?
            return null;
        }
        
        String query = "select " + expr + " from " + from;
       
        AST parsed = inlineFunctions(query);
        
        AST fromAST= parsed.getFirstChild().getFirstChild().getFirstChild();
        
        // we re-construct the from after inlining
        // we assume that we have ranges like a.b.c x
        from="";
        String separator="";
        
        while(fromAST!=null){
            // fromAST is RANGE or JOIN, its first child is expected to be the type
            AST type= fromAST.getFirstChild();

            // but sometimes its first child is "INNER" or "OUTER" or whatever
            // so we jump that because here we don't care what kind of join it is
            if(fromAST.getFirstChild().getType()!= HqlTokenTypes.DOT 
                    && fromAST.getFirstChild().getType()!=HqlTokenTypes.IDENT)
                type=type.getNextSibling();
            
            // now we write the type (path) and the label, which is its next sibling
            from+=separator+ ASTUtil.constructPath(type)+" "+type.getNextSibling();
            separator=", ";
            fromAST= fromAST.getNextSibling();
        }

        return checkASTSetOrNullable(from, parsed.getFirstChild().getFirstChild().getNextSibling().getFirstChild());
    }


    public static AST inlineFunctions(String query) {
        if (!Configuration.getQueryInliner().equals("pass1"))
            // HQL doesn't use this anyway
            return parseQuery(FunctionInliner.inline(query, QueryProvider.getQueryAnalzyer("oql")));
        return Pass1FunctionInliner.inlineAST(parseQuery(query));
    }

    private Object checkASTSetOrNullable(String from, AST ast) {
        if(ast==null)
            return null;
        if(ast.getType()==HqlTokenTypes.QUERY)
            // we don't go into subqueries
            return null;
        
        if(ast.getType()==HqlTokenTypes.DOT){
            Object o= checkLabelSetOrNullable(from, ASTUtil.constructPath(ast));
            if(o!=null)
                return o;
        }
        Object o= checkASTSetOrNullable(from, ast.getFirstChild());
        if(o!=null)
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

                if (fd.getType().equals("set") || fd.getType().equals("setintEnum") || fd.getType().equals("setcharEnum"))
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
    
    /**
     * Test method to compare two AST trees
     * @param path the path from the root to the compared nodes
     * @param t1 current node in the first tree
     * @param t2 current node in second tree
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
            if(parser.getError()==null)
                doThrow(query, e, parser.getAST());
        }
        doThrow(query, parser.getError(), parser.getAST());
        return parser.getAST();
    }

    /**
     * Add a dummy FROM section to the query if it doesn't have one, in order for it to conform to the grammars.
     * @param query
     * @return the new query
     */
    public static String checkForFrom(String query) {
        // first pass won't work without a FROM section, so we add a dummy catalog
        if (query.toLowerCase().indexOf("from") == -1) {
            return query+ " FROM org.makumba.db.makumba.Catalog "+DUMMY_PROJECTION;
        }
        return query;
    }
    
    /** Attempt to reduce the dummy FROM from the AST after inlining. 
     * Some inlining processes will add a from section, so the dummy FROM is not needed any longer.
     * @param parsed the AST
     * @return whether the query still needs a dummy from after inlining
     */
    public static boolean reduceDummyFrom(AST parsed) {
        AST from= parsed.getFirstChild().getFirstChild();
        if(from.getFirstChild().getFirstChild().getNextSibling().getText().equals(DUMMY_PROJECTION))
            if(from.getFirstChild().getNextSibling()!=null){
                // the query got a new FROM section after inlining, so we can remove our dummy catalog
                from.setFirstChild(from.getFirstChild().getNextSibling());
                return false;
            }
            else
                // there is no from even  after inlining, 
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

}

