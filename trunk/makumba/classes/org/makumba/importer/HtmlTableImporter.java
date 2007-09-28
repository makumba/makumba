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

package org.makumba.importer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.Transaction;
import org.makumba.InvalidValueException;
import org.makumba.commons.Configuration;
import org.makumba.commons.HtmlTagEnumerator;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.TransactionProvider;

/**
 * Utility class making it possible to import data from a HTML table into a Makumba-based database.
 * This class was created in order to import data from a Lotus Notes database, since there was no other
 * mean to extract data than this one.
 * 
 * @author Cristian Bogdan
 * @version $Id$
 */
public class HtmlTableImporter {
    ObjectImporter imp;

    boolean inRow = false;

    boolean inCell = false;

    String text;

    Vector data;

    String fieldOrder[];

    String type;

    Transaction db;

    void endOfCell() {
        if (inCell)
            data.addElement(text);
        text = null;
    }

    void endOfRow() {
        endOfCell();
        if (data != null && !data.isEmpty())
            if (data.size() != fieldOrder.length)
                java.util.logging.Logger.getLogger("org.makumba." + "import").severe(
                    type + ": invalid HTML table row length: " + data.size() + "\r\nin: " + data);
            else
                try {
                    db.insert(type, importVector());
                } catch (InvalidValueException e) {
                    java.util.logging.Logger.getLogger("org.makumba." + "import").warning("record not inserted --> " + e.getMessage());
                }
    }

    public HtmlTableImporter(Transaction db, DataDefinition type, Reader r, String tableStartTag, String[] fieldOrder)
            throws IOException {
        this.imp = new ObjectImporter(type, true);
        this.fieldOrder = fieldOrder;
        this.type = type.getName();
        this.db = db;
        String[] tables = { type.getName() };
        HtmlTableImporter._delete(db.getName(), db.getName(), tables);
        HtmlTagEnumerator e = new HtmlTagEnumerator(r);
        while (e.next() && !e.getTag().equals(tableStartTag))
            ;

        String s;

        while (e.next()) {
            if (e.getTagType().toLowerCase().equals("tr")) {
                endOfRow();
                inRow = true;
                inCell = false;
                data = new Vector();
            } else if (inRow && e.getTagType().toLowerCase().equals("td")) {
                endOfCell();
                inCell = true;
            } else if (inCell && (s = e.getNonHtml()) != null && s.length() > 0)
                text = s;
            if (e.getTagType().toLowerCase().equals("/table")) {
                endOfRow();
                java.util.logging.Logger.getLogger("org.makumba." + "import").severe("end of table encountered");
                return;
            }
        }
        java.util.logging.Logger.getLogger("org.makumba." + "import").severe("end of table missing");
    }

    Dictionary importVector() {
        Dictionary d = new Hashtable();
        Vector v1 = new Vector();

        for (int i = 0; i < fieldOrder.length; i++)
            if (data.elementAt(i) != null) {
                Object o = imp.getValue(fieldOrder[i], (String) data.elementAt(i), db, null);
                if (o != null)
                    d.put(fieldOrder[i], o);
                v1.addElement(o);
            }
        java.util.logging.Logger.getLogger("org.makumba." + "import").finest(v1.toString());

        return d;
    }

    /**
     * Deletes the records of certain types. Useful for failed imports or copies. The database configuration must have
     * admin# confirmations that match each of the indicated types. Use _delete(d, d, ...) for databases that need
     * re-import of data of certain types. Deletion is logged (see {@link java.util.logging.Logger},
     * {@link org.makumba.MakumbaSystem#setLoggingRoot(java.lang.String)}) in the <b><code>"db.admin.delete"</code></b>
     * logger, with {@link java.util.logging.Level#INFO} logging level.
     */
    public static void _delete(String whereDB, String provenienceDB, String[] typeNames, boolean ignoreDbsv) {
        Configuration config = new Configuration();
        (new TransactionProvider(config))._delete(whereDB, provenienceDB, typeNames, ignoreDbsv);
    }

    /**
     * Deletes the records of certain types that originate from a certain database. Useful for failed imports or copies.
     * The database configuration must have admin# confirmations that match each of the indicated types. Use _delete(d,
     * d, ...) for databases that need re-import of data of certain types. Deletion is logged (see
     * {@link java.util.logging.Logger}, {@link org.makumba.MakumbaSystem#setLoggingRoot(java.lang.String)}) in the
     * <b><code>"db.admin.delete"</code></b> logger, with {@link java.util.logging.Level#INFO} logging level.
     */
    public static void _delete(String whereDB, String provenienceDB, String[] typeNames) {
        _delete(whereDB, provenienceDB, typeNames, false);
    }

    public static void main(String[] argv) throws IOException {
        String[] args = new String[argv.length - 4];
        System.arraycopy(argv, 4, args, 0, args.length);
        
        Configuration config = new Configuration();
        
        TransactionProvider tp = new TransactionProvider(config);

        new HtmlTableImporter(tp.getConnectionTo(argv[0]), (new DataDefinitionProvider(new Configuration())).getDataDefinition(argv[1]),
                new BufferedReader(new InputStreamReader(new FileInputStream(argv[2]))), argv[3], args);
    }
}
