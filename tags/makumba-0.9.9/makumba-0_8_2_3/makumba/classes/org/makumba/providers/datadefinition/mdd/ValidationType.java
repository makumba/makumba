package org.makumba.providers.datadefinition.mdd;

/**
 * Validation rule types
 * @author Manuel Gay
 * @version $Id: ValidationType.java,v 1.1 May 21, 2009 2:27:41 PM manu Exp $
 */
public enum ValidationType {

    COMPARISON("comparison rule"),
    UNIQUENESS("uniqueness rule", FieldType.CHAR, FieldType.BOOLEAN, FieldType.DATE, FieldType.INT, FieldType.PTR, FieldType.TEXT),
    RANGE("range rule", FieldType.INT, FieldType.REAL),
    LENGTH("length rule", FieldType.CHAR, FieldType.TEXT),
    REGEX("regular expression rule", FieldType.CHAR, FieldType.TEXT);
    
    
    
    ValidationType(String description, FieldType... fieldType) {
        this.description = description;
        this.fieldTypes = fieldType;
    }
    
    private FieldType[] fieldTypes;
    
    private String description;
    
    public boolean checkApplicability(FieldType fieldType) {
        for(FieldType t : fieldTypes) {
            if(t.equals(fieldType)) {
                return true;
            }
        }
        return false;
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public String getApplicableTypes() {
        String s = "";
        
        for(int i=0; i < fieldTypes.length; i++) {
            
            if(i + 1 == fieldTypes.length && fieldTypes.length != 1)
                s +=" and ";
            
            s += fieldTypes[i].getTypeName();

            if(i + 1 < fieldTypes.length && i + 1 != fieldTypes.length - 1)
                s+=", ";
        }
        return s;
    }
    
}
