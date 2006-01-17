package org.makumba.db.hibernate.hql;

import org.makumba.DataDefinition;
import org.makumba.DataDefinitionNotFoundError;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaSystem;

import antlr.RecognitionException;
import antlr.SemanticException;


public class MddObjectType implements ObjectType {
    
    public Object determineType(String type, String field) throws RecognitionException, SemanticException {
        if(field==null)
        try{
            MakumbaSystem.getDataDefinition(type);
            return type;
        }catch(DataDefinitionNotFoundError err){ return null; }

        //System.out.println("Trying to get field type: " + field + " from type " + type + " ...");
        DataDefinition dd = null;

        if (field.equals("id")) {
            return type;
        } else if (field.startsWith("hibernate_")) {
            String ptrToCheck = field.substring(field.indexOf("_"));
            DataDefinition ddPtr = MakumbaSystem.getDataDefinition(type);
            FieldDefinition fiPtr = ddPtr.getFieldDefinition(ptrToCheck);
            if (fiPtr.getType().equals("ptr")) {
                return fiPtr.getForeignTable().getName();
            }
        }

        try {
            dd = MakumbaSystem.getDataDefinition(type);
        } catch (DataDefinitionNotFoundError e) {
            throw new SemanticException("No such MDD \"" + type + "\"");
        }

        FieldDefinition fi = dd.getFieldDefinition(field);
        if(fi == null)
            throw new SemanticException("No such field \"" + field + "\" in Makumba type \"" + dd.getName() + "\"");


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

        else if (fi.getType().equals("setComplex") || fi.getType().equals("setintEnum")
                || fi.getType().equals("setcharEnum")) {
            return sub.getName();

        } else if (fi.getType().equals("set")) {
            //System.out.println("In SET: Trying to get field type: " + field + " from type " + type + " ...");
            //System.out.println(MakumbaSystem.getDataDefinition(foreign.getName()).getName());
            
            return MakumbaSystem.getDataDefinition(foreign.getName()).getName();

        } else
            //System.out.println(MakumbaSystem.getDataDefinition(type).getFieldDefinition(field).getIntegerType());
            return new Integer(MakumbaSystem.getDataDefinition(type).getFieldDefinition(field).getIntegerType());

    }
}
