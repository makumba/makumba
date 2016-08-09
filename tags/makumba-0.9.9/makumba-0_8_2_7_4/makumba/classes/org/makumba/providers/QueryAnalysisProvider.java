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
import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;
import org.makumba.providers.query.FunctionInliner;

import test.oqlanalyzer;

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
        return getRawQueryAnalysis(inlineFunctions(query));
    }

    public QueryAnalysis getQueryAnalysis(String query, String insertIn) {
        return getRawQueryAnalysis(inlineFunctions(query), insertIn);
    }

    public String inlineFunctions(String query) {
        try {
            return (String) inlinedQueries.getResource(query);
        } catch (NullPointerException e) {
            initializeCache(query, e);
            String inlined = (String) inlinedQueries.getResource(query);
            return inlined;
        }
    }

    NamedResources inlinedQueries;

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
        if (expr.toLowerCase().indexOf(" from ") != -1)
            // subqueries do not need separate queries
            return null;

        if(from == null) {
            // wtf?
            return null;
        }
        
        String query = "select " + expr + " from " + from;
        query = inlineFunctions(query);
        
        int lastFromIndex = query.toLowerCase().lastIndexOf(" from ");
        expr = query.substring(0, lastFromIndex);
        expr = expr.substring(7);
        
        // since FROM may have changed due to inlining of functions that require joins, we also need to replace the from
        // we also check for where, maybe a query function appended it
        
        int lastWhereIndex = query.toLowerCase().indexOf(" where ");
        if(lastWhereIndex < lastFromIndex) {
            lastWhereIndex = -1;
        }
        
        if(lastWhereIndex > -1) {
            from = query.substring(query.indexOf(" from ") + 6, lastWhereIndex);
        } else {
            from = query.substring(lastFromIndex + 6);
        }
        
        // at this stage, we might either have a.b.c or have something else, such as a method like exists(subquery)
        // so in that case, we have to return null, but we first have to check that this is the case
        // FIXME it seems that in the time of OQL, makumba was not built to handle complex expressions of this kind
        // we might have to do a lot more work here
        // this is a very clumsy check, but it does the job for now
        if(expr.indexOf("(")  > -1 && expr.endsWith(")")) {
            return null;
        }
                

        int n = 0;
        int m = 0;
        while (true) {
            // FIXME: this is a not that good algorithm for finding label.field1.fiel2.field3
            while (n < expr.length() && !isMakId(expr.charAt(n)))
                n++;

            if (n == expr.length())
                return null;
            m = n;
            while (n < expr.length() && isMakId(expr.charAt(n)))
                n++;
            Object nl = checkLabelSetOrNullable(from, expr.substring(m, n));
            if (nl != null)
                return nl;
            if (n == expr.length())
                return null;
        }
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

    boolean initializedCache;

    private synchronized void initializeCache(String query, NullPointerException e) {
        if (inlinedQueries == null) {
            inlinedQueries = NamedResources.getStaticCache(NamedResources.makeStaticCache("Inlined queries by "
                    + getClass().getName().substring(getClass().getName().lastIndexOf(".") + 1),
                new NamedResourceFactory() {
                    private static final long serialVersionUID = 1L;

                    protected Object makeResource(Object nm, Object hashName) throws Exception {
                        
                        String result = "";
                        
                        if(!Configuration.getQueryInliner().equals("tree")) {
                            result = FunctionInliner.inline((String) nm, QueryAnalysisProvider.this);
                        } else {
                            // don't inline here, inline directly during processing
                            result = (String)nm;

                        }
                        return result;
                        
                    }
                }, true));
            initializedCache = true;
        } else {
            if (!initializedCache)
                throw e;
        }
    }

    /** return the first character(s) in a parameter designator */
    public abstract String getParameterSyntax();

    public static void main(String[] args) {
        for (int i = 0; i < oqlanalyzer.TEST_MDD_FUNCTIONS.length; i++) {
            String string = oqlanalyzer.TEST_MDD_FUNCTIONS[i];
            System.out.println(string + "\n=>\n" + QueryProvider.getQueryAnalzyer("oql").inlineFunctions(string)
                    + "\nshould be:\n" + oqlanalyzer.TEST_MDD_FUNCTION_RESULTS[i] + "\n\n");
        }
    }
    

}
