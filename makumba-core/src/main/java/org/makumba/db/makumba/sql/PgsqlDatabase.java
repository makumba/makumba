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
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

import org.makumba.FieldDefinition;
import org.makumba.Text;

/** the database adapter for PostgreSQL */
public class PgsqlDatabase extends org.makumba.db.makumba.sql.Database {
    /** simply calls super */
    public PgsqlDatabase(Properties p) {
        super(p);
    }

    /** Postgres column names are case-insensitive */
    protected String getFieldName(String s) { // FIXME needs a new nameresolver
        return s.toLowerCase(); // nr.getFieldNameInSource(s).toLowerCase();
    }

    /**
     * the postgres jdbc driver does not return sql states... we just let every state pass, but print the exception
     */
    @Override
    protected void checkState(SQLException e, String state) {
        System.out.println(e);
    }

    // TODO now in sqlEngines.properties -->OK?
    // /** returns org.makumba.db.sql.pgsql.RecordManager */
    // protected Class getTableClass()
    // { return org.makumba.db.sql.pgsql.RecordManager.class; }

    // Moved from pgsql.textManager
    @Override
    protected int getSQLType(FieldDefinition fd) {
        switch (fd.getIntegerType()) {
            case FieldDefinition._text:
                return Types.VARCHAR;
            default:
                return super.getSQLType(fd);
        }
    }

    // Moved from pgsql.textManager
    @Override
    public void setArgument(FieldDefinition fd, PreparedStatement ps, int n, Object o) throws SQLException {
        switch (fd.getIntegerType()) {
            case FieldDefinition._text:
                Text t = Text.getText(o);
                ps.setString(n, t.getString());
                break;
            default:
                super.setArgument(fd, ps, n, o);
        }
    }

    // Moved from pgsql.textManager
    /** returns Postgres TEXT */
    @Override
    protected String getFieldDBType(FieldDefinition fd) {
        switch (fd.getIntegerType()) {
            case FieldDefinition._text:
                return "TEXT";
            default:
                return super.getFieldDBType(fd);
        }

    }
}
