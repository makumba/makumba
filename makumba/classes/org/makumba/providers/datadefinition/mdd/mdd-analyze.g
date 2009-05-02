header {
    package org.makumba.providers.datadefinition.mdd;
}

class MDDAnalyzeBaseWalker extends TreeParser;

options {
    importVocab=MDD;
    buildAST=true;
    k=2;
}

{
    // Check if type is allowed
    protected void checkFieldType(AST type) { }
    
    // Check if subfield type is allowed - same as field type but without ptrOne and setComplex
    protected void checkSubFieldType(AST type) { }
    
    // Check if name of parent in subfield is the name of parent
    protected void checkSubFieldName(AST parentName, AST name) { }
}

declaration
    : (fieldDeclaration)*
    ;

fieldDeclaration
    : #(
            FIELD
            fn:FIELDNAME
            (MODIFIER)*
            ft:fieldType { checkFieldType(#ft); }
            (FIELDCOMMENT)?
              (
                #(
                    SUBFIELD
                    PARENTFIELDNAME {checkSubFieldName(#fn, #PARENTFIELDNAME); }
                    SUBFIELDNAME
                    (MODIFIER)*
                    sft:fieldType { checkFieldType(#sft); }
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
                 INTENUMTEXT
                 INTENUMINDEX
                 (DEPRECATED)?
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
