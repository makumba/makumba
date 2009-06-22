package org.makumba.providers.datadefinition.mdd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.makumba.CompositeValidationException;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.InvalidValueException;
import org.makumba.MakumbaError;
import org.makumba.Pointer;
import org.makumba.Text;
import org.makumba.ValidationRule;
import org.makumba.commons.StringUtils;

public class FieldDefinitionImpl implements FieldDefinition {

    // basic field info
    protected MDDNode mdd;
    
    protected String name;
    
    protected FieldType type;
    
    protected String description;
    
    // default value for this field
    private Object defaultValue;
    
    // for unknown mak type, probably macro type
    protected String unknownType;

    // modifiers
    protected boolean fixed;

    protected boolean notNull;

    protected boolean notEmpty;

    protected boolean unique;
    
    // intEnum - index, string
    private DualHashBidiMap intEnumValues = new DualHashBidiMap();

    private DualHashBidiMap intEnumValuesDeprecated = new DualHashBidiMap();
    
    // char length
    protected int charLength;
    
    // pointed type
    protected String pointedType;
    
    // subfield - ptrOne, setComplex
    protected MDDNode subfield;
    
    
    // validation rules for this field
    private Hashtable<String, ValidationRule> validationRules = new Hashtable<String, ValidationRule>();

    
    /** methods for base fields **/
    
    public void setName(String name) {
        this.name = name;
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
    
    public void addIntEnumValue(int index, String text) {
        intEnumValues.put(index, text);
    }

    public void addIntEnumValueDeprecated(int index, String text) {
        intEnumValuesDeprecated.put(index, text);
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

    // FIXME
    public boolean isFileType() {
        return type == FieldType.FILE;
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
                    
                    // FIXME
                    /*
                    if (isFileType() && !(value instanceof Pointer)) {// file is a transformed to a pointer type on MDD
                        // parsing but the binary input is on the name of the field, not field.content
                        return check_binary_ValueImpl(value);
                    } else {
                        return check_ptrIndex_ValueImpl(value);
                    }
                    */
                    
                    return checkPointer(value);
                    
                    
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
                    try {
                        return Text.getText(value);
                    } catch (InvalidValueException e) {
                        throw new InvalidValueException(this, e.getMessage());
                    }
                case BINARY:
                case FILE:
                    try {
                        return Text.getText(value);
                    } catch (InvalidValueException e) {
                        throw new InvalidValueException(this, e.getMessage());
                    }
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
        if(!intEnumValues.containsKey(value) /* && !intEnumValuesDeprecated.containsKey(value) */) {
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

    public Object checkValueImpl(Object value) {
        // TODO Auto-generated method stub
        return null;
    }

    public DataDefinition getDataDefinition() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getDataType() {
        // TODO Auto-generated method stub
        return null;
    }

    public Date getDefaultDate() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getDefaultInt() {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getDefaultString() {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getDefaultValue() {
        // TODO Auto-generated method stub
        return null;
    }

    public Vector<String> getDeprecatedValues() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getEmptyValue() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getEnumeratorSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    public DataDefinition getForeignTable() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getIntAt(int i) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getIntegerType() {
        // TODO Auto-generated method stub
        return 0;
    }

    public Class<?> getJavaType() {
        return this.type.getJavaType();
    }

    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getNameAt(int i) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getNameFor(int i) {
        // TODO Auto-generated method stub
        return null;
    }

    public Enumeration<String> getNames() {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getNull() {
        return this.type.getNullType();
    }

    public FieldDefinition getOriginalFieldDefinition() {
        // TODO Auto-generated method stub
        return null;
    }

    public DataDefinition getPointedType() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getStringAt(int i) {
        // TODO Auto-generated method stub
        return null;
    }

    public DataDefinition getSubtable() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getTitleField() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getType() {
        // TODO Auto-generated method stub
        return null;
    }

    public Enumeration getValues() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getWidth() {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean hasDescription() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean hasTitleFieldIndicated() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isAssignableFrom(FieldDefinition fd) {
        // TODO Auto-generated method stub
        return false;
    }


    public boolean isDefaultField() {
        // TODO Auto-generated method stub
        return false;
    }


    public boolean shouldEditBySingleInput() {
        // TODO Auto-generated method stub
        return false;
    }

    protected Object normalCheck(Object value) {
        if (!getJavaType().isInstance(value)) {
            throw new org.makumba.InvalidValueException(this, getJavaType(), value);
        }
        return value;
    }
    
}
