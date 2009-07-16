package org.makumba.providers.datadefinition.mdd;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class DataDefinitionImpl implements DataDefinition, ValidationDefinition, Serializable {
        
    private static final long serialVersionUID = 5973863780194787175L;

    public static final String ENUM_FIELD_NAME = "enum";

    /** name of the data definition **/
    protected String name;
    
    /** pointer to the subfield, for construction of subfield names dynamically **/
    protected String ptrSubfield;
    
    /** name of the index field **/
    protected String indexName = "";
    
    /** the title field **/
    protected String titleField;
    
    /** the original title expression **/
    protected String titleFieldExpr;
    
    /** the type of the title expression **/
    protected enum TitleFieldType {FIELD, FUNCTION};
    protected TitleFieldType titleFieldType;
    
    /** origin of the data definition **/
    protected URL origin;
    
    /** parent MDD of the subfield **/
    protected DataDefinition parent;
    
    /** name of the parent field, for ptrOne and setComplex **/
    protected String fieldNameInParent;
    
    /** indicator if this is MDD is a file subfield **/
    protected boolean isFileSubfield = false;
    
    /** name of the enumerator field, for setIntEnum and charIntEnum **/
    protected String setMemberFieldName;
    
    /** name of the field that holds the relational pointer to the parent field **/
    protected String setOwnerFieldName;
        
    protected LinkedHashMap<String, FieldDefinition> fields = new LinkedHashMap<String, FieldDefinition>();
    
    protected LinkedHashMap<String, ValidationRule> validationRules = new LinkedHashMap<String, ValidationRule>();    
    
    protected LinkedHashMap<Object, MultipleUniqueKeyDefinition> multiFieldUniqueList = new LinkedHashMap<Object, MultipleUniqueKeyDefinition>();

    protected LinkedHashMap<String, DataDefinition.QueryFragmentFunction> functions = new LinkedHashMap<String, DataDefinition.QueryFragmentFunction>();

    private transient MDDNode mddNode;
    
    private transient LinkedHashMap<String, FieldNode> postponedFields = new LinkedHashMap<String, FieldNode>();

    
    /** make a virtual data definition **/
    public DataDefinitionImpl(String name) {
        this.name = name;
        this.origin = null;
    }
    
    /** constructor for virtual subfield data definitions, like file **/
    public DataDefinitionImpl(String name, DataDefinitionImpl parent) {
        this.name = parent.getName();
        this.ptrSubfield = "->" + name;
        this.origin = parent.origin;
        this.parent = parent;
        this.fieldNameInParent = name;
        addStandardFields(name);
    }
    
    
    /** constructor for subfield data definitions during parsing **/
    public DataDefinitionImpl(String fieldName, MDDNode mdd, DataDefinition parent) {
        this.fieldNameInParent = fieldName;
        this.parent = parent;
        this.mddNode = mdd;
    }
    
    /** constructor for data definitions during parsing **/
    public DataDefinitionImpl(MDDNode mdd) {
        this.mddNode = mdd;
    }
    
    /**
     * Builds the {@link DataDefinitionImpl} based on the {@link MDDNode}. This step needs to be done separately,
     * as a MDD may refer to itself (through ptr or set).
     */
    protected void build() {
        this.name = parent != null ? parent.getName() : mddNode.getName();
        this.ptrSubfield = mddNode.ptrSubfield;
        this.origin = mddNode.origin;
        this.indexName = mddNode.indexName;
        this.isFileSubfield = mddNode.isFileSubfield;
        switch(mddNode.titleField.titleType) {
            case MDDTokenTypes.FIELD:
                this.titleFieldType = TitleFieldType.FIELD;
                this.titleFieldExpr = mddNode.titleField.getText();
                break;
            case MDDTokenTypes.FUNCTION:
                this.titleFieldType = TitleFieldType.FUNCTION;
                this.titleFieldExpr = mddNode.titleField.functionName;
                break;
        }
        
        this.multiFieldUniqueList = mddNode.multiFieldUniqueList;
        
        addStandardFields(getName());
        addFieldNodes(mddNode.fields, false);
        addValidationRules(mddNode.validationRules);
        addFunctions(mddNode.functions);

        // evaluate the title, when all fields and functions are processed
        evaluateTitle();

        // finally also add the postponed fields, which basically are fields that in some way point to this DD
        addFieldNodes(postponedFields, true);
        

    }
    
    /**
     * add the validation rules to the data definition, i.e. make sure each field definition gets the validation definition it needs.
     * we can do this only at this stage since we now work with field definitions and not field nodes.
     */
    private void addValidationRules(LinkedHashMap<String, ValidationRule> validationRules) {
        
        for(ValidationRule v : validationRules.values()) {
            
            ValidationRuleNode n = (ValidationRuleNode) v;
            
            for(String field : v.getValidationRuleArguments()) {
                FieldDefinition fd = this.getFieldOrPointedFieldDefinition(field);
                if(fd == null) {
                    // we are in a subfield
                    FieldDefinition subFd = this.getFieldDefinition(n.field.mdd.fieldNameInParent);
                    if(subFd == null) {
                        // we try our luck another way
                        subFd = this.getFieldDefinition(n.field.subfield.fieldNameInParent);
                        if(subFd == null) {
                            throw new RuntimeException("could not retrieve field definition for validation rule " + v.getRuleName());
                        }
                    }
                    
                   fd = subFd.getSubtable().getFieldDefinition(field);
                    
                    
                    if(fd == null) {
                        throw new RuntimeException("could not retrieve field definition for validation rule " + v.getRuleName());
                    }
                }
                fd.addValidationRule(v);
            }
        }
        
        this.validationRules = validationRules;
    }

    /**
     * method needed for converting FieldNodes into FieldDefinitionImpl objects.
     * FieldNode cannot implement FieldDefinition due to a conflict with the getType() type with ANTLR.
     */
    private void addFieldNodes(LinkedHashMap<String, FieldNode> fields, boolean secondPass) {
        for(FieldNode f : fields.values()) {
            FieldDefinitionImpl field = new FieldDefinitionImpl(this, f);
            
            switch(f.makumbaType) {
                case FILE:
                    // when we have a file field, we transform it into an ptrOne with a specific structure
                    FieldDefinitionImpl fileField = buildFileField(f);
                    addField(fileField);
                    break;
                case PTRONE:
                    addField(field);
                    break;
                case SETCOMPLEX:
                    field.subfield.setOwnerFieldName = addPointerToParent(field, getPointerName(this.getName(), field));
                    addField(field);
                    break;
                case SET:
                    if(!secondPass && field.getPointedType() == this) {
                        postponedFields.put(f.name, f);
                        continue;
                    }
                    
                    field.subfield.setOwnerFieldName = addPointerToParent(field, getPointerName(this.getName(), field));
                    field.subfield.setMemberFieldName = addPointerToForeign(field);
                    addField(field);
                    break;
                case SETINTENUM:
                case SETCHARENUM:
                    field.subfield.setOwnerFieldName = addPointerToParent(field, getPointerName(this.getName(), field));
                    addField(field);

                    // add enum field
                    String type = "";
                    if(field.type == FieldType.SETINTENUM)
                        type = "intEnum";
                    if(field.type == FieldType.SETCHARENUM)
                        type = "charEnum";
                    
                    FieldDefinitionImpl enumField = new FieldDefinitionImpl(ENUM_FIELD_NAME, type);
                    enumField.charEnumValues = field.charEnumValues;
                    enumField.charEnumValuesDeprecated = field.charEnumValuesDeprecated;
                    enumField.intEnumValues = field.intEnumValues;
                    enumField.intEnumValuesDeprecated = field.intEnumValuesDeprecated;
                    enumField.mdd = field.subfield;
                    enumField.description = field.name;
                    field.subfield.addField(enumField);
                    field.subfield.setMemberFieldName = ENUM_FIELD_NAME;
                    field.subfield.titleFieldExpr = "enum";
                    field.subfield.titleField = "enum";
                    break;
                default:
                    addField(field);
            }
        }
    }
    
    /**
     * Adds a pointer to this {@link DataDefinitionImpl} into the subField
     * 
     * @param subField the subField to which to add a pointer
     * @return the name of the pointer field in the subField, computed so as not to collide with existing subField fields
     */
    private String addPointerToParent(FieldDefinitionImpl subField, String parentName) {
        
        FieldDefinitionImpl ptr = new FieldDefinitionImpl(parentName, "ptrRel");
        subField.subfield.addField(ptr);
        ptr.mdd = subField.subfield;
        ptr.fixed = true;
        ptr.notNull = true;
        ptr.type = FieldType.PTRREL;
        ptr.pointed = this;
        ptr.pointedType = this.getName();
        ptr.description = "relational pointer";
        
        return parentName;
    }
    
    /**
     * Adds a pointer to a foreign {@link DataDefinition} to the subField. Used for external sets only.
     * @param subField the subfield of the set
     * @return the name of the pointer field in the subField
     */
    private String addPointerToForeign(FieldDefinitionImpl subField) {
        
        // FIXME this is a hack in order to create the right names for the pointer to the parent field in case of types that point to
        // themselves. this was the behavior in the old parser and somehow not having it messes up the behavior of other parts of mak
        // probably the order of creation of some types is different, i.e. those pointers were added first before any other fields
        // but this is not the case anymore
        String pointed = null;
        if(subField.getPointedType() == this) {
            pointed = getPointerName(subField.pointedType, subField);
        } else {
            pointed = subField.pointedType;
        }
        int s = pointed.indexOf("->");
        if(s != -1)
            pointed = pointed.substring(s+2);
        int n = pointed.lastIndexOf('.');
        if (n != -1) {
            pointed = pointed.substring(n + 1);
        }
        
        FieldDefinitionImpl ptr = new FieldDefinitionImpl(pointed, "ptrRel");
        subField.subfield.addField(ptr);
        subField.subfield.titleField = subField.getPointedType().getTitleFieldName();
        ptr.mdd = subField.subfield;
        ptr.fixed = true;
        ptr.notNull = true;
        ptr.type = FieldType.PTRREL;
        ptr.pointed = subField.getPointedType();
        ptr.pointedType = subField.pointedType;
        ptr.description = "relational pointer";
        
        return pointed;
    }

    /**
     * Computes the name for a pointer field in a subfield, so as not to collide with existing fields
     * @param typeName the original name of the type
     * @param subField the subfield to which to add the pointer
     * @return the name of the pointer field in the subField, computed so as not to collide with existing subField fields
     */
    private String getPointerName(String typeName, FieldDefinitionImpl subField) {
        int s = typeName.indexOf("->");
        if(s != -1)
            typeName = typeName.substring(s+2);
        int n = typeName.lastIndexOf('.');
        if (n != -1) {
            typeName = typeName.substring(n + 1);
        }
        
        while (subField.getSubtable().getFieldDefinition(typeName) != null) {
            typeName = typeName + "_";
        }
        return typeName;
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
        fi.pointedType = this.getName();
        fi.pointed = this;
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
    private FieldDefinitionImpl buildFileField(FieldNode f) {
        FieldDefinitionImpl fi = new FieldDefinitionImpl(f.name, "ptrOne");
        fi.mdd = this;
        DataDefinitionImpl dd = new DataDefinitionImpl(f.getName(), this);
        dd.isFileSubfield = true;
        dd.addField(new FieldDefinitionImpl("content", "binary", dd));
        dd.addField(new FieldDefinitionImpl("contentLength", "int", dd));
        dd.addField(new FieldDefinitionImpl("contentType", "char", dd));
        dd.addField(new FieldDefinitionImpl("originalName", "char", dd));
        dd.addField(new FieldDefinitionImpl("name", "char", dd));
        dd.addField(new FieldDefinitionImpl("imageWidth", "int", dd));
        dd.addField(new FieldDefinitionImpl("imageHeight", "int", dd));
        fi.subfield = dd;
        
        return fi;
    }
    
    
    private static final Pattern ident = Pattern.compile("[a-zA-Z]\\w*");
    
    /** pre-processes functions (adds this. everywhere needed) **/
    public void addFunctions(HashMap<String, QueryFragmentFunction> funcNames) {
        
        for (String fn : funcNames.keySet()) {
        
            StringBuffer sb = new StringBuffer();
            QueryFragmentFunction f = funcNames.get(fn);
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
                if (this.fields.get(id) != null || after == '(' && funcNames.get(id) != null) {
                    m.appendReplacement(sb, "this." + id);
                    found = true;
                }
            }
            m.appendTail(sb);
            if (found) {
                java.util.logging.Logger.getLogger("org.makumba.db.query.inline").fine(
                    queryFragment + " -> " + sb.toString());
                f = new QueryFragmentFunction(f.getName(), f.getSessionVariableName(), sb.toString(),
                        f.getParameters(), f.getErrorMessage());

            }
            addFunction(f.getName(), f);
        }
    }
    
    /**
     * evaluates the title field:
     * <ul>
     * <li>for simple field names, sets title to field name</li>
     * <li>for paths, checks the path and sets the title to the path</li>
     * <li>for functions, sets the title to the function expression</li>
     * <li>if no title indicated, take the first field in the DD as title</li>
     * </ul>
     * all the checking has already been done in {@link TitleFieldNode}
     * TODO add support for function calls in !title and for use of expressions
     */
    private void evaluateTitle() {
        if(titleFieldExpr == null) {
            // if we have no tite field set, the title field is the first field in the MDD
            // but not the PtrIndex
            if(fields.keySet().size() > 3) {
                titleField = (String) (Arrays.asList(fields.keySet().toArray())).get(3);
                // if this happens to be an MDD which points to itself we may have to use the following trick to get the right title
            } else if(fields.keySet().size() == 3 && postponedFields.keySet().size() > 0) {
                titleField = (String) (Arrays.asList(postponedFields.keySet().toArray())).get(0);
            } else {
                titleField = (String) (Arrays.asList(fields.keySet().toArray())).get(0);
            }
        } else if(titleFieldType == TitleFieldType.FUNCTION){
            QueryFragmentFunction f = functions.get(titleFieldExpr);
            // TODO here we could inline a function call from title
            titleField = f.getName() + "()"; //f.getQueryFragment(); 
        } else {
            titleField = titleFieldExpr;
        }
    }
    

    /** base methods **/
    
    public String getName() {
        return (parent != null ? parent.getName() : name ) + ptrSubfield;
    }
    
    public String getTitleFieldName() {
        return this.titleField;
    }
    
    public boolean isTemporary() {
        return origin == null;
    }

    // FIXME for now we keep the old behaviour but we may want to be more robust here
    public long lastModified() {
        return new java.io.File(this.origin.getFile()).lastModified();
    }

    public DataDefinition getDataDefinition() {
        return this;
    }
    
    
    /** methods related to fields **/

    public void addField(FieldDefinition fd) {
        ((FieldDefinitionImpl)fd).mdd = this;
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
        functions.put(name, function);
    }

    public Collection<QueryFragmentFunction> getActorFunctions() {
        ArrayList<QueryFragmentFunction> actorFunctions = new ArrayList<QueryFragmentFunction>();
        for (QueryFragmentFunction function : functions.values()) {
            if (function.isActorFunction()) {
                actorFunctions.add(function);
            }
        }
        return actorFunctions;
    }
    
    public QueryFragmentFunction getFunction(String name) {
        return functions.get(name);
    }

    public Collection<QueryFragmentFunction> getFunctions() {
        return functions.values();
    }

    public Collection<QueryFragmentFunction> getSessionFunctions() {
        ArrayList<QueryFragmentFunction> sessionFunctions = new ArrayList<QueryFragmentFunction>();
        for (QueryFragmentFunction function : functions.values()) {
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
        return setMemberFieldName;
    }

    public String getSetOwnerFieldName() {
        return setOwnerFieldName;
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

    /** which is the name of the creation timestamp field, if any? */
    public String getCreationDateFieldName() {
        return createName;
    }

    /** which is the name of the modification timestamp field, if any? */
    public String getLastModificationDateFieldName() {
        return modifyName;
    }

    @Override
    public String toString() {
        return getName();
    }
    
    public String toString1() {
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
    
    sb.append("\n   === Functions \n\n");
    
    for(DataDefinition.QueryFragmentFunction f : functions.values()) {
        sb.append(f.toString()+ "\n");
    }
    
    return sb.toString();
    }

    public String getStructure() {
        StringBuffer sb = new StringBuffer();
        
        sb.append("getName() " + getName() + "\n");
        sb.append("getFieldNames()\n");
        for(String n : getFieldNames()) {
            sb.append(n + "\n");
        }
        sb.append("isTemporary() " + isTemporary() + "\n");
        sb.append("getTitleFieldName() " + getTitleFieldName() + "\n");
        sb.append("getIndexPointerFieldName() " + getIndexPointerFieldName() + "\n");
        sb.append("getParentField()\n");
        sb.append(getParentField() + "\n");
        sb.append("getSetMemberFieldName() " + getSetMemberFieldName() + "\n");
        sb.append("getSetOwnerFieldName() " + getSetOwnerFieldName() + "\n");
        sb.append("lastModified() " + lastModified() + "\n");
        sb.append("getFieldDefinition()\n");
        for(String n : getFieldNames()) {
            sb.append(((FieldDefinitionImpl)getFieldDefinition(n)).getStructure() + "\n");
        }
        sb.append("getReferenceFields()\n");
        for(FieldDefinition fi : getReferenceFields()) {
            sb.append(((FieldDefinitionImpl)fi).getStructure() + "\n");
        }
        
        
        
        return sb.toString();
        
    }
}
