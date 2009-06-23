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
    protected String indexName;
    
    /** the title field **/
    protected String titleField;
    
    /** origin of the data definition **/
    protected URL origin;
    
    /** parent of the subfield **/
    protected DataDefinitionImpl parent;
    
    /** name of the parent field, for ptrOne and setComplex **/
    protected String parentFieldName;
    
    /** indicator if this is MDD is a file subfield **/
    protected boolean isFileSubfield = false;
    
    protected LinkedHashMap<String, FieldDefinition> fields = new LinkedHashMap<String, FieldDefinition>();
    
    protected LinkedHashMap<String, ValidationRule> validationRules = new LinkedHashMap<String, ValidationRule>();    
    
    protected HashMap<String, QueryFragmentFunction> functionNames = new HashMap<String, QueryFragmentFunction>();

    private HashMap<Object, MultipleUniqueKeyDefinition> multiFieldUniqueList = new HashMap<Object, MultipleUniqueKeyDefinition>();

    
    public DataDefinitionImpl(MDDNode mdd) {
        this.indexName = mdd.indexName;
        this.isFileSubfield = mdd.isFileSubfield;
        this.name = mdd.name;
        this.origin = mdd.origin;
        this.parent = new DataDefinitionImpl(mdd.parent);

        this.titleField = mdd.titleField.getText();
        addFieldNodes(mdd.fields);
  
        // TODO convert ValidationRuleNode to validation Rule or else.
        
//        this.validationRules = mdd.validationRules;

    }
    
    /**
     * method needed for converting FieldNodes into FieldDefinitionImpl objects.
     * FieldNode cannot implement FieldDefinition due to a conflict with the getType() type with ANTLR.
     */
    private void addFieldNodes(LinkedHashMap<String, FieldNode> fields) {
        for(FieldNode f : fields.values()) {
            addField(new FieldDefinitionImpl(this, f));
        }
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
        return parent.getFieldDefinition(parentFieldName);
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
}
