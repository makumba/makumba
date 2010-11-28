package org.makumba.providers.query;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.InvalidFieldTypeException;
import org.makumba.ProgrammerError;
import org.makumba.QueryFragmentFunction;
import org.makumba.commons.RegExpUtils;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.QueryAnalysisProvider;
import org.makumba.providers.QueryProvider;

/**
 * Inliner for query functions.
 * 
 * @author Cristian Bogdan
 * @version $Id: FunctionInliner.java,v 1.1 Jul 7, 2008 5:11:53 PM cristi Exp $
 */
public class FunctionInliner {
    // TODO: store all involved functions with error messages and their FROMs to be able to trace back the error
    // FIXME: catch StringIndexOutOfBonds e.g. when counting parantheses and throw ProgrammerErrors

    private static DataDefinitionProvider ddp = DataDefinitionProvider.getInstance();

    public static String NAME = "[a-zA-Z]\\w*";

    public static final String PATTERN_FUNCTION_CALL_BEGIN =
    // name . name . name ...
    "((" + NAME + ")(" + RegExpUtils.whitespace + "\\." + RegExpUtils.whitespace + NAME + ")+)"
    // (
            + RegExpUtils.whitespace + "\\(";

    public static final String PATTERN_ACTOR =
    //
    "actor" + RegExpUtils.whitespace + "\\(" + RegExpUtils.whitespace +
    // actor(a.b.c)
            "(" + NAME + "(\\." + NAME + ")*)" + RegExpUtils.whitespace + "\\)" + RegExpUtils.whitespace;

    public static final Pattern functionBegin = Pattern.compile(PATTERN_FUNCTION_CALL_BEGIN);

    public static final Pattern actor = Pattern.compile(PATTERN_ACTOR);

    private String functionText;

    private ArrayList<String> parameterExpr = new ArrayList<String>();

    private QueryFragmentFunction functionDefinition;

    private DataDefinition calleeType;

    private String inlinedFunction;

    private String functionObject;

    private FunctionInliner(String query, Matcher m, QueryAnalysisProvider qp, QuerySectionProcessor qsp) {
        String from = qsp.getInitialFrom();
        findFunctionBody(query, m);
        findFunctionObject(m, from, qp);
        if (functionDefinition.getParameters().getFieldNames().size() != parameterExpr.size()) {
            throw new ProgrammerError("parameter number " + parameterExpr + " does not match function "
                    + functionDefinition);
        }
        QuerySectionProcessor func = new QuerySectionProcessor(addThisToFunction(calleeType, functionDefinition), 0);
        int n = 0;
        for (String parameter : parameterExpr) {
            String inlineParameter = inline(parameter, qp, qsp);
            checkParameter(n, inlineParameter, from, qp);
            func.replaceParameter(functionDefinition.getParameters().getFieldDefinition(n).getName(), inlineParameter);
            n++;
        }

        qsp.addFromWhere(func, functionObject);
        inlinedFunction = func.getProjectionText();
    }

    private void checkParameter(int n, String inlineParameter, String from, QueryAnalysisProvider qp) {
        if (inlineParameter.trim().startsWith(qp.getParameterSyntax())) {
            return;
        }

        FieldDefinition fieldDefinition = functionDefinition.getParameters().getFieldDefinition(n);
        FieldDefinition actual = qp.getQueryAnalysis("SELECT " + inlineParameter + " FROM " + from).getProjectionType().getFieldDefinition(
            0);

        if (!fieldDefinition.isAssignableFrom(actual)) {
            throw new ProgrammerError("formal paramter " + fieldDefinition.getName() + " of type "
                    + fieldDefinition.getDataType() + " is not matched by the actual value given "
                    + parameterExpr.get(n) + " of type " + actual.getDataType() + " for function " + functionDefinition);
        }
    }

    private void findFunctionObject(Matcher m, String from, QueryAnalysisProvider qp) {
        if (from != null && from.length() > 0) {
            calleeType = qp.getQueryAnalysis("SELECT 1 FROM " + from).getLabelType(m.group(2));
        }
        if (calleeType == null) {
            String possibleMdd = m.group(1);
            int n = possibleMdd.lastIndexOf(".");
            if (n != -1) {
                String possibleFunction = possibleMdd.substring(n + 1);
                possibleMdd = possibleMdd.substring(0, n);
                calleeType = DataDefinitionProvider.getInstance().getDataDefinition(possibleMdd.trim());
                if (calleeType != null) {
                    functionDefinition = ddp.getQueryFragmentFunctions(calleeType.getName()).getFunction(
                        possibleFunction.trim());
                } else {
                    throw new org.makumba.DataDefinitionNotFoundError(possibleMdd);
                }
                if (functionDefinition == null) {
                    throw new org.makumba.NoSuchFieldException(calleeType, possibleFunction);
                }
                functionObject = null;
                if (addThisToFunction(calleeType, functionDefinition).indexOf("this") != -1) {
                    throw new ProgrammerError("Cannot use 'this' in function used statically" + m.group());
                }
                return;
            } else {
                throw new org.makumba.NoSuchLabelException("no such label '" + m.group(2) + "'.");
            }

        }
        String referenceSequence = m.group(1);
        int dot = referenceSequence.indexOf(".");

        while (true) {
            int dot1 = referenceSequence.indexOf(".", dot + 1);
            if (dot1 == -1) {
                String fn = referenceSequence.substring(dot + 1);
                functionDefinition = ddp.getQueryFragmentFunctions(calleeType.getName()).getFunction(fn);
                if (functionDefinition == null) {
                    throw new ProgrammerError(fn + " is not a function in " + calleeType.getName());
                }
                functionObject = referenceSequence.substring(0, dot);
                break;
            }
            FieldDefinition fd = calleeType.getFieldDefinition(referenceSequence.substring(dot + 1, dot1));
            if (fd == null) {
                throw new org.makumba.NoSuchFieldException(calleeType, referenceSequence.substring(dot + 1, dot1));
            }
            if (!fd.getType().startsWith("ptr")) {
                throw new InvalidFieldTypeException(fd, "pointer");
            }
            calleeType = fd.getPointedType();
            dot = dot1;
        }
    }

    private void findFunctionBody(String query, Matcher m) {
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
            } else if (c == '(') {
                parLevel++;
            }
            if (parLevel == 1 && c == ',') {
                parameterExpr.add(query.substring(lastParam, index));
                lastParam = index + 1;
            }
        }
        functionText = query.substring(m.start(), index);
    }

    @Override
    public String toString() {
        return " [" + functionText + ":" + functionDefinition + ":" + functionObject + "] ";
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
    public static String inline(String expr, QueryAnalysisProvider qp) {
        return inline(expr, qp, null);
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
    static String inline(String expr, QueryAnalysisProvider qp, QuerySectionProcessor qsp) {
        String initialQuery = expr;
        while (true) {
            Matcher m;
            if ((m = functionBegin.matcher(expr)).find()) {
                QuerySectionProcessor qspText = null;
                QuerySectionProcessor qs = qsp;
                if (qs == null) {
                    qs = qspText = new QuerySectionProcessor(expr, m.start());
                }
                FunctionInliner fi = new FunctionInliner(expr, m, qp, qs);
                if (qspText == null) {
                    qspText = new QuerySectionProcessor(expr, 0);
                }

                qspText.replaceExpr(m.start(), fi.functionText.length(), fi.inlinedFunction);
                expr = qspText.getText();
                continue;
            }

            if ((m = actor.matcher(expr)).find()) {
                String actorType = m.group(1);
                ddp.getDataDefinition(actorType);
                if (m.end() < expr.length() && expr.charAt(m.end()) == '.') {
                    QuerySectionProcessor qspText = null;
                    QuerySectionProcessor qs = qsp;
                    if (qs == null) {
                        qs = qspText = new QuerySectionProcessor(expr, m.start());
                    }
                    String actorLabel = getActorLabel(actorType);
                    qs.addFromWhere(actorType + " " + actorLabel, actorLabel + "=" + qp.getParameterSyntax()
                            + actorLabel);
                    if (qspText == null) {
                        qspText = new QuerySectionProcessor(expr, 0);
                    }
                    qspText.replaceExpr(m.start(), m.group().trim().length(), actorLabel);
                    expr = qspText.getText();
                } else {
                    QuerySectionProcessor qspText = new QuerySectionProcessor(expr, 0);
                    qspText.replaceExpr(m.start(), m.group().trim().length(), qp.getParameterSyntax()
                            + getActorLabel(actorType));
                    expr = qspText.getText();
                }
                continue;
            }
            break;
        }
        if (!expr.equals(initialQuery)) {
            java.util.logging.Logger.getLogger("org.makumba.db.query.inline").fine(initialQuery + " \n-> " + expr);
        }

        return expr;

    }

    private static String getActorLabel(String actorType) {
        return "actor_" + actorType.trim().replace('.', '_');
    }

    public static void main(String[] args) throws Exception {
        String[] queries = {
                "SELECT p FROM test.Person p WHERE p.nameMin3CharsLong() AND actor(test.Person).name is not null",
                "SELECT p as p, p.indiv as indiv FROM test.Person p WHERE p.nameMin3CharsLong()",
                "SELECT p AS p, p.indiv AS indiv FROM test.Person p WHERE p.nameMin3CharsLong()",
                "SELECT p FROM test.Person p WHERE p.nameMin3CharsLong() AND p.nameMin2CharsLong() AND p.name<>NIL",
                "SELECT p FROM test.Person p WHERE p.name<>NIL OR p.nameMin3CharsLong() AND p.nameMin2CharsLong()",
                "SELECT p.nameMin3CharsLong() FROM test.Person p",
                "SELECT test.Person.someTest().firstSex, actor(test.Person).gender",
                "SELECT p.indiv.name AS col1,character_length(p.indiv.name) AS col2 FROM test.Person p WHERE p.someFunctionWithParams(2,5,7)"

        //
        };

        for (String querie : queries) {
            inline(querie, QueryProvider.getQueryAnalzyer("oql"));
        }
    }

    public static final Pattern ident = Pattern.compile("[a-zA-Z]\\w*");

    public static String addThisToFunction(DataDefinition mdd, QueryFragmentFunction func) {
        String queryFragment = func.getQueryFragment();
        StringBuffer sb = new StringBuffer();
        Matcher m = ident.matcher(queryFragment);
        boolean found = false;
        while (m.find()) {
            String id = queryFragment.substring(m.start(), m.end());
            int after = -1;
            for (int index = m.end(); index < queryFragment.length(); index++) {
                char c = queryFragment.charAt(index);
                if (c == ' ' || c == '\t') {
                    continue;
                }
                after = c;
                break;
            }
            int before = -1;
            for (int index = m.start() - 1; index >= 0; index--) {
                char c = queryFragment.charAt(index);
                if (c == ' ' || c == '\t') {
                    continue;
                }
                before = c;
                break;
            }

            // TODO: either look for other keywords (than end) or better rewrite this with ASTs
            // if we have an actor we don't append "this"
            if (before == '.' || id.equals("this") || id.equals("actor") || id.equals("end")
                    || func.getParameters().getFieldDefinition(id) != null) {
                continue;
            }
            if (mdd.getFieldDefinition(id) != null || after == '('
                    && ddp.getQueryFragmentFunctions(mdd.getName()).getFunction(id) != null) {
                m.appendReplacement(sb, "this." + id);
                found = true;
            }
        }
        m.appendTail(sb);

        if (found) {
            java.util.logging.Logger.getLogger("org.makumba.db.query.inline").fine(
                queryFragment + " -> " + sb.toString());
        }

        return sb.toString();

    }
}
