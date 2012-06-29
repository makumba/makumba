// /////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003 http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//  -------------
//  $Id: timestampFormatter.java 2568 2008-06-14 01:06:21Z rosso_nero $
//  $Name$
/////////////////////////////////////
package org.makumba.db;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.InvalidValueException;
import org.makumba.MakumbaError;
import org.makumba.ProgrammerError;
import org.makumba.commons.ArrayMap;
import org.makumba.commons.NameResolver;
import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;
import org.makumba.commons.ParamInfo;
import org.makumba.commons.TextList;
import org.makumba.providers.QueryAnalysisProvider;
import org.makumba.providers.QueryParameters;
import org.makumba.providers.QueryProvider;
import org.makumba.providers.query.mql.MqlHqlGenerator;
import org.makumba.providers.query.mql.MqlQueryAnalysis;
import org.makumba.providers.query.mql.MqlQueryAnalysis.ParamConstant;
import org.makumba.providers.query.mql.MqlSqlGenerator;

/**
 * A native query, either SQL or HQL. Based on the defunct MqlParameterTransformer by manu. Stores the query in TextList
 * format, with only the parameters needing replacement, depending on their cardinality. The query analysis results
 * (parameter types, projection types) is also stored. An explicit effort is made not to store the whole
 * MqlQueryAnalysis which contains a lot of tree and FieldDefinition data.
 * 
 * @author cristi
 * @author Manuel Bernhardt <manuel@makumba.org>
 * @version $Id: MqlSQLQueryGenerator.java,v 1.1 Mar 30, 2010 4:17:00 PM manu Exp $
 */
public class NativeQuery {

    QueryParameters queryParameters;

    TextList text;

    boolean update;

    boolean noFrom;

    String oql;

    DataDefinition projectionType;

    private Map<String, Object> constantValues;

    Dictionary<String, Integer> keyIndex;

    public NativeQuery(MqlQueryAnalysis qAna, String lang, String insertIn, NameResolver nr) {
        this.queryParameters = qAna.getQueryParameters();
        text = qAna.getText(lang.equals("hql") ? new MqlHqlGenerator() : new MqlSqlGenerator()).resolve(nr);
        // System.out.println(text.debug());
        noFrom = qAna.getNoFrom();
        oql = qAna.getQuery();
        projectionType = qAna.getProjectionType();
        constantValues = qAna.getConstantValues();

        keyIndex = new Hashtable<String, Integer>();

        int i = 0;
        for (FieldDefinition fi : getProjectionType().getFieldDefinitions()) {
            if (!fi.getType().startsWith("set")) {
                keyIndex.put(fi.getName(), new Integer(i));
            }
            i++;
        }

    }

    public Map<String, Object> getConstantValues() {
        return constantValues;
    }

    public Dictionary<String, Integer> getKeyIndex() {
        return keyIndex;
    }

    /**
     * Make up a constant result of one row if the query has no from and only constant projections
     * 
     * @return a constant result of one row, containing the respective constants, with the indicated column names
     */
    public Vector<Dictionary<String, Object>> getConstantResult(Map<String, Object> args, int offset, int limit) {
        if (getConstantValues() == null) {
            throw new IllegalStateException("The query " + oql
                    + " has a FROM or non-constant projections so it cannot return a constant result");
        }
        Vector<Dictionary<String, Object>> ret = new Vector<Dictionary<String, Object>>(1);
        if (offset > 0 || limit == 0) {
            return ret;
        }
        Dictionary<String, Integer> keyIndex = new java.util.Hashtable<String, Integer>();
        Object[] val = new Object[getConstantValues().size()];
        int i = 0;
        for (String s : getConstantValues().keySet()) {

            Object column = getConstantValues().get(s);
            if (column instanceof ParamConstant) {
                val[i] = args.get(((ParamConstant) column).getParamName());
            } else {
                val[i] = column;
            }
            keyIndex.put(s, new Integer(i++));
        }

        ret.add(new ArrayMap(keyIndex, val));
        return ret;

    }

    public DataDefinition getProjectionType() {
        return projectionType;
    }

    public String getCommand(Map<String, Object> args) {
        String sql = text.resolveParameters(new ParamInfo.MultipleWriter(args));
        if (noFrom) {
            return sql.substring(0, sql.toLowerCase().lastIndexOf("from")).trim();
        }
        return sql;
    }

    public String getCommandForWriter(ParamInfo.Writer wr) {
        return text.resolveParameters(wr);
    }

    public static NativeQuery getNativeQuery(String query, String queryLang, String insertIn, NameResolver nameResolver) {

        return (NativeQuery) NamedResources.getStaticCache(natives).getResource(
            new Object[] { query, queryLang, insertIn, nameResolver });
    }

    private static int natives = NamedResources.makeStaticCache("Native Queries", new NamedResourceFactory() {

        private static final long serialVersionUID = -9039330018176247478L;

        @Override
        protected Object getHashObject(Object name) throws Throwable {
            Object[] multi = (Object[]) name;
            // the NameResolver is not part of the key because tehre is usually one resolver per queryLanguage
            return "" + multi[0] + "#" + multi[1] + "#" + multi[2];
        }

        @Override
        protected Object makeResource(Object name, Object hashName) throws Throwable {
            Object[] multi = (Object[]) name;
            MqlQueryAnalysis queryAnalysis = (MqlQueryAnalysis) QueryProvider.getQueryAnalzyer("oql").getQueryAnalysis(
                (String) multi[0], (String) multi[2]);
            if (((String) multi[1]).equals("hql")) {
                // FIXME: this messes up the MqlQueryAnalysis so if the same query will be needed for MQL in the same
                // system, it will flop...
                queryAnalysis.prepareForHQL();
            }
            return new NativeQuery(queryAnalysis, (String) multi[1], (String) multi[2], (NameResolver) multi[3]);
        }

    });

    public static interface ParameterHandler {
        public void handle(int index, FieldDefinition type, Object value);
    }

    private InvalidValueException checkValue(ParameterHandler h, int index, FieldDefinition fd, Object o) {
        try {
            if (o == null) {
                h.handle(index, fd, fd.getNull());
                return new InvalidValueException("should not be null");
            }
            o = fd.checkValue(o);
            h.handle(index, fd, o);
            return null;
        } catch (InvalidValueException ivex) {
            h.handle(index, fd, fd.getNull());
            ; // or a dummy value for that type
            return ivex;
        }
    }

    /** Iterates through all parameters, checks their value and assigns them to a given ParameterHandler */
    public int assignParameters(ParameterHandler handler, Map<String, Object> argsMap) throws ProgrammerError {
        if (argsMap == null) {
            throw new MakumbaError("Empty arguments provided");
        }

        int expandedIndex = 0;
        int paramIndex = 0;
        Map<String, Exception> errors = new HashMap<String, Exception>();
        Map<String, Integer> correct = new HashMap<String, Integer>();

        for (String nm : queryParameters.getParameterOrder()) {
            InvalidValueException ive = null;

            Object val = argsMap.get(nm);
            if (val == null) {
                throw new ProgrammerError("The parameter '" + nm + "' should not be null");
            }
            String name = QueryAnalysisProvider.getActualParameterName(nm);

            FieldDefinition fd = queryParameters.getParameterTypes().getFieldDefinition(paramIndex);
            if (val instanceof List<?>) {
                if (!queryParameters.isMultiValue(paramIndex)) {
                    throw new InvalidValueException("parameter " + queryParameters.getParameterOrder().get(paramIndex)
                            + " at position " + paramIndex + " " + " cannot have multiple values " + "\nquery: " + oql);
                }
                List<?> v = (List<?>) val;

                // build expanded parameter types definition
                for (int k = 0; k < v.size(); k++) {
                    ive = checkValue(handler, expandedIndex, fd, v.get(k));
                    expandedIndex++;
                }
            } else {
                ive = checkValue(handler, expandedIndex, fd, val);
                expandedIndex++;
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
        return expandedIndex;
    }

}
