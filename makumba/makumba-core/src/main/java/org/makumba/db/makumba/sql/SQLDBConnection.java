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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.makumba.DBError;
import org.makumba.db.makumba.DBConnection;
import org.makumba.providers.TransactionProvider;

import com.mchange.v2.c3p0.PooledDataSource;

public class SQLDBConnection extends DBConnection {
    static int nconn = 0;

    int n;

    private Connection conn;

    SQLDBConnection(org.makumba.db.makumba.Database db, TransactionProvider tp, PooledDataSource pooledDataSource)
            throws SQLException {
        super(db, tp);
        n = nconn++;
        makeConnection(pooledDataSource);
    }

    private void makeConnection(PooledDataSource pooledDataSource) throws SQLException {

        conn = pooledDataSource.getConnection();

        if (conn.getMetaData().supportsTransactions()) {
            conn.setAutoCommit(false);
        }
        if (conn.getMetaData().supportsTransactionIsolationLevel(Database.DESIRED_TRANSACTION_LEVEL)) {
            conn.setTransactionIsolation(Database.DESIRED_TRANSACTION_LEVEL);
        }

        if (org.makumba.db.makumba.sql.Database.supportsUTF8()) {
            Statement st = this.createStatement();
            st.execute("SET CHARACTER SET utf8");
            st.close();
        }
    }

    @Override
    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            Database.logException(e, this);
            throw new DBError(e);
        }
    }

    @Override
    public void commit() {
        try {
            conn.commit();
        } catch (SQLException e) {
            Database.logException(e, this);
            throw new DBError(e);
        }
    }

    @Override
    public void rollback() {
        try {
            conn.rollback();
        } catch (SQLException e) {
            Database.logException(e, this);
            throw new DBError(e);
        }
    }

    private Connection getConnection() throws SQLException {
        // if(conn.isClosed()) { MakumbaSystem.getMakumbaLogger("db.exception").warning("reconnecting connection "+n);
        // makeConnection(); }
        return conn;
    }

    @Override
    public String toString() {
        return "connection " + n;
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        return getConnection().getMetaData();
    }

    public Statement createStatement() throws SQLException {
        return getConnection().createStatement();
    }

    public PreparedStatement getPreparedStatement(String s) {
        try {
            return getConnection().prepareStatement(s);
        } catch (SQLException e) {
            org.makumba.db.makumba.sql.Database.logException(e);
            throw new DBError(e);
        }
    }

    public PreparedStatement getInsertPreparedStatement(String s) {
        try {
            return getConnection().prepareStatement(s, PreparedStatement.RETURN_GENERATED_KEYS);
        } catch (SQLException e) {
            org.makumba.db.makumba.sql.Database.logException(e);
            throw new DBError(e);
        }
    }
    // try{
    // return (PreparedStatement)preparedStatements.getResource(s);
    // }catch(RuntimeWrappedException e)
    // {
    // if(e.getReason() instanceof SQLException)
    // {
    // org.makumba.db.sql.Database.logException((SQLException)e.getReason());
    // throw new DBError(e.getReason());
    // }
    // throw e;
    // }
    // }
    //
    // NamedResources preparedStatements= new NamedResources(new NamedResourceFactory()
    // {
    // protected Object makeResource(Object nm) throws SQLException {
    // return conn.prepareStatement((String)nm);
    // }
    // });
}
