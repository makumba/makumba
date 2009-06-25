package org.makumba.providers.datadefinition.mdd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Vector;

import org.makumba.CompositeValidationException;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.InvalidValueException;
import org.makumba.MakumbaError;
import org.makumba.Pointer;
import org.makumba.Text;
import org.makumba.ValidationRule;
import org.makumba.commons.StringUtils;
import org.makumba.providers.DataDefinitionProvider;

// TODO add charEnum and setCharEnum
public class FieldDefinitionImpl implements FieldDefinition {

    // basic field info
    protected DataDefinition mdd;
    
    protected String name;
    
    protected FieldType type;
    
    protected String description;
    
    // default value for this field
    private Object defaultValue;

    // modifiers
    protected boolean fixed;

    protected boolean notNull;

    protected boolean notEmpty;

    protected boolean unique;
    
    // intEnum
    private LinkedHashMap<Integer, String> intEnumValues = new LinkedHashMap<Integer, String>();

    private LinkedHashMap<Integer, String> intEnumValuesDeprecated = new LinkedHashMap<Integer, String>();
    
    // charEnum
    protected Vector<String> charEnumValues = new Vector<String>();

    protected Vector<String> charEnumValuesDeprecated = new Vector<String>();
    
    // char length
    protected int charLength;
    
    // pointed type name
    protected String pointedType;
    
    // pointed type 
    protected transient DataDefinition pointed;
    
    // subfield - ptrOne, setComplex
    protected DataDefinition subfield;
    
    
    // validation rules for this field
    private Hashtable<String, ValidationRule> validationRules = new Hashtable<String, ValidationRule>();

    // name of the field definition parent, needed for serialization
    private String originalFieldDefinitionParent;

    // name of the original field definition, needed for serialization
    private String originalFieldDefinitionName;
    
    /**
     * Creates a field definition given a name, type and description
     * @param name the name of the field
     * @param type the type of the filed, e.g. char, int, ptr - but no relational type definition
     * @param description the description of the field
     */
    public FieldDefinitionImpl(String name, String type, String description) {
        this(name, type);
        this.description = description;
    }
    
    /**
     * Minimal constructor for standard fields
     */
    public FieldDefinitionImpl(String name, DataDefinition dd) {
        this.name = name;
        this.mdd = dd;
    }
    

    /**
     * Creates a field definition given a name and a type
     * @param name the name of the field
     * @param type the type of the filed, e.g. char, int, ptr - but no relational type definition
     */
    // FIXME what about pointed in the case of ptr?

    public FieldDefinitionImpl(String name, String type) {
        this.name = name;
        fixed = false;
        notNull = false;
        notEmpty = false;
        unique = false;
        try {
            this.type = FieldType.valueOf(type.toUpperCase());
            if(this.type == FieldType.CHAR) {
                charLength = 255;
            }

        } catch(IllegalArgumentException e) {
            // type is not a strict type
            if(type.startsWith("char")) {
                
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
    
    /** for virtual FieldDefinition */
    public FieldDefinitionImpl(String name, FieldDefinition fi) {
        this.name = name;
        type = FieldType.valueOf(fi.getType());
        fixed = fi.isFixed();
        notEmpty = fi.isNotEmpty();
        unique = fi.isUnique();
        defaultValue = fi.getDefaultValue();
        description = fi.getDescription();
        
        switch (type) {
            case PTRONE:
            // FIXME what to do with these two?
            case SETCHARENUM:
            case SETINTENUM:

            case SETCOMPLEX:
            case FILE:
                subfield = fi.getSubtable();
            case SET:
                pointed = fi.getSubtable();
        }
        
        if (type == FieldType.PTRINDEX) {
            type = FieldType.PTR;
            pointed = fi.getDataDefinition();
        }
        validationRules = ((FieldDefinitionImpl)fi).validationRules;

        // store names of original field definition and data definition; see getOriginalFieldDefinition() for details
        if (fi.getDataDefinition() != null) {
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
    public FieldDefinitionImpl(DataDefinition mdd, FieldNode f) {
        System.out.println("creating new field def " + f.name);
        this.mdd = mdd;
        this.name = f.name;
        this.fixed = f.fixed;
        this.notEmpty = f.notEmpty;
        this.notNull = f.notNull;
        this.unique = f.unique;
        this.charLength = f.charLength;
        this.defaultValue = f.defaultValue;
        this.description = f.description;
        this.type = f.makumbaType;
        this.intEnumValues = f.intEnumValues;
        this.intEnumValuesDeprecated = f.intEnumValuesDeprecated;
        this.pointedType = f.pointedType;
        
        
        // we build the pointed type lazily, that way we also avoid getting into nasty loops
        /*
        if(f.pointedType != null && (this.type == FieldType.PTR || this.type == FieldType.PTRREL || this.type == FieldType.SET)) {
            this.pointed = MDDProvider.getMDD(pointedType);
        }
        */
        
        this.validationRules = f.validationRules;
        
        // TODO check if this works
        this.originalFieldDefinitionName = name;
        this.originalFieldDefinitionParent = getDataDefinition().getName();
        
        // we have to transform the subfield MDDNode into a DataDefinitionImpl
        if(f.subfield != null &&f.makumbaType == FieldType.SETCOMPLEX || f.makumbaType == FieldType.PTRONE) {
            System.out.println("this file has a subfield, going to create MDD");
                this.subfield = new DataDefinitionImpl(f.subfield, mdd);
        }
    }



    /** methods for base fields **/
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }

    public void setType(String type) {
        this.type = FieldType.valueOf(type);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    public void setNotEmpty(boolean notEmpty) {
        this.notEmpty = notEmpty;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    public String getDescription() {
        if (description == null) {
            return name;
        }
        if (description.trim().equals("")) {
            return name;
        }
        return description;
    }
    
    public boolean hasDescription() {
        return this.description != null;
    }
    
    public DataDefinition getDataDefinition() {
        return this.mdd;
    }


    
    
    /** methods for modifiers **/
    
    public boolean isUnique() {
        return unique;
    }
    
    public boolean isFixed() {
        return fixed;
    }
    
    public boolean isNotEmpty() {
        return notEmpty;
    }

    public boolean isNotNull() {
        return notNull;
    }


    /** methods for type **/
    
    public boolean isBinaryType() {
        return type == FieldType.BINARY;
    }

    public boolean isComplexSet() {
        return type == FieldType.SETCOMPLEX;
    }

    public boolean isDateType() {
        return type == FieldType.DATE;
    }

    public boolean isEnumType() {
        return type == FieldType.INTENUM || type == FieldType.CHARENUM;
    }

    public boolean isExternalSet() {
        return type == FieldType.SET;
    }

    public boolean isFileType() {
        return subfield != null && ((DataDefinitionImpl) subfield).isFileSubfield;
    }

    public boolean isIndexPointerField() {
        return type == FieldType.PTRINDEX;
    }

    public boolean isIntegerType() {
        return type == FieldType.INT;
    }

    public boolean isInternalSet() {
        return type == FieldType.SETCOMPLEX || type == FieldType.SETINTENUM || type == FieldType.SETCHARENUM;
    }

    public boolean isNumberType() {
        return isIntegerType() || isRealType();
    }

    public boolean isPointer() {
        return type == FieldType.PTR;
    }

    public boolean isRealType() {
        return type == FieldType.REAL;
    }

    public boolean isSetEnumType() {
        return type == FieldType.SETCHARENUM || type == FieldType.SETINTENUM;
    }

    public boolean isSetType() {
        return isInternalSet() || isExternalSet();
    }

    public boolean isStringType() {
        return type == FieldType.CHAR || type == FieldType.TEXT;
    }

    
    
    /** methods for validation rules **/
    
    public void addValidationRule(Collection<ValidationRule> rules) {
        if (rules != null) {
            for (ValidationRule validationRule : rules) {
                addValidationRule((ValidationRule) validationRule);
            }
        }
    }

    public void addValidationRule(ValidationRule rule) {
        validationRules.put(rule.getRuleName(), rule);
    }

    public Collection<ValidationRule> getValidationRules() {
        // we sort the rules, so that comparison rules come in the end
        ArrayList<ValidationRule> arrayList = new ArrayList<ValidationRule>(validationRules.values());
        Collections.sort(arrayList);
        return arrayList;
    }
    
    
    
    /** methods for checks (assignability) **/

    public void checkInsert(Dictionary<String, Object> d) {
        Object o = d.get(getName());
        if (isNotNull() && (o == null || o.equals(getNull()))) {
            // FIXME: call this in RecordEditor.readFrom, to have more possible exceptions gathered at once
            throw new CompositeValidationException(new InvalidValueException(this, ERROR_NOT_NULL));
        } else if (isNotEmpty() && StringUtils.isEmpty(o)) {
            // FIXME: call this in RecordEditor.readFrom, to have more possible exceptions gathered at once
            throw new CompositeValidationException(new InvalidValueException(this, ERROR_NOT_EMPTY));
        }
        if (o != null) {
            d.put(getName(), checkValue(o));
        }
    }

    public void checkUpdate(Dictionary<String, Object> d) {
        Object o = d.get(getName());
        if (isNotEmpty() && StringUtils.isEmpty(o)) {
         // FIXME: call this in RecordEditor.readFrom, to have more possible exceptions gathered at once
            throw new CompositeValidationException(new InvalidValueException(this, ERROR_NOT_EMPTY));
        }
        if (o == null) {
            return;
        }
        if (isFixed()) {
         // FIXME: call this in RecordEditor.readFrom, to have more possible exceptions gathered at once
            throw new CompositeValidationException(new InvalidValueException(this, "You cannot update a fixed field"));
        }
        d.put(getName(), checkValue(o));
    }

    public Object checkValue(Object value) {
        
        if (!value.equals(getNull())) {
            
            switch(type) {
                
                case CHAR:
                    normalCheck(value);
                    String s = (String) value;
                    if (s.length() > getWidth()) {
                        throw new InvalidValueException(this, "String too long for char[] field. Maximum width: " + getWidth()
                                + " given width " + s.length() + ".\n\tGiven value <" + s + ">");
                    }
                    return value;
                case CHARENUM:
                    throw new MakumbaError("not implemented");
                case DATE:
                case DATECREATE:
                case DATEMODIFY:
                    return normalCheck(value);
                case INT:
                 // we allow Integer and Long types (Long might come e.g. from JSTL <ftm:parseNumber ...> which returns a Long
                    if (!(value instanceof Integer || value instanceof Long)) {
                        throw new org.makumba.InvalidValueException(this, getJavaType(), value);
                    }
                    if (value instanceof Integer) {
                        return value;
                    } else { // if it is a Long, we convert it to an Integer
                        // FIXME: this might potentially mean losing some data, if the Long is too long for an Integer
                        // Solution: Either makumba stores the date as long, or we throw an error if the value is too big?
                        // See: http://bugs.best.eu.org/1071
                        return ((Long) value).intValue();
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

                    Vector<Object> v = (Vector) value;

                    FieldDefinition ptr = getForeignTable().getFieldDefinition(getForeignTable().getIndexPointerFieldName());

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
                    
                    normalCheck(value);
                    v = (Vector) value;
    
                    for (int i = 0; i < v.size(); i++) {
                        if (v.elementAt(i) == null || v.elementAt(i).equals(org.makumba.Pointer.NullInteger)) {
                            throw new org.makumba.InvalidValueException(this, "set members cannot be null");
                        } else {
                            
                        }
                        v.setElementAt(checkIntEnum(v.elementAt(i)), i);
                    }
                    return v;
                    
                case SETCHARENUM:
                    throw new MakumbaError("not implemented");
                    //return check_setcharEnum_ValueImpl(value);
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
        if (value instanceof Integer && !intEnumValues.containsKey(value) /* && !intEnumValuesDeprecated.containsKey(value)*/) {
            throw new org.makumba.InvalidValueException(this, "int value set to int enumerator (" + value
                    + ") is not a member of " + Arrays.toString(intEnumValues.keySet().toArray()));
        }
        if (!(value instanceof String)) {
            throw new org.makumba.InvalidValueException(this,
                    "int enumerators only accept values of type Integer or String. Value supplied (" + value
                            + ") is of type " + value.getClass().getName());
        }
        if(!intEnumValues.containsValue(value) /* && !intEnumValuesDeprecated.containsKey(value) */) {
            throw new org.makumba.InvalidValueException(this, "string value set to int enumerator (" + value
                    + ") is neither a member of " + Arrays.toString(intEnumValues.values().toArray()) + " nor a member of " + Arrays.toString(intEnumValues.keySet().toArray()));
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
                && ((DataDefinition) ((FieldDefinitionImpl) fi).subfield).getName().equals(getForeignTable().getName());
    }

    public boolean is_real_AssignableFrom(FieldDefinition fi) {
        return base_isAssignableFrom(fi) || fi.getType().equals("intEnum") || fi.getType().equals("int");
    }

    public boolean is_set_AssignableFrom(FieldDefinition fi) {
        return "nil".equals(fi.getType()) || getType().equals(fi.getType())
                && getForeignTable().getName().equals(fi.getForeignTable().getName());
    }
    
    

    /** methods for types (java, sql, null) **/
    
    public String getDataType() {
        return type.getDataType();
    }
    
    public Class<?> getJavaType() {
        return this.type.getJavaType();
    }
    
    public int getIntegerType() {
        return type.getIntegerType();
    }
    
    public Object getNull() {
        // file is a transformed to a pointer type on MDD parsing
        // but the binary input is on the name of the field, not field.content
        if(type == FieldType.PTRONE && isFileType()) {
            return Pointer.NullText;
        }
        
        return this.type.getNullType();
    }
    
    public Object getEmptyValue() {
        return type.getEmptyValue();
    }
    
    public String getType() {
        return this.type.getTypeName();
    }
    
    public boolean isDefaultField() {
        return type == FieldType.PTRINDEX || type == FieldType.DATECREATE || type == FieldType.DATEMODIFY;
    }



    
    /** methods for default values **/
    

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
    
    public int getDefaultInt() {
        switch (type) {
            case INT:
            case INTENUM:
                return (Integer) getDefaultValue();
            case SETINTENUM:
             // FIXME this is returning the wrong thing
                // in the old implementation it was returning the default value of the "enum"
                // field in the subfield MDD, which exists no longer
                // however this field seems never to be used
                return -1;
            default:
                throw new RuntimeException("Shouldn't be here");
        }
    }

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
    public Object getDefaultValue() {
        if (defaultValue == null) {
            return getEmptyValue();
        }
        return defaultValue;
    }

    
    

    /** methods for enumerations **/
    
    public Vector<String> getDeprecatedValues() {
        return new Vector<String>(intEnumValuesDeprecated.values());
    }
    

    public int getEnumeratorSize() {
        switch (type) {
            case CHARENUM:
                throw new MakumbaError("not implemented");
            case INTENUM:
                return this.intEnumValues.size();
            case SETCHARENUM:
                throw new MakumbaError("not implemented");
            case SETINTENUM:
                return this.intEnumValues.size();
            default:
                throw new RuntimeException("Shouldn't be here");
        }
    }
    
    public int getIntAt(int i) {
        if(i > intEnumValues.size()) {
            throw new RuntimeException("intEnum size is " + intEnumValues.size() + ", index is " + i);
        }
        
        Iterator<Integer> it = intEnumValues.keySet().iterator();
        int k = 0;
        int res = 0;
        while(k != i) {
            res = it.next();
        }
        return res;
    }

    public String getNameAt(int i) {
        if(i > intEnumValues.size()) {
            throw new RuntimeException("intEnum size is " + intEnumValues.size() + ", index is " + i);
        }

        Iterator<String> it = intEnumValues.values().iterator();
        int k = 0;
        String res = "";
        while(k != i) {
            res = it.next();
        }
        return res;
    }

    public String getNameFor(int i) {
        if(type != FieldType.INTENUM) {
            throw new RuntimeException("getNameFor works only for intEnum");
        }
        return (String) intEnumValues.get(i);
    }

    public Collection<String> getNames() {
        return intEnumValues.values();
    }
    
    public Collection getValues() {
        return intEnumValues.keySet();
    }

    
    
    
    /** methods for relational types **/
    
    
    public DataDefinition getForeignTable() {
        switch (type) {
            case PTR:
            case PTRREL:
                return this.subfield;
            case SET:
                return pointerToForeign().getForeignTable();
            default:
                throw new RuntimeException("Shouldn't be here");
        }
    }
    
    // TODO understand this
    FieldDefinition pointerToForeign() {
        return getSubtable().getFieldDefinition((String) getSubtable().getFieldNames().elementAt(4));
    }

    public FieldDefinition getOriginalFieldDefinition() {
        // we can't store a reference to the original field definition, otherwise it will be serialised in the form
        // responder, and in turn will serialise it's data definition, which might cause issues like locking..
        // thus, we do a lookup here
        DataDefinition dataDefinition = DataDefinitionProvider.getInstance().getDataDefinition(
            originalFieldDefinitionParent);
        return dataDefinition != null ? dataDefinition.getFieldDefinition(originalFieldDefinitionName) : null;
    }


    public DataDefinition getPointedType() {
        switch (type) {
            case PTRINDEX:
                return getDataDefinition();
            case PTRONE:
            case SETCHARENUM:
            case SETCOMPLEX:
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

    public DataDefinition getSubtable() {
        switch (type) {
            case PTRONE:
            case SETCHARENUM:
            case SETCOMPLEX:
            case SETINTENUM:
            case FILE:
                return this.subfield;
            case SET:
                if(this.pointed == null) {
                    this.pointed = MDDProvider.getMDD(pointedType);
                }
                return this.pointed;
            default:
                throw new RuntimeException("Trying to get a sub-able for a '" + getType() + "' for field '" + name
                        + "'.");
        }
    }

    public String getTitleField() {
        switch (type) {
            case PTR:
            case SET:
                if (((DataDefinitionImpl) pointed).titleField != null) {
                    return ((DataDefinitionImpl) pointed).titleField;
                }
                return getForeignTable().getTitleFieldName();
            default:
                throw new RuntimeException("Shouldn't be here");
        }
    }

    

    public int getWidth() {
        switch (type) {
            case CHAR:
                return this.charLength;
            case CHARENUM:
                return this.intEnumValues.size();
            case SETCHARENUM:
                return this.intEnumValues.size();
            default:
                throw new RuntimeException("Shouldn't be here");
        }
    }

    public boolean shouldEditBySingleInput() {
        return !(getIntegerType() == _ptrOne || getIntegerType() == _setComplex);
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("== Field name: " + name + "\n");
        sb.append("== Field type: " + type.getTypeName() + "\n");
        sb.append("== Modifiers: " + (fixed? "fixed ":"") + (unique? "unique ":"") + (notNull? "not null ":"") + (notEmpty? "not empty ":"")  + "\n");
        if(description != null) sb.append("== Description: "+ description + "\n");

        switch(type) {
            case CHAR:
                sb.append("== char length: " + charLength + "\n");
                break;
            case INTENUM:
            case SETINTENUM:
                sb.append("== int enum values:" + Arrays.toString(intEnumValues.keySet().toArray()) + "\n");
                sb.append("== int enum names:" + Arrays.toString(intEnumValues.values().toArray()) + "\n");
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

    
    
}
