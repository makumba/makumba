package org.makumba.providers.datadefinition.mdd;

import org.makumba.DataDefinitionParseError;

import antlr.collections.AST;

public class MDDAnalyzeWalker extends MDDAnalyzeBaseWalker {
    
   @Override
   protected void checkFieldType(AST type) {
       System.out.println("Checking field type: "+type);
       System.out.println("First child: "+type.getFirstChild());
   }
   
   @Override
    protected void checkSubFieldName(AST parentName, AST name) {
       if(parentName != null && name != null && !parentName.getText().equals(name.getText())) {
           throw new DataDefinitionParseError("The subfield '" + name.getText() + "' " + " should have as parent name " + parentName);
       }
    }
    
    

}
