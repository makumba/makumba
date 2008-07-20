package org.makumba.providers.query;

import java.util.ArrayList;
import java.util.regex.Pattern;

import org.makumba.ProgrammerError;
import org.makumba.commons.RegExpUtils;

/**
 * Given a query and a starting point, this class identifies the subquery where the starting point is, its parent query,
 * its grandparent query ... ending with the outer query. After this analysis, it can insert text in the query and
 * expand the FROM and WHERE sections. For all queries and subqueries the class separates the projection, FROM and WHERE
 * sections. To analyze/process just the outer query, use 0 as a starting point.
 * 
 * @author Cristian Bogdan
 * @version $Id: $
 */
public class QuerySectionProcessor {
    StringBuffer query;

    class SubqueryData {
        private int start = -1, end = -1;

        private int fromStart = -1, fromEnd = -1;

        private int whereStart = -1, whereEnd = -1;

        private int projectionStart = -1, projectionEnd = -1;

        ArrayList<Insertion> insertions = new ArrayList<Insertion>();

        Insertion myFromWhere = new Insertion();

        public void setWhereStart(int index) {
            whereStart = index;
            whereEnd = end;
            checkFromEnd(index - 7);
        }

        public void checkFromEnd(int index) {
            if (fromStart != -1 && (fromEnd == -1 || fromEnd >= index))
                fromEnd = index;
            // we might have no from and no where but orderby or groupby
            if (projectionEnd >= index)
                projectionEnd = index;
        }

        public void checkWhereEnd(int index) {
            if (whereStart != -1 && (whereEnd == -1 || whereEnd >= index))
                whereEnd = index;
        }

        public void setFromStart(int index) {
            projectionEnd = index - 5;
            fromStart = index;
            fromEnd = end;
        }

        public void setStart(int i) {
            start = i;
            projectionStart = i;
        }

        public void setProjectionStart(int i) {
            projectionStart = i;
        }

        public void setEnd(int i) {
            end = i;
            projectionEnd = i;
        }

        public void shift(int index, int delta) {
            end += delta;
            if (projectionStart > index)
                projectionStart += delta;
            if (fromStart > index)
                fromStart += delta;
            if (fromEnd > index)
                fromEnd += delta;
            if (whereStart > index)
                whereStart += delta;
            if (whereEnd > index)
                whereEnd += delta;
            if (projectionEnd > index)
                projectionEnd += delta;
        }

        public void addFromWhere() {
            for (Insertion ins : insertions)
                addFromWhere(ins.from, ins.where);
        }

        public void addFromWhere(String from, String where) {
            if (fromEnd == -1) {
                fromStart = end;
                fromEnd = end;
                QuerySectionProcessor.this.replace(fromEnd, 0, " FROM " + from);
                fromEnd = end;
            } else
                QuerySectionProcessor.this.replace(fromEnd, 0, ", " + from);

            if (where == null)
                return;
            if (whereStart == -1) {
                int x = fromEnd;
                QuerySectionProcessor.this.replace(fromEnd, 0, " WHERE " + where);
                whereEnd = fromEnd;
                fromEnd = x;
                whereStart = fromEnd + 7;
            } else {
                if (!where.trim().startsWith("(") || !where.trim().endsWith(")"))
                    where = "(" + where + ")";
                String wh = getWhere().trim();
                boolean para = wh.startsWith("(") && wh.endsWith(")");
                QuerySectionProcessor.this.replace(whereStart, 0, where + " AND " + (para ? "" : "("));
                if (!para)
                    QuerySectionProcessor.this.replace(whereEnd, 0, ")");
            }
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(query, projectionStart, projectionEnd);
            if (fromStart != -1)
                sb.append(" FROM ").append(query, fromStart, fromEnd);
            if (whereStart != -1)
                sb.append(" WHERE ").append(query, whereStart, whereEnd);
            return sb.toString();
        }

        public String getWhere() {
            if (whereStart == -1)
                return null;
            return query.substring(whereStart, whereEnd);
        }

        public String getFrom() {
            if (fromStart == -1)
                return null;
            return query.substring(fromStart, fromEnd);
        }

        public String getProjection() {
            return query.substring(projectionStart, projectionEnd);
        }

        public void addInsertion(Insertion ins) {
            if (myFromWhere.contains(ins))
                return;
            for (Insertion in : insertions) {
                if (in.contains(ins))
                    return;
            }
            insertions.add(ins);
        }

        /** the object is now readily constructed */
        public void pack() {
            myFromWhere.from = getFrom();
            myFromWhere.where = getWhere();
        }
    }

    ArrayList<SubqueryData> subqueries = new ArrayList<SubqueryData>();

    private String initialFrom;

    public QuerySectionProcessor(String query, int startPoint) {
        int[] levels = new int[query.length()];

        this.query = new StringBuffer(query);
        String lowerQuery = query.toLowerCase();

        int parLevel = 0;
        for (int i = 0; i < query.length(); i++) {
            if (query.charAt(i) == '(')
                parLevel++;
            levels[i] = parLevel;
            if (query.charAt(i) == ')')
                parLevel--;
        }
        if (parLevel != 0)
            throw new ProgrammerError("Unbalanced parantheses in query " + query);
        int myLevel = levels[startPoint];
        int min = startPoint;
        int max = startPoint + 1;
        for (int lev = myLevel; lev >= 0; lev--) {
            SubqueryData sd = new SubqueryData();
            subqueries.add(0, sd);
            for (int i = min; i >= 0; i--) {
                if (sd.start == -1) {
                    if (levels[i] > lev)
                        levels[i] = -1;
                    else if (levels[i] < lev && levels[i] != -1) {
                        sd.setStart(min = i + 1);
                        if (lowerQuery.substring(min).trim().startsWith("select "))
                            sd.setProjectionStart(lowerQuery.substring(min).indexOf("select") + 7);
                    }
                } else {
                    if (levels[i] >= lev)
                        levels[i] = -1;
                }
            }
            if (sd.start == -1) {
                sd.setStart(0);
                if (lowerQuery.trim().startsWith("select "))
                    sd.setProjectionStart(lowerQuery.indexOf("select") + 7);
            }

            for (int i = max; i < query.length(); i++) {
                if (sd.end == -1) {
                    if (levels[i] > lev)
                        levels[i] = -1;
                    else if (levels[i] < lev && levels[i] != -1) {
                        sd.setEnd(max = i);
                    }
                } else {
                    if (levels[i] >= lev)
                        levels[i] = -1;
                }
            }
            if (sd.end == -1)
                sd.setEnd(query.length());
        }

        // find froms
        int findIndex = 0;
        while (true) {
            findIndex = lowerQuery.indexOf("from ", findIndex);
            if (findIndex == -1)
                break;
            if (levels[findIndex] != -1) {
                subqueries.get(levels[findIndex]).setFromStart(findIndex + 5);
            }
            findIndex++;
        }
        // downgrade levels that don't contain froms, remove their subquery data
        for (int lev = 1; lev <= myLevel; lev++) {
            if (subqueries.get(lev).fromStart == -1) {
                for (int i = 0; i < levels.length; i++) {
                    if (levels[i] >= lev) {
                        levels[i]--;
                    }
                }
                subqueries.remove(lev);
                lev--;
                myLevel--;
            }
        }
        // find wheres
        findIndex = 0;
        while (true) {
            findIndex = lowerQuery.indexOf(" where ", findIndex);
            if (findIndex == -1)
                break;
            if (levels[findIndex] != -1) {
                subqueries.get(levels[findIndex]).setWhereStart(findIndex + 7);
            }
            findIndex++;
        }

        // limit FROM and WHERE with other sections
        findIndex = 0;
        while (true) {
            findIndex = lowerQuery.indexOf(" order by ", findIndex);
            if (findIndex == -1)
                break;
            if (levels[findIndex] != -1) {
                subqueries.get(levels[findIndex]).checkWhereEnd(findIndex);
                subqueries.get(levels[findIndex]).checkFromEnd(findIndex);
            }
            findIndex++;
        }

        // limit FROM and WHERE with other sections
        findIndex = 0;
        while (true) {
            findIndex = lowerQuery.indexOf(" group by ", findIndex);
            if (findIndex == -1)
                break;
            if (levels[findIndex] != -1) {
                subqueries.get(levels[findIndex]).checkWhereEnd(findIndex);
                subqueries.get(levels[findIndex]).checkFromEnd(findIndex);
            }
            findIndex++;
        }

        StringBuffer from = new StringBuffer();

        String separator = "";
        for (SubqueryData sd : subqueries) {
            sd.pack();
            if (sd.fromStart != -1) {
                from.append(separator).append(sd.getFrom());
                separator = ",";
            }
        }
        initialFrom = from.toString();
    }

    /**
     * Return the initial FROM section of all subqueries up to the starting point. This gives the complete type-context
     * for a function inlined at the starting point.
     */
    public String getInitialFrom() {
        return initialFrom;
    }

    /** Return the projection section of the outer query, without the word SELECT */
    public String getProjectionText() {
        return subqueries.get(0).getProjection();
    }

    /** Inline a parameter. */
    public void replaceParameter(String name, String parameterInline) {
        int index = 0;
        while ((index = (query.indexOf(name, index))) != -1) {
            if (query.substring(0, index).trim().endsWith(".")
                    || query.substring(index + name.length()).trim().startsWith("(")) {
                index++;
                continue;
            }
            replaceExpr(index, name.length(), parameterInline);
            index += parameterInline.length();
        }

    }

    void replaceThis(String thisExpr) {
        int index = 0;
        while ((index = (query.indexOf("this", index))) != -1) {
            replace(index, "this".length(), thisExpr);
        }
    }

    /** Replace the expression begining at regionStart, of length regionLength with the given text. */
    public void replaceExpr(int regionStart, int regionLength, String text) {
        replace(regionStart, regionLength, //
            paranthesize( //
                query.substring(0, regionStart).trim(), // 
                query.substring(regionStart + regionLength).trim(), // 
                text));
    }

    /** Replace the region begining at regionStart, of length regionLength with the given text. */
    private void replace(int regionStart, int regionLength, String text) {
        if (closed)
            throw new IllegalStateException("Cannot add text after getText() was called");

        for (SubqueryData dt : subqueries) {
            dt.shift(regionStart, text.length() - regionLength);
        }
        query.delete(regionStart, regionStart + regionLength);
        query.insert(regionStart, text);
    }

    static class Insertion {
        String from;

        String where;

        public boolean contains(Insertion ins) {
            // TODO: this should detect the labels defined in both insertions
            // and name identically the labels that have same definition;
            // then check if all labels are the same and have identical definitions, and if all where conditions are
            // equivalent.
            // for now we only check if things are identical or if the included FROM and WHERE are subqueries of the current ones
            // that's enough for actor expansion
            return from!=null && from.indexOf(ins.from)!=-1
                    && (ins.where == null && where == null || where != null && where.indexOf(ins.where)!=-1);
        }
    }

    boolean closed = false;

    /**
     * Given a query, add from it to the FROM section and to the WHERE condition of the outermost possible suquery. If
     * the FROM uses a label defined in a subquery, the sections will be added to that subquery. Otherwise everything
     * will be added to the outer query. The labels of the given query will be adapted according to the context of this
     * query as in the end the text of that query will make it somewhere into this query. The "this" of the given query
     * wil be replaced by thisExpr.
     * 
     * @param func
     *            the section processor of the function expression that might contain a from or a where
     * @param thisExpr
     *            the object that will substitute "this" in the expression
     */
    public void addFromWhere(QuerySectionProcessor func, String thisExpr) {
        // TODO this does no addition yet
        // func.getFrom();
        // func.getWhere();
        // use Insertion

        func.replaceThis(thisExpr);
    }

    /**
     * Add to the FROM section and to the WHERE condition of the outermost suquery.
     * 
     * @param from
     *            the from section
     * @param where
     *            the where section for joining it
     */
    public void addFromWhere(String from, String where) {
        if (closed)
            throw new IllegalStateException("Cannot add text after getText() was called");

        Insertion ins = new Insertion();
        ins.from = from;
        ins.where = where;
        subqueries.get(0).addInsertion(ins);
    }

    /** Return the query text */
    public String getText() {
        for (SubqueryData dt : subqueries)
            dt.addFromWhere();
        closed = true;
        return query.toString();
    }

    /** Return the WHERE section of the outer query, without the word WHERE. */
    public String getWhere() {
        return subqueries.get(0).getWhere();
    }

    /** Return the FROM section of the outer query, without the word FROM */
    public String getFrom() {
        return subqueries.get(0).getFrom();
    }

    public static final String PATTERN_ALL_FUNCTION_CALL_BEGIN = // name [. name . name ...]
    "((" + FunctionInliner.NAME + ")" + //
            "(" + RegExpUtils.whitespace + "\\." + RegExpUtils.whitespace + FunctionInliner.NAME + ")*)" //
            // (
            + RegExpUtils.whitespace + "\\(";

    public static final Pattern allFunctionBegin = Pattern.compile(PATTERN_ALL_FUNCTION_CALL_BEGIN);

    public static final Pattern ident = Pattern.compile(FunctionInliner.NAME);

    static String paranthesize(String before, String after, String expr) {
        String trimExpr = expr.trim();

        // if the expression is already paranthesized, we return
        if (trimExpr.startsWith("(") && trimExpr.trim().endsWith(")"))
            return trimExpr;

        // we don't paranthesize identifiers or parameters
        if (ident.matcher(trimExpr).matches() || (trimExpr.startsWith("$") || trimExpr.startsWith(":"))
                && ident.matcher(trimExpr.substring(1)).matches())
            return trimExpr;

        // we don't paranthesize numbers
        try {
            // TODO: replace with regex
            Double.parseDouble(trimExpr);
            return trimExpr;
        } catch (NumberFormatException nfe) {
        }
        // we don't paranthesize 'strings'
        if (trimExpr.startsWith("\'") && trimExpr.endsWith("\'"))
            return trimExpr;

        // we never paranthesize [a.b.]function() or actor(...)
        if (allFunctionBegin.matcher(trimExpr).find() && trimExpr.endsWith(")"))
            return trimExpr;

        // if there are already parantheses before and after the expression, we return
        if (before.endsWith("(") && after.startsWith(")"))
            return trimExpr;

        // if we are after a select or a comma
        if ((before.toLowerCase().endsWith("select") || before.endsWith(",")) && // and we are before an as or a
                // comma or a from
                (after.toLowerCase().startsWith("as") || after.startsWith(",") || after.toLowerCase().startsWith("from")))
            return trimExpr;

        // otherwise we put the expression in paranthesis
        return "(" + trimExpr + ")";
    }

    public static void main(String[] argv) {
        String[] queries = { "SELECT m.id AS col1,it.project.color AS col2,m.TS_create AS col3,it.project.id AS col4,it.subject AS col5 FROM projman.Message m JOIN m.item it WHERE (not exists(FROM projman.Item i join i.events  e WHERE  i=m.item AND e.who=:principal AND e.type IN (0, 2,3) )) AND m.ofMyBusiness() ORDER BY m.TS_create desc "
        //
        };
        for (int i = 0; i < queries.length; i++) {
            java.util.regex.Matcher m = FunctionInliner.functionBegin.matcher(queries[i]);
            if (m.find())
                new QuerySectionProcessor(queries[i], 0);
        }

    }

}
