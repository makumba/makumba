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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.makumba.DataDefinition;
import org.makumba.DataDefinitionParseError;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaError;
import org.makumba.ValidationDefinitionParseError;
import org.makumba.ValidationRule;
import org.makumba.DataDefinition.QueryFragmentFunction;
import org.makumba.commons.RegExpUtils;
import org.makumba.commons.ReservedKeywords;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.datadefinition.makumba.validation.BasicValidationRule;
import org.makumba.providers.datadefinition.makumba.validation.ComparisonValidationRule;
import org.makumba.providers.datadefinition.makumba.validation.NumberRangeValidationRule;
import org.makumba.providers.datadefinition.makumba.validation.RangeValidationRule;
import org.makumba.providers.datadefinition.makumba.validation.RegExpValidationRule;
import org.makumba.providers.datadefinition.makumba.validation.StringLengthValidationRule;
import org.makumba.providers.query.mql.HqlParser;

import antlr.collections.AST;

public class RecordParser {
    public static final String VALIDATION_INDICATOR = "%";

    // regular expressions for multi-field unique keys //
    public static final String multiUniqueRegExpElement = RegExpUtils.LineWhitespaces + "(" + RegExpUtils.fieldName
            + ")" + RegExpUtils.LineWhitespaces;

    public static final String multiUniqueRegExpElementRepeatment = "(?:" + RegExpUtils.LineWhitespaces + "," + "(?:"
            + multiUniqueRegExpElement + "))*";

    public static final String multiUniqueRegExp = RegExpUtils.LineWhitespaces + "(?:" + multiUniqueRegExpElement + ")"
            + multiUniqueRegExpElementRepeatment + RegExpUtils.LineWhitespaces;

    public static final Pattern multiUniquePattern = Pattern.compile(multiUniqueRegExp);

    public static final String validationRuleErrorMessageSeparatorChar = " : ";

    // regular expressions for validation definitions //
    public static final String validationDefinitionRegExp = RegExpUtils.LineWhitespaces + "(" + RegExpUtils.fieldName
            + ")" + RegExpUtils.LineWhitespaces + VALIDATION_INDICATOR + "(matches|length|range|compare|unique)"
            + RegExpUtils.LineWhitespaces + "=" + RegExpUtils.LineWhitespaces + "(.+)" + RegExpUtils.LineWhitespaces
            + validationRuleErrorMessageSeparatorChar + RegExpUtils.LineWhitespaces + ".+";

    public static final Pattern validationDefinitionPattern = Pattern.compile(validationDefinitionRegExp);

    // regular expressions for function definitions //
    /**
     * defines all possible types. <br>
     * FIXME: maybe this shall be move to {@link FieldDefinition}?
     */
    public static final String funcDefParamTypeRegExp = "(?:char|char\\[\\]|int|real|date|intEnum|charEnum|text|binary|ptr|set|setIntEnum|setCharEnum|ptr)";

    public static final String funcDefParamValueRegExp = "(?:\\d+|" + RegExpUtils.fieldName + ")";

    /** defines "int a" or "int 5". */
    public static final String funcDefParamRegExp = funcDefParamTypeRegExp + RegExpUtils.minOneLineWhitespace
            + funcDefParamValueRegExp + "(?:" + RegExpUtils.minOneLineWhitespace + funcDefParamValueRegExp + ")?";

    /** treats (int a, char b, ...) */
    public static final String funcDefParamRepeatRegExp = "\\((" + "(?:" + funcDefParamRegExp + ")" + "(?:"
            + RegExpUtils.LineWhitespaces + "," + RegExpUtils.LineWhitespaces + funcDefParamRegExp + ")*"
            + RegExpUtils.LineWhitespaces + ")?\\)";

    /** treats function(params) { queryFragment } errorMessage. */
    public static final String funcDefRegExp = "(" + RegExpUtils.fieldName + "%)?" + "(" + RegExpUtils.fieldName + ")"
            + funcDefParamRepeatRegExp + RegExpUtils.LineWhitespaces + "\\{" + RegExpUtils.LineWhitespaces
            + "(.[^\\}]+)" + RegExpUtils.LineWhitespaces + "(?:\\}" + RegExpUtils.LineWhitespaces + "(.*))?";

    public static final Pattern funcDefPattern = Pattern.compile(funcDefRegExp);

    public static final Pattern ident = Pattern.compile("[a-zA-Z]\\w*");

    OrderedProperties text;

    OrderedProperties fields = new OrderedProperties();

    OrderedProperties subfields = new OrderedProperties();

    DataDefinitionParseError mpe;

    Properties definedTypes;

    RecordInfo dd;

    // moved from ptrOneParser
    HashMap<String, RecordParser> ptrOne_RecordParsers = new HashMap<String, RecordParser>();

    // moved from setParser
    HashMap<String, DataDefinition> setParser_settbls = new HashMap<String, DataDefinition>();

    // moved from subtableParser
    HashMap<String, RecordInfo> subtableParser_subtables = new HashMap<String, RecordInfo>();

    HashMap<String, RecordInfo> subtableParser_here = new HashMap<String, RecordInfo>();

    private ArrayList<String> unparsedValidationDefinitions = new ArrayList<String>();

    /** title field expression to be evaluated later, after all subfields were found */
    private String titleExpressionToEvaluate;

    private String titleExpressionToEvaluateOrigCmd;

    public static boolean isValidationRule(String s) {
        return validationDefinitionPattern.matcher(s).matches();
    }

    public static boolean isFunction(String s) {
        return funcDefPattern.matcher(s).matches();
    }

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
            throw new MakumbaError(e, "Internal error in parser while parsing " + dd.getName() + " from " + dd.origin);
        }
        if (!mpe.isSingle() && !(dd.getParentField() != null)) {
            throw mpe;
        }
    }

    DataDefinition parse(java.net.URL u, String path) {
        dd = new RecordInfo(u, path);
        parse(dd);
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
            dd.addStandardFields(dd.getName().substring(dd.getName().lastIndexOf('.') + 1));
            parse();
        } catch (RuntimeException e) {
            throw new MakumbaError(e, "Internal error in parser while parsing " + dd.getName());
        }
        if (!mpe.isSingle() && dd.getParentField() == null) {
            throw mpe;
        }

        return dd;
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
        if (text.size() != 0) {
            mpe.add(fail("unrecognized commands", text.toString()));
        }

        // make a FieldParser for each field, let it parse and substitute
        // itself
        treatMyFields();

        // based on field and function info, look at function body and put it in the dd
        compileFunctions();

        // send info from the subfield table to the subfields
        configSubfields();

        // call solveAll() on all subfields
        treatSubfields();

        evaluateTitleExpressionInPointedType();

        // parse validation definition
        parseValidationDefinition();

        // after all fields are processed, process the multi field indices & check for field existance
        checkMultipleUniqueFields();

    }

    private void evaluateTitleExpressionInPointedType() {
        if (titleExpressionToEvaluate != null) {
            try {
                FieldDefinition fieldDef = dd.getFieldOrPointedFieldDefinition(titleExpressionToEvaluate);
                if (fieldDef == null) {
                    mpe.add(fail("no such field in a pointed type for title", makeLine(
                        titleExpressionToEvaluateOrigCmd, titleExpressionToEvaluate)));
                    return;
                }
                dd.title = titleExpressionToEvaluate;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /** Check whether all fields used in multiple uniqueness checks are defined in the data definition. */
    private void checkMultipleUniqueFields() {
        for (int i = 0; i < dd.getMultiFieldUniqueKeys().length; i++) {
            DataDefinition.MultipleUniqueKeyDefinition multiUniqueKeyDefinition = (DataDefinition.MultipleUniqueKeyDefinition) dd.getMultiFieldUniqueKeys()[i];
            for (int j = 0; j < multiUniqueKeyDefinition.getFields().length; j++) {
                String fieldName = multiUniqueKeyDefinition.getFields()[j];

                // check for potential sub-fields
                DataDefinition checkedDataDef = dd;
                int indexOf = -1;
                while ((indexOf = fieldName.indexOf(".")) != -1) {
                    // we have a sub-record-field
                    String subFieldName = fieldName.substring(0, indexOf);
                    fieldName = fieldName.substring(indexOf + 1);
                    checkedDataDef = checkedDataDef.getFieldDefinition(subFieldName).getPointedType();
                }
                if (checkedDataDef.getFieldDefinition(fieldName) == null) {
                    mpe.add(new DataDefinitionParseError(dd.getName(), "Unique index contains an unknown field: "
                            + fieldName, multiUniqueKeyDefinition.getLine()));
                } else if (checkedDataDef != dd) {
                    multiUniqueKeyDefinition.setKeyOverSubfield(true);
                }
            }
        }
    }

    void separateFields() {
        for (Enumeration<String> e = text.keys(); e.hasMoreElements();) {
            String k = e.nextElement();
            if (k.indexOf('!') == 0) {
                continue;
            }

            if (k.indexOf("->") == -1) {
                fields.putLast(k, text.getOriginal(k), text.remove(k));
            } else {
                subfields.putLast(k, text.getOriginal(k), text.remove(k));
            }
        }
    }

    void setTitle() {
        String origCmd = (String) text.getOriginal("!title");
        String ttl = (String) text.remove("!title");
        String ttlt = null;
        if (ttl != null) {
            if (ttl.contains(".")) {
                // we point to a field in a ptd type ==> need to evaluate it later, after all fields are evaluated
                titleExpressionToEvaluateOrigCmd = origCmd;
                titleExpressionToEvaluate = ttl;
                return;
            } else if (fields.get(ttlt = ttl.trim()) == null) {
                mpe.add(fail("no such field for title", makeLine(origCmd, ttl)));
                return;
            }
        } else if (fields.get("name") != null) {
            ttlt = "name";
        } else
        // if there are any relations, we skip their fields as
        // titles...
        if (fields.size() > 0) {
            ttlt = fields.keyAt(0);
        }
        dd.title = ttlt;
    }

    static java.net.URL getResource(String s) {
        return org.makumba.commons.ClassResource.get(s);
    }

    static public java.net.URL findDataDefinition(String s, String ext) {
        // must specify a filename, not a directory (or package), see bug 173
        java.net.URL u = findDataDefinitionOrDirectory(s, ext);
        if (u != null && (s.endsWith("/") || getResource(s + '/') != null)) {
            return null;
        }
        return u;
    }

    /**
     * Looks up a data definition. First tries to see if an arbitrary webapp root path was passed, if not uses the
     * classpath
     * 
     * @param s
     *            the name of the type
     * @param ext
     *            the extension (e.g. mdd)
     * @return a URL to the MDD file, null if none was found
     */
    static public java.net.URL findDataDefinitionOrDirectory(String s, String ext) {
        java.net.URL u = null;
        if (s.startsWith("/")) {
            s = s.substring(1);
        }
        if (s.endsWith(".") || s.endsWith("//")) {
            return null;
        }

        // if a webappRoot was passed, we fetch the MDDs from there, not using the CP
        if (RecordInfo.webappRoot != null) {
            File f = new File(RecordInfo.webappRoot);
            if (!f.exists() || (f.exists() && !f.isDirectory())) {
                throw new MakumbaError("webappRoot " + RecordInfo.webappRoot
                        + " does not appear to be a valid directory");
            }
            String mddPath = RecordInfo.webappRoot + "/WEB-INF/classes/dataDefinitions/" + s.replace('.', '/') + "."
                    + ext;
            File mdd = new File(mddPath.replaceAll("/", File.separator));
            if (mdd.exists()) {
                try {
                    u = new java.net.URL("file://" + mdd.getAbsolutePath());
                } catch (MalformedURLException e) {
                    throw new MakumbaError("internal error while trying to retrieve URL for MDD "
                            + mdd.getAbsolutePath());
                }
            }
        }

        if (u == null) {
                u = getResource("dataDefinitions/" + s.replace('.', '/') + "." + ext);
                
            // this is maybe a directory?
            // FIXME: this doesn't work if the MDDs are not in the dataDefinitions directory, but in the classes folder directly 
            if(u == null)
                u = getResource("dataDefinitions" + (s.length() == 0 ? "" : "/") + s);
                
            if (u == null) {
                u = getResource(s.replace('.', '/') + "." + ext);
            }
        }
        return u;
    }

    void solveIncludes() {
        int line = 0;
        OrderedProperties inclText;
        Vector<String> overridenFields = new Vector<String>();

        for (Enumeration<String> e = text.keys(); e.hasMoreElements(); line++) {
            String st = e.nextElement();

            if (st.startsWith("!include")) {
                String ok = text.getOriginal(st);
                String incl = (String) text.remove(st);
                line--;
                String s = incl.trim();
                java.net.URL u = findDataDefinition(s, "idd");
                // String n = "." + dd.getName();
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

                for (Enumeration<String> k = inclText.keys(); k.hasMoreElements();) {
                    String key = k.nextElement();
                    String val = text.getProperty(key);
                    if (val == null) {
                        // new field, not overriden in main mdd
                        text.putAt(++line, key, inclText.getOriginal(key), inclText.getProperty(key));
                    } else {
                        // field is overriden in main mdd, ignore it
                        overridenFields.add(key);
                    }
                }
            }
        }

        // now we remove all overriden empty fields
        // keep the non-overriden ones with empty definiton to report a mdd
        // error
        for (String key : overridenFields) {
            if (((String) text.get(key)).trim().length() == 0) {
                text.remove(key);
            }
        }
    }

    void readTypes() {
        for (Enumeration<String> e = text.keys(); e.hasMoreElements();) {
            String s = e.nextElement();
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
        for (Enumeration<String> e = fields.keys(); e.hasMoreElements(); line++) {
            nm = e.nextElement();
            String val = fields.get(nm);
            if (matchFunction(nm + val)) {
                continue;
            }

            // check name for validity:
            for (int i = 0; i < nm.length(); i++) {
                if (i == 0 && !Character.isJavaIdentifierStart(nm.charAt(i)) || i > 0
                        && !Character.isJavaIdentifierPart(nm.charAt(i))) {
                    mpe.add(fail("Invalid character \"" + nm.charAt(i) + "\" in field name \"" + nm + "\"", nm));
                }
            }

            if (ReservedKeywords.isReservedKeyword(nm)) {
                mpe.add(fail("Error: field name cannot be one of the reserved keywords "
                        + ReservedKeywords.getKeywordsAsString(), nm));
            }

            fi = new FieldInfo(dd, nm);
            dd.addField1(fi);
            try {
                parse(nm, new FieldCursor(this, makeLine(fields, nm)));
            } catch (DataDefinitionParseError pe) {
                dd.fields.remove(nm);
                dd.fieldOrder.remove(nm);
                mpe.add(pe);
                continue;
            }
        }
    }

    boolean matchFunction(String line) {
        // check if the line is a function definition
        Matcher matcher = funcDefPattern.matcher(line);
        if (!matcher.matches()) {
            return false;
        }

        String sessionVariableName = matcher.group(1);
        if (sessionVariableName != null) {
            sessionVariableName = sessionVariableName.replace("%", "");
        }
        String name = matcher.group(2);
        if (funcNames.get(name) != null) {
            mpe.add(new DataDefinitionParseError(dd.getName(), "Duplicate function name: " + name, line));
        }
        String paramsBlock = matcher.group(3); // params are not split yet, we get them all in one
        String queryFragment = matcher.group(4);
        String errorMessage = matcher.group(5);
        DataDefinition ddParams = new RecordInfo(dd.getName() + "." + matcher.group(0));
        if (StringUtils.isNotBlank(paramsBlock)) {
            String[] params = paramsBlock.split(",");
            for (String element : params) {
                // make sure to trim(), if we have "int x, int y", the second one param will be " int y"
                String paramType = element.trim().split(" ")[0].trim();
                if (paramType.equals("ptr")) { // we have a ptr, treat separately
                    String paramMdd = element.trim().split(" ")[1].trim();
                    String paramName = element.trim().split(" ")[2].trim();
                    DataDefinition pointedDD = DataDefinitionProvider.getInstance().getDataDefinition(paramMdd);
                    ddParams.addField(new FieldInfo(paramName,
                            (FieldInfo) pointedDD.getFieldDefinition(pointedDD.getIndexPointerFieldName())));
                } else {
                    String paramName = element.trim().split(" ")[1].trim();
                    if (paramType.equals("char[]")) { // we substitute char[] with the max char length
                        paramType = ("char[255]");
                    }
                    ddParams.addField(new FieldInfo(paramName, paramType));
                }
            }
        }

        DataDefinition.QueryFragmentFunction function = new DataDefinition.QueryFragmentFunction(name,
                sessionVariableName, queryFragment, ddParams, errorMessage, null);
        funcNames.put(function.getName(), function);
        return true;
    }

    HashMap<String, QueryFragmentFunction> funcNames = new HashMap<String, QueryFragmentFunction>();

    void compileFunctions() {
        for (String fn : funcNames.keySet()) {
            QueryFragmentFunction f = funcNames.get(fn);
            StringBuffer sb = new StringBuffer();
            String queryFragment = f.getQueryFragment();
            Matcher m = ident.matcher(queryFragment);
            boolean found = false;
            while (m.find()) {
                String id = queryFragment.substring(m.start(), m.end());
                int after = -1;
                for (int index = m.end(); index < queryFragment.length(); index++) {
                    char c = queryFragment.charAt(index);
                    if (c == ' ' || c == '\t') {
                        continue;
                    }
                    after = c;
                    break;
                }
                int before = -1;
                for (int index = m.start() - 1; index >= 0; index--) {
                    char c = queryFragment.charAt(index);
                    if (c == ' ' || c == '\t') {
                        continue;
                    }
                    before = c;
                    break;
                }

                if (before == '.' || id.equals("this") || id.equals("actor")
                        || f.getParameters().getFieldDefinition(id) != null) {
                    continue;
                }
                if (dd.getFieldDefinition(id) != null || after == '(' && funcNames.get(id) != null) {
                    m.appendReplacement(sb, "this." + id);
                    found = true;
                }
            }
            m.appendTail(sb);
            if (found) {
                java.util.logging.Logger.getLogger("org.makumba.db.query.inline").fine(
                    queryFragment + " -> " + sb.toString());
                
                // now parse the function text to see if that's okay
                AST tree = getParsedFunction(f.getName(), sb.toString(), f.toString());
                
                f = new QueryFragmentFunction(f.getName(), f.getSessionVariableName(), sb.toString(),
                        f.getParameters(), f.getErrorMessage(), tree);

            }
            dd.addFunction(f.getName(), f);
        }
    }
    
    private AST getParsedFunction(String fName, String expression, String line) {
        
     // here we parse the function to see if it's okay
        // when the expression is a subquery, i.e. starts with SELECT, we add paranthesis around it
        boolean subquery = expression.toUpperCase().startsWith("SELECT ");
        
        String query = "SELECT " + (subquery?"(":"") + expression + (subquery?")":"") + " FROM " + dd.getName() + " makumbaGeneratedAlias";
        HqlParser parser = HqlParser.getInstance(query);
        try {
            parser.statement();
        } catch (Exception e) {
            mpe.add(new DataDefinitionParseError(dd.getName(), "Error in function " + fName + ": " + e.getMessage(), line));
        }
        
        return parser.getAST();
        
        
    }

    void configSubfields() {
        String nm;
        int p;
        for (Enumeration<String> e = subfields.keys(); e.hasMoreElements();) {
            nm = e.nextElement();

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
                subfields.getProperty(nm))) != null) {
                mpe.add(fail(s, makeLine(subfields, nm)));
            }
        }
    }

    void treatSubfields() {
        for (String string : dd.getFieldNames()) {
            String fieldName = (String) string;
            parseSubfields(fieldName);
        }
    }

    static String makeLine(String origKey, String value) {
        return origKey + "=" + value;
    }

    static String makeLine(OrderedProperties p, String k) {
        return p.getOriginal(k) + "=" + p.getProperty(k);
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
        Object o = u.getContent();

        URLConnection uconn = u.openConnection();

        if (uconn.getClass().getName().endsWith("FileURLConnection")) {
            read(op, new BufferedReader(new InputStreamReader((InputStream) o)));
        } else if (uconn.getClass().getName().endsWith("JarURLConnection")) {
            JarFile jf = ((JarURLConnection) uconn).getJarFile();

            // jar:file:/home/manu/workspace/parade2/webapp/WEB-INF/lib/makumba.jar!/org/makumba/devel/relations/Relation
            // .mdd
            String[] jarURL = u.toExternalForm().split("!");

            JarEntry je = jf.getJarEntry(jarURL[1].substring(1));

            read(op, new BufferedReader(new InputStreamReader(jf.getInputStream(je))));
        }
    }

    void read(OrderedProperties op, BufferedReader rd) throws IOException {
        while (true) {
            String s = null;
            s = rd.readLine();
            if (s == null) {
                break;
            }

            String st = s.trim();
            if (st.length() == 0 || st.charAt(0) == '#') {
                continue;
            }

            String lineWithoutComment = null;
            if (st.indexOf(";") == -1) {
                lineWithoutComment = st;
            } else {
                lineWithoutComment = st.substring(0, st.indexOf(";"));
            }

            // check if the line is a validation definition; also treat validation definitions in subfields
            String lineToCheck = lineWithoutComment.contains("->") ? lineWithoutComment.split("->")[lineWithoutComment.split("->").length - 1].trim()
                    : lineWithoutComment;
            Matcher matcher = validationDefinitionPattern.matcher(lineToCheck);
            if (matcher.matches()) {
                // we parse them later
                unparsedValidationDefinitions.add(lineWithoutComment);
                continue;
            }

            int l = s.indexOf('=');
            int lpar = s.indexOf('{');
            if (l == -1 && lpar == -1) {
                mpe.add(fail("non-empty, non-comment line without = or {", s));
                continue;
            }
            if ((l == -1 || l > lpar) && lpar != -1) {
                l = lpar;
            } else {
                lpar = l + 1;
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
                while (op.get(kt) != null) {
                    kt = kt + "_";
                }
            }
            String val = s.substring(lpar);
            if (op.putLast(kt, k, val) != null) {
                mpe.add(fail("ambiguous key " + kt, s));
            }
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
        ptrOne_RecordParsers.get(fieldName).parse();
        getFieldInfo(fieldName).extra2 = ((RecordParser) ptrOne_RecordParsers.get(fieldName)).dd.getTitleFieldName();
    }

    // moved from setParser
    public void parse_set_Subfields(String fieldName) {
        if (getFieldInfo(fieldName).extra2 == null) {
            getFieldInfo(fieldName).extra2 = subtableParser_subtables.get(fieldName).title = ((DataDefinition) setParser_settbls.get(fieldName)).getTitleFieldName();
        }
    }

    // moved from FieldParser
    String acceptTitle(String fieldName, String nm, String origNm, String val, Object o) {
        val = val.trim();
        if (nm.equals("!title")) {
            DataDefinition ri = (DataDefinition) o;
            if (ri.getFieldDefinition(val) == null) {
                return ri.getName() + " has no field called " + val;
            }
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
        if (ptrOne_RecordParsers.get(fieldName).text.putLast(nm, origNm, val) != null) {
            return "field already exists";
        }
        return null;
    }

    // moved from setParser
    String add_set_Text(String fieldName, String nm, String origNm, String val) {
        String s = acceptTitle(fieldName, nm, origNm, val, (DataDefinition) setParser_settbls.get(fieldName));
        if (s == null) {
            subtableParser_subtables.get(fieldName).title = val.trim();
        }
        return s;
    }

    // moved from FieldParser
    void parse(String fieldName, FieldCursor fc) throws org.makumba.DataDefinitionParseError {
        while (true) {
            if (fc.lookup("not")) {
                if (getFieldInfo(fieldName).notNull) {
                    throw fc.fail("too many not null");
                } else if (getFieldInfo(fieldName).notEmpty) {
                    throw fc.fail("too many not empty");
                }
                if (fc.lookup("null")) {
                    getFieldInfo(fieldName).notNull = true;
                } else if (fc.lookup("empty")) {
                    getFieldInfo(fieldName).notEmpty = true;
                } else {
                    throw fc.fail("null or empty expected");
                }
                fc.expectWhitespace();
                continue;
            }

            if (fc.lookup("fixed")) {
                fc.expectWhitespace();
                if (getFieldInfo(fieldName).fixed) {
                    throw fc.fail("too many fixed");
                }
                getFieldInfo(fieldName).fixed = true;
                continue;
            }

            if (fc.lookup("unique")) {
                fc.expectWhitespace();
                if (getFieldInfo(fieldName).unique) {
                    throw fc.fail("already unique");
                }
                getFieldInfo(fieldName).unique = true;
                continue;
            }

            break;
        }

        if (setType(fieldName, fc.expectTypeLiteral(), fc) == null) {
            String s = definedTypes.getProperty(getFieldInfo(fieldName).type);
            if (s == null) {
                throw fc.fail("unknown type: " + getFieldInfo(fieldName).type);
            }

            fc.substitute(getFieldInfo(fieldName).type.length(), s);

            if (setType(fieldName, fc.expectTypeLiteral(), fc) == null) {
                throw fc.fail("unknown type: " + getFieldInfo(fieldName).type);
            }
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
            if (initialType.equals(FieldInfo.getStringType(FieldInfo._file))) { // file type gets re-written to ptrOne
                return getFieldInfo(fieldName).type;
            }
            if (getFieldInfo(fieldName).type.equals(initialType)) {
                return initialType;
            }
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
            case FieldDefinition._file:
                parseFile(fieldName, fc);
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
                date_parse1(fieldName, fc);
                return;
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
            if (size.intValue() > 255 || size.intValue() < 0) {
                throw fc.fail("char size must be between 0 and 255, not " + size.toString());
            }
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
    
    public void date_parse1(String fieldName, FieldCursor fc) {
        getFieldInfo(fieldName).description = fc.lookupDescription();
        //getFieldInfo(fieldName).defaultValue = new Date();
        return;
    }

    // moved from textParser
    public void text_parse1(String fieldName, FieldCursor fc) throws org.makumba.DataDefinitionParseError {
        if (getFieldInfo(fieldName).isUnique()) {
            throw fc.fail("text fields can't be declared unique");
        }
        return;
    }

    // moved from setComplexParser
    public void setComplex_parse1(String fieldName, FieldCursor fc) {
        ptrOne_parse1(fieldName, fc);
        subtableParser_subtables.get(fieldName).mainPtr = addPtrHere(fieldName);
        return;
    }

    // moved from ptrOneParser
    public void ptrOne_parse1(String fieldName, FieldCursor fc) {
        makeSubtable(fieldName, fc);
        ptrOne_RecordParsers.put(fieldName, new RecordParser(subtableParser_subtables.get(fieldName), this));
        return;
    }

    // based on ptrOne_parse1
    public void parseFile(String fieldName, FieldCursor fc) {
        subtableParser_here.put(fieldName, dd);

        String name = getFieldInfo(fieldName).name;
        subtableParser_subtables.put(fieldName, new FileRecordInfo(dd, name));
        getFieldInfo(fieldName).type = FieldInfo.getStringType(FieldInfo._ptrOne);
        subtableParser_subtables.get(fieldName).addStandardFields(subtableParser_subtables.get(fieldName).subfield);
        getFieldInfo(fieldName).extra1 = subtableParser_subtables.get(fieldName);
        ptrOne_RecordParsers.put(fieldName, new RecordParser(subtableParser_subtables.get(fieldName), this));
        return;
    }

    // moved from setEnumParser, setChatEnumParser
    public void setCharEnum_parse1(String fieldName, FieldCursor fc) {
        FieldInfo _enum = new FieldInfo(subtableParser_subtables.get(fieldName), "enum");
        makeSubtable(fieldName, fc);
        subtableParser_subtables.get(fieldName).mainPtr = addPtrHere(fieldName);
        subtableParser_subtables.get(fieldName).addField1(_enum);
        subtableParser_subtables.get(fieldName).title = _enum.name;
        subtableParser_subtables.get(fieldName).setField = _enum.name;
        _enum.type = "charEnum";
        fc.expectCharEnum(_enum);
        getFieldInfo(fieldName).description = fc.lookupDescription();
        _enum.description = getFieldInfo(fieldName).getDescription() == null ? _enum.name
                : getFieldInfo(fieldName).getDescription();
        return;
    }

    // moved from setEnumParser, setIntEnumParser
    public void setIntEnum_parse1(String fieldName, FieldCursor fc) {
        FieldInfo _enum = new FieldInfo(subtableParser_subtables.get(fieldName), "enum");
        makeSubtable(fieldName, fc);
        subtableParser_subtables.get(fieldName).mainPtr = addPtrHere(fieldName);
        subtableParser_subtables.get(fieldName).addField1(_enum);
        subtableParser_subtables.get(fieldName).title = _enum.name;
        subtableParser_subtables.get(fieldName).setField = _enum.name;
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

        if (o != null) {
            getFieldInfo(fieldName).extra1 = o;
        }
        try {
            getFieldInfo(fieldName).description = fc.lookupDescription();
        } catch (org.makumba.DataDefinitionParseError e) {
            throw fc.fail("table specifier or nothing expected");
        }

        if (o != null) {
            return;
        }

        // getFieldInfo(fieldName).unique = true;
        getFieldInfo(fieldName).type = "ptrOne";
    }

    // moved from setParser
    public void set_parse1(String fieldName, FieldCursor fc) {
        if (getFieldInfo(fieldName).isUnique()) {
            throw fc.fail("sets can't be declared unique");
        }

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
            if (s == null) {
                throw fc.fail("table, char{}, int{} or macro type expected after set");
            }

            fc.substitute(word.length(), s);

            newName = enumSet(fieldName, fc, fc.expectTypeLiteral());

            if (newName != null) {
                getFieldInfo(fieldName).type = newName;
                return;
            }

            throw fc.fail("int{} or char{} macro expected after set");
        }

        makeSubtable(fieldName, fc);
        subtableParser_subtables.get(fieldName).mainPtr = addPtrHere(fieldName);

        setParser_settbls.put(fieldName, ori);
        subtableParser_subtables.get(fieldName).setField = addPtr(fieldName,
            ((RecordInfo) setParser_settbls.get(fieldName)).getBaseName(), ori);
        return;
    }

    // moved from setParser
    String enumSet(String fieldName, FieldCursor fc, String word) {
        String newName;
        if (fc.lookup("{")) {
            newName = "set" + word + "Enum";
            getFieldInfo(fieldName).type = newName;
            if (newName != null) {
                return newName;
            }
            fc.fail("int{} or char{} expected after set");
        }
        return null;
    }

    // moved from subtableParser
    void makeSubtable(String fieldName, FieldCursor fc) {
        subtableParser_here.put(fieldName, dd);

        subtableParser_subtables.put(fieldName, subtableParser_here.get(fieldName).makeSubtable(
            getFieldInfo(fieldName).name));
        subtableParser_subtables.get(fieldName).addStandardFields(subtableParser_subtables.get(fieldName).subfield);
        getFieldInfo(fieldName).extra1 = subtableParser_subtables.get(fieldName);
    }

    // moved from subtableParser
    String addPtr(String fieldName, String name, DataDefinition o) {
        int n = name.lastIndexOf('.');
        if (n != -1) {
            name = name.substring(n + 1);
        }
        while (subtableParser_subtables.get(fieldName).fields.get(name) != null) {
            name = name + "_";
        }

        FieldInfo ptr = new FieldInfo(subtableParser_subtables.get(fieldName), name);
        subtableParser_subtables.get(fieldName).addField1(ptr);
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
        subtableParser_subtables.get(fieldName).relations = 1;
        if (subtableParser_here.get(fieldName).getParentField() != null) {
            return addPtr(fieldName, subtableParser_here.get(fieldName).subfield, subtableParser_here.get(fieldName));
        } else {
            return addPtr(fieldName, subtableParser_here.get(fieldName).name, subtableParser_here.get(fieldName));
        }
    }

    public void parseValidationDefinition() throws ValidationDefinitionParseError {
        ValidationDefinitionParseError mpe = new ValidationDefinitionParseError();
        for (String line : unparsedValidationDefinitions) {
            try {
                line = line.trim();
                if (line.indexOf(";") != -1) { // cut off end-of-line comments
                    line = line.substring(0, line.indexOf(";")).trim();
                }

                String subFieldName = null;
                RecordInfo targetDD = dd;
                if (line.contains("->")) {
                    int index = line.lastIndexOf("->");
                    subFieldName = line.substring(0, index).replace("->", ".");
                    line = line.substring(index + 2);
                    targetDD = (RecordInfo) dd.getFieldOrPointedFieldDefinition(subFieldName).getPointedType();
                }

                // check if the line is a validation definition
                Matcher singleValidationMatcher = validationDefinitionPattern.matcher(line);
                if (!singleValidationMatcher.matches()) {
                    throw new ValidationDefinitionParseError(targetDD.getName(), "Illegal rule definition!", line);
                }
                if (line.indexOf(validationRuleErrorMessageSeparatorChar) == -1) {
                    throw new ValidationDefinitionParseError(targetDD.getName(),
                            "Rule does not consist of the two parts <rule>:<message>!", line);
                }
                String fieldName = singleValidationMatcher.group(1).trim();
                String operation = singleValidationMatcher.group(2).trim();
                String ruleDef = singleValidationMatcher.group(3).trim();
                String errorMessage = line.substring(
                    line.lastIndexOf(validationRuleErrorMessageSeparatorChar)
                            + validationRuleErrorMessageSeparatorChar.length()).trim();
                String ruleName = line;
                ValidationRule rule = null;
                Matcher matcher;

                // check all possible validation types
                if (StringUtils.equals(operation, RegExpValidationRule.getOperator())) {
                    // regexp validation
                    FieldDefinition fd = DataDefinitionProvider.getFieldDefinition(targetDD, fieldName, line);
                    rule = new RegExpValidationRule(fd, fieldName, ruleName, errorMessage, ruleDef);

                } else if (StringUtils.equals(operation, NumberRangeValidationRule.getOperator())) {
                    // number (int or real) validation
                    FieldDefinition fd = DataDefinitionProvider.getFieldDefinition(targetDD, fieldName, line);
                    matcher = RangeValidationRule.getMatcher(ruleDef);
                    if (!matcher.matches()) {
                        throw new ValidationDefinitionParseError("", "Illegal range definition", line);
                    }
                    rule = new NumberRangeValidationRule(fd, fieldName, ruleName, errorMessage,
                            matcher.group(1).trim(), matcher.group(2).trim());

                } else if (StringUtils.equals(operation, StringLengthValidationRule.getOperator())) {
                    // string lenght (char or text) validation
                    FieldDefinition fd = DataDefinitionProvider.getFieldDefinition(targetDD, fieldName, line);
                    matcher = RangeValidationRule.getMatcher(ruleDef);
                    if (!matcher.matches()) {
                        throw new ValidationDefinitionParseError("", "Illegal range definition", line);
                    }
                    rule = new StringLengthValidationRule(fd, fieldName, ruleName, errorMessage,
                            matcher.group(1).trim(), matcher.group(2).trim());

                } else if (StringUtils.equals(operation, ComparisonValidationRule.getOperator())) {
                    // comparison validation, compares two fields or a field with a constant
                    // fieldName = matcher.group(1);
                    matcher = ComparisonValidationRule.getMatcher(ruleDef);
                    if (!matcher.matches()) {
                        throw new ValidationDefinitionParseError("", "Illegal comparison definition", line);
                    }
                    if (targetDD.getFieldDefinition(fieldName) == null) { // let's see if the first part is a field name
                        fieldName = matcher.group(1);
                    }
                    String functionName = null;
                    if (BasicValidationRule.isValidFunctionCall(fieldName)) {
                        functionName = BasicValidationRule.extractFunctionNameFromStatement(fieldName);
                        fieldName = BasicValidationRule.extractFunctionArgument(fieldName);
                    }
                    FieldDefinition fd = DataDefinitionProvider.getFieldDefinition(targetDD, fieldName, line);
                    String operator = matcher.group(2).trim();
                    String compareTo = matcher.group(3).trim();
                    if (fd.getIntegerType() == FieldDefinition._date
                            && ComparisonValidationRule.matchesDateExpression(compareTo)) {
                        // we have a comparison to a date constant / expression
                        rule = new ComparisonValidationRule(fd, fieldName, compareTo, ruleName, errorMessage, operator);
                    } else {
                        FieldDefinition otherFd = DataDefinitionProvider.getFieldDefinition(targetDD, compareTo, line);
                        rule = new ComparisonValidationRule(fd, fieldName, functionName, otherFd, compareTo, ruleName,
                                errorMessage, operator);
                    }
                } else if (StringUtils.equals(operation, "unique")) {
                    // check if the line defines a multi-field unique key
                    matcher = multiUniquePattern.matcher(ruleDef);
                    if (!matcher.matches()) {
                        throw new ValidationDefinitionParseError("", "Illegal multi-field unique definition", line);
                    }
                    ArrayList<String> groupList = new ArrayList<String>();
                    for (int j = 1; j <= matcher.groupCount(); j++) {
                        if (matcher.group(j) != null) {
                            // checking if the fields exist will be done later
                            groupList.add(matcher.group(j).trim());
                        }
                    }
                    String[] groups = (String[]) groupList.toArray(new String[groupList.size()]);
                    targetDD.addMultiUniqueKey(new DataDefinition.MultipleUniqueKeyDefinition(groups, line,
                            errorMessage));
                    java.util.logging.Logger.getLogger("org.makumba.datadefinition.makumba").finer(
                        "added multi-field unique key: "
                                + new DataDefinition.MultipleUniqueKeyDefinition(groups, line, errorMessage));
                    continue;
                } else {
                    // no recognised rule
                    throw new ValidationDefinitionParseError("", "Rule type not recognised!", line);
                }
                rule.getFieldDefinition().addValidationRule(rule);
                // validationRules.put(fieldName, rule);
                targetDD.addValidationRule(rule);
                java.util.logging.Logger.getLogger("org.makumba.datadefinition.makumba").finer("added rule: " + rule);
            } catch (ValidationDefinitionParseError e) {
                mpe.add(e);
            }

            if (!mpe.isSingle()) {
                throw mpe;
            }
        }

        if (!mpe.isSingle()) {
            throw mpe;
        }
        // System.out.println("Finished parsing validation definition '" + name + "'.");
    }

    public static void main(String[] args) {
        // test some function definition
        System.out.println("Testing some reg-exps:");
        RegExpUtils.evaluate(RecordParser.funcDefPattern, " someFunc() { abc } errorMessage",
            " someFunc(char[] a, int 5) {abc}errorMessages", "someFunction(int a, char[] b) { yeah}errorMessage3",
            "someOtherFunction(int age, char[] b) { this.age > age } You are too young!");
        System.out.println("\n\n*****************************************************************************");
        RegExpUtils.evaluate(RecordParser.multiUniquePattern, "unique12%unique = age, email : these need to be unique!");

        // test some mdd reading
        System.out.println("\n\n*****************************************************************************");
        System.out.println("Testing reading test.Person MDD:");
        DataDefinition recordInfo = RecordInfo.getRecordInfo("test.Person");

        System.out.println("\n\n*****************************************************************************");
        printFunctions(recordInfo);
        printFunctions(recordInfo.getFieldDefinition("address").getPointedType());
        printFunctions(recordInfo.getFieldOrPointedFieldDefinition("address.sth").getPointedType());

        System.out.println("\n\n*****************************************************************************");
        printValidationDefinitions(recordInfo);
        printValidationDefinitions(recordInfo.getFieldDefinition("address").getPointedType());
        printValidationDefinitions(recordInfo.getFieldOrPointedFieldDefinition("address.sth").getPointedType());

        System.out.println("\n\n*****************************************************************************");
        FieldDefinition fi = recordInfo.getFieldDefinition("someAttachment");
        System.out.println("extra1:" + ((FieldInfo) fi).extra1);
        System.out.println("extra2:" + ((FieldInfo) fi).extra2);
        System.out.println("extra3:" + ((FieldInfo) fi).extra3);
        System.out.println("File sub-ptr in test.Person:");
        System.out.println("\ttype: " + fi);
        DataDefinition pointedType = fi.getPointedType();
        System.out.println("\tname: " + pointedType);
        System.out.println("\tfields: " + pointedType.getFieldNames());
        for (String string : pointedType.getFieldNames()) {
            System.out.println("\t\t" + string + ": " + pointedType.getFieldDefinition(string));
        }
    }

    private static void printValidationDefinitions(DataDefinition recordInfo) {
        System.out.println("Validation definitions in " + recordInfo);
        for (String field : recordInfo.getFieldNames()) {
            Collection<ValidationRule> validationRules = recordInfo.getValidationDefinition().getValidationRules(field);
            if (validationRules.size() > 0) {
                System.out.println("\t" + field);
            }
            for (ValidationRule validationRule : validationRules) {
                System.out.println("\t\t" + validationRule);
            }
        }
        System.out.println("\n");
    }

    private static void printFunctions(DataDefinition dd) {
        System.out.println("Functions in " + dd);
        Collection<QueryFragmentFunction> functions = dd.getFunctions();
        for (QueryFragmentFunction queryFragmentFunction : functions) {
            System.out.println("\t" + queryFragmentFunction);
            System.out.println("\t\tparameters:");
            DataDefinition parameters = queryFragmentFunction.getParameters();
            for (int i = 0; i < parameters.getFieldNames().size(); i++) {
                FieldDefinition fieldDefinition = parameters.getFieldDefinition(i);
                System.out.println("\t\t\t" + fieldDefinition.getDataType() + " " + fieldDefinition.getName());
            }
            if (parameters.getFieldNames().size() == 0) {
                System.out.println("\t\t\t--None--");
            }
        }
        System.out.println("\n");
    }

}
