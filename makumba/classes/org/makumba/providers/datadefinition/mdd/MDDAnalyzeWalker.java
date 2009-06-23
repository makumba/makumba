package org.makumba.providers.datadefinition.mdd;

import java.net.URL;
import java.util.HashMap;

import org.makumba.DataDefinition;
import org.makumba.DataDefinitionParseError;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaError;
import org.makumba.providers.datadefinition.mdd.validation.ComparisonValidationRule;
import org.makumba.providers.datadefinition.mdd.validation.RangeValidationRule;
import org.makumba.providers.datadefinition.mdd.validation.RegExpValidationRule;

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

    protected HashMap<String, MDDAST> typeShorthands = new HashMap<String, MDDAST>();
    
    public MDDAnalyzeWalker(String typeName, URL origin, MDDFactory factory) {
        this.origin = origin;
        this.typeName = typeName;
        this.mdd = new MDDNode(typeName, origin);
        this.factory = factory;
        
    }
    
    @Override
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
        }
        // TODO add ptr and set destination check / registration
        
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
    protected void checkSubFieldName(AST parentName, AST name) {
        if (parentName != null && name != null && !parentName.getText().equals(name.getText())) {
            factory.doThrow(this.typeName, "The subfield '" + name.getText() + "' "
                    + " should have as parent name " + parentName, name);
        }
    }

    @Override
    protected void addTypeShorthand(AST name, AST fieldType) {
        System.out.println("Registering new type shorthand " + name.getText());
        typeShorthands.put(name.getText(), (MDDAST)fieldType);
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
    protected void createValidationRule(AST vr, String field, ValidationType type) {
        
        switch(type) {
            case RANGE:
            case LENGTH:
                setCurrentValidationRule(new RangeValidationRule(mdd, vr, field));
                break;
            case REGEXP:
                setCurrentValidationRule(new RegExpValidationRule(mdd, vr, field));
                break;
            case COMPARISON:
                setCurrentValidationRule(new ComparisonValidationRule(mdd, vr));
            default:
                throw new RuntimeException("no matching validation rule found!");
        }
        
        
    }
    
    @Override
    protected void checkRuleApplicability() {
        try {
            getCurrentValidationRule().checkApplicability();
        } catch(Throwable t) {
            factory.doThrow(this.typeName, t.getMessage(), getCurrentValidationRule());
        }
    }
}
