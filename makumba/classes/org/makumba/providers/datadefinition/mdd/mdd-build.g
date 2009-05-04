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
    k=2;
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

    protected void processUnknownType(FieldNode field) { }

}

dataDefinition
    : (declaration)*
    ;

declaration
    : fieldDeclaration
    | titleDeclaration
    ;

fieldDeclaration
    : #(f:FIELD {
        if(((FieldNode)#f_in).makumbaType == null) {
          processUnknownType((FieldNode)#f_in);
        }
      }
      (sf:FIELD {
        if(((FieldNode)#sf_in).makumbaType == null) {
          processUnknownType((FieldNode)#sf_in);
        }
      } )* )
    ;

titleDeclaration
    : TITLEFIELD
    ;