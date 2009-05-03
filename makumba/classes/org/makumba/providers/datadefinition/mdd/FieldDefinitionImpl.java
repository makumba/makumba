package org.makumba.providers.datadefinition.mdd;

import java.util.Collection;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.ValidationRule;

public class FieldDefinitionImpl implements FieldDefinition {

    // basic field info
    private String name;

    private String type;
    
    private String description;

    // modifiers
    private boolean fixed;

    private boolean notNull;

    private boolean notEmpty;

    private boolean unique;

    
    private Object defaultValue;
    
    
    /** for intEnum */
    private DualHashBidiMap intEnumValues = new DualHashBidiMap();

    private DualHashBidiMap intEnumValuesDeprecated = new DualHashBidiMap();

    
    
    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
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
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isComplexSet() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isDateType() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isEnumType() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isExternalSet() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isFileType() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isIndexPointerField() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isIntegerType() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isInternalSet() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isNumberType() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isPointer() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isRealType() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isSetEnumType() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isSetType() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isStringType() {
        // TODO Auto-generated method stub
        return false;
    }

    
    
    
    
    
    
    
    

    public void addValidationRule(ValidationRule rule) {
        // TODO Auto-generated method stub

    }

    public void addValidationRule(Collection<ValidationRule> rules) {
        // TODO Auto-generated method stub

    }

    public void checkInsert(Dictionary<String, Object> d) {
        // TODO Auto-generated method stub

    }

    public void checkUpdate(Dictionary<String, Object> d) {
        // TODO Auto-generated method stub

    }

    public Object checkValue(Object value) {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
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

    public Collection<ValidationRule> getValidationRules() {
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

}
