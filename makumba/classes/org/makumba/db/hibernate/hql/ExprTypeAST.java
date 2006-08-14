/*
 * Created on 14-Jul-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.makumba.db.hibernate.hql;

import antlr.CommonAST;

public class ExprTypeAST extends CommonAST{
    int dataType;
    static public final int NULL=-1;
    static public final int INT=1;
    static public final int STRING=2;
    static public final int FLOAT=3;
    static public final int LONG=4;
    static public final int DOUBLE=5;
    static public final int BOOLEAN=6;
    
    
    public ExprTypeAST(int type){ dataType=type; setText(""+type); }
}
