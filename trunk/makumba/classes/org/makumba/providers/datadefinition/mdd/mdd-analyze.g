// TODO
//   create subfields
//   transform tree after field declaration for builder
//   take care of title field (put it in MDDNode)
//   post processing (builder):
//     resolve type shorthands, replace wherever needed (i.e. when AST type is UNKNOWN)

// other todo:
//   add !include mechanism (from beginning in parser)
//   take care of validation
//   take care of functions

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
    
    private FieldNode currentField;
    
    // set currently analyzed field
    protected void setCurrentField(FieldNode field) { this.currentField = field; }
    
    // get currently analyzed field
    protected FieldNode getCurrentField() { if(this.currentField == null) this.currentField = new FieldNode(mdd, "dummy"); return this.currentField; }
    
    // set makumba type of currently analyzed field
    protected void setCurrentFieldType(FieldType type) { if(this.currentField != null) this.currentField.makumbaType = type; }
    
    // Check if type and type attributes are correct
    protected void checkFieldType(AST type) { }
    
    // Check if subfield type is allowed - same as field type but without ptrOne and setComplex
    protected void checkSubFieldType(AST type) { }
    
    // Check if name of parent in subfield is the name of parent
    protected void checkSubFieldName(AST parentName, AST name) { }
    
    // Add type shorthand
    protected void addTypeShorthand(AST name, AST fieldType) { }
    
    // Add modifier
    protected void addModifier(FieldNode field, String modifier) { }
    
    // Create subfield - setComplex, ptrOne
    protected void addSubfield(FieldNode field) { }
    
}

dataDefinition
    : (declaration)*
    ;

declaration
    : fieldDeclaration
    | titleDeclaration
    | typeDeclaration
    ;

fieldDeclaration
    : #(
            FIELD
            fn:FIELDNAME { FieldNode field = new FieldNode(mdd, #fn.getText()); setCurrentField(field); }
            (m:MODIFIER { addModifier(field, #m.getText()); })*
            ft:fieldType { checkFieldType(#ft); }
            (fc:FIELDCOMMENT { getCurrentField().description = #fc.getText(); } )?
              (
                #(
                    SUBFIELD
                    PARENTFIELDNAME { checkSubFieldName(#fn, #PARENTFIELDNAME); }
                    SUBFIELDNAME
                    (MODIFIER)*
                    sft:fieldType { checkSubFieldType(#sft); }
                    (FIELDCOMMENT)?
                 )
              )*
       )
    ;
    
    
fieldType
    : UNKNOWN_TYPE
    | #(CHAR { setCurrentFieldType(FieldType.CHAR); }
        CHAR_LENGTH
       )
    | INT { setCurrentFieldType(FieldType.INT); }
    | #(
        INTENUM { setCurrentFieldType(FieldType.INTENUM); } ( { boolean isDeprecated = false; }
                 it:INTENUMTEXT
                 ii:INTENUMINDEX
                 (id:DEPRECATED { isDeprecated = true; } )?
                 {
                    if(isDeprecated) {
                        getCurrentField().addIntEnumValueDeprecated(Integer.parseInt(#ii.getText()), #it.getText());
                    } else {
                        getCurrentField().addIntEnumValue(Integer.parseInt(#ii.getText()), #it.getText());
                    }
                 }
                )*
        )
                
    | REAL { setCurrentFieldType(FieldType.REAL); }
    | BOOLEAN { setCurrentFieldType(FieldType.BOOLEAN); }
    | TEXT { setCurrentFieldType(FieldType.TEXT); }
    | BINARY { setCurrentFieldType(FieldType.BINARY); }
    | FILE { setCurrentFieldType(FieldType.FILE); }
    | DATE { setCurrentFieldType(FieldType.DATE); }
    | #(PTR { setCurrentFieldType(FieldType.PTR); } (POINTED_TYPE { setCurrentFieldType(FieldType.PTRONE); })? )
    | #(SET { setCurrentFieldType(FieldType.SET); } (POINTED_TYPE { setCurrentFieldType(FieldType.SETCOMPLEX); })? ) 
    ;
    
titleDeclaration
    : TITLEFIELD
    ;

typeDeclaration! // we kick out the declaration after registering it
    : name:TYPENAME ft:fieldType { checkFieldType(#ft); addTypeShorthand(#name, #ft); }
    ;
