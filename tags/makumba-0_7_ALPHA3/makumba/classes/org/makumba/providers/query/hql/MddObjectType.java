package org.makumba.providers.query.hql;

import org.makumba.DataDefinition;
import org.makumba.DataDefinitionNotFoundError;
import org.makumba.FieldDefinition;
import org.makumba.ProgrammerError;
import org.makumba.providers.DataDefinitionProvider;

import antlr.RecognitionException;
import antlr.SemanticException;

public class MddObjectType implements ObjectType {

    private DataDefinitionProvider ddp = DataDefinitionProvider.getInstance();

    /**
     * Based on a type name and a field name, this method attempts to return the type of the field
     * @param type the type name, e.g. "general.Person"
     * @param field the field name, e.g. "age"
     * @return the type of the field, if it could be computed
     * 
     */
    public Object determineType(String type, String field) throws RecognitionException, SemanticException {
        if (field == null)
            try {
                ddp.getDataDefinition(type);
                return type;
            } catch (DataDefinitionNotFoundError err) {
                return null;
            }

        // System.out.println("Trying to get field type: " + field + " from type " + type + " ...");
        DataDefinition dd = null;

        if (field.equals("id")) {
            return type;
        }
        try {
            dd = ddp.getDataDefinition(type);
        } catch (DataDefinitionNotFoundError e) {
            // throw new SemanticException("No such MDD \"" + type + "\"");
            throw new ProgrammerError("No such MDD \"" + type + "\"");
        }

        if (field.equals("enum_") && dd.getFieldDefinition("enum") != null)
            // FIXME: need to check if this is really a setEnum generated type
            return dd.getFieldDefinition("enum");

        FieldDefinition fi = dd.getFieldDefinition(field);

        if (fi == null)
            // throw new SemanticException("No such field \"" + field + "\" in Makumba type \"" + dd.getName() + "\"");
            throw new ProgrammerError("No such field \"" + field + "\" in Makumba type \"" + dd.getName() + "\"");

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
            return sub.getName();

        else if (fi.getType().equals("setComplex") || fi.getType().equals("setintEnum")
                || fi.getType().equals("setcharEnum")) {
            return sub.getName();

        } else if (fi.getType().equals("set")) {
            // System.out.println("In SET: Trying to get field type: " + field + " from type " + type + " ...");
            // System.out.println(MakumbaSystem.getDataDefinition(foreign.getName()).getName());

            return ddp.getDataDefinition(foreign.getName()).getName();

        } else
            // System.out.println(MakumbaSystem.getDataDefinition(type).getFieldDefinition(field).getIntegerType());
            return ddp.getDataDefinition(type).getFieldDefinition(field);
    }

    /** given a type descriptor, resolve it to an integer for type analysis */
    public int getTypeOf(Object descriptor) {
        return ((FieldDefinition) descriptor).getIntegerType();
    }

}
