/*
 * Created on 21-Jul-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.makumba.db.hibernate.hql;

import java.util.Map;

import org.makumba.DataDefinition;
import org.makumba.DataDefinitionNotFoundError;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaSystem;

import antlr.RecognitionException;
import antlr.SemanticException;
import antlr.collections.AST;

public class ObjectTypeAST extends ExprTypeAST {

    private String objectType;
    
    public ObjectTypeAST(AST lhs, AST rhs, Map aliasTypes, ObjectType typeComputer) throws RecognitionException {
        super(-2);
        String type = null;
        if (lhs instanceof ObjectTypeAST) {
            type = ((ObjectTypeAST)lhs).getObjectType();
        } else {
            type = (String) aliasTypes.get(lhs.getText());
            if (type == null) {
                throw new SemanticException("unknown alias: " + lhs.getText() + " in property reference: "
                        + lhs.getText() + "." + rhs.getText());
            }
        }

        Object computedType = "";
        setDescription(rhs.getText());
        computedType = typeComputer.determineType(type, rhs.getText());
        
        //System.out.println("GOT TYPE: " + computedType);

        if (computedType instanceof Integer) {
            setDataType(((Integer) computedType).intValue());
            
        } else {
            setObjectType(computedType.toString());
        }
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

}
