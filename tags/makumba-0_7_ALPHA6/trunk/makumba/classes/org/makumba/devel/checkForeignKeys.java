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
//  $Id: open.java 2631 2008-06-17 23:01:21Z rosso_nero $
//  $Name$
/////////////////////////////////////

package org.makumba.devel;

import java.util.ArrayList;
import java.util.Vector;

import org.makumba.DBError;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaSystem;
import org.makumba.db.makumba.DBConnection;
import org.makumba.db.makumba.DBConnectionWrapper;
import org.makumba.db.makumba.Database;
import org.makumba.db.makumba.MakumbaTransactionProvider;
import org.makumba.db.makumba.sql.SQLDBConnection;
import org.makumba.db.makumba.sql.TableManager;

/**
 * Opens given database tables; if allowed to, this would also trigger alter commands.
 * <p>
 * Usage: <code>java org.makumba.devel.open [source [type1 [type2 ...] ] ]</code>
 * </p>
 * If no source is specified the default database specified in <i>MakumbaDatabase.properties</i> is used. If not types
 * are specified, all MDDs found in the webapp are processed.
 * 
 * @author Cristian Bogdan
 */
public class checkForeignKeys {
    public static ArrayList<Throwable> errors = new ArrayList<Throwable>();

    public static void main(String[] argv) {
        Database d = null;
        try {
            if (argv.length == 0) {
                d = MakumbaTransactionProvider.findDatabase("MakumbaDatabase.properties");
            } else {
                d = MakumbaTransactionProvider.getDatabase(argv[0]);
            }
            DBConnection connection = d.getDBConnection();
            if (connection instanceof DBConnectionWrapper) {
                connection = ((DBConnectionWrapper) connection).getWrapped();
            }
            SQLDBConnection sqlConnection = ((SQLDBConnection) connection);

            Vector<String> mddsInDirectory = org.makumba.MakumbaSystem.mddsInDirectory("dataDefinitions");
//            Vector<String> mddsInDirectory = org.makumba.MakumbaSystem.mddsInDirectory("");
            
            Vector<String> clean = (Vector<String>) mddsInDirectory.clone();
            for (int i = 0; i < mddsInDirectory.size(); i++) {
                String element = (String) mddsInDirectory.get(i);
                if (element.contains("broken") || element.contains("dataDefinitions")) {
                    clean.remove(element);
                }
            }
            mddsInDirectory = clean;

            String[] tables = mddsInDirectory.toArray(new String[mddsInDirectory.size()]);
            for (int i = 0; i < tables.length; i++) {
                System.out.println("\n**** Checking foreign keys of MDD '" + tables[i] + "', #" + (i + 1) + " of "
                        + tables.length + "\n");
                DataDefinition dd = MakumbaSystem.getDataDefinition(tables[i]);
                try {
                    TableManager table = (TableManager) d.getTable(dd.getName());
                    processDataDefinition(d, sqlConnection, dd, table);
                } catch (Throwable t) {
                    System.out.println(t.getMessage());
                    t.printStackTrace();
                    errors.add(t);
                }
            }
        } finally {
            if (d != null) {
                d.close();
            }
        }
        System.out.println("\n\n\n=====================================================================");
        System.out.println("The following " + errors.size() + " errors occured:");
        for (Throwable e : errors) {
            System.out.println("\n" + e.getMessage() + "\n");
        }
    }

    private static void processDataDefinition(Database d, SQLDBConnection sqlConnection, DataDefinition dd,
            TableManager table) throws DBError {
        for (String string : dd.getFieldNames()) {
            FieldDefinition fi = dd.getFieldDefinition(string);
            String fieldName = fi.getName();
            String brief = dd.getName() + "#" + fieldName + " (" + fi.getDescription() + ")";
            if (fi.getType().startsWith("set") || fi.getType().equals("ptrOne")) {
                TableManager subTable = (TableManager) d.getTable(fi.getSubtable().getName());
                processDataDefinition(d, sqlConnection, fi.getSubtable(), subTable);
            } else {
                try {
                    table.manageForeignKeys(fieldName, sqlConnection, brief);
                } catch (Throwable t) {
                    System.out.println(t.getMessage());
                    t.printStackTrace();
                    errors.add(t);
                }
            }
        }
    }
}
