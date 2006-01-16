/*
 * Created on 14-Jul-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.makumba.db.hibernate.hql;

import antlr.CommonAST;

public class ExprTypeAST extends CommonAST {
    int dataType;
    private String identifier;
    private String description;
    
    public void setIdentifier(String i) {
        this.identifier = i;
    }
    /*
    
    public static final int _ptr = 0;
    public static final int _ptrRel = 1;
    public static final int _ptrOne = 2;
    public static final int _ptrIndex = 3;
    public static final int _int = 4;
    public static final int _intEnum = 5;
    public static final int _char = 6;
    public static final int _charEnum = 7;
    public static final int _text = 8;
    public static final int _date = 9;
    public static final int _dateCreate = 10;
    public static final int _dateModify = 11;
    public static final int _set = 12;
    public static final int _setComplex = 13;
    public static final int _nil = 14;
    public static final int _real = 15;
    public static final int _setCharEnum = 16;
    public static final int _setIntEnum = 17;
    
    */
    
    static public final int NULL=-1;
    static public final int INT=4;
    static public final int STRING=6;
    static public final int LONGSTRING=8;
    static public final int DOUBLE=15;
    static public final int BOOLEAN=16;
    
    static public final int LONG=4;
    static public final int FLOAT=3;
    
    public ExprTypeAST(int type){
        dataType=type;
    }
    
    public String getText() {
        String s = super.getText();
        if(identifier != null) {
            s+=" as "+identifier;
        }
        return s;
    }

    public String getIdentifier() {
        return identifier;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }
    
    public String getObjectType() {
        return null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
