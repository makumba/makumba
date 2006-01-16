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

public class MddObjectTypeAST extends ExprTypeAST implements ObjectType {
    
    private String objectType;

    public MddObjectTypeAST(AST lhs, AST rhs, Map aliasTypes)
            throws SemanticException {
        super(-2);
        String type = null;
        if (lhs instanceof MddObjectTypeAST) {
            type = lhs.getText();
        } else {
            type = (String) aliasTypes.get(lhs.getText());
            if (type == null) {
                throw new SemanticException("unknown alias: " + lhs.getText()
                        + " in property reference: " + lhs.getText() + "."
                        + rhs.getText());
            }
        }

        Object computedType = "";
        try {
            computedType = determineType(type, rhs.getText());
        } catch (RecognitionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("GOT TYPE: " + computedType);

        if(computedType instanceof Integer) {
            setDataType(((Integer)computedType).intValue());
        }
        
        setObjectType((String) computedType);
        
    }

    
    public Object determineType(String type, String field)
            throws RecognitionException, SemanticException {
        System.out.println("Trying to get field type: " + field + " from type "
                + type + " ...");

        String computedType;
        DataDefinition dd;
        try {
            dd = MakumbaSystem.getDataDefinition(type);
        } catch(DataDefinitionNotFoundError e) {
            throw new SemanticException("No such MDD \"" + type + "\"");
        }
        
        FieldDefinition fi = dd.getFieldDefinition(field);
        try {
            computedType = fi.getType();
        } catch (NullPointerException ne) {
            try {
                throw new SemanticException("No such field \"" + field
                        + "\" in Makumba type \"" + dd.getName() + "\"");
            } catch (SemanticException e) {
                //TODO throw error in Syntax
                e.printStackTrace();
            }
        }

        DataDefinition foreign = null, sub = null;

        try {
            foreign = fi.getForeignTable();
        } catch (Exception e) {
        }
        try {
            sub = fi.getSubtable();
        } catch (Exception e) {
        }

        if (fi.getType().equals("ptr"))
            return foreign.getName();
        
        else if (fi.getType().equals("ptrOne"))
            return sub.getIndexPointerFieldName();
        
        else if (fi.getType().equals("setComplex")
                || fi.getType().equals("setintEnum")
                || fi.getType().equals("setcharEnum")) {
            return sub.getName();
            
        } else if (fi.getType().equals("set")) {
            return sub.getName();
            
        } else
            return new Integer(MakumbaSystem.getDataDefinition(type).getFieldDefinition(
                    field).getIntegerType());

    }


    public String getObjectType() {
        return objectType;
    }


    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

}
