package org.makumba.providers.datadefinition.mdd;

import org.makumba.Pointer;

/**
 * Enum for Makumba field types
 * 
 * @author Veronika Bodmann
 * @author Manuel Gay
 * @version $Id: FieldType.java,v 1.1 May 3, 2009 10:40:42 AM manu Exp $
 */
public enum FieldType {

    PTR("ptr", 0, Pointer.class, Pointer.Null),
    PTRREL("ptrRel", 1, Pointer.class, Pointer.Null),
    PTRONE("ptrOne", 2, Pointer.class, Pointer.Null),
    PTRINDEX("ptrIndex", 3, Pointer.class, Pointer.Null),
    INT("int", 4, java.lang.Integer.class, Pointer.NullInteger),
    INTENUM("intEnum", 5, java.lang.Integer.class, Pointer.NullInteger),
    CHAR("char", 6, java.lang.String.class, Pointer.NullString),
    CHARENUM("charEnum", 7, java.lang.String.class, Pointer.NullString),
    TEXT("text", 8, org.makumba.Text.class, Pointer.NullText),
    DATE("date", 9, java.util.Date.class, Pointer.NullDate),
    DATECREATE("dateCreate", 10, java.util.Date.class, Pointer.NullDate),
    DATEMODIFY("dateModify", 11, java.util.Date.class, Pointer.NullDate),
    SET("set", 12, java.util.Vector.class, Pointer.NullSet),
    SETCOMPLEX("setComplex", 13, null, Pointer.Null),
    NIL("nil", 14, null, null),
    REAL("real", 15, java.lang.Double.class, Pointer.NullReal),
    SETCHARENUM("setCharEnum", 16, java.util.Vector.class, Pointer.NullSet),
    SETINTENUM("setIntEnum", 17, java.util.Vector.class, Pointer.NullSet),
    BINARY("binary", 18, org.makumba.Text.class, Pointer.NullText),
    BOOLEAN("boolean", 19, java.lang.Boolean.class, Pointer.NullBoolean),
    FILE("file", 20, null, Pointer.NullText);

    private int type = -1;

    private Class<?> javaType;

    private Object nullType;

    private String name = "";

    FieldType(String name, int type, Class<?> javaType, Object nullType) {
        this.name = name;
        this.type = type;
        this.javaType = javaType;
        this.nullType = nullType;
    }

    public String getTypeName() {
        return this.name;
    }
    
    public Class<?> getJavaType() {
        return this.javaType;
    }
    
    public Object getNullType() {
        return this.nullType;
    }

    public int getType() {
        return this.type;
    }

}
