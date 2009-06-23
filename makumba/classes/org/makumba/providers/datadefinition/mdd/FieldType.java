package org.makumba.providers.datadefinition.mdd;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.makumba.Pointer;

/**
 * Enum for Makumba field types
 * 
 * @author Veronika Bodmann
 * @author Manuel Gay
 * @version $Id: FieldType.java,v 1.1 May 3, 2009 10:40:42 AM manu Exp $
 */
public enum FieldType {
    
    PTR("ptr", 0, Pointer.class, Pointer.Null, null, "pointer"),
    PTRREL("ptrRel", 1, Pointer.class, Pointer.Null, null, "pointer"),
    PTRONE("ptrOne", 2, Pointer.class, Pointer.Null, null, "pointer"),
    PTRINDEX("ptrIndex", 3, Pointer.class, Pointer.Null, null, "pointer"),
    INT("int", 4, java.lang.Integer.class, Pointer.NullInteger, FieldType.emptyInt, "int"),
    INTENUM("intEnum", 5, java.lang.Integer.class, Pointer.NullInteger, FieldType.emptyInt, "int"),
    CHAR("char", 6, java.lang.String.class, Pointer.NullString, "", "char"),
    CHARENUM("charEnum", 7, java.lang.String.class, Pointer.NullString, "", "char"),
    TEXT("text", 8, org.makumba.Text.class, Pointer.NullText, "", "text"),
    DATE("date", 9, java.util.Date.class, Pointer.NullDate, FieldType.emptyDate, "datetime"),
    DATECREATE("dateCreate", 10, java.util.Date.class, Pointer.NullDate, FieldType.emptyDate, "timestamp"),
    DATEMODIFY("dateModify", 11, java.util.Date.class, Pointer.NullDate, FieldType.emptyDate, "timestamp"),
    SET("set", 12, java.util.Vector.class, Pointer.NullSet, null, "set"),
    SETCOMPLEX("setComplex", 13, null, Pointer.Null, null, "null"),
    NIL("nil", 14, null, null, null, null),
    REAL("real", 15, java.lang.Double.class, Pointer.NullReal, FieldType.emptyReal, "real"),
    SETCHARENUM("setCharEnum", 16, java.util.Vector.class, Pointer.NullSet, null, "setchar"),
    SETINTENUM("setIntEnum", 17, java.util.Vector.class, Pointer.NullSet, null, "setint"),
    BINARY("binary", 18, org.makumba.Text.class, Pointer.NullText, "", "binary"),
    BOOLEAN("boolean", 19, java.lang.Boolean.class, Pointer.NullBoolean, false, "boolean"),
    FILE("file", 20, null, Pointer.NullText, null, null);
    
    private int type = -1;

    private Class<?> javaType;

    private Object nullType;
    
    private String dataType;

    private Object emptyValue;
    
    private String name = "";

    FieldType(String name, int type, Class<?> javaType, Object nullType, Object emptyValue, String dataType) {
        this.name = name;
        this.type = type;
        this.javaType = javaType;
        this.nullType = nullType;
        this.dataType = dataType;
        this.emptyValue = emptyValue;
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

    public int getIntegerType() {
        return this.type;
    }
    
    public String getDataType() {
        return this.dataType;
    }
    
    public Object getEmptyValue() {
        return this.emptyValue;
    }

    private static final Date emptyDate;

    static final Object emptyInt = new Integer(0);

    static final Object emptyReal = new Double(0d);
    
    static {
        Calendar c = new GregorianCalendar(org.makumba.MakumbaSystem.getTimeZone());
        c.clear();
        c.set(1900, 0, 1);
        emptyDate = c.getTime();
    }

    
}
