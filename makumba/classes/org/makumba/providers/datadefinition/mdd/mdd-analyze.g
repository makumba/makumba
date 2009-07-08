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
    
    // Check if type and type attributes are correct
    // TODO maybe refactor, i.e. use the already set variables (pointedType, charLength, ...) instead of traversing the AST
    // keep type AST for error processing
    protected void checkFieldType(AST type, FieldNode field) { }
    
    // Check if subfield type is allowed - same as field type but without ptrOne and setComplex
    protected void checkSubFieldType(AST type, FieldNode field) { }
    
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
     
    // add validation rule arguments, i.e. field names that should be checked
    protected void addValidationRuleArgument(String name, ValidationRuleNode n) {n.arguments.add(name);}
     
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
    | functionDeclaration[null]
    ;


//////////////// FIELD DECLARATION

fieldDeclaration { FieldType fieldType = null; }
    : #( 
            f:FIELD
            fn:FIELDNAME { FieldNode field = new FieldNode(mdd, #fn.getText(), #f); field.wasIncluded = ((MDDAST)#f_in).wasIncluded; }
              (m:MODIFIER { addModifier(field, #m.getText()); })*
              fieldType=ft:fieldType[field] { checkFieldType(#ft, field); field.makumbaType = fieldType; }
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
			// FUNCTION DECLARATION
			| functionDeclaration[field]
			// FIELD DECLARATION
			|
			(
				{
          			FieldType subFieldType = null;
          		}
				sfn:SUBFIELDNAME { FieldNode subField = new FieldNode(field.subfield, #sfn.getText(), #sf); subField.wasIncluded = ((MDDAST)#sfn_in).wasIncluded; }
				(sm:MODIFIER { addModifier(subField, #sm.getText());} )*
				subFieldType=sft:fieldType[subField] { checkSubFieldType(#sft, subField); subField.makumbaType = subFieldType; }
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
        (cl:CHAR_LENGTH { field.charLength = Integer.parseInt(#cl.getText()); })?
       )
    | INT { fieldType = FieldType.INT; }
    | #(INTENUM { fieldType = FieldType.INTENUM; } ( intEnumBody[field] )* )
	| #(SETINTENUM { fieldType = FieldType.SETINTENUM; } ( intEnumBody[field] )* { field.initIntEnumSubfield(); } )
    | #(CHARENUM { fieldType = FieldType.CHARENUM; } ( charEnumBody[field] )* )
    | #(SETCHARENUM { fieldType = FieldType.SETCHARENUM; } ( charEnumBody[field] )* { field.initCharEnumSubfield(); } )
	| REAL { fieldType = FieldType.REAL; }
    | BOOLEAN { fieldType = FieldType.BOOLEAN; }
    | TEXT { fieldType = FieldType.TEXT; }
    | BINARY { fieldType = FieldType.BINARY; }
    | FILE { fieldType = FieldType.FILE; }
    | DATE { fieldType = FieldType.DATE; }
    | #(PTR { fieldType = FieldType.PTRONE; #fieldType.setType(PTRONE); } (p:POINTED_TYPE { field.pointedType = #p.getText(); #fieldType.setType(PTR); fieldType = FieldType.PTR; })? )
    | #(SET { fieldType = FieldType.SETCOMPLEX; #fieldType.setType(SETCOMPLEX); } (s:POINTED_TYPE { field.pointedType = #s.getText(); #fieldType.setType(SET); fieldType = FieldType.SET; field.initSetSubfield(); })? )
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
                }
                field.addIntEnumValue(Integer.parseInt(#sii.getText()), #sit.getText());
             }
	;

charEnumBody[FieldNode field]
	: { boolean isDeprecated = false; }
                 ee:CHARENUMELEMENT
                 (cd:DEPRECATED { isDeprecated = true; } )?
                 {
                    if(isDeprecated) {
                        field.addCharEnumValueDeprecated(#ee.getText());
                    } 
                    field.addCharEnumValue(#ee.getText());
                 }
	;

titleDeclaration
    : tf:TITLEFIELDFIELD { #tf.setType(TITLEFIELD); ((TitleFieldNode)#tf).titleType = FIELD; #titleDeclaration = #tf; }
    | (
    	{String[] nameAndArgs;}
      	nameAndArgs=tfun:functionCall
      	{TitleFieldNode title = new TitleFieldNode(nameAndArgs); #titleDeclaration = title; }
      )
    ;

typeDeclaration! // we kick out the declaration after registering it
	: name:TYPENAME
	  // dummy field, needed for keeping intEnum values
	  { FieldNode field = new FieldNode(mdd, #name.getText(), #name); FieldType type = null; }
	  type=ft:fieldType[field] { checkFieldType(#ft, field); field.makumbaType = type; addTypeShorthand(#name, field); }
    ;
    
    
    
    
//////////////// VALIDATION RULES

validationRuleDeclaration[FieldNode subField]
	: {ValidationRuleNode v = null; }
	  (
	  		v = rangeValidationRule[subField]
	  	|	v = lengthValidationRule[subField]
	  	|	v = multiUniquenessValidationRule[subField]
	  	|	v = comparisonValidationRule[subField]
	  	|	v = regexValidationRule[subField]
	  )
	  m:MESSAGE {v.message = #m.getText();}
	  
	  {
		mdd.addValidationRule(v);
		#validationRuleDeclaration = v;
	  }
	  
	;

comparisonValidationRule[FieldNode subField] returns [ValidationRuleNode v = null;]
	: #(c:COMPARE {v = createMultiFieldValidationRule(#c, ValidationType.COMPARISON); }
		(fn:FUNCTION_ARGUMENT {addValidationRuleArgument(#fn.getText(), v);})*
		ce:COMPARE_EXPRESSION
		{
			v.comparisonExpression = (ComparisonExpressionNode) #ce_in;
		}
	  )
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
	
regexValidationRule[FieldNode subField] returns [ValidationRuleNode v = null; ]
	: #(MATCHES fn:FUNCTION_ARGUMENT b:FUNCTION_BODY)
	
	{
		v = createSingleFieldValidationRule(#fn, #fn.getText(), ValidationType.REGEX, subField);
		v.expression = #b.getText();
	}
	;
	

//////////////// FUNCTIONS

functionDeclaration[FieldNode subField]
	: #(FUNCTION {String sessionVar = null;}
	    (s:SESSIONVAR_NAME {sessionVar = #s.getText();})?
	    fn:FUNCTION_NAME
	    {
	    	FunctionNode funct = new FunctionNode(mdd, fn.getText());
	    	funct.sessionVariableName = sessionVar;
	    }
	    functionArgumentDeclaration[funct]
	    b:FUNCTION_BODY {funct.queryFragment = #b.getText();}
	    (m:MESSAGE {funct.errorMessage = #m.getText();})?
	    {
    		mdd.addFunction(funct);
    		#functionDeclaration = funct;
	  	}
	  )
	;

functionArgumentDeclaration[FunctionNode funct]
	: { FieldNode dummy = new FieldNode(mdd, funct.name, funct); FieldType argumentType = null;}
	  	(
		  	argumentType=fieldType[dummy]
		  	an:FUNCTION_ARGUMENT_NAME
		  	{funct.addParameter(an.getText(), argumentType, dummy.pointedType);}
	  	)*
	;
	
functionCall returns[String[] nameAndArgs = null;]
	: {nameAndArgs = new String[20]; int i = 1; } 
	n:FUNCTION_NAME {nameAndArgs[0] = #n.getText(); } (FUNCTION_ARGUMENT {nameAndArgs[i] = #n.getText(); i++; })*
	;