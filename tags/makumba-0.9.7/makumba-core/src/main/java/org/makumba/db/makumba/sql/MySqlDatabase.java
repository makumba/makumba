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
//  $Id: MsSqlDatabase.java 2606 2008-06-15 15:07:00Z rosso_nero $
//  $Name$
/////////////////////////////////////

package org.makumba.db.makumba.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;

/** the database adapter for MySQL Server */
public class MySqlDatabase extends org.makumba.db.makumba.sql.Database {

    public static final String regExpForeignKeyError = ".*\\(`(.*)`, CONSTRAINT `(.*)` FOREIGN KEY \\(`(.*)`\\) REFERENCES `(.*)` \\(`(.*)`\\)\\)";

    public static final Pattern patternForeignKeyError = Pattern.compile(regExpForeignKeyError);

    public MySqlDatabase(Properties p) {
        super(p);
    }

    @Override
    protected String createDbSpecific(String command) {
        return command + " ENGINE=InnoDB";
    }

    @Override
    public String parseReadableForeignKeyErrorMessage(SQLException se) {
        // MySQL foreign key errors are as follows:
        //
        // Foreign Key exception. Cannot delete or update a parent row: a foreign key constraint fails
        // (`makumba/test_Person__address__languages_`, CONSTRAINT `test_Person__address__languages__ibfk_1` FOREIGN KEY
        // (`address_`) REFERENCES `test_Person__address_` (`address_`))
        String msg = se.getMessage();
        Matcher matcher = patternForeignKeyError.matcher(msg.trim());
        if (matcher.matches()) {
            try {
                String group = matcher.group(1);
                String referingTableName = getMddName(group.substring(group.indexOf("/") + 1));
                String referedTableName = getMddName(matcher.group(4));
                return "Trying to delete an entry from " + referedTableName + ", while an entry " + referingTableName
                        + " still refers to it. Try to invert the order of deletion.";
            } catch (Exception e) {
                e.printStackTrace();
                return se.getMessage();
            }
        } else {
            return se.getMessage();
        }
    }

    @Override
    public Map<String, String> getDuplicateFields(SQLException e) {
        // we get an error message of the kind
        // Duplicate entry '11-a@b.com' for key 'age_email'

        Map<String, String> res = new HashMap<String, String>();

        String[] error = e.getMessage().split("'");
        String[] values = error[1].split("-");
        String[] fields = error[3].split("_");

        for (int i = 0; i < fields.length; i++) {
            res.put(fields[i], values[i]);
        }

        return res;
    }

    public static String getMddName(String referingTableName) {
        return StringUtils.removeEnd(referingTableName, "_").replaceAll("__", "->").replaceAll("_", ".");
    }

    @Override
    protected int getSQLType(FieldDefinition fd) {
        switch (fd.getIntegerType()) {
            case FieldDefinition._text:
                return -1;
            default:
                return super.getSQLType(fd);
        }
    }

    @Override
    protected String getQueryAutoIncrementSyntax() {
        return "SELECT LAST_INSERT_ID()";
    }

    @Override
    protected String getCreateAutoIncrementSyntax() {
        return "AUTO_INCREMENT PRIMARY KEY";
    }

    /** mysql needs to have it adjustable, depending on version (see bug 512) */
    @Override
    protected String getTableMissingStateName(SQLDBConnection dbc) {
        try {
            // version:"3.0.5-gamma" has major:3, minor:0
            String version = dbc.getMetaData().getDriverVersion();
            int major = dbc.getMetaData().getDriverMajorVersion();
            int minor = dbc.getMetaData().getDriverMinorVersion();
            String mark = "" + major + "." + minor + ".";
            String minorStr = version.substring(version.indexOf(mark) + mark.length());
            if (minorStr.indexOf('-') == -1) {
                minorStr = minorStr.substring(0, minorStr.indexOf(' '));
            } else {
                minorStr = minorStr.substring(0, minorStr.indexOf('-'));
            }
            int minor2 = Integer.parseInt(minorStr);

            if (major > 3 || major == 3 && minor > 0 || major == 3 && minor == 0 && minor2 >= 8) {
                return "tableMissing";
            } else {
                return "tableMissing-before308";
            }
        } catch (Exception e) {
            // e.printStackTrace();
            return "tableMissing";
        }
    }

    /** checks if an alteration is needed, and calls doAlter if so */
    @Override
    protected void onAlter(DataDefinition dd, org.makumba.db.makumba.sql.SQLDBConnection dbc, boolean alter)
            throws SQLException {
        Statement st = dbc.createStatement();
        ResultSet rs = st.executeQuery("SHOW CREATE TABLE " + getDBName(dd));
        rs.next();
        String def = rs.getString(2).trim();
        if (def.lastIndexOf(')') > def.lastIndexOf(" TYPE=InnoDB")
                && def.lastIndexOf(')') > def.lastIndexOf(" ENGINE=InnoDB")) {
            String s = "ALTER TABLE " + getDBName(dd) + " ENGINE=InnoDB";
            if (alter) {
                java.util.logging.Logger.getLogger("org.makumba.db.init.tablechecking").info(getName() + ": " + s);
                st.executeUpdate(s);
            } else {
                java.util.logging.Logger.getLogger("org.makumba.db.init.tablechecking").warning("should alter: " + s);
            }

        }
        rs.close();
        st.close();
    }

}
