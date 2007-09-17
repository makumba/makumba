// /////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003 http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
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

package org.makumba.providers.datadefinition.makumba;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.makumba.DataDefinition;
import org.makumba.DataDefinitionParseError;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaError;
import org.makumba.MakumbaSystem;
import org.makumba.ValidationDefinitionParseError;
import org.makumba.ValidationRule;
import org.makumba.controller.validation.BasicValidationRule;
import org.makumba.controller.validation.ComparisonValidationRule;
import org.makumba.controller.validation.NumberRangeValidationRule;
import org.makumba.controller.validation.RegExpValidationRule;
import org.makumba.controller.validation.StringLengthValidationRule;
import org.makumba.util.ClassResource;
import org.makumba.util.RegExpUtils;
import org.makumba.util.ReservedKeywords;

public class RecordParser {
    public static final String FILE_EXTENSION_VALIDATION = ".vd";

    public static final String multiUniqueRegExpElement = RegExpUtils.LineWhitespaces + "(" + RegExpUtils.fieldName
            + ")" + RegExpUtils.LineWhitespaces;

    public static final String multiUniqueRegExpElementRepeatment = "(?:" + RegExpUtils.LineWhitespaces + "," + "(?:"
            + multiUniqueRegExpElement + "))*";

    public static final String multiUniqueRegExp = "!unique" + RegExpUtils.LineWhitespaces + "\\((?:"
            + multiUniqueRegExpElement + ")" + multiUniqueRegExpElementRepeatment + RegExpUtils.LineWhitespaces + "\\)";

    public static final Pattern multiUniquePattern = Pattern.compile(multiUniqueRegExp);

    OrderedProperties text;

    OrderedProperties fields = new OrderedProperties();

    OrderedProperties subfields = new OrderedProperties();

    DataDefinitionParseError mpe;

    Properties definedTypes;

    DataDefinition dd;

    // moved from ptrOneParser
    HashMap ptrOne_RecordParsers = new HashMap();

    // moved from setParser
    HashMap setParser_settbls = new HashMap();

    // moved from subtableParser
    HashMap subtableParser_subtables = new HashMap();

    HashMap subtableParser_here = new HashMap();

    RecordParser() {
        definedTypes = new Properties();
    }

    // for parsing of subtalbes
    RecordParser(RecordInfo dd, RecordParser rp) {
        this.dd = dd;
        text = new OrderedProperties();
        definedTypes = rp.definedTypes;
        mpe = rp.mpe;
    }

    void parse(RecordInfo dd) {
        this.dd = dd;
        text = new OrderedProperties();
        mpe = new DataDefinitionParseError();

        try {
            read(text, dd.origin);
        } catch (IOException e) {
            throw fail(e);
        }
        try {
            // make the default pointers resulted from the table name
            dd.addStandardFields(dd.name.substring(dd.name.lastIndexOf('.') + 1));
            parse();
        } catch (RuntimeException e) {
            throw new MakumbaError(e, "Internal error in parser while parsing " + dd.getName());
        }
        if (!mpe.isSingle() && !(dd.getParentField() != null))
            throw mpe;
    }

    DataDefinition parse(java.net.URL u, String path) {
        dd = new RecordInfo(u, path);
        parse((RecordInfo) dd);
        return dd;
    }

    RecordInfo parse(String txt) {
        dd = new RecordInfo();
        text = new OrderedProperties();
        mpe = new DataDefinitionParseError();

        try {
            read(text, txt);
        } catch (IOException e) {
            throw fail(e);
        }
        try {
            // make the default pointers resulted from the table name
            ((RecordInfo) dd).addStandardFields(dd.getName().substring(dd.getName().lastIndexOf('.') + 1));
            parse();
        } catch (RuntimeException e) {
            throw new MakumbaError(e, "Internal error in parser while parsing " + dd.getName());
        }
        if (!mpe.isSingle() && dd.getParentField() == null)
            throw mpe;

        return (RecordInfo) dd;
    }

    void parse() {
        // include all the files and add them to the text, delete the
        // !include command
        solveIncludes();

        // put fields in the fields table, subfields in subfields
        separateFields();

        // determine the title field, delete !title
        setTitle();

        // read predefined types, delete all !type.*
        readTypes();

        // commands should be finished at this point
        if (text.size() != 0)
            mpe.add(fail("unrecognized commands", text.toString()));

        // make a FieldParser for each field, let it parse and substitute
        // itself
        treatMyFields();

        // send info from the subfield table to the subfields
        configSubfields();

        // call solveAll() on all subfields
        treatSubfields();

        // after all fields are processed, process the multi field indices & check for field existance
        checkMultipleUnique();

    }

    private void checkMultipleUnique() {
        for (int i = 0; i < dd.getMultiFieldUniqueKeys().length; i++) {
            DataDefinition.MultipleUniqueKeyDefinition multiUniqueKeyDefinition = (DataDefinition.MultipleUniqueKeyDefinition) dd.getMultiFieldUniqueKeys()[i];
            for (int j = 0; j < multiUniqueKeyDefinition.getFields().length; j++) {
                if (dd.getFieldDefinition(multiUniqueKeyDefinition.getFields()[j]) == null) {
                    mpe.add(new DataDefinitionParseError(dd.getName(), "Unique index contains an unknown field: "
                            + multiUniqueKeyDefinition.getFields()[j], multiUniqueKeyDefinition.getLine()));
                }
            }
        }
    }

    void separateFields() {
        for (Enumeration e = text.keys(); e.hasMoreElements();) {
            String k = (String) e.nextElement();
            if (k.indexOf('!') == 0)
                continue;

            if (k.indexOf("->") == -1)
                fields.putLast(k, text.getOriginal(k), text.remove(k));
            else
                subfields.putLast(k, text.getOriginal(k), text.remove(k));
        }
    }

    void setTitle() {
        String origCmd = (String) text.getOriginal("!title");
        String ttl = (String) text.remove("!title");
        String ttlt = null;
        if (ttl != null) {
            if (fields.get(ttlt = ttl.trim()) == null) {
                mpe.add(fail("no such field for title", makeLine(origCmd, ttl)));
                return;
            }
        } else if (fields.get("name") != null)
            ttlt = "name";
        else
        // if there are any relations, we skip their fields as
        // titles...
        if (fields.size() > 0)
            ttlt = fields.keyAt(0);
        ((RecordInfo) dd).title = ttlt;
    }

    static java.net.URL getResource(String s) {
        return org.makumba.util.ClassResource.get(s);
    }

    static public java.net.URL findDataDefinition(String s, String ext) {
        // must specify a filename, not a directory (or package), see bug 173
        java.net.URL u = findDataDefinitionOrDirectory(s, ext);
        if (u != null) {
            if (u.toString().endsWith("/") || new File(u.getPath()).isDirectory()) {
                return null;
            }
        }
        return u;
    }

    static public java.net.URL findDataDefinitionOrDirectory(String s, String ext) {
        String s1 = null;
        java.net.URL u = null;
        if (s.startsWith("/"))
            s = s.substring(1);
        if (s.endsWith(".") || s.endsWith("//"))
            return null;
        u = getResource(s.replace('.', '/') + "." + ext);
        if (u == null) {
            u = getResource("dataDefinitions/" + s.replace('.', '/') + "." + ext);
            if (u == null) {
                u = getResource(s1 = "dataDefinitions/" + s.replace('.', '/'));
                if (u == null) {
                    u = getResource(s1 = s.replace('.', '/'));
                }
            }
        }
        return u;
    }

    /** Find the URL to the validation definition with the given name. */
    static public java.net.URL findValidationDefinition(String s) {
        String definitionFileName = s.replace('.', '/') + FILE_EXTENSION_VALIDATION;
        URL url = ClassResource.get(definitionFileName);
        if (url == null) {
            url = ClassResource.get("dataDefinitions/" + definitionFileName);
            if (url == null) {
                url = ClassResource.get("validationDefinitions/" + definitionFileName);
            }
        }
        return url;
    }

    void solveIncludes() {
        int line = 0;
        OrderedProperties inclText;
        Vector overridenFields = new Vector();

        for (Enumeration e = text.keys(); e.hasMoreElements(); line++) {
            String st = (String) e.nextElement();

            if (st.startsWith("!include")) {
                String ok = text.getOriginal(st);
                String incl = (String) text.remove(st);
                line--;
                String s = incl.trim();
                java.net.URL u = findDataDefinition(s, "idd");
                String n = "." + dd.getName();
                // if(u==null && s.indexOf('.')==-1)
                // u=findTable(n.substring(1, n.lastIndexOf('.')+1));

                if (u == null) {
                    mpe.add(fail("could not find include file " + s, ok + "=" + incl));
                    return;
                }
                try {
                    inclText = new OrderedProperties();
                    read(inclText, u);
                } catch (IOException ioe) {
                    mpe.add(fail("could not find include file " + s + " " + ioe, ok + "=" + incl));
                    ;
                    return;
                }

                for (Enumeration k = inclText.keys(); k.hasMoreElements();) {
                    String key = (String) k.nextElement();
                    String val = text.getProperty(key);
                    if (val == null) // new field, not overriden in main mdd
                        text.putAt(++line, key, inclText.getOriginal(key), inclText.getProperty(key));
                    else
                        // field is overriden in main mdd, ignore it
                        overridenFields.add(key);
                }
            }
        }

        // now we remove all overriden empty fields
        // keep the non-overriden ones with empty definiton to report a mdd
        // error
        for (Enumeration k = overridenFields.elements(); k.hasMoreElements();) {
            String key = (String) k.nextElement();
            if (((String) text.get(key)).trim().length() == 0)
                text.remove(key);
        }
    }

    void readTypes() {
        for (Enumeration e = text.keys(); e.hasMoreElements();) {
            String s = (String) e.nextElement();
            if (s.startsWith("!type.")) {
                String nm = s.substring(6);
                definedTypes.put(nm, text.remove(s));
            }
        }
    }

    FieldCursor currentRowCursor;

    FieldInfo getFieldInfo(String fieldName) {
        return (FieldInfo) dd.getFieldDefinition(fieldName);
    }

    void treatMyFields() {
        FieldInfo fi;
        String nm;

        int line = 0;
        for (Enumeration e = fields.keys(); e.hasMoreElements(); line++) {
            nm = (String) e.nextElement();
            // check name for validity:
            for (int i = 0; i < nm.length(); i++) {
                if (i == 0 && !Character.isJavaIdentifierStart(nm.charAt(i)) || i > 0
                        && !Character.isJavaIdentifierPart(nm.charAt(i)))
                    mpe.add(fail("Invalid character \"" + nm.charAt(i) + "\" in field name \"" + nm + "\"", nm));
            }

            if (ReservedKeywords.isReservedKeyword(nm)) {
                mpe.add(fail("Error: field name cannot be one of the reserved keywords "
                        + ReservedKeywords.getKeywordsAsString(), nm));
            }

            fi = new FieldInfo((RecordInfo) dd, nm);
            ((RecordInfo) dd).addField1(fi);
            try {
                parse(nm, new FieldCursor(this, makeLine(fields, nm)));
            } catch (DataDefinitionParseError pe) {
                ((RecordInfo) dd).fields.remove(nm);
                ((RecordInfo) dd).fieldOrder.remove(nm);
                mpe.add(pe);
                continue;
            }
        }
    }

    void configSubfields() {
        String nm;
        int p;
        for (Enumeration e = subfields.keys(); e.hasMoreElements();) {
            nm = (String) e.nextElement();

            p = nm.indexOf("->");
            FieldInfo fieldInfo = getFieldInfo(nm.substring(0, p));
            if (fieldInfo == null) { // we did not find the field info, i.e. accessed unknownField->field.
                mpe.add(fail("Could not find subfield '" + nm.substring(0, p) + "'", makeLine(subfields, nm)));
                continue;
            }
            String type = (String) fieldInfo.type;
            if (type == null) {
                mpe.add(fail("no such field in subfield definition", makeLine(subfields, nm)));
                continue;
            }

            String s;
            if ((s = addText(nm.substring(0, p), nm.substring(p + 2), subfields.getOriginal(nm),
                subfields.getProperty(nm))) != null)
                mpe.add(fail(s, makeLine(subfields, nm)));
        }
    }

    void treatSubfields() {
        for (Enumeration e = dd.getFieldNames().elements(); e.hasMoreElements();) {
            String fieldName = (String) e.nextElement();
            parseSubfields(fieldName);
        }
    }

    static String makeLine(String origKey, String value) {
        return origKey + "=" + value;
    }

    static String makeLine(OrderedProperties p, String k) {
        return p.getOriginal(k) + "=" + p.getProperty(k);
    }

    public static Vector commaString2Vector(String s) {
        Vector v = new Vector();

        while (true) {
            int p = s.indexOf(',');
            if (p == -1) {
                String toAdd = s.trim();
                if (toAdd.length() > 0)
                    v.addElement(toAdd);
                return v;
            } else {
                String toAdd = s.substring(0, p).trim();
                if (toAdd.length() > 0)
                    v.addElement(toAdd);
                s = s.substring(p + 1);
            }
        }
    }

    static String listArguments(Vector v) {
        StringBuffer sb = new StringBuffer();
        if (v != null && v.size() > 0) {
            sb.append('(').append(v.elementAt(0));
            for (int i = 1; i < v.size(); i++)
                sb.append(", ").append(v.elementAt(i));
            sb.append(')');
        }
        return sb.toString();
    }

    DataDefinitionParseError fail(String why, String where) {
        return new DataDefinitionParseError(dd.getName(), why, where, where.length());
    }

    DataDefinitionParseError fail(IOException ioe) {
        return new DataDefinitionParseError(dd.getName(), ioe);
    }

    void read(OrderedProperties op, String txt) throws IOException {
        read(op, new BufferedReader(new StringReader(txt)));
    }

    void read(OrderedProperties op, java.net.URL u) throws IOException {
        read(op, new BufferedReader(new InputStreamReader((InputStream) u.getContent())));
    }

    void read(OrderedProperties op, BufferedReader rd) throws IOException {
        while (true) {
            String s = null;
            s = rd.readLine();
            if (s == null)
                break;

            String st = s.trim();
            int l = s.indexOf('=');
            if (st.length() == 0 || st.charAt(0) == '#')
                continue;
            Matcher multiUniqueMatcher = multiUniquePattern.matcher(st);
            if (l == -1 && multiUniqueMatcher.matches()) { // if we do have a multiple key def, parse & store it
                // we will analyse if the fields are correct later
                ArrayList groupList = new ArrayList();
                for (int j = 1; j <= multiUniqueMatcher.groupCount(); j++) {
                    if (multiUniqueMatcher.group(j) != null) {
                        groupList.add(multiUniqueMatcher.group(j));
                    }
                }
                String[] groups = (String[]) groupList.toArray(new String[groupList.size()]);
                dd.addMultiUniqueKey(new DataDefinition.MultipleUniqueKeyDefinition(groups, s));
                continue;
            }

            if (l == -1) {
                mpe.add(fail("non-empty, non-comment line without =", s));
                continue;
            }
            String k = s.substring(0, l);
            String kt = k.trim();
            if (kt.length() == 0) {
                mpe.add(fail("zero length key", s));
                continue;
            }
            if (kt.charAt(0) == '!' && kt.length() == 1) {
                mpe.add(fail("zero length command", s));
                continue;
            }

            if (kt.startsWith("!include")) {
                if (kt.length() > 8) {
                    mpe.add(fail("unknown command: " + kt, s));
                    continue;
                }
                while (op.get(kt) != null)
                    kt = kt + "_";
            }
            String val = s.substring(l + 1);
            if (op.putLast(kt, k, val) != null)
                mpe.add(fail("ambiguous key " + kt, s));
        }
        rd.close();
    }

    // moved from FieldParser
    public void parseSubfields(String fieldName) {
        switch (getFieldInfo(fieldName).getIntegerType()) {
            case FieldDefinition._setComplex:
            case FieldDefinition._ptrOne:
                parse_ptrOne_Subfields(fieldName);
                break;
            case FieldDefinition._set:
                parse_set_Subfields(fieldName);
                break;
            default:
                ;
        }
    }

    // moved from ptrOneParser
    public void parse_ptrOne_Subfields(String fieldName) {
        ((RecordParser) ptrOne_RecordParsers.get(fieldName)).parse();
        getFieldInfo(fieldName).extra2 = ((RecordParser) ptrOne_RecordParsers.get(fieldName)).dd.getTitleFieldName();
    }

    // moved from setParser
    public void parse_set_Subfields(String fieldName) {
        if (getFieldInfo(fieldName).extra2 == null) {
            getFieldInfo(fieldName).extra2 = ((RecordInfo) subtableParser_subtables.get(fieldName)).title = ((DataDefinition) setParser_settbls.get(fieldName)).getTitleFieldName();
        }
    }

    // moved from FieldParser
    String acceptTitle(String fieldName, String nm, String origNm, String val, Object o) {
        val = val.trim();
        if (nm.equals("!title")) {
            DataDefinition ri = (DataDefinition) o;
            if (ri.getFieldDefinition(val) == null)
                return ri.getName() + " has no field called " + val;
            getFieldInfo(fieldName).extra2 = val;
            return null;
        }
        return addText(fieldName, nm, origNm, val);
    }

    // moved from FieldParser
    String addText(String fieldName, String nm, String origNm, String val) {
        switch (getFieldInfo(fieldName).getIntegerType()) {
            case FieldDefinition._ptr:
            case FieldDefinition._ptrRel:
                return add_ptr_Text(fieldName, nm, origNm, val);
            case FieldDefinition._ptrOne:
            case FieldDefinition._setComplex:
                return add_ptrOne_Text(fieldName, nm, origNm, val);
            case FieldDefinition._set:
                return add_set_Text(fieldName, nm, origNm, val);
            default:
                return base_addText(fieldName, nm, origNm, val);
        }
    }

    // original from FieldParser
    String base_addText(String fieldName, String nm, String origNm, String val) {
        return "subfield not allowed";
    }

    // moved from ptrParser
    String add_ptr_Text(String fieldName, String nm, String origNm, String val) {
        return acceptTitle(fieldName, nm, origNm, val, getFieldInfo(fieldName).extra1);
    }

    // moved from ptrOneParser
    String add_ptrOne_Text(String fieldName, String nm, String origNm, String val) {
        if (((RecordParser) ptrOne_RecordParsers.get(fieldName)).text.putLast(nm, origNm, val) != null)
            return "field already exists";
        return null;
    }

    // moved from setParser
    String add_set_Text(String fieldName, String nm, String origNm, String val) {
        String s = acceptTitle(fieldName, nm, origNm, val, (DataDefinition) setParser_settbls.get(fieldName));
        if (s == null)
            ((RecordInfo) subtableParser_subtables.get(fieldName)).title = val.trim();
        return s;
    }

    // moved from FieldParser
    void parse(String fieldName, FieldCursor fc) throws org.makumba.DataDefinitionParseError {
        while (true) {
            if (fc.lookup("not")) {
                if (getFieldInfo(fieldName).notNull)
                    throw fc.fail("too many not null");
                fc.expect("null");
                fc.expectWhitespace();
                getFieldInfo(fieldName).notNull = true;
                continue;
            }

            if (fc.lookup("fixed")) {
                fc.expectWhitespace();
                if (getFieldInfo(fieldName).fixed)
                    throw fc.fail("too many fixed");
                getFieldInfo(fieldName).fixed = true;
                continue;
            }

            if (fc.lookup("unique")) {
                fc.expectWhitespace();
                if (getFieldInfo(fieldName).unique)
                    throw fc.fail("already unique");
                getFieldInfo(fieldName).unique = true;
                continue;
            }

            break;
        }

        if (setType(fieldName, fc.expectTypeLiteral(), fc) == null) {
            String s = definedTypes.getProperty(getFieldInfo(fieldName).type);
            if (s == null)
                throw fc.fail("unknown type: " + getFieldInfo(fieldName).type);

            fc.substitute(getFieldInfo(fieldName).type.length(), s);

            if (setType(fieldName, fc.expectTypeLiteral(), fc) == null)
                throw fc.fail("unknown type: " + getFieldInfo(fieldName).type);
        }
        getFieldInfo(fieldName).description = getFieldInfo(fieldName).description == null ? getFieldInfo(fieldName).name
                : getFieldInfo(fieldName).description;

    }

    // moved from FieldParser
    String setType(String fieldName, String type, FieldCursor fc) throws org.makumba.DataDefinitionParseError {
        String initialType = type;
        getFieldInfo(fieldName).type = type;
        while (true) {
            if (FieldInfo.integerTypeMap.get(getFieldInfo(fieldName).type) == null) {
                // getFieldInfo(fieldName).type = null;
                return null;
            }
            parse1(fieldName, fc);
            if (getFieldInfo(fieldName).type.equals(initialType))
                return initialType;
            initialType = getFieldInfo(fieldName).type;
        }
    }

    /**
     * switch for the existing parse methods. set the field type to another value if we want to change it
     */
    void parse1(String fieldName, FieldCursor fc) {
        switch (getFieldInfo(fieldName).getIntegerType()) {
            case FieldDefinition._charEnum:
                charEnum_parse1(fieldName, fc);
                return;
            case FieldDefinition._char:
                char_parse1(fieldName, fc);
                return;
            case FieldDefinition._intEnum:
                intEnum_parse1(fieldName, fc);
                return;
            case FieldDefinition._int:
                int_parse1(fieldName, fc);
                return;
            case FieldDefinition._ptrOne:
                ptrOne_parse1(fieldName, fc);
                return;
            case FieldDefinition._ptrRel:
            case FieldDefinition._ptr:
                ptr_parse1(fieldName, fc);
                return;
            case FieldDefinition._setCharEnum:
                setCharEnum_parse1(fieldName, fc);
                return;
            case FieldDefinition._setComplex:
                setComplex_parse1(fieldName, fc);
                return;
            case FieldDefinition._setIntEnum:
                setIntEnum_parse1(fieldName, fc);
                return;
            case FieldDefinition._set:
                set_parse1(fieldName, fc);
                return;
            case FieldDefinition._text:
                text_parse1(fieldName, fc);
                return;
            case FieldDefinition._date:
            case FieldDefinition._real:
            case FieldDefinition._ptrIndex:
            case FieldDefinition._dateCreate:
            case FieldDefinition._dateModify:
                simple_parse1(fieldName, fc);
                return;
            default:
                ;
        }
    }

    // moved from intParser
    public void int_parse1(String fieldName, FieldCursor fc) {
        if (!fc.lookup("{")) {
            getFieldInfo(fieldName).description = fc.lookupDescription();
            return;
        }
        getFieldInfo(fieldName).type = "intEnum";
    }

    // moved from intEnumParser
    public void intEnum_parse1(String fieldName, FieldCursor fc) {
        fc.expectIntEnum(getFieldInfo(fieldName));
        getFieldInfo(fieldName).description = fc.lookupDescription();
        return;
    }

    // moved from charParser
    public void char_parse1(String fieldName, FieldCursor fc) {
        if (!fc.lookup("{")) {
            fc.expect("[");
            Integer size = fc.expectInteger();
            if (size.intValue() > 255 || size.intValue() < 0)
                throw fc.fail("char size must be between 0 and 255, not " + size.toString());
            getFieldInfo(fieldName).extra2 = size;
            fc.expect("]");
            getFieldInfo(fieldName).description = fc.lookupDescription();
            return;
        }

        getFieldInfo(fieldName).type = "charEnum";
    }

    // moved from charEnumParser
    public void charEnum_parse1(String fieldName, FieldCursor fc) {
        fc.expectCharEnum(getFieldInfo(fieldName));
        getFieldInfo(fieldName).description = fc.lookupDescription();
        return;
    }

    // moved from simpleParser
    public void simple_parse1(String fieldName, FieldCursor fc) {
        getFieldInfo(fieldName).description = fc.lookupDescription();
        return;
    }

    // moved from textParser
    public void text_parse1(String fieldName, FieldCursor fc) throws org.makumba.DataDefinitionParseError {
        if (getFieldInfo(fieldName).isUnique())
            throw fc.fail("text fields can't be declared unique");
        return;
    }

    // moved from setComplexParser
    public void setComplex_parse1(String fieldName, FieldCursor fc) {
        ptrOne_parse1(fieldName, fc);
        ((RecordInfo) subtableParser_subtables.get(fieldName)).mainPtr = addPtrHere(fieldName);
        return;
    }

    // moved from ptrOneParser
    public void ptrOne_parse1(String fieldName, FieldCursor fc) {
        makeSubtable(fieldName, fc);
        ptrOne_RecordParsers.put(fieldName, new RecordParser(((RecordInfo) subtableParser_subtables.get(fieldName)),
                this));
        return;
    }

    // moved from setEnumParser, setChatEnumParser
    public void setCharEnum_parse1(String fieldName, FieldCursor fc) {
        FieldInfo _enum = new FieldInfo(((RecordInfo) subtableParser_subtables.get(fieldName)), "enum");
        makeSubtable(fieldName, fc);
        ((RecordInfo) subtableParser_subtables.get(fieldName)).mainPtr = addPtrHere(fieldName);
        ((RecordInfo) subtableParser_subtables.get(fieldName)).addField1(_enum);
        ((RecordInfo) subtableParser_subtables.get(fieldName)).title = _enum.name;
        ((RecordInfo) subtableParser_subtables.get(fieldName)).setField = _enum.name;
        _enum.type = "charEnum";
        fc.expectCharEnum(_enum);
        getFieldInfo(fieldName).description = fc.lookupDescription();
        _enum.description = getFieldInfo(fieldName).getDescription() == null ? _enum.name
                : getFieldInfo(fieldName).getDescription();
        return;
    }

    // moved from setEnumParser, setIntEnumParser
    public void setIntEnum_parse1(String fieldName, FieldCursor fc) {
        FieldInfo _enum = new FieldInfo(((RecordInfo) subtableParser_subtables.get(fieldName)), "enum");
        makeSubtable(fieldName, fc);
        ((RecordInfo) subtableParser_subtables.get(fieldName)).mainPtr = addPtrHere(fieldName);
        ((RecordInfo) subtableParser_subtables.get(fieldName)).addField1(_enum);
        ((RecordInfo) subtableParser_subtables.get(fieldName)).title = _enum.name;
        ((RecordInfo) subtableParser_subtables.get(fieldName)).setField = _enum.name;
        _enum.type = "intEnum";
        fc.expectIntEnum(_enum);
        getFieldInfo(fieldName).description = fc.lookupDescription();
        _enum.description = getFieldInfo(fieldName).getDescription() == null ? _enum.name
                : getFieldInfo(fieldName).getDescription();
        return;
    }

    // moved from ptrParser
    public void ptr_parse1(String fieldName, FieldCursor fc) {
        Object o = fc.lookupTableSpecifier();

        if (o != null)
            getFieldInfo(fieldName).extra1 = o;
        try {
            getFieldInfo(fieldName).description = fc.lookupDescription();
        } catch (org.makumba.DataDefinitionParseError e) {
            throw fc.fail("table specifier or nothing expected");
        }

        if (o != null)
            return;

        // getFieldInfo(fieldName).unique = true;
        getFieldInfo(fieldName).type = "ptrOne";
    }

    // moved from setParser
    public void set_parse1(String fieldName, FieldCursor fc) {
        if (getFieldInfo(fieldName).isUnique())
            throw fc.fail("sets can't be declared unique");

        DataDefinition ori = fc.lookupTableSpecifier();
        if (ori == null) {
            String word = fc.lookupTypeLiteral();
            if (word == null) {
                try {
                    getFieldInfo(fieldName).description = fc.lookupDescription();
                } catch (org.makumba.DataDefinitionParseError pe) {
                    throw fc.fail("table specifier, enumeration type, or nothing expected");
                }
                getFieldInfo(fieldName).type = "setComplex";
                return;
            }
            String newName = enumSet(fieldName, fc, word);
            if (newName != null) {
                getFieldInfo(fieldName).type = newName;
                return;
            }
            String s = fc.rp.definedTypes.getProperty(word);
            if (s == null)
                throw fc.fail("table, char{}, int{} or macro type expected after set");

            fc.substitute(word.length(), s);

            newName = enumSet(fieldName, fc, fc.expectTypeLiteral());

            if (newName != null) {
                getFieldInfo(fieldName).type = newName;
                return;
            }

            throw fc.fail("int{} or char{} macro expected after set");
        }

        makeSubtable(fieldName, fc);
        ((RecordInfo) subtableParser_subtables.get(fieldName)).mainPtr = addPtrHere(fieldName);

        setParser_settbls.put(fieldName, ori);
        ((RecordInfo) subtableParser_subtables.get(fieldName)).setField = addPtr(fieldName,
            ((RecordInfo) setParser_settbls.get(fieldName)).getBaseName(), ori);
        return;
    }

    // moved from setParser
    String enumSet(String fieldName, FieldCursor fc, String word) {
        String newName;
        if (fc.lookup("{")) {
            newName = "set" + word + "Enum";
            getFieldInfo(fieldName).type = newName;
            if (newName != null)
                return newName;
            fc.fail("int{} or char{} expected after set");
        }
        return null;
    }

    // moved from subtableParser
    void makeSubtable(String fieldName, FieldCursor fc) {
        subtableParser_here.put(fieldName, dd);

        subtableParser_subtables.put(fieldName,
            ((RecordInfo) subtableParser_here.get(fieldName)).makeSubtable(getFieldInfo(fieldName).name));
        ((RecordInfo) subtableParser_subtables.get(fieldName)).addStandardFields(((RecordInfo) subtableParser_subtables.get(fieldName)).subfield);
        getFieldInfo(fieldName).extra1 = ((RecordInfo) subtableParser_subtables.get(fieldName));
    }

    // moved from subtableParser
    String addPtr(String fieldName, String name, DataDefinition o) {
        int n = name.lastIndexOf('.');
        if (n != -1)
            name = name.substring(n + 1);
        while (((RecordInfo) subtableParser_subtables.get(fieldName)).fields.get(name) != null)
            name = name + "_";

        FieldInfo ptr = new FieldInfo(((RecordInfo) subtableParser_subtables.get(fieldName)), name);
        ((RecordInfo) subtableParser_subtables.get(fieldName)).addField1(ptr);
        ptr.fixed = true;
        ptr.notNull = true;
        ptr.type = "ptrRel";
        ptr.extra1 = o;
        ptr.description = "relational pointer";
        return name;
    }

    // moved from subtableParser
    String addPtrHere(String fieldName) {
        // System.err.println(here.canonicalName()+"
        // "+subtable.canonicalName());
        ((RecordInfo) subtableParser_subtables.get(fieldName)).relations = 1;
        if (((RecordInfo) subtableParser_here.get(fieldName)).getParentField() != null)
            return addPtr(fieldName, ((RecordInfo) subtableParser_here.get(fieldName)).subfield,
                ((RecordInfo) subtableParser_here.get(fieldName)));
        else
            return addPtr(fieldName, ((RecordInfo) subtableParser_here.get(fieldName)).name,
                ((RecordInfo) subtableParser_here.get(fieldName)));
    }

    public void parseValidationDefinition() throws ValidationDefinitionParseError {
        System.out.println("*** parsing!");
        URL url = findValidationDefinition(dd.getName());
        if (url == null) {
            System.out.println("No validation definition found for " + dd.getName() + ", no parsing. mdd:" + this);
            return;
        }
        // System.out.println("Started parsing validation definition '" + name + "'.");
        ValidationDefinitionParseError mpe = new ValidationDefinitionParseError();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader((InputStream) url.getContent()));
            // new BufferedReader(new FileReader(url.getFile()));
            String line = null;
            while ((line = rd.readLine()) != null) {
                try {
                    line = line.trim();
                    if (line.equals("") || line.startsWith("#")) {
                        continue; // skip empty lins & comment lines
                    }
                    if (line.indexOf(";") != -1) { // cut off end-of-line comments
                        line = line.substring(0, line.indexOf(";")).trim();
                    }
                    String[] definitionParts = line.split(":");
                    if (definitionParts.length < 3) {
                        throw new ValidationDefinitionParseError(dd.getName(),
                                "Rule does not consist of the three parts 'identifier':'message':'rule'!", line);
                    }

                    String ruleName = definitionParts[0].trim();
                    String errorMessage = definitionParts[1].trim();
                    String ruleDef = definitionParts[2].trim();
                    ValidationRule rule = null;
                    String fieldName = null;
                    Matcher matcher;
                    // check all possible validation types
                    if ((matcher = RegExpValidationRule.getMatcher(ruleDef)).matches()) {
                        // regexp validation
                        fieldName = matcher.group(1);
                        FieldDefinition fd = getFieldDefinition(line, fieldName);
                        rule = new RegExpValidationRule(fd, fieldName, ruleName, errorMessage, matcher.group(3));

                    } else if ((matcher = NumberRangeValidationRule.getMatcher(ruleDef)).matches()) {
                        // number (int or real) validation
                        fieldName = matcher.group(1);
                        FieldDefinition fd = getFieldDefinition(line, fieldName);
                        rule = new NumberRangeValidationRule(fd, fieldName, ruleName, errorMessage, matcher.group(2),
                                matcher.group(3));

                    } else if ((matcher = StringLengthValidationRule.getMatcher(ruleDef)).matches()) {
                        // string lenght (char or text) validation
                        fieldName = matcher.group(1);
                        FieldDefinition fd = getFieldDefinition(line, fieldName);
                        rule = new StringLengthValidationRule(fd, fieldName, ruleName, errorMessage, matcher.group(2),
                                matcher.group(3));

                    } else if ((matcher = ComparisonValidationRule.getMatcher(ruleDef)).matches()) {
                        // comparison validation, compares two fields or a field with a constant
                        fieldName = matcher.group(1);
                        String functionName = null;
                        if (BasicValidationRule.isValidFunctionCall(fieldName)) {
                            functionName = BasicValidationRule.extractFunctionNameFromStatement(fieldName);
                            fieldName = BasicValidationRule.extractFunctionArgument(fieldName);
                        }
                        FieldDefinition fd = getFieldDefinition(line, fieldName);
                        String operator = matcher.group(2);
                        String compareTo = matcher.group(3);
                        if (fd.getIntegerType() == FieldDefinition._date
                                && ComparisonValidationRule.matchesDateExpression(compareTo)) {
                            // we have a comparison to a date constant / expression
                            rule = new ComparisonValidationRule(fd, fieldName, compareTo, ruleName, errorMessage,
                                    operator);
                        } else {
                            FieldDefinition otherFd = getFieldDefinition(line, compareTo);
                            rule = new ComparisonValidationRule(fd, fieldName, functionName, otherFd, compareTo,
                                    ruleName, errorMessage, operator);
                        }
                    } else {
                        // no recognised rule
                        throw new ValidationDefinitionParseError("", "Rule type not recognised!", line);
                    }

                    rule.getFieldDefinition().addValidationRule(rule);
                    // validationRules.put(fieldName, rule);
                    ((RecordInfo) dd).addValidationRule(rule);
                    System.out.println("Added new validation rule: " + rule);
                } catch (ValidationDefinitionParseError e) {
                    mpe.add(e);
                }

            }
            rd.close();
            if (!mpe.isSingle()) {
                throw mpe;
            }
        }

        catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (!mpe.isSingle()) {
            throw mpe;
        }
        // System.out.println("Finished parsing validation definition '" + name + "'.");
    }

    protected FieldDefinition getFieldDefinition(String line, String fieldName) throws ValidationDefinitionParseError {
        DataDefinition checkedDataDef = dd;

        // treat sub-fields
        int indexOf = -1;
        while ((indexOf = fieldName.indexOf(".")) != -1) {
            // we have a sub-record-field
            String subFieldName = fieldName.substring(0, indexOf);
            fieldName = fieldName.substring(indexOf + 1);
            checkedDataDef = checkedDataDef.getFieldDefinition(subFieldName).getPointedType();
        }

        FieldDefinition fd = checkedDataDef.getFieldDefinition(fieldName);
        if (fd == null) {
            throw new ValidationDefinitionParseError(checkedDataDef.getName(), "Field &lt;" + fieldName
                    + "&gt; not defined in type " + dd.getName() + "!", line);
        }
        return fd;
    }

    public static void main(String[] args) {
        RegExpUtils.evaluate(RecordParser.multiUniquePattern, new String[] { "!unique(abc)", "!unique(abc,def)",
                "!unique( abc )", "!unique(abc , def )" });
        DataDefinition dd = MakumbaSystem.getDataDefinition("test.Person");
    }

}

class OrderedProperties extends Dictionary {
    Vector ks = new Vector();

    Hashtable orig = new Hashtable();

    Hashtable content = new Hashtable();

    OrderedProperties() {
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("{");
        Enumeration e = keys();
        if (e.hasMoreElements()) {
            Object o = e.nextElement();
            sb.append(o).append("=").append(get(o));
            while (e.hasMoreElements()) {
                o = e.nextElement();
                sb.append(", ").append(o).append("= ").append(get(o));
            }
        }
        return sb.append('}').toString();
    }

    public Enumeration elements() {
        return ((Hashtable) content.clone()).elements();
    }

    public Object get(Object key) {
        return content.get(key);
    }

    public Enumeration keys() {
        return ((Vector) ks.clone()).elements();
    }

    public String getOriginal(String key) {
        return (String) orig.get(key);
    }

    public String keyAt(int i) {
        return (String) ks.elementAt(i);
    }

    public Object remove(Object key) {
        ks.removeElement(key);
        orig.remove(key);
        return content.remove(key);
    }

    public Object putAt(int n, Object key, Object origKey, Object value) {
        ks.insertElementAt(key, n);
        orig.put(key, origKey);
        return content.put(key, value);
    }

    public synchronized Object putLast(Object key, Object origKey, Object value) {
        Object o = content.put(key, value);
        if (o != null)
            ks.removeElement(key);
        ks.addElement(key);
        orig.put(key, origKey);
        return o;
    }

    public Object put(Object key, Object value) {
        return putLast(key, key, value);
    }

    public String getProperty(String s) {
        return (String) get(s);
    }

    public int size() {
        return content.size();
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }

}
