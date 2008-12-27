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
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.DataDefinitionNotFoundError;
import org.makumba.DataDefinitionParseError;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaError;
import org.makumba.ValidationDefinition;
import org.makumba.ValidationRule;
import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;
import org.makumba.commons.RuntimeWrappedException;

/**
 * This is the internal representation of the org.makumba. One can make RecordHandlers based on an instance of this
 * class and do useful things with it (generate sql tables, html code, etc)
 */
public class RecordInfo implements java.io.Serializable, DataDefinition, ValidationDefinition {
    private static final long serialVersionUID = 1L;

    protected static String webappRoot;

    public static void setWebappRoot(String s) {
        webappRoot = s;
    }

    java.net.URL origin;

    String name;

    Properties templateValues;

    // Vector templateArgumentNames;

    Vector<String> fieldOrder = new Vector<String>();

    HashMap<String, QueryFragmentFunction> functionNames = new HashMap<String, QueryFragmentFunction>();

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

    HashMap<String, FieldDefinition> fields = new HashMap<String, FieldDefinition>();

    // Hashtable fieldIndexes=null;

    // for subtables
    String subfield;

    String ptrSubfield = "";

    String subfieldPtr = "";

    RecordInfo papa;

    private HashMap<String, ValidationRule> validationRuleNames = new HashMap<String, ValidationRule>();

    private HashMap<Object, MultipleUniqueKeyDefinition> multiFieldUniqueList = new HashMap<Object, MultipleUniqueKeyDefinition>();

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
        if (!isTemporary()) {
            throw new RuntimeException("can't add field to non-temporary type");
        }
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

    public static int infos = NamedResources.makeStaticCache("Data definitions parsed", new NamedResourceFactory() {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        @Override
        protected Object getHashObject(Object name) {
            java.net.URL u = RecordParser.findDataDefinition((String) name, "mdd");
            if (u == null) {
                throw new DataDefinitionNotFoundError((String) name);
            }
            return u;
        }

        @Override
        protected Object makeResource(Object name, Object hashName) {
            String nm = (String) name;
            if (nm.indexOf('/') != -1) {
                nm = nm.replace('/', '.').substring(1);
            }
            return new RecordInfo((java.net.URL) hashName, nm);
        }

        @Override
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
                if (n == -1) {
                    throw e;
                }
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
            if (n == -1) {
                break;
            }
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
                if (dot) {
                    throw new DataDefinitionParseError("two consecutive dots not allowed in type name");
                }
                dot = true;
            } else {
                dot = false;
            }

            // check if type name looks valid (no weird characters or
            // spaces)
            if (path.charAt(i) != '/' && path.charAt(i) != '.') {
                if (i == 0 && !Character.isJavaIdentifierStart(path.charAt(i)) || i > 0
                        && !Character.isJavaIdentifierPart(path.charAt(i))) {
                    throw new DataDefinitionParseError("Invalid character \"" + path.charAt(i) + "\" in type name \""
                            + path + "\"");
                }
            }
        }

        if (path.indexOf('/') != -1) {
            path = path.replace('/', '.');
            if (path.charAt(0) == '.') {
                path = path.substring(1);
            }
        }

        DataDefinition ri = null;
        try {
            ri = (DataDefinition) NamedResources.getStaticCache(infos).getResource(path);
        } catch (RuntimeWrappedException e) {
            if (e.getCause() instanceof DataDefinitionParseError) {
                throw (DataDefinitionParseError) e.getCause();
            }
            if (e.getCause() instanceof DataDefinitionNotFoundError) {
                throw (DataDefinitionNotFoundError) e.getCause();
            }
            if (e.getCause() instanceof MakumbaError) {
                throw (MakumbaError) e.getCause();
            }
            throw e;
        }
        if (path.indexOf("./") == -1) {
            ((RecordInfo) ri).name = path;
        } else {
            java.util.logging.Logger.getLogger("org.makumba.debug.abstr").severe("shit happens: " + path);
        }
        return ri;
    }

    /** returns all the field names */
    public Vector<String> getFieldNames() {
        return new Vector<String>(fieldOrder);
    }

    public ArrayList<FieldDefinition> getReferenceFields() {
        ArrayList<FieldDefinition> l = new ArrayList<FieldDefinition>();
        for (FieldDefinition fieldDefinition : fields.values()) {
            FieldDefinition fd = fieldDefinition;
            if (fd.isPointer() || fd.isExternalSet() || fd.isComplexSet()) {
                l.add(fd);
            }
        }
        return l;
    }

    public ArrayList<FieldDefinition> getUniqueFields() {
        ArrayList<FieldDefinition> l = new ArrayList<FieldDefinition>();
        for (FieldDefinition fieldDefinition : fields.values()) {
            FieldDefinition fd = fieldDefinition;
            if (fd.isUnique() && !fd.isIndexPointerField()) {
                l.add(fd);
            }
        }
        return l;
    }

    /** returns the field info associated with a name */
    public FieldDefinition getFieldDefinition(String nm) {
        return fields.get(nm);
    }

    /** returns the field info associated with a name */
    public FieldDefinition getFieldOrPointedFieldDefinition(String nm) {
        if (getFieldDefinition(nm) != null) {
            return getFieldDefinition(nm);
        }
        String fieldName = nm;
        DataDefinition dd = this;

        int indexOf = -1;
        while ((indexOf = fieldName.indexOf(".")) != -1) {
            String subFieldName = fieldName.substring(0, indexOf);
            fieldName = fieldName.substring(indexOf + 1);
            FieldDefinition fieldDefinition = dd.getFieldDefinition(subFieldName);
            dd = fieldDefinition.getPointedType();
        }
        return dd.getFieldDefinition(fieldName);
    }

    /**
     * the field with the respective index, null if such a field doesn't exist
     */
    public FieldDefinition getFieldDefinition(int n) {
        if (n < 0 || n >= fieldOrder.size()) {
            return null;
        }
        return getFieldDefinition(fieldOrder.elementAt(n));
    }

    public QueryFragmentFunction getFunction(String name) {
        return functionNames.get(name);
    }

    public void addFunction(String name, QueryFragmentFunction function) {
        functionNames.put(name, function);
    }

    public Collection<QueryFragmentFunction> getFunctions() {
        return functionNames.values();
    }

    public Collection<QueryFragmentFunction> getActorFunctions() {
        ArrayList<QueryFragmentFunction> actorFunctions = new ArrayList<QueryFragmentFunction>();
        for (QueryFragmentFunction function : functionNames.values()) {
            if (function.isActorFunction()) {
                actorFunctions.add(function);
            }
        }
        return actorFunctions;
    }

    public Collection<QueryFragmentFunction> getSessionFunctions() {
        ArrayList<QueryFragmentFunction> sessionFunctions = new ArrayList<QueryFragmentFunction>();
        for (QueryFragmentFunction function : functionNames.values()) {
            if (function.isSessionFunction()) {
                sessionFunctions.add(function);
            }
        }
        return sessionFunctions;
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

    RecordInfo makeSubtable(String name) {
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
        if (papa == null) {
            return null;
        }
        return papa.getFieldDefinition(subfield);
    }

    /**
     * which is the name of the set member (Pointer, Character or Integer), for set subtables
     */
    public String getSetMemberFieldName() {
        return setField;
    }

    // moved from RecordHandler
    public void checkFieldNames(Dictionary<String, Object> d) {
        for (Enumeration<String> e = d.keys(); e.hasMoreElements();) {
            String s = e.nextElement();
            if (this.getFieldDefinition(s) == null) {
                throw new org.makumba.NoSuchFieldException(this, s);
            }
        }
    }

    @Override
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
        return validationRuleNames.get(ruleName);
    }

    public void addRule(String fieldName, Collection<ValidationRule> rules) {
        getFieldDefinition(fieldName).addValidationRule(rules);
    }

    public void addRule(String fieldName, ValidationRule rule) {
        getFieldDefinition(fieldName).addValidationRule(rule);
    }

    public Collection<ValidationRule> getValidationRules(String fieldName) {
        return getFieldDefinition(fieldName).getValidationRules();
    }

    public ValidationDefinition getValidationDefinition() {
        return this;
    }

    public boolean hasValidationRules() {
        return validationRuleNames.size() > 0;
    }

    // Multiple unique keys methods
    public MultipleUniqueKeyDefinition[] getMultiFieldUniqueKeys() {
        return multiFieldUniqueList.values().toArray(
            new MultipleUniqueKeyDefinition[multiFieldUniqueList.values().size()]);
    }

    public void addMultiUniqueKey(MultipleUniqueKeyDefinition definition) {
        multiFieldUniqueList.put(definition.getFields(), definition);
    }

    public boolean hasMultiUniqueKey(String[] fieldNames) {
        return multiFieldUniqueList.get(fieldNames) != null;
    }

    public void checkUpdate(String fieldName, Dictionary<String, Object> d) {
        Object o = d.get(fieldName);
        if (o != null) {
            switch (getFieldDefinition(fieldName).getIntegerType()) {
                case FieldDefinition._dateCreate:
                    throw new org.makumba.InvalidValueException(getFieldDefinition(fieldName),
                            "you cannot update a creation date");
                case FieldDefinition._dateModify:
                    throw new org.makumba.InvalidValueException(getFieldDefinition(fieldName),
                            "you cannot update a modification date");
                case FieldDefinition._ptrIndex:
                    throw new org.makumba.InvalidValueException(getFieldDefinition(fieldName),
                            "you cannot update an index pointer");
                default:
                    base_checkUpdate(fieldName, d);
            }
        }
    }

    private void base_checkUpdate(String fieldName, Dictionary<String, Object> d) {
        getFieldDefinition(fieldName).checkUpdate(d);
    }

}
