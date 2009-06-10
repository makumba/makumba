package org.makumba.providers.query.hql;

public class ParamTypeAST extends ExprTypeAST {

    public ParamTypeAST(int type, String name) {
        super(type);
        setText(name);
    }
    
    public int getDataType() {
        return ExprTypeAST.PARAMETER;
    }


}
