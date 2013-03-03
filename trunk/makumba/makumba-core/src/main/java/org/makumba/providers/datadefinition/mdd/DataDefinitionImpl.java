package org.makumba.providers.datadefinition.mdd;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.collections.map.ListOrderedMap;
import org.makumba.DataDefinition;
import org.makumba.DataDefinitionParseError;
import org.makumba.FieldDefinition;
import org.makumba.QueryFragmentFunction;
import org.makumba.QueryFragmentFunctions;
import org.makumba.ValidationDefinition;
import org.makumba.ValidationRule;
import org.makumba.providers.DataDefinitionProvider;

/**
 * Implementation of the {@link DataDefinition} interface.<br>
 * This implementation gets its data from a {@link MDDNode} after parsing, and adds the necessary functionality to it.
 * 
 * @author Manuel Bernhardt <manuel@makumba.org>
 * @version $Id: DataDefinitionImpl.java,v 1.1 23.06.2009 11:52:36 gaym Exp $
 */
public class DataDefinitionImpl implements DataDefinition, ValidationDefinition, Serializable {

    private static final long serialVersionUID = 5973863780194787175L;

    public static final String ENUM_FIELD_NAME = "enum";

    private transient DataDefinitionProvider ddp = DataDefinitionProvider.getInstance();

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
    protected enum TitleFieldType {
        FIELD,
        FUNCTION
    };

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

    SharedMddData sharedData;

    protected ListOrderedMap fields = new ListOrderedMap();

    private transient List<FieldDefinition> flist = makeFieldList();

    AbstractList<FieldDefinition> makeFieldList() {
        return new AbstractList<FieldDefinition>() {
            @Override
            public FieldDefinition get(int index) {
                return (FieldDefinition) fields.getValue(index);
            }

            @Override
            public int size() {
                return fields.size();
            }
        };
    }

    class SharedMddData implements Serializable {

        protected LinkedHashMap<String, ValidationRule> validationRules = new LinkedHashMap<String, ValidationRule>();

        protected LinkedHashMap<Object, MultipleUniqueKeyDefinition> multiFieldUniqueList = new LinkedHashMap<Object, MultipleUniqueKeyDefinition>();

        protected QueryFragmentFunctions functions = new QueryFragmentFunctions(DataDefinitionImpl.this);

        private transient MDDNode mddNode;

        private transient LinkedHashMap<String, FieldNode> postponedFields = new LinkedHashMap<String, FieldNode>();
    }

    /** make a virtual data definition **/
    public DataDefinitionImpl(String name) {
        this.name = name;
        this.origin = null; // this is explicit: we use the origin being null to indicated a temporary data definition
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
        this.sharedData = new SharedMddData();
        this.sharedData.mddNode = mdd;
    }

    /** constructor for data definitions during parsing **/
    public DataDefinitionImpl(MDDNode mdd) {
        this.sharedData = new SharedMddData();
        this.sharedData.mddNode = mdd;
    }

    /**
     * Builds the {@link DataDefinitionImpl} based on the {@link MDDNode}. This step needs to be done separately, as a
     * MDD may refer to itself (through ptr or set).
     */
    public void build() {
        this.name = parent != null ? parent.getName() : sharedData.mddNode.getName();
        this.ptrSubfield = sharedData.mddNode.ptrSubfield;
        this.origin = sharedData.mddNode.origin;
        this.indexName = sharedData.mddNode.indexName;
        this.isFileSubfield = sharedData.mddNode.isFileSubfield;
        switch (sharedData.mddNode.titleField.titleType) {
            case MDDTokenTypes.FIELD:
                this.titleFieldType = TitleFieldType.FIELD;
                this.titleFieldExpr = sharedData.mddNode.titleField.getText();
                break;
            case MDDTokenTypes.FUNCTION:
                this.titleFieldType = TitleFieldType.FUNCTION;
                this.titleFieldExpr = sharedData.mddNode.titleField.functionName;
                break;
        }

        this.sharedData.multiFieldUniqueList = sharedData.mddNode.multiFieldUniqueList;

        addStandardFields(getName());
        addFieldNodes(sharedData.mddNode.fields, false);
        addValidationRules(sharedData.mddNode.validationRules);
        addFunctions(sharedData.mddNode.getFunctions(false));

        // evaluate the title, when all fields and functions are processed
        evaluateTitle();

        // finally also add the postponed fields, which basically are fields that in some way point to this DD
        addFieldNodes(sharedData.postponedFields, true);

        // do the same for postponed functions
        addFunctions(sharedData.mddNode.getFunctions(true));

        // re-order fields so they appear in the natural order of the MDD
        // this is necessary since postponed fields are added last
        Set<String> postponedKeySet = sharedData.postponedFields.keySet();
        while (postponedKeySet.size() != 0) {
            String key = postponedKeySet.iterator().next();
            String postponed = key.substring(0, key.indexOf("####"));
            String previous = key.substring(key.indexOf("####") + 4);
            for (int i = 0; i < fields.size(); i++) {
                if (previous.length() == 0) {
                    fields.put(i, postponed, sharedData.postponedFields.get(key));
                    continue;
                }
                String fieldName = (String) fields.keyList().get(i);
                if (fieldName.equals(previous)) {
                    int oldIndex = fields.indexOf(postponed);
                    Object v = fields.remove(oldIndex);
                    fields.put(i + 1, postponed, v);
                    postponedKeySet.remove(key);
                }
            }
        }
        sharedData.mddNode = null;
        if (isTemporary()) {
            sharedData = null;
        }
    }

    /**
     * add the validation rules to the data definition, i.e. make sure each field definition gets the validation
     * definition it needs. we can do this only at this stage since we now work with field definitions and not field
     * nodes.
     */
    private void addValidationRules(LinkedHashMap<String, ValidationRule> validationRules) {

        for (ValidationRule v : validationRules.values()) {

            ValidationRuleNode n = (ValidationRuleNode) v;

            for (String field : v.getValidationRuleArguments()) {

                // TODO treat external sets if one day we have validation rules applying to them
                // TODO think whether that mechanism can be simplified & be unified with the one that assigns fields
                // from the parser

                FieldDefinition fd = null;

                // we first check if we are not in a subfield
                // there can be two cases
                // - we are a single validation rule which applies to one field that is member of a subfield, thus
                // belonging to it
                // - we are a multi-field validation rule which is member of a subfield. in that case, "field" is
                // actually the parent field, not a member field

                boolean hasParentDeclared = n.field != null && n.field.mdd.fieldNameInParent.length() > 0;
                boolean hasSubFieldDeclared = n.field != null && n.field.subfield != null
                        && n.field.subfield.fieldNameInParent.length() > 0;

                if (hasParentDeclared || hasSubFieldDeclared) {

                    // are we a (nested) subfield?
                    FieldDefinition subFd = null;
                    if (hasSubFieldDeclared) {
                        // nested subfield
                        if (n.field.subfield.parent.indexOf("->") > -1) {
                            // strip the parent type
                            String parentFieldName = n.field.subfield.parent.substring(this.getName().length()
                                    + "->".length());
                            FieldDefinition parent = this.getFieldOrPointedFieldDefinition(parentFieldName);
                            subFd = parent.getSubtable().getFieldDefinition(n.field.subfield.fieldNameInParent);
                        } else {
                            // simple subfield
                            subFd = this.getFieldDefinition(n.field.subfield.fieldNameInParent);
                        }
                    } else if (hasParentDeclared) {
                        subFd = this.getFieldDefinition(n.field.mdd.fieldNameInParent);
                    }

                    if (subFd == null) {
                        throw new RuntimeException("could not retrieve field definition for validation rule "
                                + v.getRuleName());
                    }
                    fd = subFd.getSubtable().getFieldDefinition(field);

                } else {
                    fd = this.getFieldOrPointedFieldDefinition(field);
                }

                if (fd == null) {
                    throw new RuntimeException("could not retrieve field definition for validation rule "
                            + v.getRuleName());
                }

                ((FieldDefinitionImpl) fd).addValidationRule(v);
            }
        }

        this.sharedData.validationRules = validationRules;
    }

    /**
     * method needed for converting FieldNodes into FieldDefinitionImpl objects. FieldNode cannot implement
     * FieldDefinition due to a conflict with the getType() type with ANTLR.
     */
    private void addFieldNodes(LinkedHashMap<String, FieldNode> fields, boolean secondPass) {
        String previousFieldName = null;
        for (FieldNode f : fields.values()) {
            FieldDefinitionImpl field = new FieldDefinitionImpl(this, f);

            switch (f.makumbaType) {
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
                    if (!secondPass && field.getPointedType() == this) {
                        // if the DD points to itself, we postpone the evaluation of this field or we get into an
                        // endless loop
                        // we keep a reference to the name of the previous field, in order to be able to place it back
                        // in
                        // the right order afterwards
                        sharedData.postponedFields.put(f.name + "####"
                                + (previousFieldName == null ? "" : previousFieldName), f);
                        previousFieldName = f.getName();
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
                    if (field.type == FieldType.SETINTENUM) {
                        type = "intEnum";
                    }
                    if (field.type == FieldType.SETCHARENUM) {
                        type = "charEnum";
                    }

                    FieldDefinitionImpl enumField = new FieldDefinitionImpl(ENUM_FIELD_NAME, type);
                    enumField.shared = field.shared;
                    enumField.description = field.name;
                    field.subfield.addField(enumField);
                    field.subfield.setMemberFieldName = ENUM_FIELD_NAME;
                    field.subfield.titleFieldExpr = "enum";
                    field.subfield.titleField = "enum";
                    break;
                default:
                    addField(field);
            }
            previousFieldName = f.getName();
        }
    }

    /**
     * Adds a pointer to this {@link DataDefinitionImpl} into the subField
     * 
     * @param subField
     *            the subField to which to add a pointer
     * @return the name of the pointer field in the subField, computed so as not to collide with existing subField
     *         fields
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
     * 
     * @param subField
     *            the subfield of the set
     * @return the name of the pointer field in the subField
     */
    private String addPointerToForeign(FieldDefinitionImpl subField) {

        // LEGACY this is a hack in order to create the right names for the pointer to the parent field in case of types
        // that point to
        // themselves. this was the behavior in the old parser and somehow not having it messes up the behavior of other
        // parts of mak
        // probably the order of creation of some types is different, i.e. those pointers were added first before any
        // other fields
        // but this is not the case anymore
        String pointed = null;

        if (subField.getPointedType() == this) {
            pointed = getPointerName(subField.pointedType, subField);
        } else if (subField.getPointedType() != this) {
            pointed = subField.pointedType;
        }

        // if the name of the foreign type is the same as the name of the parent type, we have to avoid name collision
        if (shortName(pointed).equals(shortName(this.name))) {
            pointed += "_";
            // not sure if this is the original behavior of the old parser, as it would not make sense to have a field
            // name with a _ in a subfield...
            while (subField.getSubtable().getFieldDefinition(pointed) != null) {
                pointed = pointed + "_";
            }
        }

        int s = pointed.indexOf("->");
        if (s != -1) {
            pointed = pointed.substring(s + 2);
        }

        pointed = shortName(pointed);

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

    private String shortName(String t) {
        return t.indexOf(".") > 0 ? t.substring(t.lastIndexOf(".") + 1) : t;
    }

    /**
     * Computes the name for a pointer field in a subfield, so as not to collide with existing fields
     * 
     * @param typeName
     *            the original name of the type
     * @param subField
     *            the subfield to which to add the pointer
     * @return the name of the pointer field in the subField, computed so as not to collide with existing subField
     *         fields
     */
    private String getPointerName(String typeName, FieldDefinitionImpl subField) {
        int s = typeName.indexOf("->");
        if (s != -1) {
            typeName = typeName.substring(s + 2);
        }

        typeName = shortName(typeName);

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

        name = shortName(name);
        int j = name.lastIndexOf("->");
        if (j > -1) {
            name = name.substring(j + 2);
        }

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

    public void addFunctions(Map<String, QueryFragmentFunction> funcNames) {
        for (QueryFragmentFunction f : funcNames.values()) {
            sharedData.functions.addFunction(f.getName(), f);
        }
    }

    /**
     * evaluates the title field:
     * <ul>
     * <li>for simple field names, sets title to field name</li>
     * <li>for paths, checks the path and sets the title to the path</li>
     * <li>for functions, sets the title to the function expression</li>
     * <li>if no title indicated, take the "name" field, if none exists, take the first field in the DD that is not a
     * ptr or set as title</li>
     * <li>if no title alternative found (e.g. a type with only a set or pointer), set the title field to the ptrIndex
     * of this type</li>
     * </ul>
     * all the checking has already been done in {@link TitleFieldNode} TODO add support for function calls in !title
     * and for use of expressions
     */
    private void evaluateTitle() {
        if (titleFieldExpr == null && sharedData.functions.getFunction("title") == null) {
            String ptrIndexName = (String) Arrays.asList(fields.keySet().toArray()).get(0);

            // check if we have a "name" field
            if (fields.containsKey("name")) {
                titleField = "name";
            } else if (fields.keySet().size() > 3) { // we have PtrIndex, TS_create, TS_modify

                // the title field is the first non-ptr field in the MDD
                // but not the PtrIndex
                titleField = getFirstNonPointerFieldName(3);

                if (titleField == null) {
                    // there was no non-pointer or non-set field in the MDD, so we take the ptrIndex
                    titleField = ((FieldDefinition) fields.get(ptrIndexName)).getName();
                }

                // if this happens to be an MDD which points to itself we may have to use the following trick to get the
                // right title
            } else if (fields.keySet().size() == 3 && sharedData.postponedFields.keySet().size() > 0) {
                String key = (String) Arrays.asList(sharedData.postponedFields.keySet().toArray()).get(0);
                titleField = key.substring(0, key.indexOf("####"));
            } else {
                titleField = getFirstNonPointerFieldName(0);

                if (titleField == null) {
                    // there was no non-pointer or non-set field in the MDD, so we take the ptrIndex
                    titleField = ((FieldDefinition) fields.get(ptrIndexName)).getName();
                }

            }
        } else if (titleFieldExpr == null && sharedData.functions.getFunction("title") != null) {
            // we have a title() function defined
            titleField = "title()";
        } else if (titleFieldType == TitleFieldType.FUNCTION) {
            QueryFragmentFunction f = sharedData.functions.getFunction(titleFieldExpr);
            // TODO here we could inline a function call from title
            titleField = f.getName() + "()"; // f.getQueryFragment();
        } else {
            titleField = titleFieldExpr;
        }

        // in the end, we also make the title field available as title() function if it was not defined that way
        if (sharedData.functions.getFunction("title") == null) {
            sharedData.functions.addFunction(
                "title",
                new QueryFragmentFunction(this, "title", "", titleField,
                        ddp.getVirtualDataDefinition("Parameters for function title"), ""));
        }
    }

    /**
     * Returns the name of the first field in the DD that is not a pointer or a set
     */
    private String getFirstNonPointerFieldName(int index) {
        String field = null;
        int initial = index;
        while (field == null && index < fields.size()) {
            FieldDefinition titleDef = (FieldDefinition) Arrays.asList(fields.values().toArray()).get(index);
            if (titleDef.isPointer() || titleDef.isSetType()) {
                index++;
                continue;
            }
            field = titleDef.getName();
        }
        String initialName = (String) Arrays.asList(fields.keySet().toArray()).get(initial);
        boolean isPtrOrSet = ((FieldDefinition) fields.get(initialName)).isPointer()
                || ((FieldDefinition) fields.get(initialName)).isSetType();
        if (field == null && !isPtrOrSet) {
            field = initialName;
        } else if (field == null && isPtrOrSet) {
            return null;
        }

        return field;
    }

    /** base methods **/

    @Override
    public String getName() {
        return (parent != null ? parent.getName() : name) + (ptrSubfield == null ? "" : ptrSubfield);
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getTitleFieldName() {
        return this.titleField;
    }

    @Override
    public boolean isTemporary() {
        return origin == null;
    }

    // FIXME for now we keep the old behavior but we may want to be more robust here
    @Override
    public long lastModified() {
        return new java.io.File(this.origin.getFile()).lastModified();
    }

    public DataDefinition getDataDefinition() {
        return this;
    }

    /** methods related to fields **/

    @Override
    public void addField(FieldDefinition fd) {
        ((FieldDefinitionImpl) fd).mdd = this;
        fields.put(fd.getName(), fd);
    }

    @Override
    public FieldDefinition getFieldDefinition(String name) {
        return (FieldDefinition) fields.get(name);
    }

    @Override
    public FieldDefinition getFieldDefinition(int n) {
        if (n < 0 || n >= fields.size()) {
            return null;
        }
        return (FieldDefinition) fields.getValue(n);
    }

    @Override
    public List<FieldDefinition> getFieldDefinitions() {
        return flist;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Vector<String> getFieldNames() {
        return new Vector<String>(fields.keySet());
    }

    /** returns the field info associated with a name */
    @Override
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
            if (fieldDefinition == null) {
                throw new DataDefinitionParseError("Field '" + fieldName + "' not defined in type " + dd.getName()
                        + "!");
            }
            dd = fieldDefinition.getPointedType();
        }
        return dd.getFieldDefinition(fieldName);
    }

    /** returns the field info associated with a name */
    @Override
    public QueryFragmentFunction getFunctionOrPointedFunction(String nm) {
        if (getFunctions().getFunction(nm) != null) {
            return getFunctions().getFunction(nm);
        }
        // FIXME: remove duplicated code from getFieldOrPointedFieldDefinition
        String fieldName = nm;
        DataDefinition dd = this;

        int indexOf = -1;
        while ((indexOf = fieldName.indexOf(".")) != -1) {
            String subFieldName = fieldName.substring(0, indexOf);
            fieldName = fieldName.substring(indexOf + 1);
            FieldDefinition fieldDefinition = dd.getFieldDefinition(subFieldName);
            dd = fieldDefinition.getPointedType();
        }
        return ddp.getQueryFragmentFunctions(dd.getName()).getFunction(fieldName);
    }

    /** which is the name of the index field, if any? */
    @Override
    public String getIndexPointerFieldName() {
        return indexName;
    }

    /** methods related to multiple uniqueness keys **/
    @Override
    public void addMultiUniqueKey(MultipleUniqueKeyDefinition definition) {
        sharedData.multiFieldUniqueList.put(definition.getFields(), definition);
    }

    @Override
    public boolean hasMultiUniqueKey(String[] fieldNames) {
        return sharedData.multiFieldUniqueList.get(fieldNames) != null;
    }

    @Override
    public MultipleUniqueKeyDefinition[] getMultiFieldUniqueKeys() {
        if (sharedData == null) {
            return new MultipleUniqueKeyDefinition[0];
        }
        return sharedData.multiFieldUniqueList.values().toArray(
            new MultipleUniqueKeyDefinition[sharedData.multiFieldUniqueList.values().size()]);
    }

    @Override
    public QueryFragmentFunctions getFunctions() {
        return sharedData.functions;
    }

    /**
     * If this type is the data pointed by a 1-1 pointer or subset (ptrOne and setComplex), return the type of the main
     * record, otherwise return null
     */
    @Override
    public FieldDefinition getParentField() {
        if (parent == null) {
            return null;
        }
        return parent.getFieldDefinition(fieldNameInParent);
    }

    @Override
    public String getSetMemberFieldName() {
        return setMemberFieldName;
    }

    @Override
    public String getSetOwnerFieldName() {
        return setOwnerFieldName;
    }

    /** methods for validation definitions **/

    public ValidationDefinition getValidationDefinition() {
        return this;
    }

    @Override
    public Collection<ValidationRule> getValidationRules(String fieldName) {
        return ((FieldDefinitionImpl) getFieldDefinition(fieldName)).getValidationRules();
    }

    @Override
    public boolean hasValidationRules() {
        return sharedData.validationRules.size() > 0;
    }

    @Override
    public ValidationRule getValidationRule(String ruleName) {
        return sharedData.validationRules.get(ruleName);
    }

    /** which is the name of the creation timestamp field, if any? */
    @Override
    public String getCreationDateFieldName() {
        return createName;
    }

    /** which is the name of the modification timestamp field, if any? */
    @Override
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
        if (parent != null) {
            sb.append("   == parent: " + parent.getName() + "\n");
        }
        sb.append("   == parentFieldName: " + fieldNameInParent + "\n");
        sb.append("   == isFileSubfield: " + isFileSubfield + "\n");
        sb.append("\n   === Fields \n\n");

        for (Object f : fields.values()) {
            sb.append(f.toString() + "\n");
        }

        sb.append("\n   === Validation rules \n\n");

        for (ValidationRule r : sharedData.validationRules.values()) {
            sb.append(r.toString() + "\n");
        }

        sb.append("\n   === Multi-unique keys \n\n");

        for (DataDefinition.MultipleUniqueKeyDefinition k : sharedData.multiFieldUniqueList.values()) {
            sb.append(k.toString() + "\n");
        }

        sb.append("\n   === Functions \n\n");

        for (QueryFragmentFunction f : sharedData.functions.getFunctions()) {
            sb.append(f.toString() + "\n");
        }

        return sb.toString();
    }

    public String getStructure() {
        StringBuffer sb = new StringBuffer();

        sb.append("getName() " + getName() + "\n");
        sb.append("getFieldNames()\n");
        for (FieldDefinition fd : getFieldDefinitions()) {
            sb.append(fd.getName() + "\n");
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
        for (FieldDefinition fd : getFieldDefinitions()) {
            sb.append(((FieldDefinitionImpl) fd).getStructure() + "\n");
        }

        return sb.toString();

    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        flist = makeFieldList();
    }
}
