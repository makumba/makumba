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
import java.util.Vector;

import org.makumba.DBError;
import org.makumba.DataDefinition;
import org.makumba.InvalidValueException;
import org.makumba.MakumbaError;
import org.makumba.NoSuchFieldException;
import org.makumba.db.makumba.DBConnection;
import org.makumba.db.makumba.DBConnectionWrapper;
import org.makumba.db.makumba.OQLQueryProvider;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.QueryAnalysis;
import org.makumba.providers.QueryAnalysisProvider;

/** SQL implementation of a OQL query */
public class Query implements org.makumba.db.makumba.Query {
    
    String query;

    TableManager resultHandler;

    String command;

    ParameterAssigner assigner;

    String limitSyntax;

    boolean offsetFirst;

    boolean supportsLimitInQuery;

    String insertIn;

    TableManager insertHandler;

    public String getCommand() {
        return command;
    }

    public Query(Database db, String OQLQuery, String insertIn) {
        QueryAnalysisProvider qap= null;
        query=OQLQuery;
        try {
           qap=(QueryAnalysisProvider) Class.forName(OQLQueryProvider.OQLQUERY_ANALYSIS_PROVIDER).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
  
        QueryAnalysis qA= qap.getQueryAnalysis(OQLQuery);
        
        command = qA.writeInSQLQuery(db.getNameResolverHook());
            

        resultHandler = (TableManager) db.makePseudoTable(qA.getProjectionType());
        assigner = new ParameterAssigner(db, qA);
        limitSyntax = db.getLimitSyntax();
        offsetFirst = db.isLimitOffsetFirst();
        supportsLimitInQuery = db.supportsLimitInQuery();
        this.insertIn = insertIn;
        if (insertIn != null && insertIn.length() > 0) {
            analyzeInsertIn(qA.getProjectionType(), db);
        }
    }

    public Vector<Dictionary<String, Object>> execute(Object[] args, DBConnection dbc, int offset, int limit) {
        String com = command;
        if (supportsLimitInQuery) {
            com += " " + limitSyntax; // TODO: it might happen that it should be in other places than at the end.
        }
        PreparedStatement ps = ((SQLDBConnection) dbc).getPreparedStatement(com);

        try {
            String s = assigner.assignParameters(ps, args);

            if (supportsLimitInQuery) {
                int limit1 = limit == -1 ? Integer.MAX_VALUE : limit;

                if (offsetFirst) {
                    ps.setInt(assigner.tree.parameterNumber() + 1, offset);
                    ps.setInt(assigner.tree.parameterNumber() + 2, limit1);
                } else {
                    ps.setInt(assigner.tree.parameterNumber() + 1, limit1);
                    ps.setInt(assigner.tree.parameterNumber() + 2, offset);
                }
            }

            if (s != null) {
                throw new InvalidValueException("Errors while trying to assign arguments to query:\n" + com + "\n" + s);
            }

            java.util.logging.Logger.getLogger("org.makumba.db.query.execution").fine("" + ps);
            java.util.Date d = new java.util.Date();
            ResultSet rs = null;
            try {
                rs = ps.executeQuery();
            } catch (SQLException se) {
                org.makumba.db.makumba.sql.Database.logException(se, dbc);
                throw new DBError(se, com);
            }
            long diff = new java.util.Date().getTime() - d.getTime();
            java.util.logging.Logger.getLogger("org.makumba.db.query.performance").fine("" + diff + " ms " + ps);
            return goThru(rs, resultHandler);
        } catch (SQLException e) {
            throw new org.makumba.DBError(e);
        }catch (RuntimeException e) {
            System.out.println(query);
            throw e;
        }
        finally{
            try {
                ps.close();
            } catch (SQLException e) {
                throw new org.makumba.DBError(e);
            }
        }
    }

    Vector<Dictionary<String, Object>> goThru(ResultSet rs, TableManager rm) {
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

    void analyzeInsertIn(DataDefinition proj, org.makumba.db.makumba.Database db) {
        DataDefinition insert = DataDefinitionProvider.getInstance().getDataDefinition(insertIn);
        for (String string : proj.getFieldNames()) {
            if (insert.getFieldDefinition(string) == null) {
                throw new NoSuchFieldException(insert, string);
            }
        }
        insertHandler = (TableManager) db.getTable(insert);
    }

    public int insert(Object[] args, DBConnection dbc) {
        String comma = "";
        StringBuffer fieldList = new StringBuffer();
        for (String string : resultHandler.getDataDefinition().getFieldNames()) {
            fieldList.append(comma);
            comma = ",";
            fieldList.append(insertHandler.getFieldDBName(string));
        }

        String tablename = "temp_" + (int) (Math.random() * 10000.0);

        String com = "INSERT INTO " + tablename + " ( " + fieldList + ") " + command;
        try {
            SQLDBConnection sqldbc = (SQLDBConnection) ((DBConnectionWrapper) dbc).getWrapped();
            resultHandler.create(sqldbc, tablename, true);
            PreparedStatement ps = sqldbc.getPreparedStatement(com);
            String s = assigner.assignParameters(ps, args);
            if (s != null) {
                throw new InvalidValueException("Errors while trying to assign arguments to query:\n" + com + "\n" + s);
            }

            int n = ps.executeUpdate();
            ps.close();
            
            com = "INSERT INTO " + insertHandler.getDBName() + " (" + fieldList + ") SELECT " + fieldList + " FROM "
                    + tablename;
            ps = sqldbc.getPreparedStatement(com);

            int m = ps.executeUpdate();

            if (m != n) {
                throw new MakumbaError("inserted in temp " + n + " inserted in final " + m);
            }
            Statement st = sqldbc.createStatement();
            st.execute("DROP TABLE " + tablename);
            st.close();
            ps.close();
            return n;
        } catch (SQLException e) {
            throw new org.makumba.DBError(e);
        }
    }
}
