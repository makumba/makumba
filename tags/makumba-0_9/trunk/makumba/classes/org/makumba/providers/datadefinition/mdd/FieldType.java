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

    // pointer
    PTR("ptr", 0, Pointer.class, Pointer.Null, null, "pointer"),

    // relation pointer, in mapping tables
    PTRREL("ptrRel", 1, Pointer.class, Pointer.Null, null, "pointer"),

    // pointer to a sub-record
    PTRONE("ptrOne", 2, Pointer.class, Pointer.Null, null, "pointer"),

    // primary key
    PTRINDEX("ptrIndex", 3, Pointer.class, Pointer.Null, null, "pointer"),

    // integer
    INT("int", 4, java.lang.Integer.class, Pointer.NullInteger, new Integer(0), "int"),

    // enumerated integer
    INTENUM("intEnum", 5, java.lang.Integer.class, Pointer.NullInteger, new Integer(0), "int"),

    // character
    CHAR("char", 6, java.lang.String.class, Pointer.NullString, "", "char"),

    // enumerated character
    CHARENUM("charEnum", 7, java.lang.String.class, Pointer.NullString, "", "char"),

    // long text
    TEXT("text", 8, org.makumba.Text.class, Pointer.NullText, "", "text"),

    // date
    DATE("date", 9, java.util.Date.class, Pointer.NullDate, FieldType.emptyDate(), "datetime"),

    // record creation date
    DATECREATE("dateCreate", 10, java.util.Date.class, Pointer.NullDate, FieldType.emptyDate(), "timestamp"),

    // record modification date
    DATEMODIFY("dateModify", 11, java.util.Date.class, Pointer.NullDate, FieldType.emptyDate(), "timestamp"),

    // set
    SET("set", 12, java.util.Vector.class, Pointer.NullSet, null, "set"),

    // sub-set
    SETCOMPLEX("setComplex", 13, null, Pointer.Null, null, "null"), NIL("nil", 14, null, null, null, null),

    // real number
    REAL("real", 15, java.lang.Double.class, Pointer.NullReal, new Double(0d), "real"),

    // set of char enum
    SETCHARENUM("setcharEnum", 16, java.util.Vector.class, Pointer.NullSet, null, "setchar"),

    // set of int enum
    SETINTENUM("setintEnum", 17, java.util.Vector.class, Pointer.NullSet, null, "setint"),

    // binary date
    BINARY("binary", 18, org.makumba.Text.class, Pointer.NullText, "", "binary"),

    // boolean
    BOOLEAN("boolean", 19, java.lang.Boolean.class, Pointer.NullBoolean, false, "boolean"),

    // file type with file meta-data
    FILE("file", 20, null, Pointer.NullText, null, null),

    // this is not a real type, it's needed for the HQL layer to work
    PARAMETER("parameter", 100, Object.class, Pointer.Null, null, "parameter");

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

    private static Date emptyDate;

    public static Date emptyDate() {
        if (emptyDate == null) {
            Calendar c = new GregorianCalendar(org.makumba.MakumbaSystem.getTimeZone());
            c.clear();
            c.set(1900, 0, 1);
            emptyDate = c.getTime();
        }
        return emptyDate;
    }
}