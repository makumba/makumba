package org.makumba.providers.datadefinition.mdd;

import java.util.HashMap;

import org.makumba.DataDefinition;
import org.makumba.DataDefinitionNotFoundError;
import org.makumba.MakumbaError;
import org.makumba.providers.datadefinition.mdd.validation.MultiUniquenessValidationRule;

import antlr.collections.AST;

/**
 * Postprocessor walker that performs some finalizing steps.
 * 
 * @author Manuel Gay
 * @version $Id: MDDBuildWalker.java,v 1.1 May 3, 2009 10:13:05 PM manu Exp $
 */
public class MDDPostProcessorWalker extends MDDPostProcessorBaseWalker {
    
    private MDDFactory factory = null;
    
    private HashMap<String, FieldNode> typeShorthands;
    
    public MDDPostProcessorWalker(String typeName, MDDNode mdd, HashMap<String, FieldNode> typeShorthands, MDDFactory factory) {
        this.typeName = typeName;
        this.mdd = mdd;
        this.typeShorthands = typeShorthands;
        this.factory = factory;
    }
    
    @Override
    protected void processUnknownType(AST field) {
        FieldNode fieldNode = (FieldNode) field;
        FieldNode type = typeShorthands.get(fieldNode.unknownType);
        if(type == null) {
            factory.doThrow(this.typeName, "Unknown field type: "+fieldNode.unknownType, field);
        } else {
            fieldNode.makumbaType = type.makumbaType;
            if(fieldNode.makumbaType == FieldType.INTENUM) {
                fieldNode.intEnumValues = type.intEnumValues;
                fieldNode.intEnumValuesDeprecated = type.intEnumValuesDeprecated;
            }
        }
        
        field = fieldNode;
        
    }
    
    @Override
    protected void checkTitleField(AST titleField) {
        TitleFieldNode title = (TitleFieldNode) titleField;
        
        // titleField can be a field name or a function
        switch(title.titleType) {
            
            case MDDTokenTypes.FIELD:
                checkPathValid(titleField, title.getText(), title.mdd);
                break;
                
            case FUNCTION:
                // TODO add support for calls with arguments
                if(title.functionArgs.size() > 0) {
                    factory.doThrow(this.typeName, "There's no support for function calls with arguments in the !title directive yet", titleField);
                } else {
                    if(mdd.functions.get(title.functionName) == null ) {
                        factory.doThrow(this.typeName, "Function " + title.functionName + " not defined in type " + typeName , titleField);
                    }
                }
                break;
            default:
                throw new MakumbaError("invalid title field type: " + title.titleType);
        }
        
    }

    private void checkPathValid(AST ast, String t, MDDNode mddNode) {
        
        
        if(t.indexOf(".") > -1) {
        
            while(t.indexOf(".") > -1) {
                String field = t.substring(0, t.indexOf("."));
                t = t.substring(t.indexOf(".") + 1);
                String fieldInPointed = t;
                if(fieldInPointed.indexOf(".") > -1) {
                    fieldInPointed = fieldInPointed.substring(0, fieldInPointed.indexOf("."));
                }
                FieldNode n = mddNode.fields.get(field);
                if(n == null) {
                    factory.doThrow(this.typeName, "Field " + field + " does not exist in type " + mdd.getName() , ast);
                } else {
                    // check if this is a pointer to another type
                    if(!(n.makumbaType == FieldType.PTRREL || n.makumbaType == FieldType.PTR || n.makumbaType == FieldType.PTRONE)) {
                        factory.doThrow(this.typeName, "Field " + field + " is not a pointer", ast);
                    } else {
                        // if it's a pointer, let's check if we can make something out of it
                        try {
                            DataDefinition pointed = MDDProvider.getMDD(n.pointedType);
                            if(pointed.getFieldDefinition(fieldInPointed) == null) {
                                factory.doThrow(this.typeName, "Field " + fieldInPointed + " does not exist in type " + pointed.getName(), ast);
                            }
                            
                        } catch(DataDefinitionNotFoundError d) {
                            factory.doThrow(this.typeName, "Could not find type " + n.pointedType, ast);
                        }
                    }
                }
            }
        
        } else {
            Object field = mddNode.fields.get(t);
            if(field == null) {
                factory.doThrow(this.typeName, "Field " + t + " does not exist in type " + mdd.getName(), ast);
            }
        }
    }
    
    @Override
    protected void processMultiUniqueValidationDefinitions(ValidationRuleNode v, AST v_in) {
        if(v instanceof MultiUniquenessValidationRule) {
            
            for(String path : v.multiUniquenessFields) {
                checkPathValid(v_in, path, v.mdd);
            }
            
            DataDefinition.MultipleUniqueKeyDefinition key = new DataDefinition.MultipleUniqueKeyDefinition(v.multiUniquenessFields.toArray(new String[] {}), v.message);
            mdd.addMultiUniqueKey(key);
        }
    }

}
