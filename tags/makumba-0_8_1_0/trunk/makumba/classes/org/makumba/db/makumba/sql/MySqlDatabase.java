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

import java.sql.SQLException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/** the database adapter for MySQL Server */
public class MySqlDatabase extends org.makumba.db.makumba.sql.Database {

    public static final String regExpForeignKeyError = ".*\\(`(.*)`, CONSTRAINT `(.*)` FOREIGN KEY \\(`(.*)`\\) REFERENCES `(.*)` \\(`(.*)`\\)\\)";

    public static final Pattern patternForeignKeyError = Pattern.compile(regExpForeignKeyError);

    public MySqlDatabase(Properties p) {
        super(p);
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

    public static String getMddName(String referingTableName) {
        return StringUtils.removeEnd(referingTableName, "_").replaceAll("__", "->").replaceAll("_", ".");
    }

}
