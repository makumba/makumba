package org.makumba.providers.query.mql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
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
 * @author Manuel Gay
 * @version $Id: MqlSQLQueryGenerator.java,v 1.1 Mar 30, 2010 4:17:00 PM manu Exp $
 */
public class MqlParameterTransformer implements ParameterTransformer {

    private MqlQueryAnalysis qA;

    private AST analyserTreeSQL;

    private TextList text;

    private DataDefinition expandedParamInfo = null;

    public MqlParameterTransformer(MqlQueryAnalysis qA) {
        this.qA = qA;
        this.analyserTreeSQL = qA.getAnalyserTree();
    }

    public void init(Map<String, Object> arguments) {

        if (expandedParamInfo == null) {
            expandMultipleParameters(arguments);
        }
    }

    public int getParameterCount() {
        if (expandedParamInfo == null) {
            throw new MakumbaError("Can't call this method without having set the arguments with setArguments!");
        } else {
            return expandedParamInfo.getFieldNames().size();
        }
    }

    public DataDefinition getTransformedParameterTypes() {
        return expandedParamInfo;
    }

    public String getSQLQuery(MqlSqlGenerator mg, NameResolver nr) {
        try {
            mg.statement(analyserTreeSQL);
        } catch (Throwable e) {
            QueryAnalysisProvider.doThrow(qA.getQuery(), e, analyserTreeSQL);
        }
        QueryAnalysisProvider.doThrow(qA.getQuery(), mg.error, analyserTreeSQL);

        text = mg.getText();

        // TODO: we can cache these SQL results by the key of the NameResolver
        // still we should first check if this is needed, maybe the generated SQL (or processing of it)
        // is cached already somewhere else
        String sql = text.toString(nr);
        if (qA.getNoFrom()) {
            return sql.substring(0, sql.toLowerCase().indexOf("from")).trim();
        }
        return sql;
    }

    public String getTransformedQuery(NameResolver nr) {
        return getSQLQuery(new MqlSqlGenerator(), nr);
    }

    public Object[] toParameterArray(Map<String, Object> arguments) {

        if (arguments == null) {
            throw new MakumbaError("Error: arguments shouldn't be null");
        }

        ArrayList<Object> res = new ArrayList<Object>();

        for (String string : qA.getParameterOrder()) {

            Object o = getArgumentValue(string, arguments);

            if (o instanceof List<?>) {
                List<?> v = (List<?>) o;
                for (int i = 1; i <= v.size(); i++) {
                    res.add(v.get(i - 1));
                }
            } else {
                res.add(o);
            }
        }
        return res.toArray();
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

        // expand multiple params (vectors, lists) into multiple parameter entries
        for (int i = 0; i < qA.getParameterOrder().size(); i++) {
            Object val = getArgumentValue(qA.getParameterOrder().get(i), arguments);

            // now expand the query tree from one list to a number of elements
            FieldDefinition fd = qA.getParameterTypes().getFieldDefinition(i);
            if (val instanceof List<?>) {
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

        // if we have a makumba parameter (translated by MqlAnalysisProvider#transformOQLParameters) we need to recover
        // the original argument index to get it in the map
        // indeed in the map of arguments we get, unnamed parameters like $1, $2, ... are registered with their name
        if (argumentName.startsWith(MqlQueryAnalysis.MAKUMBA_PARAM)) {
            argumentName = argumentName.substring(MqlQueryAnalysis.MAKUMBA_PARAM.length());
            int n = Integer.parseInt(argumentName);
            argumentName = "" + (n + 1);
        }
        if (argumentName.indexOf("###") > 0) {
            argumentName = argumentName.substring(0, argumentName.indexOf("###"));
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

    public static boolean isValueInvalidForPosition(FieldDefinition fd, Object value) {
        boolean isMultiTypeParam = fd.getDescription().equals("true");
        boolean isChar = fd.isStringType() && !(value instanceof String);
        boolean isPointer = fd.isPointer() && !(value instanceof Pointer);
        boolean isDifferentPointer = fd.isPointer() && value instanceof Pointer
                && !fd.getPointedType().getName().equals(((Pointer) value).getType());
        boolean isNumber = (fd.isIntegerType() || fd.isRealType()) && !(value instanceof Number);
        boolean giveUp = isMultiTypeParam && (isChar || isPointer || isNumber || isDifferentPointer);
        return giveUp;
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
            for (String arg : qA.getParameterOrder()) {

                if (arg.startsWith(MqlQueryAnalysis.MAKUMBA_PARAM)) {
                    arg = arg.substring(MqlQueryAnalysis.MAKUMBA_PARAM.length());
                    int n = Integer.parseInt(arg);
                    arg = "" + (n + 1);
                }
                if (arg.indexOf("###") > 0) {
                    arg = arg.substring(0, arg.indexOf("###"));
                }

                Object o = args.get(arg);

                if (o instanceof List<?>) {
                    sb.append(((List<?>) o).size());
                } else {
                    sb.append(1);
                }
                sb.append(" ");
            }

            return qA.getQuery() + " " + sb.toString();
        }

        @Override
        protected Object makeResource(Object name, Object hashName) throws Throwable {
            Object[] multi = (Object[]) name;
            MqlQueryAnalysis qA = (MqlQueryAnalysis) multi[0];
            @SuppressWarnings("unchecked")
            Map<String, Object> args = (Map<String, Object>) multi[1];

            return new MqlParameterTransformer(qA);
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

        // FIXME this doesn't work
        // the key is the combination of mql query + parameter cardinality
        // but this is not enough because we don't have the parameter values always being the same (i.e. 2 queries with
        // same cardinality but different values)
        // call configure resource, set arguments

        return (MqlParameterTransformer) NamedResources.getStaticCache(generators).getResource(
            new Object[] { qA, args });

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

        MqlParameterTransformer qG = new MqlParameterTransformer(qA);
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
