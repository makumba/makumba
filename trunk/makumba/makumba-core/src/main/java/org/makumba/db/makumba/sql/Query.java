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
import org.makumba.db.NativeQuery;
import org.makumba.db.NativeQuery.Parameters;
import org.makumba.db.makumba.DBConnection;
import org.makumba.providers.DataDefinitionProvider;

/** SQL implementation of a OQL query */
public class Query implements org.makumba.db.makumba.Query {

    NativeQuery nat;

    String insertIn;

    /**
     * Gets the raw SQL query to be executed
     * 
     * @return the SQL query string to be sent to the database, given a set of arguments
     */
    public String getCommand(Map<String, Object> arguments) {
        return nat.getCommand(arguments);
    }

    public Query(Database db, String MQLQuery, String insertIn) {

        nat = NativeQuery.getNativeQuery(MQLQuery, "mql", insertIn, db.getNameResolverHook());

        this.insertIn = insertIn;
    }

    @Override
    public Vector<Dictionary<String, Object>> execute(Map<String, Object> args, DBConnection dbc, int offset, int limit) {

        Database db = (Database) dbc.getHostDatabase();
        TableManager resultHandler = (TableManager) db.makePseudoTable(nat.getProjectionType());

        if (insertIn == null && nat.getConstantValues() != null) {
            // no need to send the query to the sql engine
            return nat.getConstantResult(args, offset, limit);
        }

        String com = nat.getCommand(args);
        if (db.supportsLimitInQuery()) {
            com += " " + db.getLimitSyntax(); // TODO: it might happen that it should be in other places than at the
                                              // end.
        }
        PreparedStatement ps = ((SQLDBConnection) dbc).getPreparedStatement(com);

        Parameters nap = nat.makeActualParameters(args);

        try {
            int nParam = nap.getParameterCount();
            if (db.supportsLimitInQuery()) {
                nParam += 2;
            }

            if (ps.getParameterMetaData().getParameterCount() != nParam) {
                throw new InvalidValueException("Wrong number of arguments passed to query :\n"
                        + ps.getParameterMetaData().getParameterCount() + ": " + ps + " \n" + nParam + ": " + com);
            }

            ParameterAssigner.assignParameters(db, nap, ps, args);

            if (db.supportsLimitInQuery()) {
                int limit1 = limit == -1 ? Integer.MAX_VALUE : limit;

                if (db.isLimitOffsetFirst()) {
                    ps.setInt(nap.getParameterCount() + 1, offset);
                    ps.setInt(nap.getParameterCount() + 2, limit1);
                } else {
                    ps.setInt(nap.getParameterCount() + 1, limit1);
                    ps.setInt(nap.getParameterCount() + 2, offset);
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

    @Override
    public int insert(Map<String, Object> args, DBConnection dbc) {
        if (insertIn == null) {
            throw new IllegalArgumentException("no table to insert in specified");
        }
        Database db = (Database) dbc.getHostDatabase();
        TableManager resultHandler = (TableManager) db.makePseudoTable(nat.getProjectionType());
        TableManager insertHandler;

        DataDefinition insert1 = DataDefinitionProvider.getInstance().getDataDefinition(insertIn);
        for (String string : nat.getProjectionType().getFieldNames()) {
            if (insert1.getFieldDefinition(string) == null) {
                throw new NoSuchFieldException(insert1, string);
            }
        }
        insertHandler = (TableManager) db.getTable(insert1);

        String comma = "";
        StringBuffer fieldList = new StringBuffer();
        for (String string : resultHandler.getDataDefinition().getFieldNames()) {
            fieldList.append(comma);
            comma = ",";
            fieldList.append(insertHandler.getFieldDBName(string));
        }

        SQLDBConnection sqldbc = (SQLDBConnection) dbc;
        String tablename = "temp_" + sqldbc.n;

        String com = "INSERT INTO " + tablename + " ( " + fieldList + ") " + nat.getCommand(args);
        try {
            resultHandler.create(sqldbc, tablename, true);
            PreparedStatement ps = sqldbc.getPreparedStatement(com);
            ParameterAssigner.assignParameters(db, nat.makeActualParameters(args), ps, args);

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
