package org.makumba.providers.datadefinition.mdd;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.makumba.DataDefinition;
import org.makumba.DataDefinitionNotFoundError;
import org.makumba.MakumbaError;
import org.makumba.DataDefinition.QueryFragmentFunction;
import org.makumba.providers.QueryAnalysisProvider;
import org.makumba.providers.datadefinition.mdd.ComparisonExpressionNode.ComparisonType;
import org.makumba.providers.datadefinition.mdd.validation.ComparisonValidationRule;
import org.makumba.providers.datadefinition.mdd.validation.MultiUniquenessValidationRule;
import org.makumba.providers.query.mql.HqlParser;

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
            if(fieldNode.makumbaType == FieldType.INTENUM || fieldNode.makumbaType == FieldType.SETINTENUM) {
                fieldNode.intEnumValues = type.intEnumValues;
                fieldNode.intEnumValuesDeprecated = type.intEnumValuesDeprecated;
            } else if(fieldNode.makumbaType == FieldType.CHARENUM || fieldNode.makumbaType == FieldType.SETCHARENUM) {
                fieldNode.charEnumValues = type.charEnumValues;
                fieldNode.charEnumValuesDeprecated = type.charEnumValuesDeprecated;
            }
            
            if(fieldNode.makumbaType == FieldType.SETCHARENUM || fieldNode.makumbaType == FieldType.SETINTENUM) {
                fieldNode.subfield = type.subfield;
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

    private String checkPathValid(AST ast, String t, MDDNode mddNode) {
        
        String type = "";
        
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
                            } else {
                                type = pointed.getFieldDefinition(fieldInPointed).getType();
                            }
                            
                        } catch(DataDefinitionNotFoundError d) {
                            factory.doThrow(this.typeName, "Could not find type " + n.pointedType, ast);
                        }
                    }
                }
            }
            
        } else {
            FieldNode field = mddNode.fields.get(t);
            if(field == null) {
                factory.doThrow(this.typeName, "Field " + t + " does not exist in type " + mdd.getName(), ast);
            } else {
                type =  field.makumbaType.getTypeName();
            }
        }
        return type;
    }
    
    @Override
    protected void processValidationDefinitions(ValidationRuleNode v, AST v_in) {
        if(v instanceof MultiUniquenessValidationRule) {
            
            boolean keyOverSubfield = false;
            for(String path : v.arguments) {
                checkPathValid(v_in, path, v.mdd);
                keyOverSubfield = path.indexOf(".") > -1;
            }
            
            DataDefinition.MultipleUniqueKeyDefinition key = new DataDefinition.MultipleUniqueKeyDefinition(v.arguments.toArray(new String[] {}), v.message);
            key.setKeyOverSubfield(keyOverSubfield);
            mdd.addMultiUniqueKey(key);
            
        } else if(v instanceof ComparisonValidationRule) {
            ComparisonExpressionNode ce = v.comparisonExpression;
            
            // check type
            ComparisonType lhs_type = getPartType(ce, ce.getLhs_type(), ce.getLhs(), v.field);
            ComparisonType rhs_type = getPartType(ce, ce.getRhs_type(), ce.getRhs(), v.field);
            
            if(!rhs_type.equals(rhs_type)) {
                factory.doThrow(typeName, "Invalid comparison expression: left-hand side type is " + lhs_type.name().toLowerCase() + ", right-hand side type is "+rhs_type.name().toLowerCase(), ce);
            }
            
            ce.setComparisonType(lhs_type);
            
            // check arguments vs. operands
            for(String arg : v.arguments) {
                if(!ce.getLhs().equals(arg) && !ce.getRhs().equals(arg)) {
                    factory.doThrow(typeName, "Argument '" + arg + "' is not being used in the comparison expression", ce);
                }
            }
            
            // now we do it the other way around
            if(ce.getLhs_type() == PATH && !v.arguments.contains(ce.getLhs())) {
                factory.doThrow(typeName, "Field '" + ce.getLhs() + "' not an argument of the comparison expression", ce);
            }
            
            if(ce.getRhs_type() == PATH && !v.arguments.contains(ce.getRhs())) {
                factory.doThrow(typeName, "Field '" + ce.getRhs() + "' not an argument of the comparison expression", ce);
            }
        }
            
    }
    
    private ComparisonType getPartType(ComparisonExpressionNode ce, int type, String path, FieldNode parentField) {
        
        switch(type) {
            case UPPER:
            case LOWER:
                return ComparisonType.STRING;
            case DATE:
            case NOW:
            case TODAY:
                return ComparisonType.DATE;
            case NUMBER:
            case POSITIVE_INTEGER:
            case NEGATIVE_INTEGER:
                return ComparisonType.NUMBER;
            case PATH:
                String fieldType = "";
                // we check if by chance we are in a subfield
                if(parentField != null && parentField.subfield != null) {
                    fieldType = checkPathValid(ce, path, parentField.subfield);
                } else {
                    fieldType = checkPathValid(ce, path, mdd);
                }
                FieldType ft = FieldType.valueOf(fieldType.toUpperCase());
                switch(ft) {
                    case CHAR:
                    case TEXT:
                        return ComparisonType.STRING;
                    case INT:
                    case REAL:
                        return ComparisonType.NUMBER;
                    case DATE:
                    case DATECREATE:
                    case DATEMODIFY:
                        return ComparisonType.DATE;
                    default:
                        return ComparisonType.INVALID;
                            
                }
        }
        throw new RuntimeException("could not compute comparison part type of type " + ce.toString());
        
        
    }
    
    @Override
    protected void analyzeFunction(FunctionNode f) {
        compileFunction(f);
    }
    
    
    
    private static final Pattern ident = Pattern.compile("[a-zA-Z]\\w*");
    
    private void compileFunction(FunctionNode funct) {
        // nothing to do, will be compiled at first use
    }

}
