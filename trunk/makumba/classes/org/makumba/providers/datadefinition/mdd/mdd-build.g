// TODO expand FILE type into file declaration

header {
    package org.makumba.providers.datadefinition.mdd;
}

/**
 * MDD builder. Transforms the analysed tree and builds DataDefinition and FieldDefinition objects
 */
class MDDBuildBaseWalker extends TreeParser;

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
   
}

dataDefinition
    : (declaration)*
    ;

declaration
    : fieldDeclaration
    | titleDeclaration
    ;

fieldDeclaration
    : #(f:FIELD { if(((FieldNode)#f_in).makumbaType == null) { processUnknownType(#f_in); } }
         (sf:FIELD { if(((FieldNode)#sf_in).makumbaType == null) { processUnknownType(#sf_in); } } | st:titleDeclaration )*
       )
    ;

titleDeclaration
    : t:TITLEFIELD { checkTitleField(#t_in); }
    ;