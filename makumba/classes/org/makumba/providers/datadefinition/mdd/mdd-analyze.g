// TODO

// other todo:
//   take care of validation
//   take care of functions

header {
    package org.makumba.providers.datadefinition.mdd;
    
    import java.net.URL;
    import org.apache.commons.collections.map.ListOrderedMap;
}

class MDDAnalyzeBaseWalker extends TreeParser;

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
    
    protected URL origin;
    
    protected MDDNode mdd;
    
    // Check if field name is valid
    protected void checkFieldName(AST fieldName) {}
    
    // Check if type and type attributes are correct
    // TODO maybe refactor, i.e. use the already set variables (pointedType, charLength, ...) instead of traversing the AST
    // keep type AST for error processing
    protected void checkFieldType(AST type) { }
    
    // Check if subfield type is allowed - same as field type but without ptrOne and setComplex
    protected void checkSubFieldType(AST type) { }
    
    // Check if name of parent in subfield is the name of parent
    protected void checkSubFieldName(String parentName, AST name) { }
    
    // Add type shorthand
    protected void addTypeShorthand(AST name, FieldNode fieldType) { }
    
    // Add modifier
    protected void addModifier(FieldNode field, String modifier) { }
    
    // Add field to mdd
    protected void addField(MDDNode mdd, FieldNode field) { }
    
    // Add subfield
    protected void addSubfield(FieldNode parent, FieldNode field) { }
    
    // Add multi-unique key
    protected void addMultiUniqueKey(ValidationRuleNode v, AST path) { }
    
    // create and set validation rule
    protected ValidationRuleNode createSingleFieldValidationRule(AST originAST, String fieldName, ValidationType type, FieldNode subField) { return null; }
    
    protected ValidationRuleNode createMultiFieldValidationRule(AST originAST, ValidationType type) { return null; }
        
     
    // check if rule can be applied to fields
    protected void checkRuleApplicability(ValidationRuleNode validation) {}
         
}

dataDefinition
    : (declaration)*
    ;

declaration
    : fieldDeclaration
    | t:titleDeclaration { mdd.setTitleField((TitleFieldNode) #t); }
    | typeDeclaration
    | validationRuleDeclaration[null]
    ;


//////////////// FIELD DECLARATION

fieldDeclaration { FieldType fieldType = null; }
    : #( 
            f:FIELD
            fn:FIELDNAME { checkFieldName(#fn); FieldNode field = new FieldNode(mdd, #fn.getText(), #f); field.wasIncluded = ((MDDAST)#f_in).wasIncluded; }
              (m:MODIFIER { addModifier(field, #m.getText()); })*
              fieldType=ft:fieldType[field] { checkFieldType(#ft); field.makumbaType = fieldType; }
              (fc:FIELDCOMMENT { field.description = #fc.getText(); } )?
              ( { field.initSubfield(); } subField[field] )*
      ) {
                addField(mdd, field);
                // in the end, the return tree contains only one FieldNode
                #fieldDeclaration = field;
        }
    ;

// a subfield, i.e. a normal field, a title field, a validation rule or a function, e.g.
// cars=set
// cars->!title = name
// cars->name = char[255]
// cars->name%length=[1..?] : A car must have a non-empty name
// cars->niceName() { upper(name) }
subField[FieldNode field]
	: #(
		sf:SUBFIELD
		pf:PARENTFIELDNAME { checkSubFieldName(field.name, #pf); }
		(
			// TITLE DECLARATION
			t:titleDeclaration
          	{
          		field.subfield.setTitleField((TitleFieldNode) #t);
          		field.addChild(#t);
          	}
          	// VALIDATION RULE
			| validationRuleDeclaration[field]
			// FIELD DECLARATION
			|
			(
				{
          			FieldType subFieldType = null;
          		}
				sfn:SUBFIELDNAME { FieldNode subField = new FieldNode(field.subfield, #sfn.getText(), #sf); subField.wasIncluded = ((MDDAST)#sfn_in).wasIncluded; }
				(sm:MODIFIER { addModifier(subField, #sm.getText());} )*
				subFieldType=sft:fieldType[subField] { checkSubFieldType(#sft); subField.makumbaType = subFieldType; }
				(sfc:FIELDCOMMENT { subField.description = #sfc.getText(); })?
				{
  					// we add the subField to the field
      				addSubfield(field, subField);
      				field.addChild(subField);
    	    	}
          	)
      	)
   	  )
	;

    
fieldType[FieldNode field] returns [FieldType fieldType = null; ]
    : (
      u:UNKNOWN_TYPE { field.unknownType = #u.getText(); } // will need processing afterwards, this happens when dealing with macro types - needs to be stored somehow in the field though!
    | #(CHAR { fieldType = FieldType.CHAR; }
        cl:CHAR_LENGTH { field.charLength = Integer.parseInt(#cl.getText()); }
       )
    | INT { fieldType = FieldType.INT; }
    | #(INTENUM { fieldType = FieldType.INTENUM; } ( intEnumBody[field] )* )
	| #(SETINTENUM { fieldType = FieldType.SETINTENUM; } ( intEnumBody[field] )* )
    | #(CHARENUM { fieldType = FieldType.CHARENUM; } ( charEnumBody[field] )* )
    | #(SETCHARENUM { fieldType = FieldType.SETCHARENUM; } ( charEnumBody[field] )* )
	| REAL { fieldType = FieldType.REAL; }
    | BOOLEAN { fieldType = FieldType.BOOLEAN; }
    | TEXT { fieldType = FieldType.TEXT; }
    | BINARY { fieldType = FieldType.BINARY; }
    | FILE { fieldType = FieldType.FILE; }
    | DATE { fieldType = FieldType.DATE; }
    | #(PTR { fieldType = FieldType.PTRONE; #fieldType.setType(PTRONE); } (p:POINTED_TYPE { field.pointedType = #p.getText(); #fieldType.setType(PTR); fieldType = FieldType.PTRREL; })? )
    | #(SET { fieldType = FieldType.SETCOMPLEX; #fieldType.setType(SETCOMPLEX); } (s:POINTED_TYPE { field.pointedType = #s.getText(); #fieldType.setType(SET); fieldType = FieldType.SET; })? )
    )
    {
        ((MDDAST)#fieldType).makumbaType = fieldType;
    }
    ;

intEnumBody[FieldNode field]
	:  { boolean isDeprecated = false; }
             sit:INTENUMTEXT
             sii:INTENUMINDEX
             (sid:DEPRECATED { isDeprecated = true; } )?
             {
                if(isDeprecated) {
                    field.addIntEnumValueDeprecated(Integer.parseInt(#sii.getText()), #sit.getText());
                } else {
                    field.addIntEnumValue(Integer.parseInt(#sii.getText()), #sit.getText());
                }
             }
	;

charEnumBody[FieldNode field]
	: { boolean isDeprecated = false; }
                 ee:CHARENUMELEMENT
                 (cd:DEPRECATED { isDeprecated = true; } )?
                 {
                    if(isDeprecated) {
                        field.addCharEnumValueDeprecated(#ee.getText());
                    } else {
                        field.addCharEnumValue(#ee.getText());
                    }
                 }
	;

titleDeclaration
    : tf:TITLEFIELDFIELD { #tf.setType(TITLEFIELD); ((TitleFieldNode)#tf).titleType = FIELD;}
    | tfun:TITLEFIELDFUNCTION { #tfun.setType(TITLEFIELD); ((TitleFieldNode)#tfun).titleType = FUNCTION; }
    ;

typeDeclaration! // we kick out the declaration after registering it
	: name:TYPENAME
	  // dummy field, needed for keeping intEnum values
	  { FieldNode field = new FieldNode(mdd, #name.getText(), #name); FieldType type = null; }
	  type=ft:fieldType[field] { checkFieldType(#ft); field.makumbaType = type; addTypeShorthand(#name, field); }
    ;
    
    
    
    
    
    
    
    
    
    
    
    
//////////////// VALIDATION RULES

validationRuleDeclaration[FieldNode subField]
	: {ValidationRuleNode v = null; }
	  (
	  		v = rangeValidationRule[subField]
	  	|	v = lengthValidationRule[subField]
	  	|	v = multiUniquenessValidationRule[subField]
	  	|	v = comparisonValidationRule[subField]
	  )
	  m:MESSAGE {v.message = #m.getText();}
	  
	  {
		mdd.addValidationRule(v);
		#validationRuleDeclaration = v;
	  }
	  
	;

comparisonValidationRule[FieldNode subField] returns [ValidationRuleNode v = null;]
	:#(c:COMPARE {v = createMultiFieldValidationRule(#c, ValidationType.COMPARISON); } (fn:FUNCTION_ARGUMENT)* f:FUNCTION_BODY { v.expression = #f.getText();} )
	;

multiUniquenessValidationRule[FieldNode subField] returns [ValidationRuleNode v = null;]
    : #(u:UNIQUE {v = createMultiFieldValidationRule(#u, ValidationType.UNIQUENESS); } (p:PATH {addMultiUniqueKey(v, #p); } )* )
    ;

rangeValidationRule[FieldNode subField] returns [ValidationRuleNode v = null; ]
	: #(RANGE fn:FUNCTION_ARGUMENT rl:RANGE_FROM ru:RANGE_TO)
	
	{
      	v = createSingleFieldValidationRule(#fn, #fn.getText(), ValidationType.RANGE, subField);
      	v.lowerBound = #rl.getText();
      	v.upperBound = #ru.getText();
    }
	;

lengthValidationRule[FieldNode subField] returns [ValidationRuleNode v = null; ]
	: #(LENGTH fn:FUNCTION_ARGUMENT rl:RANGE_FROM ru:RANGE_TO)
	
	{
      	v = createSingleFieldValidationRule(#fn, #fn.getText(), ValidationType.LENGTH, subField);
      	v.lowerBound = #rl.getText();
      	v.upperBound = #ru.getText();
    }
	;
	

