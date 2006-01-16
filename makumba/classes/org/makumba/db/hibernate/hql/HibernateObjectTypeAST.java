package org.makumba.db.hibernate.hql;

import java.util.Map;

import antlr.SemanticException;
import antlr.collections.AST;

public class HibernateObjectTypeAST extends ExprTypeAST implements ObjectType {
    
    public HibernateObjectTypeAST(AST lhs, AST rhs, Map aliasTypes) throws SemanticException {
        super(-2);
        String type= null;
        if(lhs instanceof HibernateObjectTypeAST){
            type= lhs.getText();
        }
        else{
            type = (String)aliasTypes.get(lhs.getText());
            if(type==null){
                throw new SemanticException("unknown alias: "+lhs.getText()+ " in property reference: "+lhs.getText()+"."+rhs.getText());
            }
        }

        Object computedType = determineType(type, rhs.getText());
        
        
    }

    public Object determineType(String type, String field) {
        /* analysis can look like
        Type t= SessionFactory.getClassMetadata(type).getPropertyType(rhs.getText());
        then check if t is a ptr or a set, and to what type it points, and do setText() with that type
        */
        
        // TODO Auto-generated method stub
        return null;
    }

}
