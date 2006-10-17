package org.makumba.db.hibernate.hql;

public class ParamTypeAST extends ExprTypeAST {

    public ParamTypeAST(int type) {
        super(type);
    }
    
    public int getDataType() {
        return ExprTypeAST.PARAMETER;
    }


}
