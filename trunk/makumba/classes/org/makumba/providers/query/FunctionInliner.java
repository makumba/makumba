package org.makumba.providers.query;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.InvalidFieldTypeException;
import org.makumba.MakumbaError;
import org.makumba.ProgrammerError;
import org.makumba.DataDefinition.QueryFragmentFunction;
import org.makumba.commons.RegExpUtils;
import org.makumba.providers.QueryProvider;

public class FunctionInliner {
    public static String NAME = "[a-zA-Z]\\w*";

    public static final String PATTERN_FUNCTION_CALL_BEGIN = "((" + NAME + ")(" + RegExpUtils.whitespace + "\\."
            + RegExpUtils.whitespace + NAME + ")+)" + RegExpUtils.whitespace + "\\(";

    public static final Pattern functionBegin = Pattern.compile(PATTERN_FUNCTION_CALL_BEGIN);

    private String from;

    private String beforeFunction;

    private String afterFunction;

    private String functionText;

    private ArrayList<String> parameterExpr = new ArrayList<String>();

    private ArrayList<String> parameterInline = new ArrayList<String>();

    private QueryFragmentFunction functionDefinition;

    private String inlinedFunction;

    public FunctionInliner(String query, QueryProvider qp) {
        this(query, findFrom(query), qp);
    }

    public FunctionInliner(String query, String from, QueryProvider qp) {
        findFunctionComponents(query, from, qp);
        //        if(functionDefinition.getParameters().getFieldNames().size()!=parameterExpr.size())
        //            throw new ProgrammerError("parameter number "+parameterExpr + " does not match function "+functionDefinition);
        int n = 0;
        for (String parameter : parameterExpr) {
            String inlineParameter = inlineParameter(parameter, qp);
            parameterInline.add(inlineParameter);
            checkParameter(n, parameter, inlineParameter, qp);
            n++;
        }
        //doInline();
    }

    private void doInline() {
        inlinedFunction = "("+functionDefinition.getQueryFragment()+")";
        for (int i = 0; i < parameterExpr.size(); i++) {
            // FIXME: this replacement is not safe, as it replaces e.g. this.paramName --> improve reg-exp for
            // replaceAll
            inlinedFunction = inlinedFunction.replaceAll(
                functionDefinition.getParameters().getFieldDefinition(i).getName(), "("+parameterInline.get(i)+")");
        }

    }

    private void checkParameter(int n, String parameter, String inlineParameter, QueryProvider qp) {
        FieldDefinition fieldDefinition = functionDefinition.getParameters().getFieldDefinition(n);
        FieldDefinition actual = qp.getQueryAnalysis("SELECT " + inlineParameter + " FROM " + from).getProjectionType().getFieldDefinition(
            0);
        /*       if(!fieldDefinition.isAssignableFrom(actual))
         throw new ProgrammerError(
         "formal paramter "+fieldDefinition.getName() + " of type "+fieldDefinition.getDataType()
         +" is not matched by the actual value given "+ parameter +" of type "+actual.getDataType()
         +" for function "+functionDefinition );       
         */}

    private String inlineParameter(String parameter, QueryProvider qp) {
        String toRight = parameter;
        StringBuffer inlined = new StringBuffer();
        while (toRight != null) {
            FunctionInliner fi = new FunctionInliner(toRight, from, qp);
            toRight = fi.getAfterFunction();
            inlined.append(fi.inline());
        }
        return inlined.toString();
    }

    /** Return the inlined function prepended by the non-functional text that came before it in the text; 
     * if no functions are found, return the text*/
    private String inline() {
        if (functionText == null)
            return beforeFunction;
        return beforeFunction + inlinedFunction;
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

    public String getBeforeFunction() {
        return beforeFunction;
    }

    public String getAfterFunction() {
        return afterFunction;
    }

    @Override
    public String toString() {
        return beforeFunction + " [" + functionText + ":" + parameterInline + ":" + from + "] " + afterFunction;
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
                return query.substring(fromBegin, index);
            char c = query.charAt(index);
            if (c == ')') {
                parLevel--;
            } else if (c == '(')
                parLevel++;

        }
    }

    public static void main(String[] args) throws Exception {
        String[] queries = {
                "SELECT p FROM test.Person p WHERE p.nameMin3CharsLong()",
                "SELECT p as p, p.indiv as indiv FROM test.Person p WHERE p.nameMin3CharsLong()",
                "SELECT p AS  p, p.indiv   AS    indiv FROM test.Person p WHERE p.nameMin3CharsLong()",
                "SELECT p FROM test.Person p WHERE p.nameMin3CharsLong() AND p.nameMin2CharsLong() AND p.name<>NIL",
                "SELECT p FROM test.Person p WHERE p.name<>NIL OR p.nameMin3CharsLong() AND p.nameMin2CharsLong()",
                "SELECT p.nameMin3CharsLong() FROM test.Person p",
                "SELECT p.indiv.name AS col1,character_length(p.indiv.name) AS col2 FROM test.Person p WHERE p.someFunctionWithParams(2,5,7)" };

        for (int i = 0; i < queries.length; i++) {
            System.out.println(new FunctionInliner(queries[i], QueryProvider.makeQueryAnalzyer("oql")));
        }
    }

}
