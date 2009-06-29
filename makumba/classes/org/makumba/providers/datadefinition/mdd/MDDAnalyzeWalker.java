package org.makumba.providers.datadefinition.mdd;

import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;

import org.hibernate.QueryException;
import org.makumba.DataDefinition;
import org.makumba.DataDefinitionParseError;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaError;
import org.makumba.MakumbaSystem;
import org.makumba.commons.ReservedKeywords;
import org.makumba.providers.Configuration;
import org.makumba.providers.QueryAnalysis;
import org.makumba.providers.QueryAnalysisProvider;
import org.makumba.providers.QueryProvider;
import org.makumba.providers.datadefinition.mdd.validation.ComparisonValidationRule;
import org.makumba.providers.datadefinition.mdd.validation.MultiUniquenessValidationRule;
import org.makumba.providers.datadefinition.mdd.validation.RangeValidationRule;
import org.makumba.providers.datadefinition.mdd.validation.RegExpValidationRule;
import org.makumba.providers.query.mql.MqlQueryAnalysis;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;

/**
 * MDD analysis walker, collects useful information for creation of {@link DataDefinition} and {@link FieldDefinition}
 * 
 * TODO implement mechanism to throw useful {@link DataDefinitionParseError} (col, line, line text)
 * 
 * @author Manuel Gay
 * @version $Id: MDDAnalyzeWalker.java,v 1.1 May 2, 2009 10:56:49 PM manu Exp $
 */
public class MDDAnalyzeWalker extends MDDAnalyzeBaseWalker {
    
    private MDDFactory factory = null;

    private MDDParser parser = null;

    protected HashMap<String, FieldNode> typeShorthands = new HashMap<String, FieldNode>();

    
    public MDDAnalyzeWalker(String typeName, URL origin, MDDFactory factory, MDDParser parser) {
        this.origin = origin;
        this.typeName = typeName;
        this.mdd = new MDDNode(typeName, origin);
        this.factory = factory;
        this.parser  = parser;
    }
    
    @Override
    protected void checkFieldName(AST fieldName) {
    
        String nm = fieldName.getText();
        
        for (int i = 0; i < nm.length(); i++) {
            if (i == 0 && !Character.isJavaIdentifierStart(nm.charAt(i)) || i > 0
                    && !Character.isJavaIdentifierPart(nm.charAt(i))) {
                factory.doThrow(this.typeName, "Invalid character \"" + nm.charAt(i) + "\" in field name \"" + nm, fieldName);
            }
        }

        if (ReservedKeywords.isReservedKeyword(nm)) {
            factory.doThrow(this.typeName, "Error: field name cannot be one of the reserved keywords "
                    + ReservedKeywords.getKeywordsAsString(), fieldName);
        }
    }
    
    @Override
    // TODO maybe refactor, i.e. use the already set variables (pointedType, charLength, ...) instead of traversing the AST
    // keep type AST for error processing
    protected void checkFieldType(AST type) {
        if(type == null)
            return;
        
        // check type attributes
        switch (type.getType()) {
            case MDDTokenTypes.CHAR:
                AST length = type.getFirstChild();
                int l = Integer.parseInt(length.getText());
                if (l > 255) {
                    factory.doThrow(this.typeName, "char has a maximum length of 255", type);
                }
                break;
            case MDDTokenTypes.PTR:
            case MDDTokenTypes.SET:
                AST pointedType = type.getFirstChild();
                // we check if we can find this type
                URL u = MDDProvider.findDataDefinition(pointedType.getText(), "mdd");
                if(u == null) {
                    factory.doThrow(this.typeName, "could not find type " + pointedType.getText(), pointedType);
                }
                break;
        }
        
        System.out.println("Checking field type: " + type);
    }

    @Override
    protected void checkSubFieldType(AST type) {
        
        System.out.println("Checking subfield type: " + type);
        checkFieldType(type);
        if(type.getType() == MDDTokenTypes.SETCOMPLEX || type.getType() == MDDTokenTypes.PTRONE) {
            factory.doThrow(this.typeName, "Subfields of subfields are not allowed.", type);
        }
    }

    @Override
    protected void checkSubFieldName(String parentName, AST name) {
        if (parentName != null && name != null && !parentName.equals(name.getText())) {
            factory.doThrow(this.typeName, "The subfield '" + name.getText() + "' "
                    + " should have as parent name " + parentName, name);
        }
    }

    @Override
    protected void addTypeShorthand(AST name, FieldNode fieldType) {
        System.out.println("Registering new type shorthand " + name.getText());
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
        
        String fieldPath = path.getText();
        if(fieldPath.indexOf(".") == -1) {
            if(!mdd.fields.containsKey(fieldPath)) {
                factory.doThrow(this.typeName, "Field " + fieldPath + " does not exist", path);
            }
        } else {
            // TODO check if the path is valid
            
        }
        
        v.multiUniquenessFields.add(path.getText());
        
    }
    
    @Override
    protected ValidationRuleNode createMultiFieldValidationRule(AST originAST, ValidationType type) {
        switch(type) {
            case UNIQUENESS:
                ValidationRuleNode n = new MultiUniquenessValidationRule(mdd, originAST, type);
                n.type = ValidationType.UNIQUENESS;
                return n;
            case COMPARISON:
                ValidationRuleNode comparison = new ComparisonValidationRule(mdd, originAST, type);
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
                factory.doThrow(this.typeName, "Subfield " + fieldName + " does not exist in field " + subField.subfield.name, originAST);
            }
        } else {
            f = mdd.fields.get(fieldName);
            if(f == null) {
                factory.doThrow(this.typeName, "Field " + fieldName + " does not exist in type " + mdd.name, originAST);
            }

        }
      
        switch(type) {
            case RANGE:
            case LENGTH:
                return new RangeValidationRule(mdd, originAST, f, type);
            case REGEXP:
                return new RegExpValidationRule(mdd, originAST, f);
            case COMPARISON:
                return new ComparisonValidationRule(mdd, originAST, type);
            default:
                throw new RuntimeException("no matching validation rule found");
        }
    }
    
    @Override
    protected void checkRuleApplicability(ValidationRuleNode validation) {
        try {
            validation.checkApplicability();
        } catch(Throwable t) {
            factory.doThrow(this.typeName, t.getMessage(), validation);
        }
    }    
}
