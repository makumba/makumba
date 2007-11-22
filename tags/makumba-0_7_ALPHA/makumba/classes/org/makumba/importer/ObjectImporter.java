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

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.Transaction;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaError;
import org.makumba.MakumbaSystem;
import org.makumba.Pointer;
import org.makumba.providers.DataDefinitionProvider;

/**
 * This class imports makumba records from text files based on markers placed in special configuration files with the
 * "mark" extension from the CLASSPATH
 * 
 * @author Cristian Bogdan
 */
public class ObjectImporter {
    Properties markers = new Properties();

    protected DataDefinition dd;

    // moved from FieldImporter
    protected boolean ignored, ignoreNotFound;

    protected String begin, end;

    MakumbaError configError;

    Properties replaceFile;

    boolean canError = true;

    boolean noWarning = false;

    String nothing;

    // moved from dateImporter
    Vector formats = new Vector();

    // moved from ptrImporter
    String joinField;

    String select;

    int index = -1;

    int nchar = -1;

    public String getMarker(String s) {
        return markers.getProperty(s);
    }

    boolean noMarkers = false;

    public ObjectImporter(DataDefinition type) {
        this(type, false);
    }

    public ObjectImporter(DataDefinition type, boolean noMarkers) {
        this.dd = type;

        this.noMarkers = noMarkers;

        String nm = type.getName().replace('.', '/') + ".mark";
        java.net.URL u = null;
        try {
            markers.load((u = org.makumba.commons.ClassResource.get("dataDefinitions/" + nm)).openStream());
        } catch (Exception e) {
            try {
                markers.load((u = org.makumba.commons.ClassResource.get(nm)).openStream());
            } catch (Exception f) {
                throw new MakumbaError(f);
            }
        }
        for (Enumeration e = dd.getFieldNames().elements(); e.hasMoreElements();) {
            String fieldName = (String) e.nextElement();
            configureField(fieldName, markers);
        }
        Vector notMarked = new Vector();
        for (Enumeration e = dd.getFieldNames().elements(); e.hasMoreElements();) {
            String fieldName = (String) e.nextElement();
            if (!isMarked(fieldName) && !isIgnored(fieldName))
                notMarked.addElement(fieldName);
        }
        if (notMarked.size() > 0)
            java.util.logging.Logger.getLogger("org.makumba." + "import").warning(
                "marker file "
                        + u
                        + " does not contain markers for:\n "
                        + notMarked
                        + "\nUse \"<fieldname>.ignore=true\" in the marked file if you are shure you don't want the field to be imported");

        boolean hasErrors = false;
        for (Enumeration e = dd.getFieldNames().elements(); e.hasMoreElements();) {
            String fieldName = (String) e.nextElement();
            if (configError != null && !isIgnored(fieldName)) {
                if (!hasErrors) {
                    hasErrors = true;
                    java.util.logging.Logger.getLogger("org.makumba." + "import").warning(
                        "marker file " + u + " contains errors. Erroneous fields will be ignored.");
                }
                ignored = true;
                java.util.logging.Logger.getLogger("org.makumba." + "import").severe(configError.toString());
            }
        }
    }

    Object getValue(String fieldName, String s, Transaction db, Pointer[] indexes) {
        if (isIgnored(fieldName))
            return null;
        return getFieldValue(fieldName, replaceField(fieldName, s), db, indexes);
    }

    protected boolean isMarked(String fieldName) {
        if (isFieldMarked(fieldName))
            return true;
        if (noMarkers) {
            String s = begin;
            begin = "x";
            boolean b = isFieldMarked(fieldName);
            begin = s;
            return b;
        }
        return false;
    }

    protected boolean usesHidden() {
        return true;
    }

    /**
     * import data from a text. indexes contains the pointers to other records imported from the same text, at the same
     * time
     */
    public Dictionary importFrom(String s, Transaction db, Pointer[] indexes) {

        Hashtable ht = new Hashtable();

        for (Enumeration e = dd.getFieldNames().elements(); e.hasMoreElements();) {
            String fieldName = (String) e.nextElement();
            this.importFieldTo(fieldName, ht, s, db, indexes);
        }
        return (Dictionary) ht;
    }

    /** imports all files from a directory */
    public static void main(String argv[]) throws Throwable {
        ObjectImporter ri = new ObjectImporter(new DataDefinitionProvider().getDataDefinition(argv[0]));
        File dir = new File(argv[1]);
        String[] lst = dir.list();
        char buffer[] = new char[8196];
        for (int i = 0; i < lst.length; i++) {
            java.util.logging.Logger.getLogger("org.makumba." + "import").finest(lst[i]);
            Reader r = new FileReader(new File(dir, lst[i]));
            StringWriter sw = new StringWriter();
            int n;
            while ((n = r.read(buffer)) != -1)
                sw.write(buffer, 0, n);
            String content = sw.toString().toString();
            java.util.logging.Logger.getLogger("org.makumba." + "import").finest(ri.importFrom(content, null, null).toString());
        }
    }

    // moved from FieldImporter
    public Object getFieldValue(String fieldName, String s, Transaction db, Pointer[] indexes) {
        switch (dd.getFieldDefinition(fieldName).getIntegerType()) {
            case FieldDefinition._ptr:
                return get_ptr_FieldValue(fieldName, s, db, indexes);
            default:
                return base_getFieldValue(fieldName, s, db, indexes);
        }
    }

    // original getFieldValue from FieldImporter
    public Object base_getFieldValue(String fieldName, String s, Transaction db, Pointer[] indexes) {
        return getFieldValue(fieldName, s);
    }

    // moved from ptrImporter
    public Object get_ptr_FieldValue(String fieldName, String s, Transaction db, Pointer[] indexes) {
        if (index != -1)
            return indexes[index];
        if (s.length() == 0)
            return null;
        String arg = s;
        if (select != null) {
            Vector v = db.executeQuery(select, arg);
            if (v.size() > 1) {
                warningField(fieldName, "too many join results for \"" + s + "\": " + v);
                return null;
            }

            if (v.size() == 1)
                return (Pointer) ((Dictionary) v.elementAt(0)).get("col1");

            warningField(fieldName, "no join results for \"" + s + "\"");
            return null;
        }
        String query = null;

        query = "SELECT p, p." + joinField + " FROM " + dd.getFieldDefinition(fieldName).getForeignTable().getName()
                + " p WHERE p." + joinField + "=$1";

        Vector v = db.executeQuery(query, arg);

        if (v.size() > 1) {
            warningField(fieldName, "too many join results for \"" + s + "\": " + v);
            return null;
        }

        if (v.size() == 1)
            return (Pointer) ((Dictionary) v.elementAt(0)).get("col1");

        if (nchar == -1) {
            warningField(fieldName, "no join results for \"" + s + "\"");
            return null;
        }

        query = "SELECT p, p." + joinField + " FROM " + dd.getFieldDefinition(fieldName).getForeignTable().getName()
                + " p WHERE p." + joinField + " like $1";
        if (s.length() < nchar)
            arg = s;
        else
            arg = s.substring(0, nchar) + "%";

        v = db.executeQuery(query, arg);

        if (v.size() > 1) {
            warningField(fieldName, "too many join results for \"" + s + "\": " + v);
            return null;
        }
        if (v.size() == 0) {
            warningField(fieldName, "no join results for \"" + s + "\"");
            return null;
        }
        return (Pointer) ((Dictionary) v.elementAt(0)).get("col1");
    }

    // moved from FieldImporter
    public Object getFieldValue(String fieldName, String s) {
        switch (dd.getFieldDefinition(fieldName).getIntegerType()) {
            case FieldDefinition._date:
            case FieldDefinition._dateCreate:
            case FieldDefinition._dateModify:
                return get_date_FieldValue(fieldName, s);
            case FieldDefinition._int:
                return get_int_FieldValue(fieldName, s);
            case FieldDefinition._intEnum:
                return get_intEnum_FieldValue(fieldName, s);
            default:
                return base_getFieldValue(fieldName, s);
        }
    }

    // original from FieldImporter
    public Object base_getFieldValue(String fieldName, String s) {
        return s;
    }

    // moved from dateImporter
    public Object get_date_FieldValue(String fieldName, String s) {
        if (s.trim().length() == 0)
            return null;
        ParseException lastpe = null;

        for (Enumeration e = formats.elements(); e.hasMoreElements();) {
            SimpleDateFormat f = (SimpleDateFormat) e.nextElement();
            try {

                return f.parse(s);
            } catch (ParseException pe) {
                lastpe = pe;
            }
        }
        warningField(fieldName, lastpe);
        return null;
    }

    // moved from intEnumImporter
    public Object get_intEnum_FieldValue(String fieldName, String s) {
        s = (String) base_getFieldValue(fieldName, s);
        if (s.trim().length() == 0)
            return null;
        Enumeration f = dd.getFieldDefinition(fieldName).getValues();
        for (Enumeration e = dd.getFieldDefinition(fieldName).getNames(); e.hasMoreElements();) {
            String v = (String) e.nextElement();
            Integer i = (Integer) f.nextElement();
            if (v.equals(s))
                return i;
        }
        warningField(fieldName, "illegal value: \"" + s + "\"");
        return null;
    }

    // moved from intImporter
    public Object get_int_FieldValue(String fieldName, String s) {
        s = (String) base_getFieldValue(fieldName, s);
        if (s.trim().length() == 0)
            return null;
        try {
            return new Integer(Integer.parseInt((String) base_getFieldValue(fieldName, s)));
        } catch (Exception e) {
            warningField(fieldName, e);
            return null;
        }
    }

    // moved from FieldImporter
    public boolean isIgnored(String fieldName) {
        if (dd.getFieldDefinition(fieldName).getType().startsWith("set")) {
            return true;
        } else {
            switch (dd.getFieldDefinition(fieldName).getIntegerType()) {
                case FieldDefinition._ptrIndex:
                case FieldDefinition._ptrOne:
                case FieldDefinition._ptrRel:
                    return true;
                default:
                    return ignored;
            }
        }
    }

    // moved from FieldImporter
    public boolean isFieldMarked(String fieldName) {
        switch (dd.getFieldDefinition(fieldName).getIntegerType()) {
            case FieldDefinition._ptr:
                return joinField != null || index != -1 || select != null;
            default:
                return begin != null;
        }
    }

    // Moved from FieldImporter
    String replaceField(String fieldName, String val) {
        if (val != null) {
            String transf = getFieldMarker(fieldName, "replace." + val);
            if (transf != null) {
                val = transf;
            } else {
                String val1 = val.replace(' ', '_').replace('=', '_');
                transf = getFieldMarker(fieldName, "replace." + val1);
                if (transf != null)
                    val = transf;
                else if (replaceFile != null) {
                    transf = replaceFile.getProperty(val);
                    if (transf != null)
                        val = transf;
                    else {
                        transf = replaceFile.getProperty(val1);
                        if (transf != null)
                            val = transf;
                    }
                }
            }
        }
        return val;
    }

    // moved from FieldImporter
    String getFieldMarker(String fieldName, String m) {
        String s = markers.getProperty(fieldName + "." + m);
        if (s != null)
            return s.trim();
        return null;
    }

    // moved from FieldImporter
    public String canonicalFieldName(String fieldName) {
        return dd.getName() + "#" + fieldName;
    }

    // moved from FieldImporter
    void warningField(String fieldName, String s) {
        String err = canonicalFieldName(fieldName) + " " + s;
        if (canError)
            java.util.logging.Logger.getLogger("org.makumba." + "import").warning(err);
        else
            throw new MakumbaError(err);
    }

    // moved from FieldImporter
    void warningField(String fieldName, Throwable t) {
        String err = canonicalFieldName(fieldName);
        if (canError)
            java.util.logging.Logger.getLogger("org.makumba." + "import").warning(err + " " + t.toString());
        else
            throw new MakumbaError(t, err);
    }

    // moved from FieldImporter
    MakumbaError makeFieldError(String fieldName, String s) {
        return new MakumbaError(canonicalFieldName(fieldName) + " " + s);
    }

    // moved from FieldImporter
    MakumbaError makeFieldError(String fieldName, Throwable t) {
        return new MakumbaError(t, canonicalFieldName(fieldName));
    }

    // moved from FieldImporter
    static String[][] htmlEscape = { { "&quot;", "&amp;", "<br>" }, { "\"", "&", "\n" } };

    // moved from FieldImporter
    static String escapeField(String fieldName, String s) {
        if (s == null)
            return null;

        StringBuffer sb = new StringBuffer();
        chars: for (int i = 0; i < s.length(); i++) {
            for (int j = 0; j < htmlEscape[0].length; j++)
                if (s.length() - i >= htmlEscape[0][j].length()
                        && s.substring(i, i + htmlEscape[0][j].length()).toLowerCase().equals(htmlEscape[0][j])) {
                    sb.append(htmlEscape[1][j]);
                    i += htmlEscape[0][j].length() - 1;
                    continue chars;
                }
            sb.append(s.charAt(i));
        }
        return sb.toString();
    }

    // moved from FieldImporter
    public boolean shouldEscapeField(String fieldName) {
        switch (dd.getFieldDefinition(fieldName).getIntegerType()) {
            case FieldDefinition._int:
            case FieldDefinition._text:
                return false;
            default:
                return true;
        }
    }

    // moved from FieldImporter
    public boolean shouldDecomposeURL(String fieldName) {
        return dd.getFieldDefinition(fieldName).getType().equals("char");
    }

    // moved from FieldImporter
    static String decomposeURL(String fieldName, String s) {
        if (s == null)
            return null;
        if (!s.startsWith("<a"))
            return s;
        int n = s.indexOf('\"');
        if (n == -1 || s.length() == n + 1)
            return s;
        int n1 = s.indexOf('\"', n + 1);
        if (n1 == -1 || s.length() == n1 + 1)
            return s;
        String s1 = s.substring(n + 1, n1);
        n = s.indexOf(">");
        if (n == -1 || s.length() == n + 1)
            return s;
        n1 = s.indexOf("</a>");
        if (n1 == -1)
            return s;
        try {
            if (!s1.equals(s.substring(n + 1, n1)))
                return s;
        } catch (StringIndexOutOfBoundsException aio) {
            java.util.logging.Logger.getLogger("org.makumba." + "import").severe("EEEE " + s + " " + s1);
            return s;
        }
        if (!s1.startsWith("http"))
            s1 = "http://" + s1;
        return s1;
    }

    // moved from FieldImporter
    public void importFieldTo(String fieldName, Dictionary d, String s, Transaction db, Pointer[] indexes) {
        try {
            if (isIgnored(fieldName) || !isFieldMarked(fieldName))
                return;

            String val = null;
            if (begin != null) {
                int beg = s.indexOf(begin);

                if (beg != -1) {
                    beg += begin.length();
                    try {
                        val = s.substring(beg, s.indexOf(end, beg));
                        if (noWarning)
                            warningField(fieldName, " found value for unfrequent field: " + val);
                    } catch (Exception e) {
                        warningField(fieldName, "no end found");
                        return;
                    }
                } else if (!ignoreNotFound && !noWarning)
                    warningField(fieldName, "begin not found");
            }
            Object o = null;
            if (shouldEscapeField(fieldName))
                val = escapeField(fieldName, val);
            val = replaceField(fieldName, val);
            if (shouldDecomposeURL(fieldName))
                val = decomposeURL(fieldName, val);

            if (begin == null || val != null)
                o = getValue(fieldName, val, db, indexes);

            if (o != null)
                if (nothing != null && o.equals(getFieldValue(fieldName, nothing))) {
                    return;
                } else
                    d.put(fieldName, o);
        } catch (RuntimeException e) {
            throw makeFieldError(fieldName, e);
        }
    }

    public void configureField(String fieldName, Properties markers) {
        if (dd.getFieldDefinition(fieldName).getType().startsWith("set")) {
            configure_none_Field(fieldName, markers);
        } else {
            switch (dd.getFieldDefinition(fieldName).getIntegerType()) {
                case FieldDefinition._ptr:
                    configure_ptr_Field(fieldName, markers);
                    break;
                case FieldDefinition._date:
                case FieldDefinition._dateCreate:
                case FieldDefinition._dateModify:
                    configure_date_Field(fieldName, markers);
                    break;
                case FieldDefinition._ptrIndex:
                case FieldDefinition._ptrOne:
                case FieldDefinition._ptrRel:
                    configure_none_Field(fieldName, markers);
                    break;
                default:
                    base_configureField(fieldName, markers);
            }
        }
    }

    // original configureField from FieldImporter
    public void base_configureField(String fieldName, Properties markers) {
        this.markers = markers;
        String s = getFieldMarker(fieldName, "ignore");
        ignored = s != null && s.equals("true");
        if (ignored)
            return;

        s = getFieldMarker(fieldName, "ignoreNotFound");
        ignoreNotFound = s != null && s.equals("true");

        begin = getFieldMarker(fieldName, "begin");
        nothing = getFieldMarker(fieldName, "nothing");
        String cf = getFieldMarker(fieldName, "replaceFile");
        if (cf != null) {
            replaceFile = new Properties();
            try {
                replaceFile.load(new java.io.FileInputStream(cf));
            } catch (java.io.IOException e) {
                configError = new MakumbaError(e);
            }
        }
        // canError=getMarker("canError")!=null;
        noWarning = getFieldMarker(fieldName, "noWarning") != null;
        end = getFieldMarker(fieldName, "end");
        if (end == null)
            end = markers.getProperty("end");
    }

    // moved from dateImporter
    public void configure_date_Field(String fieldName, Properties markers) {
        base_configureField(fieldName, markers);
        for (Enumeration e = markers.keys(); e.hasMoreElements();) {
            String s = (String) e.nextElement();
            if (s.startsWith(fieldName + ".format")) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(markers.getProperty(s).trim(),
                        MakumbaSystem.getLocale());
                dateFormat.setTimeZone(MakumbaSystem.getTimeZone());
                dateFormat.setLenient(false);
                formats.addElement(dateFormat);
            }
        }
        if (formats.size() == 0)
            configError = makeFieldError(
                fieldName,
                "has no format indicated. Use \""
                        + fieldName
                        + ".format=MM yy dd\" in the marker file.\nSee the class java.text.SimpleDateFormat to see how to compose the formatter");
    }

    // moved from noneImporter
    public void configure_none_Field(String fieldName, Properties markers) {
        base_configureField(fieldName, markers);
        if (begin != null)
            throw new MakumbaError("You cannot have markers for fields of type "
                    + dd.getFieldDefinition(fieldName).getType());
    }

    // moved from ptrImporter
    public void configure_ptr_Field(String fieldName, Properties markers) {
        base_configureField(fieldName, markers);
        if (ignored)
            return;

        joinField = getFieldMarker(fieldName, "joinField");
        select = getFieldMarker(fieldName, "select");
        try {
            index = Integer.parseInt(getFieldMarker(fieldName, "index"));
        } catch (RuntimeException e) {
        }
        if (index != -1)
            if (begin != null || joinField != null || select != null)
                configError = makeFieldError(fieldName,
                    "if pointer index is indicated, begin, end or joinfield are not needed");
            else
                ;
        else if (joinField != null) {
            if (index != -1 || select != null)
                configError = makeFieldError(fieldName,
                    "if join field is indicated, begin and end are needed, index not");
            String s = getFieldMarker(fieldName, "joinChars");
            if (s != null)
                nchar = Integer.parseInt(s);
        } else if (select != null) {
            if (index != -1 || joinField != null)
                configError = makeFieldError(fieldName, "if select is indicated, begin and end are needed, index not");
        } else
            configError = makeFieldError(fieldName, "join field or pointer index must be indicated for pointers");
    }

}
