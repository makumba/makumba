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

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.InvalidFieldTypeException;
import org.makumba.OQLParseError;
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

        HqlParser p = Pass1FunctionInliner.parseQuery(query);
        doThrow(query, p.getError(), p.getAST());
        AST parsed = Pass1FunctionInliner.inlineAST(p.getAST());

        AST fromAST= parsed.getFirstChild().getFirstChild().getFirstChild();
        
        // we re-construct the from after inlining
        // we assume that we have ranges like a.b.c x
        from="";
        String separator="";
        while(fromAST!=null){
            from+=separator+ ASTUtil.constructPath(fromAST.getFirstChild())+" "+fromAST.getFirstChild().getNextSibling();
            separator=", ";
            fromAST= fromAST.getNextSibling();
        }

        return checkASTSetOrNullable(from, parsed.getFirstChild().getFirstChild().getNextSibling().getFirstChild());
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

}
