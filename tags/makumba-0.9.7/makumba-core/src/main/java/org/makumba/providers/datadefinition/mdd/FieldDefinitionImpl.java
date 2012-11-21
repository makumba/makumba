package org.makumba.providers.datadefinition.mdd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Vector;

import org.makumba.CompositeValidationException;
import org.makumba.DataDefinition;
import org.makumba.DataDefinitionNotFoundError;
import org.makumba.FieldDefinition;
import org.makumba.InvalidValueException;
import org.makumba.MakumbaError;
import org.makumba.Pointer;
import org.makumba.Text;
import org.makumba.ValidationRule;
import org.makumba.commons.CollectionUtils;
import org.makumba.commons.StringUtils;
import org.makumba.providers.DataDefinitionProvider;

public class FieldDefinitionImpl implements FieldDefinition, Serializable {

    private static final long serialVersionUID = 1595860664381445238L;

    // private transient MakumbaAnnotationReader mar = MakumbaAnnotationReader.getInstance();

    // basic field info
    protected DataDefinitionImpl mdd;

    protected String name;

    protected SharedFieldData shared;

    protected String description;

    protected FieldType type;

    // modifiers
    protected boolean fixed;

    protected boolean notNull;

    protected boolean notEmpty;

    protected boolean unique;

    // pointed type name
    protected String pointedType;

    // char length
    protected int charLength;

    // pointed type
    protected transient DataDefinition pointed;

    // subfield - ptrOne, setComplex
    protected DataDefinitionImpl subfield;

    static class SharedFieldData implements Serializable {

        // TODO: this is unused but shared could be set to null if isEmpty() returns true.
        boolean isEmpty() {
            return defaultValue == null && notNullError == null && NaNError == null && uniqueError == null
                    && notEmptyError == null && notIntError == null && notRealError == null && notBooleanError == null
                    && intEnumValues.isEmpty() && intEnumValuesDeprecated.isEmpty() && charEnumValues.isEmpty()
                    && charEnumValuesDeprecated.isEmpty() && validationRules.isEmpty();
        }

        // default value for this field
        private Object defaultValue;

        // native validation rule messages
        protected String notNullError;

        protected String NaNError;

        protected String uniqueError;

        protected String notEmptyError;

        protected String notIntError;

        protected String notRealError;

        protected String notBooleanError;

        // intEnum - contains all values, including deprecated
        protected LinkedHashMap<Integer, String> intEnumValues = new LinkedHashMap<Integer, String>();

        // intEnum - contains depreciated values
        protected LinkedHashMap<Integer, String> intEnumValuesDeprecated = new LinkedHashMap<Integer, String>();

        // charEnum - contains all values, including deprecated
        protected Vector<String> charEnumValues = new Vector<String>();

        // chaEnum - contains depreciated values
        protected Vector<String> charEnumValuesDeprecated = new Vector<String>();

        // validation rules for this field
        private Hashtable<String, ValidationRule> validationRules = new Hashtable<String, ValidationRule>();
    }

    // name of the field definition parent, needed for serialization
    private String originalFieldDefinitionParent;

    // name of the original field definition, needed for serialization
    private String originalFieldDefinitionName;

    /**
     * Creates a field definition given a name, type and description
     * 
     * @param name
     *            the name of the field
     * @param type
     *            the type of the filed, e.g. char, int, ptr - but no relational type definition
     * @param description
     *            the description of the field
     */
    public FieldDefinitionImpl(String name, String type, String description) {
        this(name, type);
        this.description = description;
    }

    /**
     * Minimal constructor for standard fields
     */
    public FieldDefinitionImpl(String name, DataDefinitionImpl dd) {
        this.name = name;
        this.mdd = dd;
    }

    /**
     * Creates a field definition given a name and a type
     * 
     * @param name
     *            the name of the field
     * @param type
     *            the type of the filed, e.g. char, int, ptr - but no relational type definition
     */
    public FieldDefinitionImpl(String name, String type) {

        // NOTE: shared is left null!!!

        this.name = name;

        fixed = false;
        notNull = false;
        notEmpty = false;
        unique = false;
        try {
            this.type = FieldType.valueOf(type.toUpperCase());
            if (this.type == FieldType.CHAR) {
                charLength = 255;
            }

        } catch (IllegalArgumentException e) {
            // type is not a strict type
            if (type.startsWith("char")) {

                try {

                    int n = type.indexOf("[");
                    int m = type.indexOf("]");
                    if (!type.endsWith("]") || type.substring(3, n).trim().length() > 1) {
                        throw new InvalidValueException("invalid char type " + type);
                    }

                    charLength = new Integer(Integer.parseInt(type.substring(n + 1, m)));
                    this.type = FieldType.CHAR;

                } catch (StringIndexOutOfBoundsException e1) {
                    throw new InvalidValueException("bad type " + type);
                } catch (NumberFormatException f) {
                    throw new InvalidValueException("bad char[] size " + type);
                }
            }
        }
    }

    public FieldDefinitionImpl(String name, String type, DataDefinitionImpl mdd) {
        this(name, type);
        this.mdd = mdd;
    }

    /** for virtual FieldDefinition */
    public FieldDefinitionImpl(String name, FieldDefinition fi) {

        FieldDefinitionImpl f = (FieldDefinitionImpl) fi;

        this.name = name;
        this.mdd = f.mdd;
        this.type = f.type;

        if (f.shared != null && !f.shared.isEmpty()) {
            this.shared = f.shared;
        }
        this.description = f.description;
        this.fixed = f.fixed;
        this.notNull = f.notNull;
        this.notEmpty = f.notEmpty;
        this.unique = f.unique;
        this.pointedType = f.pointedType;
        this.pointed = f.pointed;
        this.charLength = f.charLength;
        this.subfield = f.subfield;

        if (type == FieldType.PTRINDEX) {
            type = FieldType.PTR;
            pointed = fi.getDataDefinition();
        }

        // store names of original field definition and data definition; see getOriginalFieldDefinition() for details
        if (fi.getDataDefinition() != null && !fi.getDataDefinition().isTemporary()) {
            originalFieldDefinitionParent = fi.getDataDefinition().getName();
            originalFieldDefinitionName = fi.getName();
        }
    }

    /** for virtual field definitions **/
    public FieldDefinitionImpl(String name, FieldDefinition field, String description) {
        this(name, field);
        this.description = description;
    }

    /** constructor used when creating the {@link DataDefinitionImpl} during parsing **/
    public FieldDefinitionImpl(DataDefinitionImpl mdd, FieldNode f) {
        shared = new SharedFieldData();
        this.mdd = mdd;
        this.name = f.name;
        this.fixed = f.fixed;
        this.notEmpty = f.notEmpty;
        this.notNull = f.notNull;
        this.unique = f.unique;

        this.shared.uniqueError = f.uniqueError;
        this.shared.notNullError = f.notNullError;
        this.shared.notEmptyError = f.notEmptyError;
        this.shared.NaNError = f.NaNError;
        this.shared.notIntError = f.notIntError;
        this.shared.notRealError = f.notRealError;
        this.shared.notBooleanError = f.notBooleanError;
        this.shared.defaultValue = f.defaultValue;
        this.shared.intEnumValues = f.intEnumValues;
        this.shared.intEnumValuesDeprecated = f.intEnumValuesDeprecated;
        this.shared.charEnumValues = f.charEnumValues;
        this.shared.charEnumValuesDeprecated = f.charEnumValuesDeprecated;
        this.shared.validationRules = f.validationRules;

        this.charLength = f.charLength;
        this.description = f.description;
        this.type = f.makumbaType;

        this.pointedType = f.pointedType;

        // store names of original field definition and data definition; see getOriginalFieldDefinition() for details
        if (getDataDefinition() != null && !getDataDefinition().isTemporary()) {
            originalFieldDefinitionParent = getDataDefinition().getName();
            originalFieldDefinitionName = getName();
        }

        // we have to transform the subfield MDDNode into a DataDefinitionImpl
        if (f.subfield != null) {
            this.subfield = new DataDefinitionImpl(f.getName(), f.subfield, mdd);
            this.subfield.build();
        }
    }

    // /** constructor used when creating the {@link DataDefinitionImpl} from a java field **/
    // public FieldDefinitionImpl(DataDefinitionImpl mdd, FieldMetadata m) {
    // this.mdd = mdd;
    // this.name = m.getField().getName();
    // this.fixed = !m.getBooleanAspectValue(MetadataAspect.UNIQUE);
    // // this.notEmpty = f.notEmpty;
    // this.notNull = m.getBooleanAspectValue(MetadataAspect.NULLABLE);
    // this.unique = m.getBooleanAspectValue(MetadataAspect.UNIQUE);
    //
    // HashMap<MessageType, String> messages = mar.getMessages(m);
    // this.uniqueError = messages.get(MessageType.UNIQUE_ERROR);
    // this.notNullError = messages.get(MessageType.NULLABLE_ERROR);
    // this.NaNError = messages.get(MessageType.NaN_ERROR);
    // this.notIntError = messages.get(MessageType.INT_ERROR);
    // this.notRealError = messages.get(MessageType.REAL_ERROR);
    // this.notBooleanError = messages.get(MessageType.BOOLEAN_ERROR);
    //
    // this.charLength = m.getIntegerAspectValue(MetadataAspect.LENGTH);
    // this.description = m.getStringAspectValue(MetadataAspect.DESCRIPTION);
    // // this.defaultValue = f.defaultValue;
    //
    // // this.type = f.makumbaType;
    //
    // this.intEnumValues = mar.getEnumValues(m);
    // this.intEnumValuesDeprecated = mar.getDeprecatedEnumValues(m);
    // // this.charEnumValues = f.charEnumValues;
    // // this.charEnumValuesDeprecated = f.charEnumValuesDeprecated;
    //
    // /*
    //
    // this.type = f.makumbaType;
    // this.intEnumValues = f.intEnumValues;
    // this.intEnumValuesDeprecated = f.intEnumValuesDeprecated;
    // this.charEnumValues = f.charEnumValues;
    // this.charEnumValuesDeprecated = f.charEnumValuesDeprecated;
    // this.pointedType = f.pointedType;
    // this.validationRules = f.validationRules;
    //
    // // store names of original field definition and data definition; see getOriginalFieldDefinition() for details
    // if (getDataDefinition() != null && !getDataDefinition().isTemporary()) {
    // originalFieldDefinitionParent = getDataDefinition().getName();
    // originalFieldDefinitionName = getName();
    // }
    //
    // // we have to transform the subfield MDDNode into a DataDefinitionImpl
    // if (f.subfield != null) {
    // this.subfield = new DataDefinitionImpl(f.getName(), f.subfield, mdd);
    // this.subfield.build();
    // }
    // */
    // }

    /** methods for base fields **/

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        if (description == null || description.trim().equals("")) {
            return name;
        }
        return description;
    }

    @Override
    public boolean hasDescription() {
        return this.description != null;
    }

    @Override
    public DataDefinitionImpl getDataDefinition() {
        return this.mdd;
    }

    /** methods for modifiers **/

    @Override
    public boolean isUnique() {
        return unique;
    }

    @Override
    public boolean isFixed() {
        return fixed;
    }

    @Override
    public boolean isNotEmpty() {
        return notEmpty;
    }

    @Override
    public boolean isNotNull() {
        return notNull;
    }

    /** methods for type **/

    @Override
    public boolean isBinaryType() {
        return type == FieldType.BINARY;
    }

    @Override
    public boolean isBooleanType() {
        return type == FieldType.BOOLEAN;
    }

    @Override
    public boolean isComplexSet() {
        return type == FieldType.SETCOMPLEX;
    }

    @Override
    public boolean isDateType() {
        return type == FieldType.DATE || type == FieldType.DATECREATE || type == FieldType.DATEMODIFY;
    }

    @Override
    public boolean isEnumType() {
        return type == FieldType.INTENUM || type == FieldType.CHARENUM;
    }

    @Override
    public boolean isExternalSet() {
        return type == FieldType.SET;
    }

    @Override
    public boolean isFileType() {
        return subfield != null && subfield.isFileSubfield;
    }

    @Override
    public boolean isIndexPointerField() {
        return type == FieldType.PTRINDEX;
    }

    @Override
    public boolean isIntegerType() {
        return type == FieldType.INT;
    }

    @Override
    public boolean isInternalSet() {
        return type == FieldType.SETCOMPLEX || type == FieldType.SETINTENUM || type == FieldType.SETCHARENUM;
    }

    @Override
    public boolean isNumberType() {
        return isIntegerType() || isRealType();
    }

    @Override
    public boolean isPointer() {
        return type == FieldType.PTR;
    }

    @Override
    public boolean isRealType() {
        return type == FieldType.REAL;
    }

    @Override
    public boolean isSetEnumType() {
        return type == FieldType.SETCHARENUM || type == FieldType.SETINTENUM;
    }

    @Override
    public boolean isSetType() {
        return isInternalSet() || isExternalSet();
    }

    @Override
    public boolean isStringType() {
        return type == FieldType.CHAR || type == FieldType.TEXT;
    }

    /** methods for validation rules **/

    public void addValidationRule(ValidationRule rule) {
        shared.validationRules.put(rule.getRuleName(), rule);
    }

    public Collection<ValidationRule> getValidationRules() {
        // we sort the rules, so that comparison rules come in the end
        ArrayList<ValidationRule> arrayList = shared == null ? new ArrayList<ValidationRule>()
                : new ArrayList<ValidationRule>(shared.validationRules.values());
        Collections.sort(arrayList);
        return arrayList;
    }

    /** methods for checks (assignability) **/

    @Override
    public void checkInsert(Dictionary<String, Object> d) {
        Object o = d.get(getName());
        if (isNotNull() && (o == null || o.equals(getNull()))) {
            // FIXME: call this in RecordEditor.readFrom, to have more possible exceptions gathered at once
            throw new CompositeValidationException(new InvalidValueException(this,
                    shared.notNullError != null ? shared.notNullError : ERROR_NOT_NULL));
        } else if (isNotEmpty() && StringUtils.isEmpty(o)) {
            // FIXME: call this in RecordEditor.readFrom, to have more possible exceptions gathered at once
            throw new CompositeValidationException(new InvalidValueException(this,
                    shared.notEmptyError != null ? shared.notEmptyError : ERROR_NOT_EMPTY));
        }
        if (o != null) {
            d.put(getName(), checkValue(o));
        }
    }

    @Override
    public Object checkValue(Object value) {

        if (!value.equals(getNull())) {

            switch (type) {

                case CHAR:
                    normalCheck(value);
                    String s = (String) value;
                    if (s.length() > getWidth()) {
                        throw new InvalidValueException(this, "String too long for char[] field. Maximum width: "
                                + getWidth() + " given width " + s.length() + ".\n\tGiven value <" + s + ">");
                    }
                    return value;
                case CHARENUM:
                    return checkCharEnum(value);
                case DATE:
                case DATECREATE:
                case DATEMODIFY:
                    return normalCheck(value);
                case INT:
                    // we allow Integer and Long types (Long might come e.g. from JSTL <ftm:parseNumber ...> which
                    // returns a Long
                    if (!(value instanceof Integer || value instanceof Long)) {
                        throw new org.makumba.InvalidValueException(this, getJavaType(), value);
                    }
                    if (value instanceof Integer) {
                        return value;
                    } else { // if it is a Long, we convert it to an Integer
                        Long l = (Long) value;
                        if (l > Integer.MAX_VALUE) {
                            throw new InvalidValueException(
                                    this,
                                    "int '"
                                            + this.name
                                            + "' with value '"
                                            + l
                                            + "' was passed as a Java Long type (probably through a JSTL or Java statement)"
                                            + " and its value is higher than the maximum value of a Java Integer, thus it can't be properly stored.");
                        } else {
                            return l.intValue();
                        }
                    }
                case INTENUM:
                    return checkIntEnum(value);
                case PTR:
                case PTRINDEX:
                case PTRONE:
                case PTRREL:

                    // file is a transformed to a pointer type on MDD
                    // parsing but the binary input is on the name of the field, not field.content
                    if (isFileType() && !(value instanceof Pointer)) {
                        return checkBinary(value);
                    } else {
                        return checkPointer(value);
                    }

                case REAL:
                    if (value instanceof Integer) {
                        return value;
                    }
                    return normalCheck(value);
                case SET:

                    try {
                        // may be just a pointer
                        Object o = checkPointer(value);
                        Vector<Object> v = new Vector<Object>();
                        if (o != null && o instanceof Pointer) {
                            v.addElement(o);
                        }
                        return v;
                    } catch (org.makumba.InvalidValueException ive) {
                    }

                    normalCheck(value);

                    @SuppressWarnings("unchecked")
                    Vector<Object> v = (Vector<Object>) value;

                    FieldDefinition ptr = getForeignTable().getFieldDefinition(
                        getForeignTable().getIndexPointerFieldName());

                    for (int i = 0; i < v.size(); i++) {
                        if (v.elementAt(i) == null || v.elementAt(i).equals(org.makumba.Pointer.Null)) {
                            throw new org.makumba.InvalidValueException(this, "set members cannot be null");
                        }
                        try {
                            v.setElementAt(ptr.checkValue(v.elementAt(i)), i);
                        } catch (org.makumba.InvalidValueException e) {
                            throw new org.makumba.InvalidValueException(this, "the set member <" + v.elementAt(i)
                                    + "> is not assignable to pointers of type " + getForeignTable().getName());
                        }
                    }
                    return v;
                case SETINTENUM:

                    Vector<Object> vect = new Vector<Object>();

                    // may just have one value
                    if (value != null && value instanceof Integer) {
                        checkIntEnum(value);
                        vect.addElement(value);
                        return vect;
                    }

                    if (value != null && value instanceof String) {
                        checkIntEnum(value);
                        vect.addElement(value);
                        return vect;
                    }

                    normalCheck(value);

                    @SuppressWarnings("unchecked")
                    Vector<Object> value2 = (Vector<Object>) value;
                    vect = value2;

                    for (int i = 0; i < vect.size(); i++) {
                        if (vect.elementAt(i) == null || vect.elementAt(i).equals(org.makumba.Pointer.NullInteger)) {
                            throw new org.makumba.InvalidValueException(this, "set members cannot be null");
                        } else {

                        }
                        vect.setElementAt(checkIntEnum(vect.elementAt(i)), i);
                    }
                    return vect;

                case SETCHARENUM:

                    // may just have one value
                    if (value != null && value instanceof String) {
                        checkCharEnum(value);
                        return CollectionUtils.toVector(value);
                    }

                    normalCheck(value);
                    @SuppressWarnings("unchecked")
                    Vector<Object> value3 = (Vector<Object>) value;
                    v = value3;

                    for (int i = 0; i < v.size(); i++) {
                        if (v.elementAt(i) == null || v.elementAt(i).equals(org.makumba.Pointer.NullString)) {
                            throw new org.makumba.InvalidValueException(this, "set members cannot be null");
                        } else {

                        }
                        v.setElementAt(checkCharEnum(v.elementAt(i)), i);
                    }
                    return v;

                case SETCOMPLEX:
                    throw new org.makumba.InvalidValueException(this, "subsets cannot be assigned directly");
                case TEXT:
                    return checkBinary(value);
                case BINARY:
                case BOOLEAN:
                    if (value instanceof Boolean) {
                        return value;
                    }
                    return normalCheck(value);
                default:
                    throw new RuntimeException("Unknown case handling for field type '" + this + "', integer type "
                            + getIntegerType());

            }
        }
        return value;
    }

    private Object checkBinary(Object value) {
        try {
            return Text.getText(value);
        } catch (InvalidValueException e) {
            throw new InvalidValueException(this, e.getMessage());
        }
    }

    private Object checkIntEnum(Object value) {
        if (value instanceof Integer && !shared.intEnumValues.containsKey(value)) {
            throw new org.makumba.InvalidValueException(this, "int value set to int enumerator (" + value
                    + ") is not a member of " + Arrays.toString(shared.intEnumValues.keySet().toArray()));
        } else if (!(value instanceof Integer) && !(value instanceof Long) && !(value instanceof String)) {
            throw new org.makumba.InvalidValueException(this,
                    "int enumerators only accept values of type Integer, Long or String. Value supplied (" + value
                            + ") is of type " + value.getClass().getName());
        } else if (value instanceof String && !shared.intEnumValues.containsValue(value)) {
            throw new org.makumba.InvalidValueException(this, "string value set to int enumerator (" + value
                    + ") is neither a member of " + Arrays.toString(shared.intEnumValues.values().toArray())
                    + " nor a member of " + Arrays.toString(shared.intEnumValues.keySet().toArray()));
        } else if (value instanceof String && shared.intEnumValues.containsValue(value)) {
            for (Integer i : shared.intEnumValues.keySet()) {
                if (shared.intEnumValues.get(i).equals(value)) {
                    return i;
                }
            }
        }

        return value;
    }

    private Object checkCharEnum(Object value) {
        if (value instanceof String && !shared.charEnumValues.contains(value)) {
            throw new org.makumba.InvalidValueException(this, "char value set to char enumerator (" + value
                    + ") is not a member of " + Arrays.toString(shared.charEnumValues.toArray()));
        }

        if (!(value instanceof String)) {
            throw new org.makumba.InvalidValueException(this,
                    "char enumerators only accept values of type String. Value supplied (" + value + ") is of type "
                            + value.getClass().getName());
        }

        return value;
    }

    private Object checkPointer(Object value) {
        if (value instanceof Pointer) {
            if (!((Pointer) value).getType().equals(getPointedType().getName())) {
                throw new InvalidValueException(this, getPointedType().getName(), (Pointer) value);
            }
            return value;
        }
        if (value instanceof String) {
            return new Pointer(getPointedType().getName(), (String) value);
        }
        throw new InvalidValueException(
                this,
                "Only java.lang.String and org.makumba.Pointer (or a java.util.Vector containing elements of those types) are assignable to makumba pointers, given value <"
                        + value + "> is of type " + value.getClass().getName());
    }

    protected Object normalCheck(Object value) {
        if (!getJavaType().isInstance(value)) {
            throw new org.makumba.InvalidValueException(this, getJavaType(), value);
        }
        return value;
    }

    @Override
    public boolean isAssignableFrom(FieldDefinition fi) {
        switch (type) {
            case INT:
                return is_int_AssignableFrom(fi);
            case INTENUM:
                return is_intEnum_AssignableFrom(fi);
            case PTR:
            case PTRREL:
                return is_ptrRel_AssignableFrom(fi);
            case REAL:
                return is_real_AssignableFrom(fi);
            case SET:
                return is_set_AssignableFrom(fi);
            default:
                return base_isAssignableFrom(fi);
        }
    }

    public boolean base_isAssignableFrom(FieldDefinition fi) {
        return fi.getType().equals("nil") || getType().equals(fi.getType());
    }

    public boolean is_int_AssignableFrom(FieldDefinition fi) {
        return base_isAssignableFrom(fi) || fi.getType().equals("intEnum");
    }

    public boolean is_intEnum_AssignableFrom(FieldDefinition fi) {
        return is_int_AssignableFrom(fi) || fi.getType().equals("int") || fi.getType().equals("char");
    }

    public boolean is_ptrRel_AssignableFrom(FieldDefinition fi) {
        return "nil".equals(fi.getType()) || getType().equals(fi.getType())
                && ((FieldDefinitionImpl) fi).pointedType.equals(getForeignTable().getName());
    }

    public boolean is_real_AssignableFrom(FieldDefinition fi) {
        return base_isAssignableFrom(fi) || fi.getType().equals("intEnum") || fi.getType().equals("int");
    }

    public boolean is_set_AssignableFrom(FieldDefinition fi) {
        return "nil".equals(fi.getType()) || getType().equals(fi.getType())
                && getForeignTable().getName().equals(fi.getForeignTable().getName());
    }

    /** methods for types (java, sql, null) **/

    @Override
    public String getDataType() {
        return type.getDataType();
    }

    @Override
    public Class<?> getJavaType() {
        return this.type.getJavaType();
    }

    @Override
    public int getIntegerType() {
        return type.getIntegerType();
    }

    @Override
    public Object getNull() {
        // file is a transformed to a pointer type on MDD parsing
        // but the binary input is on the name of the field, not field.content
        if (type == FieldType.PTRONE && isFileType()) {
            return Pointer.NullText;
        }

        return this.type.getNullType();
    }

    @Override
    public Object getEmptyValue() {
        return type.getEmptyValue();
    }

    @Override
    public String getType() {
        return this.type.getTypeName();
    }

    @Override
    public boolean isDefaultField() {
        return type == FieldType.PTRINDEX || type == FieldType.DATECREATE || type == FieldType.DATEMODIFY;
    }

    /** methods for default values **/

    @Override
    public Date getDefaultDate() {

        switch (type) {
            case DATE:
            case DATECREATE:
            case DATEMODIFY:
                return (Date) getDefaultValue();
            default:
                throw new RuntimeException("Shouldn't be here");
        }
    }

    @Override
    public int getDefaultInt() {
        switch (type) {
            case INT:
                return (Integer) getDefaultValue();
            case INTENUM:
            case SETINTENUM:
                return 0;
            default:
                throw new RuntimeException("Shouldn't be here");
        }
    }

    @Override
    public String getDefaultString() {
        switch (type) {
            case CHAR:
            case CHARENUM:
            case TEXT:
            case BINARY:
                return (String) getDefaultValue();
            case SETCHARENUM:
                // FIXME this is returning the wrong thing
                // in the old implementation it was returning the default value of the "enum"
                // field in the subfield MDD, which exists no longer
                // however this field seems never to be used
                return null;
            default:
                throw new RuntimeException("Shouldn't be here");
        }
    }

    /** returns the default value of this field */
    @Override
    public Object getDefaultValue() {
        if (shared == null || shared.defaultValue == null) {
            return getEmptyValue();
        }
        return shared.defaultValue;
    }

    /** methods for enumerations **/

    @Override
    public Vector<String> getDeprecatedValues() {
        switch (type) {
            case INTENUM:
                // TODO optimize this, maybe change interface...
                Vector<String> depr = new Vector<String>();
                for (Integer i : shared.intEnumValuesDeprecated.keySet()) {
                    depr.add(i.toString());
                }
                return depr;
            case CHARENUM:
                return shared.charEnumValuesDeprecated;
            default:
                return null;
        }
    }

    @Override
    public int getEnumeratorSize() {
        switch (type) {
            case CHARENUM:
                return this.shared.charEnumValues.size();
            case INTENUM:
                return this.shared.intEnumValues.size();
            case SETCHARENUM:
                return this.shared.charEnumValues.size();
            case SETINTENUM:
                return this.shared.intEnumValues.size();
            default:
                throw new RuntimeException("Shouldn't be here");
        }
    }

    @Override
    public int getIntAt(int i) {
        if (i > shared.intEnumValues.size()) {
            throw new RuntimeException("intEnum size is " + shared.intEnumValues.size() + ", index is " + i);
        }

        return (Integer) shared.intEnumValues.keySet().toArray()[i];
    }

    @Override
    public String getNameAt(int i) {

        switch (type) {
            case INTENUM:
            case SETINTENUM:
                if (i > shared.intEnumValues.size()) {
                    throw new RuntimeException("enumerator size is " + shared.intEnumValues.size() + ", index is " + i);
                }
                return (String) shared.intEnumValues.values().toArray()[i];

            case CHARENUM:
            case SETCHARENUM:
                if (i > shared.charEnumValues.size()) {
                    throw new RuntimeException("enumerator size is " + shared.charEnumValues.size() + ", index is " + i);
                }
                return shared.charEnumValues.elementAt(i);

            default:
                throw new RuntimeException("getNameAt works only for intEnum, setintEnum, charEnum and setcharEnum");
        }

    }

    @Override
    public String getNameFor(int i) {
        if (type != FieldType.INTENUM && type != FieldType.SETINTENUM) {
            throw new RuntimeException("getNameFor works only for intEnum");
        }
        return shared.intEnumValues.get(i);
    }

    @Override
    public Collection<String> getNames() {
        switch (type) {
            case INTENUM:
            case SETINTENUM:
                return shared.intEnumValues.values();
            case CHARENUM:
            case SETCHARENUM:
                return shared.charEnumValues;
            default:
                throw new RuntimeException("getNames() only work for intEnum and charEnum");
        }

    }

    @Override
    public Collection<?> getValues() {
        switch (type) {
            case INTENUM:
            case SETINTENUM:
                return shared.intEnumValues.keySet();
            case CHARENUM:
            case SETCHARENUM:
                return shared.charEnumValues;
            default:
                throw new RuntimeException("getNames() only work for intEnum and charEnum");
        }
    }

    /** methods for relational types **/

    @Override
    public DataDefinition getForeignTable() {
        switch (type) {
            case PTR:
            case PTRREL:
            case SET:
                if (this.pointed == null) {
                    this.pointed = DataDefinitionProvider.getMDD(pointedType);
                }
                return this.pointed;
            default:
                throw new RuntimeException("Shouldn't be here");
        }
    }

    @Override
    public FieldDefinition getOriginalFieldDefinition() {
        // we can't store a reference to the original field definition, otherwise it will be serialised in the form
        // responder, and in turn will serialise it's data definition, which might cause issues like locking..
        // thus, we do a lookup here
        DataDefinition dataDefinition = null;

        if (originalFieldDefinitionParent == null) {
            return null;
        }

        try {
            dataDefinition = DataDefinitionProvider.getInstance().getDataDefinition(originalFieldDefinitionParent);
        } catch (DataDefinitionNotFoundError dnfe) {
            dataDefinition = null;
        }
        return dataDefinition != null ? dataDefinition.getFieldDefinition(originalFieldDefinitionName) : null;
    }

    @Override
    public DataDefinition getPointedType() {
        switch (type) {
            case PTRINDEX:
                return getDataDefinition();
            case PTRONE:
            case SETCOMPLEX:
            case SETCHARENUM:
            case SETINTENUM:
            case FILE:
                return getSubtable();
            case PTRREL:
            case PTR:
            case SET:
                return getForeignTable();
            default:
                throw new RuntimeException("Shouldn't be here");
        }
    }

    @Override
    public DataDefinition getSubtable() {
        switch (type) {
            case PTRONE:
            case SETCOMPLEX:
            case SETCHARENUM:
            case SETINTENUM:
            case FILE:
            case SET:
                return this.subfield;
            default:
                throw new RuntimeException("Trying to get a sub-table for a '" + getType() + "' for field '" + name
                        + "'.");
        }
    }

    @Override
    public String getTitleField() {
        switch (type) {
            case PTR:
            case SET:
                if (pointed != null && ((DataDefinitionImpl) pointed).titleField != null) {
                    return ((DataDefinitionImpl) pointed).titleField;
                }
                return getForeignTable().getTitleFieldName();
            default:
                throw new RuntimeException("Shouldn't be here");
        }
    }

    private int longestChar = -1;

    @Override
    public int getWidth() {
        switch (type) {
            case CHAR:
                return this.charLength;
            case CHARENUM:
                if (longestChar == -1) {
                    for (String s : shared.charEnumValues) {
                        if (s.length() > longestChar) {
                            longestChar = s.length();
                        }
                    }
                }
                return longestChar;
            case SETCHARENUM:
                if (longestChar == -1) {
                    for (String s : shared.charEnumValues) {
                        if (s.length() > longestChar) {
                            longestChar = s.length();
                        }
                    }
                }
                return longestChar;
            default:
                throw new RuntimeException("Shouldn't be here");
        }
    }

    @Override
    public boolean shouldEditBySingleInput() {
        return !(getIntegerType() == _ptrOne || getIntegerType() == _setComplex);
    }

    @Override
    public String toString() {
        return getType();
    }

    public String toString1() {
        StringBuffer sb = new StringBuffer();
        sb.append("== Field name: " + name + "\n");
        sb.append("== Field type: " + type.getTypeName() + "\n");
        sb.append("== Modifiers: " + (fixed ? "fixed " : "") + (unique ? "unique " : "") + (notNull ? "not null " : "")
                + (notEmpty ? "not empty " : "") + "\n");
        if (description != null) {
            sb.append("== Description: " + description + "\n");
        }

        switch (type) {
            case CHAR:
                sb.append("== char length: " + charLength + "\n");
                break;
            case INTENUM:
            case SETINTENUM:
                sb.append("== int enum values:" + Arrays.toString(shared.intEnumValues.keySet().toArray()) + "\n");
                sb.append("== int enum names:" + Arrays.toString(shared.intEnumValues.values().toArray()) + "\n");
                break;
            case CHARENUM:
            case SETCHARENUM:
                sb.append("== char enum values:" + Arrays.toString(shared.charEnumValues.toArray()) + "\n");
                break;
            case PTR:
            case PTRINDEX:
            case SET:
                sb.append("== pointed type: " + pointedType + "\n");
                break;
            case SETCOMPLEX:
            case PTRONE:
                sb.append("== Subfield detail" + "\n\n");
                sb.append(subfield.toString() + "\n");
                break;
        }

        return sb.toString();
    }

    public String getStructure() {
        StringBuffer sb = new StringBuffer();
        sb.append("--- structure of " + getName() + "\n");

        sb.append("getName() " + getName() + "\n");
        sb.append("getDataDefinition() " + getDataDefinition().getName() + "\n");
        // sb.append("getOriginalFieldDefinition() " + getOriginalFieldDefinition().getName() + "\n");
        sb.append("isIndexPointerField() " + isIndexPointerField() + "\n");
        sb.append("getEmptyValue() " + getEmptyValue() + "\n");
        sb.append("getNull()" + getNull() + "\n");
        try {
            sb.append("hasDescription() " + hasDescription() + "\n");
        } catch (RuntimeException e) {
            sb.append("has invalid description");
        }
        sb.append("getDescription() " + getDescription() + "\n");
        sb.append("getType() " + getType() + "\n");
        sb.append("getIntegerType() " + getIntegerType() + "\n");
        sb.append("getDataType() " + getDataType() + "\n");
        sb.append("getJavaType() " + getJavaType() + "\n");
        sb.append("isFixed() " + isFixed() + "\n");
        sb.append("isNotNull() " + isNotNull() + "\n");
        sb.append("isNotEmpty() " + isNotEmpty() + "\n");
        sb.append("isUnique() " + isUnique() + "\n");
        sb.append("getDefaultValue() " + getDefaultValue() + "\n");
        sb.append("getDefaultString()\n");
        try {
            sb.append(getDefaultString() + "\n");
        } catch (RuntimeException re) {
            sb.append("was not a string\n");
        }
        sb.append("getDefaultInt()\n");
        try {
            sb.append(getDefaultInt() + "\n");
        } catch (RuntimeException re) {
            sb.append("was not an int: " + re.getMessage() + "\n");
        }
        sb.append("getDefaultDate()\n");
        try {
            sb.append(getDefaultDate() + "\n");
        } catch (RuntimeException re) {
            sb.append("was not a date\n");
        }
        sb.append("getValues()\n");
        try {
            sb.append(getValues() + "\n");
        } catch (RuntimeException re) {
            sb.append("was not an enum\n");
        }
        sb.append("getNames()\n");
        try {
            sb.append(getNames() + "\n");
        } catch (RuntimeException re) {
            sb.append("was not an enum: " + re.getMessage() + "\n");
        }
        sb.append("getEnumeratorSize()\n");
        try {
            sb.append(getEnumeratorSize() + "\n");
        } catch (RuntimeException re) {
            sb.append("was not an enum\n");
        }

        sb.append("getWidth()\n");
        try {
            sb.append(getWidth() + "\n");
        } catch (RuntimeException re) {
            sb.append("was not a char\n");
        }

        sb.append("getForeignTable()\n");
        try {
            sb.append(((DataDefinitionImpl) getForeignTable()).getName() + "\n");
        } catch (RuntimeException re) {
            sb.append("was not a ptr\n");
        }

        sb.append("getSubtable()\n");
        try {
            sb.append(((DataDefinitionImpl) getSubtable()).getStructure() + "\n");
        } catch (RuntimeException re) {
            sb.append("was not a ptr: " + re.getMessage() + "\n");
        }

        sb.append("getPointedType()\n");
        try {
            sb.append(((DataDefinitionImpl) getPointedType()).getName() + "\n");
        } catch (RuntimeException re) {
            sb.append("was not a ptr\n");
        }

        sb.append("getTitleField()\n");
        try {
            sb.append(getTitleField() + "\n");
        } catch (RuntimeException re) {
            sb.append("was not a ptr\n");
        }

        sb.append("getDeprecatedValues()\n");
        try {
            sb.append(getDeprecatedValues() + "\n");
        } catch (RuntimeException re) {
            sb.append("was not an enum\n");
        }

        sb.append("isDefaultField()" + isDefaultField() + "\n");
        sb.append("shouldEditBySingleInput() " + shouldEditBySingleInput() + "\n");
        sb.append("isDateType() " + isDateType() + "\n");
        sb.append("isNumberType() " + isNumberType() + "\n");
        sb.append("isIntegerType() " + isIntegerType() + "\n");
        sb.append("isRealType() " + isRealType() + "\n");
        sb.append("isBinaryType() " + isBinaryType() + "\n");
        sb.append("isFileType() " + isFileType() + "\n");
        sb.append("isSetType() " + isSetType() + "\n");
        sb.append("isSetEnumType() " + isSetEnumType() + "\n");
        sb.append("isEnumType() " + isEnumType() + "\n");
        sb.append("isInternalSet() " + isInternalSet() + "\n");
        sb.append("isExternalSet() " + isExternalSet() + "\n");
        sb.append("isComplexSet() " + isComplexSet() + "\n");
        sb.append("isPointer() " + isPointer() + "\n");
        sb.append("isStringType() " + isStringType() + "\n");

        sb.append("---  end structure of " + getName());

        return sb.toString();
    }

    @Override
    public String getErrorMessage(FieldErrorMessageType t) {
        switch (t) {
            case NOT_A_NUMBER:
                return this.shared.NaNError;
            case NOT_NULL:
                return this.shared.notNullError;
            case NOT_UNIQUE:
                return this.shared.uniqueError;
            case NOT_EMPTY:
                return this.shared.notEmptyError;
            case NOT_INT:
                return this.shared.notIntError;
            case NOT_REAL:
                return this.shared.notRealError;
            case NOT_BOOLEAN:
                return this.shared.notBooleanError;
            default:
                throw new MakumbaError("no such error message");
        }
    }

    public LinkedHashMap<Integer, String> getIntEnumValues() {
        return shared.intEnumValues;
    }

    public LinkedHashMap<Integer, String> getIntEnumValuesDeprecated() {
        return shared.intEnumValuesDeprecated;
    }

}
