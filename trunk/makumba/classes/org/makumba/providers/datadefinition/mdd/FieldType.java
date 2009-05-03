package org.makumba.providers.datadefinition.mdd;

/**
 * Enum for Makumba field types
 * 
 * @author Veronika Bodmann
 * @version $Id: FieldType.java,v 1.1 May 3, 2009 10:40:42 AM manu Exp $
 */
public enum FieldType {
    
    PTR("ptr", 0), PTRREL("ptrRel", 1), PTRONE("ptrOne", 2), PTRINDEX("ptrIndex", 3), INT("int", 4), INTENUM("intEnum", 5),
    CHAR("char", 6), CHARENUM("charEnum", 7), TEXT("text", 8), DATE("date", 9), DATECREATE("dateCreate", 10), DATEMODIFY("dateModify", 11),
    SET("set", 12), SETCOMPLEX("setComplex", 13), NIL("nil", 14), REAL ("real",15), SETCHARENUM("setCharEnum",16), SETINTENUM("setIntEnum",17),
    BINARY("binary",18), BOOLEAN("boolean", 19), FILE("file",20);
    
    private int type = -1;
    private String name = "";
    
    FieldType(String name, int type) {
        this.name = name;
        this.type = type;
    }
    
    public String getTypeName() {
        return this.name;
    }
    
    public int getType() {
        return this.type;
    }
    
}
