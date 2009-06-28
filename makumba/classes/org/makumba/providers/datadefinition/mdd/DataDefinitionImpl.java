package org.makumba.providers.datadefinition.mdd;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.ValidationDefinition;
import org.makumba.ValidationRule;
import org.makumba.providers.datadefinition.mdd.validation.MultiUniquenessValidationRule;

/**
 * Implementation of the {@link DataDefinition} interface.<br>
 * This implementation gets its data from a {@link MDDNode} after parsing, and adds the necessary functionality to it.
 * 
 * @author Manuel Gay
 * @version $Id: DataDefinitionImpl.java,v 1.1 23.06.2009 11:52:36 gaym Exp $
 */
public class DataDefinitionImpl implements DataDefinition, ValidationDefinition {
    
    /** name of the data definition **/
    protected String name;
    
    /** name of the index field **/
    protected String indexName = "";
    
    /** the title field **/
    protected String titleField;
    
    /** origin of the data definition **/
    protected URL origin;
    
    /** parent MDD of the subfield **/
    protected DataDefinition parent;
    
    /** name of the parent field, for ptrOne and setComplex **/
    protected String fieldNameInParent;
    
    /** indicator if this is MDD is a file subfield **/
    protected boolean isFileSubfield = false;
    
    protected LinkedHashMap<String, FieldDefinition> fields = new LinkedHashMap<String, FieldDefinition>();
    
    protected LinkedHashMap<String, ValidationRule> validationRules = new LinkedHashMap<String, ValidationRule>();    
    
    protected HashMap<String, QueryFragmentFunction> functionNames = new HashMap<String, QueryFragmentFunction>();

    private HashMap<Object, MultipleUniqueKeyDefinition> multiFieldUniqueList = new HashMap<Object, MultipleUniqueKeyDefinition>();

    
    /** make a virtual data definition **/
    public DataDefinitionImpl(String name) {
        this.name = name;
        this.origin = null;
    }
    
    /** constructor for virtual subfield data definitions **/
    public DataDefinitionImpl(String name, DataDefinition parent) {
        this.name = parent.getName() + "->" + name;
        this.origin = null;
        this.parent = parent;
        this.fieldNameInParent = name;
        addStandardFields(name);
    }
    
    
    /** constructor for subfield data definitions during parsing **/
    public DataDefinitionImpl(MDDNode mdd, DataDefinition parent) {
        System.out.println("now creating MDD for subfield " + mdd.name + " in type " + parent.getName());
        this.indexName = mdd.indexName;
        this.isFileSubfield = mdd.isFileSubfield;
        this.name = mdd.name;
        this.origin = mdd.origin;
        this.titleField = mdd.titleField.getText();
        this.validationRules = mdd.validationRules;
        this.parent = parent;
        
        System.out.println("now going to add the fields of the subfield");
        addStandardFields(name);
        addFieldNodes(mdd.fields);
    }
    
    /** constructor for data definitions during parsing **/
    public DataDefinitionImpl(MDDNode mdd) {
        System.out.println("creating dataDef " + mdd.name);
        this.indexName = mdd.indexName;
        this.isFileSubfield = mdd.isFileSubfield;
        this.name = mdd.name;
        this.origin = mdd.origin;
        this.titleField = mdd.titleField.getText();
        this.validationRules = mdd.validationRules;
        this.multiFieldUniqueList = mdd.multiFieldUniqueList;

        System.out.println("populating fields of " + mdd.name);
        addStandardFields(name);
        addFieldNodes(mdd.fields);
    }
    
    /**
     * method needed for converting FieldNodes into FieldDefinitionImpl objects.
     * FieldNode cannot implement FieldDefinition due to a conflict with the getType() type with ANTLR.
     */
    private void addFieldNodes(LinkedHashMap<String, FieldNode> fields) {
        for(FieldNode f : fields.values()) {
            System.out.println("now adding field " + f.name + " into mdd " +this.name);
            // when we have a file field, we transform it into an ptrOne with a specific structure
            if(f.makumbaType == FieldType.FILE) {
                System.out.println("FILE!");
                addField(buildFileField(this, f));
            } else {
                addField(new FieldDefinitionImpl(this, f));
            }
        }
    }
    
    /**
     * adds standard fields
     */
    private void addStandardFields(String name) {
        FieldDefinitionImpl fi;
        
        int k = name.lastIndexOf(".");
        if(k > -1)
            name = name.substring(k+1);
        int j = name.lastIndexOf("->");
        if(j > -1)
            name = name.substring(j+2);

        indexName = name;

        fi = new FieldDefinitionImpl(indexName, this);
        fi.type = FieldType.PTRINDEX;
        fi.description = "Unique index";
        fi.fixed = true;
        fi.notNull = true;
        fi.unique = true;
        addField(fi);

        fi = new FieldDefinitionImpl(DataDefinition.modifyName, this);
        fi.type = FieldType.DATEMODIFY;
        fi.notNull = true;
        fi.description = "Last modification date";
        addField(fi);

        fi = new FieldDefinitionImpl(DataDefinition.createName, this);
        fi.type = FieldType.DATECREATE;
        fi.description = "Creation date";
        fi.fixed = true;
        fi.notNull = true;
        addField(fi);
        
    }
    
    
    /**
     * builds a FieldDefinition for the file type
     */
    private FieldDefinition buildFileField(DataDefinitionImpl dataDefinitionImpl, FieldNode f) {
        System.out.println("now building file Field definition for field " + f.name + " in mdd " + dataDefinitionImpl.name);
        FieldDefinitionImpl fi = new FieldDefinitionImpl(f.name, "ptrOne");
        DataDefinitionImpl dd = new DataDefinitionImpl(f.name, this);
        dd.isFileSubfield = true;
        dd.addStandardFields(f.name);
        dd.addField(new FieldDefinitionImpl("content", "binary"));
        dd.addField(new FieldDefinitionImpl("contentLength", "int"));
        dd.addField(new FieldDefinitionImpl("contentType", "char"));
        dd.addField(new FieldDefinitionImpl("originalName", "char"));
        dd.addField(new FieldDefinitionImpl("name", "char"));
        dd.addField(new FieldDefinitionImpl("imageWidth", "int"));
        dd.addField(new FieldDefinitionImpl("imageHeight", "int"));
        fi.subfield = dd;
        
        return fi;
    }

    /** base methods **/
    
    public String getName() {
        return name;
    }
    
    public String getTitleFieldName() {
        return this.titleField;
    }
    
    public boolean isTemporary() {
        return origin == null;
    }

    public long lastModified() {
        return new java.io.File(this.origin.getFile()).lastModified();
    }

    public DataDefinition getDataDefinition() {
        return this;
    }
    
    
    /** methods related to fields **/

    public void addField(FieldDefinition fd) {
        fields.put(fd.getName(), fd);
    }
    
    public FieldDefinition getFieldDefinition(String name) {
        return fields.get(name);
    }

    public FieldDefinition getFieldDefinition(int n) {
        if (n < 0 || n >= fields.size()) {
            return null;
        }
        return (FieldDefinition) fields.values().toArray()[n];
    }

    public Vector<String> getFieldNames() {
        return new Vector<String>(fields.keySet());
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


    /** which is the name of the index field, if any? */
    public String getIndexPointerFieldName() {
        return indexName;
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
    
    
    
    /** methods related to multiple uniqueness keys **/

    public void addMultiUniqueKey(MultipleUniqueKeyDefinition definition) {
        multiFieldUniqueList.put(definition.getFields(), definition);
    }

    public boolean hasMultiUniqueKey(String[] fieldNames) {
        return multiFieldUniqueList.get(fieldNames) != null;
    }

    public MultipleUniqueKeyDefinition[] getMultiFieldUniqueKeys() {
        return multiFieldUniqueList.values().toArray(
            new MultipleUniqueKeyDefinition[multiFieldUniqueList.values().size()]);
    }
    
    
    
    
    
    /** methods for checks **/
    
    public void checkFieldNames(Dictionary<String, Object> d) {
        for (Enumeration<String> e = d.keys(); e.hasMoreElements();) {
            String s = e.nextElement();
            if (this.getFieldDefinition(s) == null) {
                throw new org.makumba.NoSuchFieldException(this, s);
            }
        }
    }

    // TODO refactor this, use Makumba type
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
                    getFieldDefinition(fieldName).checkUpdate(d);
            }
        }
    }
    
    
    
    
    
    /** methods for functions **/
    
    public void addFunction(String name, QueryFragmentFunction function) {
        functionNames.put(name, function);
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
    
    public QueryFragmentFunction getFunction(String name) {
        return functionNames.get(name);
    }

    public Collection<QueryFragmentFunction> getFunctions() {
        return functionNames.values();
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
    
    
    

    

    /**
     * If this type is the data pointed by a 1-1 pointer or subset (ptrOne and setComplex), return the type of the main record, otherwise return
     * null
     */
    public FieldDefinition getParentField() {
        if (parent == null) {
            return null;
        }
        return parent.getFieldDefinition(fieldNameInParent);
    }
    
    
    
    public String getSetMemberFieldName() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getSetOwnerFieldName() {
        // TODO Auto-generated method stub
        return null;
    }

    
    
    
    /** methods for validation definitions **/

    public ValidationDefinition getValidationDefinition() {
        return this;
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


    public boolean hasValidationRules() {
        return validationRules.size() > 0;
    }
    

    public ValidationRule getValidationRule(String ruleName) {
        return validationRules.get(ruleName);
    }

    public String getCreationDateFieldName() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getLastModificationDateFieldName() {
        // TODO Auto-generated method stub
        return null;
    }   
    
    
    @Override
    public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("==== MDD " + name + "\n");
    sb.append("   == origin: " + origin + "\n");
    sb.append("   == indexName: " + indexName + "\n");
    sb.append("   == titleField: " + titleField + "\n");
    if(parent != null)
        sb.append("   == parent: " + parent.getName() + "\n");
    sb.append("   == parentFieldName: " + fieldNameInParent + "\n");
    sb.append("   == isFileSubfield: " + isFileSubfield + "\n");
    sb.append("\n   === Fields \n\n");
    
    for(FieldDefinition f : fields.values()) {
        sb.append(f.toString()+ "\n");
    }
    
    sb.append("\n   === Validation rules \n\n");
    
    for(ValidationRule r : validationRules.values()) {
        sb.append(r.toString() + "\n");
    }
    
    
    sb.append("\n   === Multi-unique keys \n\n");
    
    for(DataDefinition.MultipleUniqueKeyDefinition k : multiFieldUniqueList.values()) {
        sb.append(k.toString()+ "\n");
    }
    
    return sb.toString();
    }
}
