package org.makumba.providers.query;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.InvalidFieldTypeException;
import org.makumba.ProgrammerError;
import org.makumba.DataDefinition.QueryFragmentFunction;
import org.makumba.commons.RegExpUtils;
import org.makumba.providers.QueryProvider;

/**
 * Inliner for query functions.
 * FIXME: cache inlining results
 * FIXME: change the replaceAll in doInline()
 * FIXME: catch StringIndexOutOfBonds e.g. when counting parantheses and throw ProgrammerErrors 
 * @author Cristian Bogdan
 * @version $Id: FunctionInliner.java,v 1.1 Jul 7, 2008 5:11:53 PM cristi Exp $
 */
public class FunctionInliner {
    public static String NAME = "[a-zA-Z]\\w*";

    public static final String PATTERN_FUNCTION_CALL_BEGIN =
    // name . name . name ...
    "((" + NAME + ")(" + RegExpUtils.whitespace + "\\." + RegExpUtils.whitespace + NAME + ")+)"
    // (
            + RegExpUtils.whitespace + "\\(";

    public static final Pattern functionBegin = Pattern.compile(PATTERN_FUNCTION_CALL_BEGIN);

    private String from;

    private String beforeFunction;

    private String afterFunction;

    private String functionText;

    private ArrayList<String> parameterExpr = new ArrayList<String>();

    private ArrayList<String> parameterInline = new ArrayList<String>();

    private QueryFragmentFunction functionDefinition;

    private String inlinedFunction;

    private String functionObject;

    private FunctionInliner(String query, String from, QueryProvider qp) {
        findFunctionComponents(query, from, qp);
        if (functionText != null) {
            if (functionDefinition.getParameters().getFieldNames().size() != parameterExpr.size())
                throw new ProgrammerError("parameter number " + parameterExpr + " does not match function "
                        + functionDefinition);
            int n = 0;
            for (String parameter : parameterExpr) {
                String inlineParameter = inline(parameter, from, qp);
                parameterInline.add(inlineParameter);
                checkParameter(n, parameter, inlineParameter, qp);
                n++;
            }
            doInline();
        }
    }

    private void doInline() {
        inlinedFunction = functionDefinition.getQueryFragment();
        for (int i = 0; i < parameterExpr.size(); i++) {
            // FIXME: this replacement is not safe, as it replaces e.g. this.paramName
            // --> need to replace parameters all at once, and only those that don't have . before or ( after them...
            inlinedFunction = inlinedFunction.replaceAll(
                functionDefinition.getParameters().getFieldDefinition(i).getName(),
                paranthesize(parameterInline.get(i)));
        }
        // FIXME: this replacement is not safe, see above
        inlinedFunction = inlinedFunction.replaceAll("this", functionObject);
    }

    private static String paranthesize(String expr) {
        if (expr.trim().startsWith("(") && expr.trim().endsWith(")"))
            return expr;
        return "(" + expr + ")";
    }

    private void checkParameter(int n, String parameter, String inlineParameter, QueryProvider qp) {
        FieldDefinition fieldDefinition = functionDefinition.getParameters().getFieldDefinition(n);
        FieldDefinition actual = qp.getQueryAnalysis("SELECT " + inlineParameter + " FROM " + from).getProjectionType().getFieldDefinition(
            0);

        if (!fieldDefinition.isAssignableFrom(actual))
            throw new ProgrammerError("formal paramter " + fieldDefinition.getName() + " of type "
                    + fieldDefinition.getDataType() + " is not matched by the actual value given " + parameter
                    + " of type " + actual.getDataType() + " for function " + functionDefinition);
    }

    private void findFunctionComponents(String query, String from, QueryProvider qp) {
        Matcher m = functionBegin.matcher(query);
        if (!m.find()) {
            beforeFunction = query;
            afterFunction = null;
        } else {
            this.from = from;
            beforeFunction = query.substring(0, m.start());
            int parLevel = 1;
            int index = m.end();
            int lastParam = m.end();
            for (; parLevel > 0; index++) {
                char c = query.charAt(index);
                if (c == ')') {
                    if (parLevel == 1 && index != lastParam) {
                        parameterExpr.add(query.substring(lastParam, index));
                    }
                    parLevel--;
                } else if (c == '(')
                    parLevel++;
                if (parLevel == 1 && c == ',') {
                    parameterExpr.add(query.substring(lastParam, index));
                    lastParam = index + 1;
                }
            }

            afterFunction = query.substring(index);
            functionText = query.substring(m.start(), index);

            DataDefinition dd = qp.getQueryAnalysis("SELECT 1 FROM " + from).getLabelType(m.group(2));
            if (dd == null) {
                throw new org.makumba.NoSuchLabelException("no such label '" + m.group(2) + "'.");
            }
            String referenceSequence = m.group(1);
            int dot = referenceSequence.indexOf(".");

            while (true) {
                int dot1 = referenceSequence.indexOf(".", dot + 1);
                if (dot1 == -1) {
                    String fn = referenceSequence.substring(dot + 1);
                    functionDefinition = dd.getFunction(fn);
                    if (functionDefinition == null)
                        throw new ProgrammerError(fn + " is not a function in " + dd.getName());
                    functionObject = referenceSequence.substring(0, dot);
                    break;
                }
                FieldDefinition fd = dd.getFieldDefinition(referenceSequence.substring(dot + 1, dot1));
                if (fd == null)
                    throw new org.makumba.NoSuchFieldException(dd, referenceSequence.substring(dot + 1, dot1));
                if (!fd.getType().startsWith("ptr"))
                    throw new InvalidFieldTypeException(fd, "pointer");
                dd = fd.getPointedType();
                dot = dot1;
            }
        }
    }

    @Override
    public String toString() {
        return beforeFunction + " [" + functionText + ":" + parameterInline + ":" + functionDefinition + ":"
                + functionObject + "] " + afterFunction;
    }

    /**
     * Finds the FROM section of a query provided subqueries are in parantheses.
     * 
     * @param query
     *            the query in question
     * @return the FROM section, without the word FROM
     */
    public static String findFrom(String query) {
        String lower = query.toLowerCase();
        int parLevel = 0;

        int fromBegin = -1;
        for (int index = 0;; index++) {
            String toRighty = lower.substring(index);
            if (parLevel == 0 && toRighty.startsWith(" from "))
                fromBegin = index + 6;
            if (parLevel == 0
                    && (toRighty.length() == 0 || toRighty.startsWith(" where ") || toRighty.startsWith(" group by ") || toRighty.startsWith(" order by")))
                if(fromBegin!=-1)
                    return query.substring(fromBegin, index);
                else 
                    return "";
            char c = query.charAt(index);
            if (c == ')') {
                parLevel--;
            } else if (c == '(')
                parLevel++;

        }
    }

    /**
     * Inline query functions in a query using the given query provider
     * 
     * @param expression
     *            the expression
     * @param qp
     *            the query provider
     * @return the query with inlined query functions
     */
    public static String inline(String query, QueryProvider qp) {
        return inline(query, findFrom(query), qp);
    }

    /**
     * Inline query functions in an expression or query with the given label types, using given query provider
     * 
     * @param expression
     *            the expression
     * @param from
     *            the label types as a QL FROM
     * @param qp
     *            the query provider
     * @return the expression with inlined query functions
     */
    public static String inline(String expression, String from, QueryProvider qp) {
        String inlinedQuery = expression;
        boolean didInline;
        boolean didWork = false;
        do {
            StringBuffer inlined = new StringBuffer();
            didInline = false;
            String toRight = inlinedQuery;
            while (functionBegin.matcher(toRight).find()) {
                FunctionInliner fi = new FunctionInliner(toRight, from, qp);
                toRight = fi.afterFunction;
                didInline = true;
                didWork = true;
                addInlinedToBuffer(inlined, toRight, fi);
            }
            inlined.append(toRight);
            inlinedQuery = inlined.toString();
        } while (didInline);
        if (didWork)
            java.util.logging.Logger.getLogger("org.makumba." + "db.query.inline").info(
                expression + " \n-> " + inlinedQuery);
        return inlinedQuery;
    }

    /** we add inlined text and try to reduce the number of parantheses */
    private static void addInlinedToBuffer(StringBuffer inlined, String toRight, FunctionInliner fi) {
        if (
        // if things around start and end with paranthesis
        fi.beforeFunction.trim().endsWith("(")
                && toRight.trim().startsWith(")")
                ||
                // or we are after a select or a comma
                (fi.beforeFunction.trim().toLowerCase().endsWith("select") || fi.beforeFunction.trim().endsWith(","))
                // and
                &&
                // we are before an as or a comma or a from
                (toRight.trim().toLowerCase().startsWith("as") || toRight.trim().startsWith(","))
                || toRight.trim().toLowerCase().startsWith("from"))
            // then we skip parantheses
            inlined.append(fi.beforeFunction).append(fi.inlinedFunction);
        else
            inlined.append(fi.beforeFunction).append("(").append(fi.inlinedFunction).append(")");
    }

    public static void main(String[] args) throws Exception {
        String[] queries = {
                "SELECT p FROM test.Person p WHERE p.nameMin3CharsLong()",
                "SELECT p as p, p.indiv as indiv FROM test.Person p WHERE p.nameMin3CharsLong()",
                "SELECT p AS p, p.indiv AS indiv FROM test.Person p WHERE p.nameMin3CharsLong()",
                "SELECT p FROM test.Person p WHERE p.nameMin3CharsLong() AND p.nameMin2CharsLong() AND p.name<>NIL",
                "SELECT p FROM test.Person p WHERE p.name<>NIL OR p.nameMin3CharsLong() AND p.nameMin2CharsLong()",
                "SELECT p.nameMin3CharsLong() FROM test.Person p",
                "SELECT p.indiv.name AS col1,character_length(p.indiv.name) AS col2 FROM test.Person p WHERE p.someFunctionWithParams(2,5,7)" };

        for (int i = 0; i < queries.length; i++) {
            inline(queries[i], QueryProvider.makeQueryAnalzyer("oql"));
        }
    }

}
