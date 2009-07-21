package org.makumba.providers.datadefinition.mdd;

import java.net.URL;
import java.util.HashMap;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaError;
import org.makumba.providers.datadefinition.mdd.validation.ComparisonValidationRule;
import org.makumba.providers.datadefinition.mdd.validation.MultiUniquenessValidationRule;
import org.makumba.providers.datadefinition.mdd.validation.RangeValidationRule;
import org.makumba.providers.datadefinition.mdd.validation.RegExpValidationRule;
import antlr.collections.AST;

/**
 * MDD analysis walker, collects useful information for creation of {@link DataDefinition} and {@link FieldDefinition}
 * 
 * @author Manuel Gay
 * @version $Id: MDDAnalyzeWalker.java,v 1.1 May 2, 2009 10:56:49 PM manu Exp $
 */
public class MDDAnalyzeWalker extends MDDAnalyzeBaseWalker {
    
    private MDDFactory factory = null;

    private MDDParser parser = null;

    protected HashMap<String, FieldNode> typeShorthands = new HashMap<String, FieldNode>();

    private boolean strictTypeCheck;

    
    public MDDAnalyzeWalker(String typeName, URL origin, MDDFactory factory, MDDParser parser, boolean strictTypeCheck) {
        this.origin = origin;
        this.typeName = typeName;
        this.mdd = new MDDNode(typeName, origin);
        this.factory = factory;
        this.parser  = parser;
        this.strictTypeCheck = strictTypeCheck;
    }
    
    @Override
    // TODO maybe refactor, i.e. use the already set variables (pointedType, charLength, ...) instead of traversing the AST
    // keep type AST for error processing
    protected void checkFieldType(AST type, FieldNode field) {
        if(type == null)
            return;
        
        // check type attributes
        switch (type.getType()) {
            case MDDTokenTypes.CHAR:
                AST length = type.getFirstChild();
                if(length != null) {
                    int l = Integer.parseInt(length.getText());
                    if (l > 255) {
                        factory.doThrow(this.typeName, "char has a maximum length of 255", type);
                    }
                }
                break;
            case MDDTokenTypes.PTR:
                checkPointed(type);
                break;
            case MDDTokenTypes.SET:
                checkPointed(type);
                checkModifiers(type, field);
                break;
            case MDDTokenTypes.SETCHARENUM:
            case MDDTokenTypes.SETINTENUM:
            case MDDTokenTypes.SETCOMPLEX:
                checkModifiers(type, field);
                break;
        }
    }

    private void checkModifiers(AST type, FieldNode field) {
        if(field.unique) {
            factory.doThrow(this.typeName, "sets can't be unique", type);
        }
    }

    private void checkPointed(AST type) {
        AST pointedType = type.getFirstChild();
        if(strictTypeCheck) {
            // we check if we can find this type
            URL u = MDDProvider.findDataDefinition(pointedType.getText(), "mdd");
            if(u == null) {
                factory.doThrow(this.typeName, "could not find type " + pointedType.getText(), pointedType);
            }
        }
    }

    @Override
    protected void checkSubFieldType(AST type, FieldNode field) {
        checkFieldType(type, field);
//        if(type.getType() == MDDTokenTypes.SETCOMPLEX || type.getType() == MDDTokenTypes.PTRONE) {
//            factory.doThrow(this.typeName, "Subfields of subfields are not allowed.", type);
//        }
    }

    @Override
    protected void checkSubFieldName(String parentName, AST name) {
        if (parentName != null && name != null && !parentName.equals(name.getText())) {
            factory.doThrow(this.typeName, "The subfield '" + name.getText() + "'"
                    + " should have as parent name " + parentName, name);
        }
    }
    
    
    @Override
    protected FieldNode getParentField(AST parentField) {
        
        FieldNode f = null;
        
        if(parentField.getText().indexOf("->") > -1) {
            
            String path = parentField.getText();
            MDDNode searchMDD = mdd;
            
            while(path.indexOf("->") > -1) {
                String p = path.substring(0, path.indexOf("->"));
                path = path.substring(path.indexOf("->") + 2, path.length());
                FieldNode parent = searchMDD.fields.get(p);
                if(parent == null) {
                    factory.doThrow(typeName, "Field " + p + " does not exist.", parentField);
                }
                searchMDD = parent.subfield;
            }
            
            f = searchMDD.fields.get(path);
            
            
        } else {
            f = mdd.fields.get(parentField.getText());
            
        }
        if(f == null) {
            factory.doThrow(typeName, "Field " + parentField.getText() + " does not exist.", parentField);
        }
        f.initSubfield();
        return f;
        
    }
    

    @Override
    protected void addTypeShorthand(AST name, FieldNode fieldType) {
        typeShorthands.put(name.getText(), fieldType);
    }
    
    @Override
    protected void addModifier(FieldNode field, String modifier) {
        
        if(modifier.equals("unique")) {
            field.unique = true;
        } else if(modifier.equals("not null")) {
            field.notNull = true;
        } else if(modifier.equals("fixed")) {
            field.fixed = true;
        } else if(modifier.equals("not empty")) {
            field.notEmpty = true;
        } else {
            throw new MakumbaError("Modifier " + modifier + " invalid");
        }
    }
    
    @Override
    protected void addField(MDDNode mdd, FieldNode field) {
        FieldNode previous = mdd.fields.get(field.name);
        if(previous != null && (previous.wasIncluded || field.wasIncluded)) {
            mdd.fields.remove(field.name);
            mdd.addField(field);
        } else if(previous != null && !(previous.wasIncluded || field.wasIncluded)) {
            factory.doThrow(typeName, "Duplicated field definition for field " + field.name, field);
        } else {
            mdd.addField(field);
        }
    }
    
    @Override
    protected void addSubfield(FieldNode parent, FieldNode field) {
        FieldNode previous = parent.subfield.fields.get(field.name);
        if(previous != null && (previous.wasIncluded || field.wasIncluded)) {
            parent.subfield.fields.remove(field.name);
            parent.subfield.addField(field);
        } else if(previous != null && !(previous.wasIncluded || field.wasIncluded)) {
            factory.doThrow(typeName, "Duplicated field definition for field " + field.name, field);
        } else {
            parent.subfield.addField(field);
        }
    }
    
    @Override
    protected void addMultiUniqueKey(ValidationRuleNode v, AST path) {
        // we check for validity of the paths during postprocessing, when we have all the fields
        v.multiUniquenessFields.add(path.getText());
    }
    
    @Override
    protected ValidationRuleNode createMultiFieldValidationRule(AST originAST, ValidationType type, FieldNode subField) {
        switch(type) {
            case UNIQUENESS:
                ValidationRuleNode n = new MultiUniquenessValidationRule(mdd, originAST, type, subField);
                return n;
            case COMPARISON:
                ValidationRuleNode comparison = new ComparisonValidationRule(mdd, originAST, type, subField);
                return comparison;
            default:
                throw new RuntimeException("no matching validation rule found!");
        }
    }

    @Override
    protected ValidationRuleNode createSingleFieldValidationRule(AST originAST, String fieldName, ValidationType type, FieldNode subField) {
        
        // if fieldNode is not null, we got the FieldNode of a subfield, and hence fetch the field from there
        FieldNode f = null;
        if(subField != null) {
            f = subField.subfield.fields.get(fieldName);
            if(f == null) {
                factory.doThrow(this.typeName, "Subfield " + fieldName + " does not exist in field " + subField.subfield.getName(), originAST);
            }
        } else {
            f = mdd.fields.get(fieldName);
            if(f == null) {
                factory.doThrow(this.typeName, "Field " + fieldName + " does not exist in type " + mdd.getName(), originAST);
            }

        }
      
        switch(type) {
            case RANGE:
            case LENGTH:
                return new RangeValidationRule(mdd, originAST, f, type);
            case REGEX:
                return new RegExpValidationRule(mdd, originAST, f, type);
            default:
                throw new RuntimeException("no matching validation rule found");
        }
    }
    
    @Override
    protected void addValidationRuleArgument(String name, ValidationRuleNode n) {
        n.arguments.add(name);
    }
    
    @Override
    protected void checkRuleApplicability(ValidationRuleNode validation) {
        try {
            validation.checkApplicability();
        } catch(Throwable t) {
            factory.doThrow(this.typeName, t.getMessage(), validation);
        }
    }
    
    @Override
    protected void addNativeValidationRuleMessage(AST fieldName, AST errorType, String message) {
        
        // TODO add subfield support
        
        FieldNode f = mdd.fields.get(fieldName.getText());
        if(f == null) {
            factory.doThrow(typeName, "Field " + fieldName.getText() + " does not exist", fieldName);
        }
        
        switch(errorType.getType()) {
            case UNIQUE:
                f.uniqueError = message;
                break;
            case NOTNULL:
                f.notNullError = message;
                break;
            case NAN:
                f.NaNError = message;
                break;
            case NOTEMPTY:
                f.notEmptyError = message;
                break;
            case NOTINT:
                f.notIntError = message;
                break;
            case NOTREAL:
                f.notRealError = message;
                break;
                
        }
    }
    
}