package org.makumba.providers.datadefinition.mdd;

import java.util.HashMap;

import org.makumba.DataDefinitionNotFoundError;

import antlr.TokenStream;
import antlr.collections.AST;

/**
 * MDD Parser extending the parser generated by ANTLR for performing specific pre-processing operations
 * @author Manuel Gay
 * 
 * @version $Id: MDDParser.java,v 1.1 May 12, 2009 11:37:36 AM manu Exp $
 */
public class MDDParser extends MDDBaseParser {

    private MDDFactory factory = null;

    
    public MDDParser(TokenStream lexer, MDDFactory factory) {
        super(lexer);
        this.factory = factory;
    }
    
    @Override
    protected AST include(AST type) {
        AST included = null;
        try {
            included = factory.parseIncludedDataDefinition(type.getText());
        } catch(DataDefinitionNotFoundError e) {
            factory.doThrow(type.getText(), "Could not find included data definition", type);
        }
        
        // TODO fix line number
        /*
        MDDAST t = (MDDAST)included;
        while(t.getNextSibling() != null) {
            ((MDDAST)t.getFirstChild()).setLine(type.getLine());
            t = (MDDAST) t.getNextSibling();
        }
        */
        
        return included;
        
    }
    
    @Override
    protected AST includeSubField(AST type, AST parentField) {
        AST included = include(type);
        return transformToSubfield(parentField, included);
    }

    private AST transformToSubfield(AST parentField, AST included) {
        // process the included AST so that it fits the subField structure parentName -> field = type
        AST t = included;
        AST result = null;
        
        while(t != null) {
            
            //  subfield
            //  |
            //  |-- parentFieldName
            //  |   |
            //  |   subFieldName
            //  |   |
            //  |   subFieldType
            //  |
            //  subfield2
            //  ...
            MDDAST subfield = new MDDAST();
            subfield.setText("->!");
            subfield.setType(MDDTokenTypes.SUBFIELD);
            subfield.setLine(parentField.getLine());
            
            MDDAST parentFieldName = new MDDAST();
            parentFieldName.setText(parentField.getText());
            parentFieldName.setType(MDDTokenTypes.PARENTFIELDNAME);
            parentFieldName.setLine(parentField.getLine());
            
            subfield.setFirstChild(parentFieldName);
            
            // build tree
            t.getFirstChild().setType(MDDTokenTypes.SUBFIELDNAME);
            
            subfield.getFirstChild().setNextSibling(t.getFirstChild());
            
            if(result == null) {
                result = subfield;
            } else {
                getLastSibling(result).setNextSibling(subfield);
            }
           
           t = t.getNextSibling();
        }
        return result;
    }
    
    private AST getLastSibling(AST t) {
        while(t.getNextSibling() != null) {
            t = t.getNextSibling();
        }
        return t;
    }
    
    private HashMap<String, AST> fieldsToDisable = new HashMap<String, AST>();

    private HashMap<String, AST> fieldsToTransform = new HashMap<String, AST>();

    @Override
    protected void transformToFile(AST field) {
        fieldsToTransform.put(field.getText(), field);
    }
    
    @Override
    protected void disableField(AST field) {
        fieldsToDisable.put(field.getText(), field);
    }
    
    private AST fileAST = null;
    
    /**
     * performs post-processing tasks of parsing:<br>
     * - deactivates disabled fields from inclusions<br>
     * - replaces field type with a ptrOne containing the file definition<br>
     */
    protected void postProcess() {
        AST t = getAST();
        AST prec = null;
        while(t.getNextSibling() != null) {
            prec = t;
            t = t.getNextSibling();
            if(t.getType() == MDDTokenTypes.FIELD && fieldsToDisable.containsKey(t.getText())) {
                prec.setNextSibling(t.getNextSibling());
            }
            
            /*

            if(t.getType() == MDDTokenTypes.FIELD && fieldsToTransform.containsKey(t.getText())) {
                // we have a data definition
                // and we want the file type to be extended into this data definition
                // so that we can do stuff like file.contentLength
                // for this we need to treat it as a ptrOne to a virtual DD
                
                MDDAST fileField = new MDDAST();
                fileField.setType(FIELD);
                fileField.setText(t.getText());
                
                MDDAST fileName = new MDDAST();
                fileName.setType(FIELDNAME);
                fileName.setText(t.getText());
                
                // we replace the file by a ptrOne
                MDDAST fileType = new MDDAST();
                fileType.setType(FIELDTYPE);
                fileType.setText("ptr");
                
                fileField.setFirstChild(fileName);
                fileName.setNextSibling(fileType);
                
                if(fileAST == null) {
                    fileAST = factory.parse("file", fileTypeDef);
                }

                AST includedFile = transformToSubfield(fileField, fileAST);
                t = t.getNextSibling();
                includedFile.setNextSibling(t);
                prec.setNextSibling(fileField);
                

                

           }
           */
        }
    }
    
    private final static String fileTypeDef = 
          "content = binary\n"
        + "contentLength = int\n"
        + "contentType = char[255]\n"
        + "originalName = char[255]\n"
        + "name = char[255]\n"
        + "imageWidth = int\n"
        + "imageHeight = int\n";
}
