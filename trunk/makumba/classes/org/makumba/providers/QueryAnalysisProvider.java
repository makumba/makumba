package org.makumba.providers;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.InvalidFieldTypeException;
import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;
import org.makumba.providers.query.FunctionInliner;

public abstract class QueryAnalysisProvider {
    protected abstract QueryAnalysis getRawQueryAnalysis(String query);

    public QueryAnalysis getQueryAnalysis(String query) {
        return getRawQueryAnalysis(inlineFunctions(query));
    }

    public String inlineFunctions(String query) {
        try {
            return (String) inlinedQueries.getResource(query);
        } catch (NullPointerException e) {
            initializeCache(query, e);
            return (String) inlinedQueries.getResource(query);
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

        String query = "SELECT " + expr + " FROM " + from;
        query = inlineFunctions(query);
        expr = query.substring(7);
        expr = expr.substring(0, expr.indexOf("FROM"));

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
            throw new org.makumba.NoSuchLabelException("no such label '" + substring + "'.");
        }
        while (true) {
            int dot1 = referenceSequence.indexOf(".", dot + 1);
            if (dot1 == -1) {
                String fn = referenceSequence.substring(dot + 1);
                FieldDefinition fd = dd.getFieldDefinition(fn);
                if (fd == null && (fd = getAlternativeField(dd, fn)) == null)
                    throw new org.makumba.NoSuchFieldException(dd, fn);

                if (fd.getType().equals("set"))
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
                        return FunctionInliner.inline((String) nm, QueryAnalysisProvider.this);
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

}
