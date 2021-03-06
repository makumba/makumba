header {
    package org.makumba.providers.datadefinition.mdd;
}

{/**
 * MDD builder. Transforms the analysed tree and builds DataDefinition and FieldDefinition objects
 */
@SuppressWarnings("all")} class MDDPostProcessorBaseWalker extends TreeParser;

options {
    importVocab=MDD;
    buildAST=true;
    k=1;
}

{
    RecognitionException error;
    
    public void reportError(RecognitionException e) {
        error=e;
    }

    public void reportError(String s) {
        if (error == null)
            error = new RecognitionException(s);
    }    
    
    
    protected String typeName;
    
    protected MDDNode mdd;

    protected void processUnknownType(AST field) { }
    
    protected void checkTitleField(AST titleField) { }
    
    protected void processValidationDefinitions(ValidationRuleNode v, AST v_in) { }
    
    protected void analyzeFunction(FunctionNode f) { }
   
}

dataDefinition
    : (declaration)*
    ;

declaration
    : fieldDeclaration
    | titleDeclaration
    | validationRuleDeclaration
    | functionDeclaration
    ;

fieldDeclaration
    : #(f:FIELD { if(((FieldNode)#f_in).makumbaType == null) { processUnknownType(#f_in); } }
         (subField)*
       )
    ;

subField
    : #(sf:FIELD { if(((FieldNode)#sf_in).makumbaType == null) { processUnknownType(#sf_in); } } (subField)* )
    | subFieldTypes
    ;
    
    
subFieldTypes
    : st:titleDeclaration
      | v:validationRuleDeclaration
      | functionDeclaration
      ;

titleDeclaration
    : t:TITLEFIELD {checkTitleField(#t_in); }
    ;

validationRuleDeclaration
	: v:VALIDATION { processValidationDefinitions((ValidationRuleNode)v, #v_in); }
	;
	
functionDeclaration
	: f:FUNCTION { analyzeFunction((FunctionNode)#f_in); }
	;
