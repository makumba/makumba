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

//TODO extra comments about changes from refactoring

package org.makumba.providers.datadefinition.makumba;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.DataDefinitionNotFoundError;
import org.makumba.DataDefinitionParseError;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaError;
import org.makumba.MakumbaSystem;
import org.makumba.ValidationDefinition;
import org.makumba.ValidationRule;
import org.makumba.controller.validation.ComparisonValidationRule;
import org.makumba.controller.validation.NumberRangeValidationRule;
import org.makumba.controller.validation.RegExpValidationRule;
import org.makumba.controller.validation.StringLengthValidationRule;
import org.makumba.util.NamedResourceFactory;
import org.makumba.util.NamedResources;
import org.makumba.util.RuntimeWrappedException;

/**
 * This is the internal representation of the org.makumba. One can make RecordHandlers based on an instance of this
 * class and do useful things with it (generate sql tables, html code, etc)
 */
public class RecordInfo implements java.io.Serializable, DataDefinition, ValidationDefinition {
    private static final long serialVersionUID = 1L;

    static ArrayList operators = new ArrayList();

    static {
        operators.add(RegExpValidationRule.getOperator());
        operators.add(NumberRangeValidationRule.getOperator());
        operators.add(StringLengthValidationRule.getOperator());
        operators.addAll(ComparisonValidationRule.getOperators());
    }

    java.net.URL origin;

    String name;

    Properties templateValues;

    // Vector templateArgumentNames;

    Vector fieldOrder = new Vector();

    String title;

    String indexName;

    static final String createName = "TS_create";

    static final String modifyName = "TS_modify";

    // for set and setComplex subtables
    String mainPtr;

    // for set tables, also used for setintEnum and setcharEnum to store the
    // name of the int or char field
    String setField;

    // nr of relations, 0= none, 1= 1:n, 2= m:n
    int relations = 0;

    Hashtable fields = new Hashtable();

    // Hashtable fieldIndexes=null;

    // for subtables
    String subfield;

    String ptrSubfield = "";

    String subfieldPtr = "";

    RecordInfo papa;

    private Hashtable validationRuleNames = new Hashtable();

    private Hashtable multiFieldUniqueList = new Hashtable();

    private boolean alreadyParsed = false;

    void addStandardFields(String name) {
        FieldInfo fi;

        indexName = name;

        fi = new FieldInfo(this, indexName);
        fi.type = "ptrIndex";
        fi.description = "Unique index";
        fi.fixed = true;
        fi.notNull = true;
        fi.unique = true;
        addField1(fi);

        fi = new FieldInfo(this, modifyName);
        fi.type = "dateModify";
        fi.notNull = true;
        fi.description = "Last modification date";
        addField1(fi);

        fi = new FieldInfo(this, createName);
        fi.type = "dateCreate";
        fi.description = "Creation date";
        fi.fixed = true;
        fi.notNull = true;
        addField1(fi);
    }

    public boolean isTemporary() {
        return origin == null;
    }

    RecordInfo() {
        name = "temp" + hashCode();
    }

    /** make a temporary recordInfo that is only used for query results */
    public RecordInfo(String name) {
        this.name = name;
        origin = null;
    }

    protected void addField1(FieldDefinition fi) {
        fieldOrder.addElement(fi.getName());
        fields.put(fi.getName(), fi);
        ((FieldInfo) fi).dd = this;
    }

    /** only meant for building of temporary types */
    public void addField(FieldDefinition fi) {
        if (!isTemporary())
            throw new RuntimeException("can't add field to non-temporary type");
        addField1(fi);
        // the field cannot be of set type...
        // if(fieldIndexes==null)
        // fieldIndexes=new Hashtable();
        // fieldIndexes.put(fi.getName(), new Integer(fieldIndexes.size()));
    }

    RecordInfo(java.net.URL origin, String path) {
        name = path;
        this.origin = origin;
        // templateArgumentNames= new Vector(0);
    }

    RecordInfo(RecordInfo ri, String subfield) {
        // initStandardFields(subfield);
        name = ri.name;
        origin = ri.origin;
        this.subfield = subfield;
        this.papa = ri;
        // this.templateArgumentNames= ri.templateArgumentNames;
        ptrSubfield = papa.ptrSubfield + "->" + subfield;
        subfieldPtr = papa.subfieldPtr + subfield + "->";
    }

    static int infos = NamedResources.makeStaticCache("Data definitions parsed", new NamedResourceFactory() {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        protected Object getHashObject(Object name) {
            java.net.URL u = RecordParser.findDataDefinition((String) name, "mdd");
            if (u == null) {
                throw new DataDefinitionNotFoundError((String) name);
            }
            return u;
        }

        protected Object makeResource(Object name, Object hashName) {
            String nm = (String) name;
            if (nm.indexOf('/') != -1)
                nm = nm.replace('/', '.').substring(1);
            return new RecordInfo((java.net.URL) hashName, nm);
        }

        protected void configureResource(Object name, Object hashName, Object resource) {
            new RecordParser().parse((RecordInfo) resource);
        }
    });

    /**
     * returns the record info with the given absolute name
     * 
     * @throws org.makumba.DataDefinitionNotFoundError
     *             if the name is not a valid record info name
     * @throws org.makumba.DataDefinitionParseError
     *             if the syntax is wrong or a referred resource can't be found
     */
    public static DataDefinition getRecordInfo(String name) {
        int n = name.indexOf("->");
        if (n == -1) {
            try {
                return getSimpleRecordInfo(name);
            } catch (DataDefinitionNotFoundError e) {
                n = name.lastIndexOf(".");
                if (n == -1)
                    throw e;
                try {
                    return getRecordInfo(name.substring(0, n) + "->" + name.substring(n + 1));
                } catch (DataDefinitionParseError f) {
                    throw e;
                }
            }
        }

        DataDefinition ri = getRecordInfo(name.substring(0, n));
        while (true) {
            name = name.substring(n + 2);
            n = name.indexOf("->");
            if (n == -1)
                break;
            ri = ri.getFieldDefinition(name.substring(0, n)).getSubtable();
        }
        ri = ri.getFieldDefinition(name).getSubtable();
        return ri;
    }

    public static synchronized DataDefinition getSimpleRecordInfo(String path) {
        // this is to avoid a stupid error if path is "..."
        boolean dot = false;
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == '.') {
                if (dot)
                    throw new DataDefinitionParseError("two consecutive dots not allowed in type name");
                dot = true;
            } else
                dot = false;

            // check if type name looks valid (no weird characters or
            // spaces)
            if (path.charAt(i) != '/' && path.charAt(i) != '.')
                if (i == 0 && !Character.isJavaIdentifierStart(path.charAt(i)) || i > 0
                        && !Character.isJavaIdentifierPart(path.charAt(i)))
                    throw new DataDefinitionParseError("Invalid character \"" + path.charAt(i) + "\" in type name \""
                            + path + "\"");
        }

        if (path.indexOf('/') != -1) {
            path = path.replace('/', '.');
            if (path.charAt(0) == '.')
                path = path.substring(1);
        }

        DataDefinition ri = null;
        try {
            ri = (DataDefinition) NamedResources.getStaticCache(infos).getResource(path);
        } catch (RuntimeWrappedException e) {
            if (e.getReason() instanceof DataDefinitionParseError)
                throw (DataDefinitionParseError) e.getReason();
            if (e.getReason() instanceof MakumbaError)
                throw (MakumbaError) e.getReason();
            throw e;
        }
        if (path.indexOf("./") == -1)
            ((RecordInfo) ri).name = path;
        else
            MakumbaSystem.getMakumbaLogger("debug.abstr").severe("shit happens: " + path);
        return ri;
    }

    /** returns all the field names */
    public Vector getFieldNames() {
        return (Vector) fieldOrder.clone();
    }

    /** returns the field info associated with a name */
    public FieldDefinition getFieldDefinition(String nm) {
        return (FieldDefinition) fields.get(nm);
    }

    /**
     * the field with the respective index, null if such a field doesn't exist
     */
    public FieldDefinition getFieldDefinition(int n) {
        if (n < 0 || n >= fieldOrder.size())
            return null;
        return getFieldDefinition((String) fieldOrder.elementAt(n));
    }

    /** returns the path-like abstract-level name of this record info */
    public String getName() {
        return name + ptrSubfield;
    }

    // TODO: see if this method is still needed, can't it be expressed in
    // terms of the DataDefinition interface?
    String getBaseName() {
        return name;
    }

    /**
     * if this is a subtable, the field prefix is maintable-> TODO: see if this method is still needed, can't it be
     * expressed in terms of the DataDefinition interface?
     */
    public String fieldPrefix() {
        return subfieldPtr;
    }

    DataDefinition makeSubtable(String name) {
        return new RecordInfo(this, name);
    }

    /** which is the name of the index field, if any? */
    public String getIndexPointerFieldName() {
        return indexName;
    }

    /**
     * which is the name of the pointer to the main table, for set and internal set subtables
     */
    public String getSetOwnerFieldName() {
        return mainPtr;
    }

    // -----------
    /** the title field indicated, or the default one */
    public String getTitleFieldName() {
        return title;
    }

    /** which is the name of the creation timestamp field, if any? */
    public String getCreationDateFieldName() {
        return createName;
    }

    /** which is the name of the modification timestamp field, if any? */
    public String getLastModificationDateFieldName() {
        return modifyName;
    }

    /**
     * If this type is the data pointed by a 1-1 pointer or subset, return the type of the main record, otherwise return
     * null
     */
    public FieldDefinition getParentField() {
        if (papa == null)
            return null;
        return papa.getFieldDefinition(subfield);
    }

    /**
     * which is the name of the set member (Pointer, Character or Integer), for set subtables
     */
    public String getSetMemberFieldName() {
        return setField;
    }

    // moved from RecordHandler
    public void checkFieldNames(Dictionary d) {
        for (Enumeration e = d.keys(); e.hasMoreElements();) {
            Object o = e.nextElement();
            if (!(o instanceof String))
                throw new org.makumba.NoSuchFieldException(this,
                        "Dictionaries passed to makumba DB operations should have String keys. Key <" + o
                                + "> is of type " + o.getClass() + getName());
            if (this.getFieldDefinition((String) o) == null)
                throw new org.makumba.NoSuchFieldException(this, (String) o);
            String checkFieldName = (String) o;
        }
    }

    public String toString() {
        return getName();
    }

    public java.net.URL getOrigin() {
        return origin;
    }

    public long lastModified() {
        return new java.io.File(this.getOrigin().getFile()).lastModified();
    }

    // Validation definition specific methods //
    public DataDefinition getDataDefinition() {
        return this;
    }

    public void addValidationRule(ValidationRule rule) {
        validationRuleNames.put(rule.getRuleName(), rule);
    }

    public ValidationRule getValidationRule(String ruleName) {
        return (ValidationRule) validationRuleNames.get(ruleName);
    }

    public void addRule(String fieldName, Collection rules) {
        getFieldDefinition(fieldName).addValidationRule(rules);
    }

    public void addRule(String fieldName, ValidationRule rule) {
        getFieldDefinition(fieldName).addValidationRule(rule);
    }

    public Collection getValidationRules(String fieldName) {
        return getFieldDefinition(fieldName).getValidationRules();
    }

    public ValidationDefinition getValidationDefinition() {
        // now parse the validation definition
        // TODO: parse only once, use a boolean flag to discover
        if (!alreadyParsed) {
            RecordParser recordParser = new RecordParser(this, new RecordParser());
            recordParser.parseValidationDefinition();
            alreadyParsed = true;
        }
        return this;
    }

    public ArrayList getRulesSyntax() {
        return operators;
    }

    // Mutliple unique keys methods
    public MultipleUniqueKeyDefinition[] getMultiFieldUniqueKeys() {
        return (MultipleUniqueKeyDefinition[]) multiFieldUniqueList.values().toArray(
            new MultipleUniqueKeyDefinition[multiFieldUniqueList.values().size()]);
    }

    public void addMultiUniqueKey(MultipleUniqueKeyDefinition definition) {
        multiFieldUniqueList.put(definition.getFields(), definition);
    }

    public boolean hasMultiUniqueKey(String[] fieldNames) {
        return multiFieldUniqueList.get(fieldNames) != null;
    }

}
