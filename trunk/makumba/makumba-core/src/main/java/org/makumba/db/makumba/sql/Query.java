///////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003  http://www.makumba.org
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

package org.makumba.db.makumba.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Dictionary;
import java.util.Map;
import java.util.Vector;

import org.makumba.DBError;
import org.makumba.DataDefinition;
import org.makumba.InvalidValueException;
import org.makumba.MakumbaError;
import org.makumba.NoSuchFieldException;
import org.makumba.db.makumba.DBConnection;
import org.makumba.db.makumba.MQLQueryProvider;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.QueryAnalysisProvider;
import org.makumba.providers.query.mql.MqlParameterTransformer;
import org.makumba.providers.query.mql.MqlQueryAnalysis;

/** SQL implementation of a OQL query */
public class Query implements org.makumba.db.makumba.Query {

    String query;

    TableManager resultHandler;

    MqlQueryAnalysis qA;

    String limitSyntax;

    boolean offsetFirst;

    boolean supportsLimitInQuery;

    String insertIn;

    Database db;

    TableManager insertHandler;

    /**
     * Gets the raw SQL query to be executed
     * 
     * @return the SQL query string to be sent to the database, given a set of arguments
     */
    public String getCommand(Map<String, Object> arguments) {
        return MqlParameterTransformer.getSQLQueryGenerator(qA, arguments).getTransformedQuery(db.getNameResolverHook());
    }

    public Query(Database db, String MQLQuery, String insertIn) {
        QueryAnalysisProvider qap = null;
        this.db = db;
        query = MQLQuery;
        try {
            qap = (QueryAnalysisProvider) Class.forName(MQLQueryProvider.MQLQUERY_ANALYSIS_PROVIDER).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        qA = (MqlQueryAnalysis) (insertIn != null && insertIn.length() > 0 ? qap.getQueryAnalysis(MQLQuery, insertIn)
                : qap.getQueryAnalysis(MQLQuery));

        resultHandler = (TableManager) db.makePseudoTable(qA.getProjectionType());
        limitSyntax = db.getLimitSyntax();
        offsetFirst = db.isLimitOffsetFirst();
        supportsLimitInQuery = db.supportsLimitInQuery();
        this.insertIn = insertIn;
        if (insertIn != null && insertIn.length() > 0) {
            analyzeInsertIn(qA.getProjectionType(), db);
        }
    }

    @Override
    public Vector<Dictionary<String, Object>> execute(Map<String, Object> args, DBConnection dbc, int offset, int limit) {
        if ((insertIn == null || insertIn.length() == 0) && qA.getConstantValues() != null) {
            // no need to send the query to the sql engine
            return qA.getConstantResult(args, offset, limit);
        }

        MqlParameterTransformer qG = MqlParameterTransformer.getSQLQueryGenerator(qA, args);

        String com = qG.getTransformedQuery(db.getNameResolverHook());
        if (supportsLimitInQuery) {
            com += " " + limitSyntax; // TODO: it might happen that it should be in other places than at the end.
        }
        PreparedStatement ps = ((SQLDBConnection) dbc).getPreparedStatement(com);

        try {
            int nParam = qG.getParameterCount();
            if (supportsLimitInQuery) {
                nParam += 2;
            }

            if (ps.getParameterMetaData().getParameterCount() != nParam) {
                throw new InvalidValueException("Wrong number of arguments passed to query :\n"
                        + ps.getParameterMetaData().getParameterCount() + ": " + ps + " \n" + nParam + ": " + com);
            }

            ParameterAssigner.assignParameters(db, qG, ps, args);

            if (supportsLimitInQuery) {
                int limit1 = limit == -1 ? Integer.MAX_VALUE : limit;

                if (offsetFirst) {
                    ps.setInt(qG.getParameterCount() + 1, offset);
                    ps.setInt(qG.getParameterCount() + 2, limit1);
                } else {
                    ps.setInt(qG.getParameterCount() + 1, limit1);
                    ps.setInt(qG.getParameterCount() + 2, offset);
                }
            }

            java.util.logging.Logger.getLogger("org.makumba.db.query.execution").fine(
                "" + db.getWrappedStatementToString(ps));
            java.util.Date d = new java.util.Date();
            ResultSet rs = null;
            try {
                rs = ps.executeQuery();
            } catch (SQLException se) {
                org.makumba.db.makumba.sql.Database.logException(se, dbc);
                throw new DBError(se, com);
            }
            long diff = new java.util.Date().getTime() - d.getTime();
            java.util.logging.Logger.getLogger("org.makumba.db.query.performance").fine(
                "" + diff + " ms " + db.getWrappedStatementToString(ps));
            return goThru(rs, resultHandler);
        } catch (SQLException e) {
            throw new org.makumba.DBError(e);
        } catch (RuntimeException e) {
            // System.out.println(query);
            throw e;
        } finally {
            try {
                ps.close();
            } catch (SQLException e) {
                throw new org.makumba.DBError(e);
            }
        }
    }

    private Vector<Dictionary<String, Object>> goThru(ResultSet rs, TableManager rm) {
        // TODO: if(!supportsLimitInQuery) { do slower limit & offset in java}
        int size = rm.keyIndex.size();

        Vector<Dictionary<String, Object>> ret = new Vector<Dictionary<String, Object>>(100, 100);
        Object[] dt;
        try {
            while (rs.next()) {
                rm.fillResult(rs, dt = new Object[size]);
                ret.addElement(new org.makumba.commons.ArrayMap(rm.keyIndex, dt));
            }
            rs.close();
        } catch (SQLException e) {
            throw new org.makumba.DBError(e, rm.getDataDefinition().getName());
        }
        return ret;
    }

    private void analyzeInsertIn(DataDefinition proj, org.makumba.db.makumba.Database db) {
        DataDefinition insert = DataDefinitionProvider.getInstance().getDataDefinition(insertIn);
        for (String string : proj.getFieldNames()) {
            if (insert.getFieldDefinition(string) == null) {
                throw new NoSuchFieldException(insert, string);
            }
        }
        insertHandler = (TableManager) db.getTable(insert);
    }

    @Override
    public int insert(Map<String, Object> args, DBConnection dbc) {

        MqlParameterTransformer qG = MqlParameterTransformer.getSQLQueryGenerator(qA, args);

        String comma = "";
        StringBuffer fieldList = new StringBuffer();
        for (String string : resultHandler.getDataDefinition().getFieldNames()) {
            fieldList.append(comma);
            comma = ",";
            fieldList.append(insertHandler.getFieldDBName(string));
        }

        SQLDBConnection sqldbc = (SQLDBConnection) dbc;
        String tablename = "temp_" + sqldbc.n;

        String com = "INSERT INTO " + tablename + " ( " + fieldList + ") "
                + qG.getTransformedQuery(db.getNameResolverHook());
        try {
            resultHandler.create(sqldbc, tablename, true);
            PreparedStatement ps = sqldbc.getPreparedStatement(com);
            ParameterAssigner.assignParameters(db, qG, ps, args);

            int n = ps.executeUpdate();
            ps.close();

            com = "INSERT INTO " + insertHandler.getDBName() + " (" + fieldList + ") SELECT " + fieldList + " FROM "
                    + tablename;
            ps = sqldbc.getPreparedStatement(com);

            int m = ps.executeUpdate();

            if (m != n) {
                throw new MakumbaError("inserted in temp " + n + " inserted in final " + m);
            }

            ps.close();
            return n;
        } catch (SQLException e) {
            throw new org.makumba.DBError(e);
        } finally {
            try {
                Statement st = sqldbc.createStatement();
                st.execute("DROP TABLE " + tablename);
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
