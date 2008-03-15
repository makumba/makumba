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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Logger;

import org.makumba.DataDefinition;
import org.makumba.Transaction;
import org.makumba.providers.DataDefinitionProvider;

/**
 * This class imports makumba objects from fields of Java objects. Imported classes have the opportunity to say what not
 * to import (field String[] noImport). They can also do post processing (method boolean importTransform(Object o)) and
 * decide (by the boolean result of importTransform) whether the object is written or not in the makumba database.
 * 
 * @author Cristian Bogdan
 */
public class ObjectToRecord {
    Method transform, clean;

    DataDefinition type;

    Hashtable fields = new Hashtable();

    public ObjectToRecord(Class c, String type) {
        try {
            Class args[] = { java.util.Hashtable.class, org.makumba.db.makumba.Database.class };
            try {
                transform = c.getMethod("importTransform", args);
            } catch (NoSuchMethodException nsme) {
            }

            try {
                clean = c.getMethod("importClean", args);
            } catch (NoSuchMethodException nsme) {
            }

            this.type = DataDefinitionProvider.getInstance().getDataDefinition(type);

            Field no = null;
            try {
                no = c.getField("noImport");
            } catch (java.lang.NoSuchFieldException nsfe) {
            }

            String[] noImp = {};
            if (no != null)
                noImp = (String[]) no.get(null);

            Object dummy = "dummy";
            Hashtable noImport = new Hashtable();

            for (int i = 0; i < noImp.length; i++)
                noImport.put(noImp[i], dummy);

            Field acc = null;
            try {
                acc = c.getField("accountedImport");
            } catch (java.lang.NoSuchFieldException nsfe) {
            }

            String[] accountedImp = {};
            if (acc != null)
                accountedImp = (String[]) acc.get(null);

            Hashtable accountedImport = new Hashtable();

            for (int i = 0; i < accountedImp.length; i++)
                accountedImport.put(accountedImp[i], dummy);

            Enumeration e = this.type.getFieldNames().elements();
            for (int i = 0; i < 3; i++)
                // skipping default fields
                e.nextElement();
            for (; e.hasMoreElements();) {
                String s = (String) e.nextElement();
                Field f = null;
                try {
                    f = c.getField(s);
                } catch (java.lang.NoSuchFieldException nsfe1) {
                }
                if (f != null) {
                    if (noImport.get(s) == null)
                        fields.put(s, f);
                } else if (accountedImport.get(s) == null)
                    java.util.logging.Logger.getLogger("org.makumba." + "import").severe(
                        "No Java correspondent for " + type + "." + s + " in " + c.getName());
            }
            Field flds[] = c.getFields();
            for (int i = 0; i < flds.length; i++) {
                try {
                    flds[i].get(null);
                } catch (NullPointerException npe) {
                    String s = flds[i].getName();

                    if (this.type.getFieldDefinition(s) == null && noImport.get(s) == null)
                        java.util.logging.Logger.getLogger("org.makumba." + "import").severe(
                            "No Makumba correspondent for " + c.getName() + "." + s + " in " + type);
                }
            }

        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException();
        }
    }

    boolean cleaned = false;

    public Hashtable importObject(Object o, org.makumba.db.makumba.Database db) {
        try {
            Hashtable h = new Hashtable();
            Object args[] = { h, db };

            h.put(type.getIndexPointerFieldName(), db.getPointer(type.getName(), o.hashCode()));

            for (Enumeration e = fields.keys(); e.hasMoreElements();) {
                String s = (String) e.nextElement();
                Object value = ((Field) fields.get(s)).get(o);

                if (value != null) {
                    if (!value.getClass().getName().startsWith("java"))
                        value = db.getPointer(type.getFieldDefinition(s).getForeignTable().getName(), value.hashCode());
                    h.put(s, value);
                }
            }

            if (!cleaned && clean != null && ((Boolean) clean.invoke(o, args)).booleanValue()) {
                db.deleteFrom(db.getName(), type.getName(), false);
                cleaned = true;
            }
            if (transform == null || ((Boolean) transform.invoke(o, args)).booleanValue()) {
                Transaction dbc = db.getDBConnection();
                try {
                    dbc.insert(type.getName(), h);
                } finally {
                    dbc.close();
                }
            }
            return h;
        } catch (InvocationTargetException ite) {
            ite.getTargetException().printStackTrace();
        } catch (org.makumba.InvalidValueException ive) {
            java.util.logging.Logger.getLogger("org.makumba." + "import").warning(ive.getMessage());
            return null;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        throw new RuntimeException();
    }

    /** import an integer from a hashtable */
    public static void importInteger(String java, String mdd, Hashtable hjava, Hashtable hmdd) {
        String s = (String) hjava.remove(java);
        if (s == null)
            return;
        Integer i = null;
        try {
            i = new Integer(Integer.parseInt(s.trim()));
        } catch (NumberFormatException nfe) {
            if (s.trim().length() > 0)
                Logger.getLogger("org.makumba." + "import").warning(s);
            return;
        }
        hmdd.put(mdd, i);
    }

    /** import an string from a hashtable */
    public static void importString(String java, String mdd, Hashtable hjava, Hashtable hmdd) {
        String s = (String) hjava.remove(java);
        if (s == null)
            return;
        hmdd.put(mdd, s.trim());
    }

    static Integer zero = new Integer(0);

    static Integer one = new Integer(1);

    /** import a boolean choice from a hashtable */
    public static void importBoolean(String java, String mdd, Hashtable hjava, Hashtable hmdd, String on) {
        String s = (String) hjava.remove(java);
        hmdd.put(mdd, zero);
        if (s == null)
            return;
        if (s.trim().equals(on))
            hmdd.put(mdd, one);
    }

}
