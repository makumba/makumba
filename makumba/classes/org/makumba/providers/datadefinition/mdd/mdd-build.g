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

    protected void setModifier(FieldDefinitionImpl field, AST modifier) { }

}

dataDefinition
    : (declaration)*
    ;

declaration
    : fieldDeclaration
    | titleDeclaration
    ;

fieldDeclaration
    : #(
            FIELD { FieldDefinitionImpl field = new FieldDefinitionImpl(); }
            fn:FIELDNAME { field.setName(#fn.getText()); }
            (m:MODIFIER { setModifier(field, m); } )*
            ft:fieldType
            (FIELDCOMMENT)?
              (
                #(
                    SUBFIELD
                    PARENTFIELDNAME
                    SUBFIELDNAME
                    (MODIFIER)*
                    sft:fieldType
                    (FIELDCOMMENT)?
                 )
              )*
       )
    ;
    
    
fieldType
    : UNKNOWN_TYPE
    | #(CHAR
        CHAR_LENGTH
       )
    | INT
    | #(
        INTENUM (
                 it:INTENUMTEXT
                 ii:INTENUMINDEX
                 (id:DEPRECATED)?
                )*
        )
                
    | REAL
    | BOOLEAN
    | TEXT
    | BINARY
    | FILE
    | DATE
    | #(PTR (POINTED_TYPE)?)
    | #(SET (POINTED_TYPE)?) 
    ;
    
titleDeclaration
    : TITLEFIELD
    ;