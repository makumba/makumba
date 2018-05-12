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
import org.makumba.FieldDefinition;
import org.makumba.InvalidValueException;
import org.makumba.MakumbaError;
import org.makumba.NoSuchFieldException;
import org.makumba.commons.ArrayMap;
import org.makumba.db.NativeQuery;
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
    public Vector<Dictionary<String, Object>> execute(Map<String, Object> args, final DBConnection dbc, int offset,
            int limit) {

        final Database db = (Database) dbc.getHostDatabase();

        if (insertIn == null && nat.getConstantValues() != null) {
            // no need to send the query to the sql engine
            return nat.getConstantResult(args, offset, limit);
        }

        String com = nat.getCommand(args);
        if (db.supportsLimitInQuery()) {
            com += " " + db.getLimitSyntax(); // TODO: it might happen that it should be in other places than at the
                                              // end.
        }
        final PreparedStatement ps = ((SQLDBConnection) dbc).getPreparedStatement(com);
        // final String comFinal = com;
        // Parameters nap = nat.makeActualParameters(args);

        try {
            int nParam = nat.assignParameters(db.makeParameterHandler(ps, dbc, com), args);

            // int nParam = nap.getParameterCount();
            if (db.supportsLimitInQuery()) {
                nParam += 2;
            }

            if (ps.getParameterMetaData().getParameterCount() != nParam) {
                throw new InvalidValueException("Wrong number of arguments passed to query :\n"
                        + ps.getParameterMetaData().getParameterCount() + ": " + ps + " \n" + nParam + ": " + com);
            }

            // ParameterAssigner.assignParameters(db, nap, ps, args);

            if (db.supportsLimitInQuery()) {
                int limit1 = limit == -1 ? Integer.MAX_VALUE : limit;

                if (db.isLimitOffsetFirst()) {
                    ps.setInt(nParam - 1, offset);
                    ps.setInt(nParam, limit1);
                } else {
                    ps.setInt(nParam + 1, limit1);
                    ps.setInt(nParam + 2, offset);
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
            return goThru(db, rs);
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

    private Vector<Dictionary<String, Object>> goThru(Database db, ResultSet rs) {
        // TODO: if(!supportsLimitInQuery) { do slower limit & offset in java}
        int size = nat.getKeyIndex().size();

        Vector<Dictionary<String, Object>> ret = new Vector<Dictionary<String, Object>>(100, 100);
        Object[] dt;
        try {
            while (rs.next()) {
                dt = new Object[size];
                int n = nat.getProjectionType().getFieldDefinitions().size();
                for (int i = 0; i < n; i++) {
                    FieldDefinition fd = nat.getProjectionType().getFieldDefinition(i);
                    if (fd.getType().startsWith("set")) {
                        continue;
                    }
                    try {
                        dt[i] = db.getValue(fd, rs, i + 1);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        java.util.logging.Logger.getLogger("org.makumba.db.query.execution").log(
                            java.util.logging.Level.SEVERE,
                            "" + i + " " + nat.getProjectionType().getName() + " " + nat.getKeyIndex() + " "
                                    + fd.getName(), e);
                        throw e;
                    }
                }
                ret.addElement(new ArrayMap(nat.getKeyIndex(), dt));
            }

            rs.close();
        } catch (SQLException e) {
            throw new org.makumba.DBError(e, nat.toString());
        }
        return ret;
    }

    @Override
    public int insert(Map<String, Object> args, DBConnection dbc) {
        if (insertIn == null) {
            throw new IllegalArgumentException("no table to insert in specified");
        }
        Database db = (Database) dbc.getHostDatabase();

        DataDefinition insert1 = DataDefinitionProvider.getInstance().getDataDefinition(insertIn);
        for (FieldDefinition fd : nat.getProjectionType().getFieldDefinitions()) {
            if (insert1.getFieldDefinition(fd.getName()) == null) {
                throw new NoSuchFieldException(insert1, fd.getName());
            }
        }

        String comma = "";
        StringBuffer fieldList = new StringBuffer();
        for (FieldDefinition fd : nat.getProjectionType().getFieldDefinitions()) {
            fieldList.append(comma);
            comma = ",";
            fieldList.append(db.getFieldDBName(fd));
        }

        SQLDBConnection sqldbc = (SQLDBConnection) dbc;
        String tablename = "temp_" + sqldbc.n;

        String com = "INSERT INTO " + tablename + " ( " + fieldList + ") " + nat.getCommand(args);
        try {
            PreparedStatement create = sqldbc.getPreparedStatement(db.createStatement(tablename,
                nat.getProjectionType()));
            create.executeUpdate();
            create.close();
            PreparedStatement ps = sqldbc.getPreparedStatement(com);

            nat.assignParameters(db.makeParameterHandler(ps, dbc, com), args);

            int n = ps.executeUpdate();
            ps.close();

            com = "INSERT INTO " + db.getDBName(insert1) + " (" + fieldList + ") SELECT " + fieldList + " FROM "
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
