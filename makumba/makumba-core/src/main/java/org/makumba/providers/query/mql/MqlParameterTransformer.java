package org.makumba.providers.query.mql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.InvalidValueException;
import org.makumba.MakumbaError;
import org.makumba.Pointer;
import org.makumba.ProgrammerError;
import org.makumba.commons.NameResolver;
import org.makumba.commons.NameResolver.TextList;
import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;
import org.makumba.db.TransactionImplementation;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.ParameterTransformer;
import org.makumba.providers.QueryAnalysis;
import org.makumba.providers.QueryAnalysisProvider;

import antlr.collections.AST;

/**
 * MQL implementation of the {@link ParameterTransformer}, which generates SQL based on a {@link QueryAnalysis}
 * 
 * @author Manuel Bernhardt <manuel@makumba.org>
 * @version $Id: MqlSQLQueryGenerator.java,v 1.1 Mar 30, 2010 4:17:00 PM manu Exp $
 */
public class MqlParameterTransformer implements ParameterTransformer {

    private final MqlQueryAnalysis qA;

    private final AST analyserTreeSQL;

    private TextList text;

    private DataDefinition expandedParamInfo = null;

    private final MqlSqlGenerator mg;

    public MqlParameterTransformer(MqlQueryAnalysis qA, MqlSqlGenerator mg) {
        this.qA = qA;
        this.analyserTreeSQL = qA.getAnalyserTree();
        this.mg = mg;
    }

    @Override
    public void init(Map<String, Object> arguments) {

        if (expandedParamInfo == null) {
            expandMultipleParameters(arguments);
        }

        try {
            mg.statement(analyserTreeSQL);
        } catch (Throwable e) {
            QueryAnalysisProvider.doThrow(qA.getQuery(), e, analyserTreeSQL);
        }
        QueryAnalysisProvider.doThrow(qA.getQuery(), mg.error, analyserTreeSQL);

        text = mg.getText();
    }

    @Override
    public int getParameterCount() {
        if (expandedParamInfo == null) {
            throw new MakumbaError("Can't call this method without having set the arguments with setArguments!");
        } else {
            return expandedParamInfo.getFieldNames().size();
        }
    }

    @Override
    public DataDefinition getTransformedParameterTypes() {
        return expandedParamInfo;
    }

    @Override
    public String getTransformedQuery(NameResolver nr) {
        String sql = text.toString(nr);
        if (qA.getNoFrom()) {
            return sql.substring(0, sql.toLowerCase().indexOf("from")).trim();
        }
        return sql;
    }

    @Override
    public Object[] toParameterArray(Map<String, Object> arguments) {

        if (arguments == null) {
            throw new MakumbaError("Error: arguments shouldn't be null");
        }

        Map<String, Exception> errors = new HashMap<String, Exception>();
        Map<String, Integer> correct = new HashMap<String, Integer>();

        ArrayList<Object> res = new ArrayList<Object>();

        int paramIndex = 0;
        DataDefinition dd = qA.getQueryParameters().getParameterTypes();
        for (String string : qA.getQueryParameters().getParameterOrder()) {

            String name = QueryAnalysisProvider.getActualParameterName(string);
            Object o = arguments.get(name);

            FieldDefinition fd = dd.getFieldDefinition(paramIndex);
            InvalidValueException ive = null;

            if (o instanceof List<?>) {
                List<?> v = (List<?>) o;

                for (int i = 0; i < v.size(); i++) {
                    ive = checkValue(fd, v.get(i), res);
                }
            } else {
                ive = checkValue(fd, o, res);
            }
            if (ive != null) {
                if (correct.get(name) == null) {
                    errors.put(name, ive);
                }
            } else {
                errors.remove(name);
                correct.put(name, paramIndex);
            }
            paramIndex++;
        }
        if (!errors.isEmpty()) {
            String s = "";
            for (String o : errors.keySet()) {
                s += "\nargument: " + o + "; exception:\n" + errors.get(o);
            }
            throw new InvalidValueException(s);
        }
        return res.toArray();
    }

    private InvalidValueException checkValue(FieldDefinition fd, Object o, ArrayList<Object> res) {
        try {
            o = fd.checkValue(o);
            res.add(o);
            return null;
        } catch (InvalidValueException ivex) {
            res.add(Pointer.Null); // or a dummy value for that type
            return ivex;
        }
    }

    /**
     * Expands multiple parameters, i.e. parameters that are vectors or lists. This is necessary for execution of the
     * SQL query. This method expands the analyser tree and multiplies the parameters according the size of the multiple
     * parameters, and sets the expandedParamInfo so that clients of the {@link ParameterTransformer} can use it to do
     * type checking on the SQL query parameters.
     */
    private void expandMultipleParameters(Map<String, Object> arguments) throws ProgrammerError {

        expandedParamInfo = DataDefinitionProvider.getInstance().getVirtualDataDefinition(
            "SQL parameters for " + qA.getQuery());

        ArrayList<AST> queryParams = findQueryParameters(analyserTreeSQL, new ArrayList<AST>());

        // expand multiple params (vectors, lists) into multiple parameter
        // entries
        for (int i = 0; i < qA.getQueryParameters().getParameterOrder().size(); i++) {
            Object val = getArgumentValue(qA.getQueryParameters().getParameterOrder().get(i), arguments);

            // now expand the query tree from one list to a number of elements
            FieldDefinition fd = qA.getQueryParameters().getParameterTypes().getFieldDefinition(i);
            if (val instanceof List<?>) {
                if (!qA.getQueryParameters().isMultiValue(i)) {
                    throw new InvalidValueException("parameter " + qA.getQueryParameters().getParameterOrder().get(i)
                            + " at position " + i + " " + " cannot have multiple values " + "\nquery: " + qA.getQuery());
                }
                List<?> v = (List<?>) val;
                AST qp = queryParams.get(i);
                AST next = qp.getNextSibling();

                // we have to append as n - 1 parameters to the tree
                for (int j = 0; j < v.size() - 1; j++) {

                    // expand tree
                    qp.setNextSibling(ASTUtil.create(qA.getAnalyserFactory(), HqlSqlTokenTypes.NAMED_PARAM, "?"));
                    qp = qp.getNextSibling();
                    if (j == v.size() - 1) {
                        qp.setNextSibling(next);
                    }
                }

                // build expanded parameter types definition
                for (int k = 0; k < v.size(); k++) {
                    expandedParamInfo.addField(DataDefinitionProvider.getInstance().makeFieldWithName(
                        fd.getName() + "_" + k, fd));
                }
            } else {
                expandedParamInfo.addField(DataDefinitionProvider.getInstance().makeFieldWithName(fd.getName(), fd));
            }
        }
    }

    /**
     * Gets the value of a given argument, applies name transformation if necessary, and checks if the value is not null
     * 
     * @param argumentName
     *            the name of the argument to get
     * @param arguments
     *            the map of arguments. Note that this is a special map, see
     *            {@link TransactionImplementation#paramsToMap(Object args)}
     */
    private Object getArgumentValue(String argumentName, Map<String, Object> arguments) throws ProgrammerError {

        if (arguments == null) {
            throw new MakumbaError("Empty arguments provided");
        }

        if (argumentName == null) {
            throw new MakumbaError("Empty argumentName provided");

        }

        Object val = arguments.get(argumentName);
        if (val == null) {
            throw new ProgrammerError("The parameter '" + argumentName + "' should not be null");
        }
        return val;
    }

    /**
     * Find all the named parameters in the analysis tree and puts them in a list
     */
    private ArrayList<AST> findQueryParameters(AST tree, ArrayList<AST> result) {

        if (tree == null) {
            return result;
        }

        // we only look for named params since those are the ones MQL uses
        if (tree.getType() == HqlSqlTokenTypes.NAMED_PARAM) {
            result.add(tree);
        }

        // recursive-descent traversal, first the children, then the siblings
        findQueryParameters(tree.getFirstChild(), result);
        findQueryParameters(tree.getNextSibling(), result);

        return result;
    }

    private static int generators = NamedResources.makeStaticCache("SQL Query Generators", new NamedResourceFactory() {

        private static final long serialVersionUID = -9039330018176247478L;

        @Override
        protected Object getHashObject(Object name) throws Throwable {

            Object[] multi = (Object[]) name;
            MqlQueryAnalysis qA = (MqlQueryAnalysis) multi[0];
            @SuppressWarnings("unchecked")
            Map<String, Object> args = (Map<String, Object>) multi[1];

            StringBuffer sb = new StringBuffer();
            for (String arg : qA.getQueryParameters().getParameterOrder()) {

                arg = QueryAnalysisProvider.getActualParameterName(arg);

                Object o = args.get(arg);

                if (o instanceof List<?>) {
                    sb.append(((List<?>) o).size());
                } else {
                    sb.append(1);
                }
                sb.append(" ");
            }
            sb.append(multi[2]);

            return qA.getQuery() + " " + sb.toString();
        }

        @Override
        protected Object makeResource(Object name, Object hashName) throws Throwable {
            Object[] multi = (Object[]) name;
            MqlQueryAnalysis qA = (MqlQueryAnalysis) multi[0];
            @SuppressWarnings("unchecked")
            Map<String, Object> args = (Map<String, Object>) multi[1];

            String lang = (String) multi[2];
            return new MqlParameterTransformer(qA, lang.equals("hql") ? new MqlHqlGenerator() : new MqlSqlGenerator());
        }

        @Override
        protected void configureResource(Object name, Object hashName, Object resource) throws Throwable {
            Object[] multi = (Object[]) name;
            @SuppressWarnings("unchecked")
            Map<String, Object> args = (Map<String, Object>) multi[1];

            ((MqlParameterTransformer) resource).init(args);

        }
    });

    public static MqlParameterTransformer getSQLQueryGenerator(MqlQueryAnalysis qA, Map<String, Object> args) {

        return (MqlParameterTransformer) NamedResources.getStaticCache(generators).getResource(
            new Object[] { qA, args, "mql" });

    }

    public static MqlParameterTransformer getSQLQueryGenerator(MqlQueryAnalysis qA, Map<String, Object> args,
            String language) {

        return (MqlParameterTransformer) NamedResources.getStaticCache(generators).getResource(
            new Object[] { qA, args, language });

    }

    public static void main(String[] args) {

        MqlQueryAnalysis qA = new MqlQueryAnalysis(
                "SELECT i.name, $actor_test_Individual FROM test.Individual i WHERE i.surname=$surname OR i = $surname",
                false, true);

        Map<String, Object> arguments = new HashMap<String, Object>();

        Vector<String> test = new Vector<String>();
        test.add("la");
        test.add("la");
        test.add("la");
        arguments.put("actor_test_Individual", test);
        arguments.put("surname", "john");
        arguments.put("2", "stuff");

        MqlParameterTransformer qG = new MqlParameterTransformer(qA, new MqlSqlGenerator());
        qG.init(arguments);

        String sql = qG.getTransformedQuery(new NameResolver());
        System.out.println("QUERY: " + sql);

        Object[] arg = qG.toParameterArray(arguments);
        System.out.println("ARGS: " + Arrays.toString(arg));

        System.out.println("SIZE: " + qG.getParameterCount());

        System.out.println("TYPES: + " + qG.getTransformedParameterTypes());
        for (String n : qG.getTransformedParameterTypes().getFieldNames()) {
            System.out.println(qG.getTransformedParameterTypes().getFieldDefinition(n));
        }
    }

}
