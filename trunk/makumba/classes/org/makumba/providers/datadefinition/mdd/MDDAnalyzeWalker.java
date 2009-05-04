package org.makumba.providers.datadefinition.mdd;

import java.net.URL;
import java.util.HashMap;

import org.makumba.DataDefinition;
import org.makumba.DataDefinitionParseError;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaError;

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

    protected HashMap<String, AnalysisAST> typeShorthands = new HashMap<String, AnalysisAST>();
    
    public MDDAnalyzeWalker(String typeName, URL origin) {
        this.origin = origin;
        this.typeName = typeName;
        this.mdd = new MDDNode(typeName, origin);
        
    }

    @Override
    protected void checkFieldType(AST type) {
        
        // check type attributes
        switch (type.getType()) {
            case MDDTokenTypes.CHAR:
                AST length = type.getFirstChild();
                int l = Integer.parseInt(length.getText());
                if (l > 255) {
//                    throw new DataDefinitionParseError(typeName, "char too long " + ((MDDNode)type).getParent().getLine());
                    throw new DataDefinitionParseError(typeName, "char has a maximum size of 255");
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
            // FIXME give line and col
            throw new DataDefinitionParseError(typeName, "Subfields of subfields are not allowed.");
        }
    }

    @Override
    protected void checkSubFieldName(AST parentName, AST name) {
        if (parentName != null && name != null && !parentName.getText().equals(name.getText())) {
            throw new DataDefinitionParseError("The subfield '" + name.getText() + "' "
                    + " should have as parent name " + parentName);
        }
    }

    @Override
    protected void addTypeShorthand(AST name, AST fieldType) {
        System.out.println("Registering new type shorthand " + name.getText());
        typeShorthands.put(name.getText(), (AnalysisAST)fieldType);
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
            throw new MakumbaError("Should not be here");
        }
        
    }
    

}
