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
    
    private ValidationRuleNode currentValidationRule;
    
    // ordered map to keep track of fields and handle duplicates, i.e. overriden fields
    private ListOrderedMap fields = new ListOrderedMap();
    
    
    // set makumba type of currently analyzed field
    //protected void setCurrentFieldType(FieldType type) { if(this.currentField != null) this.currentField.makumbaType = type; }
    
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
        
    // create and set validation rule
    protected void createValidationRule(AST vr, String field, ValidationType type) { }
    
    // set current validation rule
    protected void setCurrentValidationRule(ValidationRuleNode validation) {
    	this.currentValidationRule = validation;
    }
    
    // get current validation rule
    protected ValidationRuleNode getCurrentValidationRule() {
    	return this.currentValidationRule;
    }
    
    // check if rule can be applied to fields
    protected void checkRuleApplicability() {}
        
}

dataDefinition
    : (declaration)*
    ;

declaration
    : fieldDeclaration
    | t:titleDeclaration { mdd.setTitleField((TitleFieldNode) #t); }
    | typeDeclaration
    | validationRuleDeclaration
    ;


//////////////// FIELD DECLARATION

fieldDeclaration {FieldType fieldType = null; }
    : #( 
            f:FIELD
            fn:FIELDNAME { checkFieldName(#fn); FieldNode field = new FieldNode(mdd, #fn.getText(), #f); }
              (m:MODIFIER { addModifier(field, #m.getText()); })*
              fieldType=ft:fieldType[field] { checkFieldType(#ft); field.makumbaType = fieldType; }
              (fc:FIELDCOMMENT { field.description = #fc.getText(); } )?
              ( { field.initSubfield(); } subField[field] )*
      ) {
                mdd.addField(field);
                            
                // in the end, the return tree contains only one FieldNode
                #fieldDeclaration = field;
                
                // handle overriden fields
                // TODO maybe remove this
                if(fields.containsKey(#fn.getText())) {
                  // fetch previous field, replace sibling with next
                  int i = fields.indexOf(#fn.getText());
                  AST previous = (AST) fields.getValue(i-1);
                  AST next = null;
                  if(fields.size() <= i+1) {
                    next = #field;
                  } else {
                    next = (AST)fields.getValue(i+1);
                  }
                  previous.setNextSibling(next);
                }
                
                fields.put(#fn.getText(), #fieldDeclaration);    
         }
    ;

// a subfield, i.e. a normal field, a title field, a validation rule or a function, e.g.
// cars=set
// cars->!title = name
// cars->name = char[255]
// cars->name%length=[1..?] : A car must have a non-empty name
// cars->niceName() { upper(name) }
subField[FieldNode field]
	: 
                  #(
                      sf:SUBFIELD
                      pf:PARENTFIELDNAME { checkSubFieldName(field.name, #pf); }
                      (
                          t:titleDeclaration
                          	{
                          		field.subfield.setTitleField((TitleFieldNode) #t);
                          		field.addChild(#t);
                          	}
                          | validationRuleDeclaration
                          |
                          (
                          	{
                          		FieldType subFieldType = null;
                          	}
                            sfn:SUBFIELDNAME { FieldNode subField = new FieldNode(field.subfield, #sfn.getText(), #sf); /*setCurrentField(subField);*/ }
                            (sm:MODIFIER { addModifier(subField, #sm.getText());} )*
                            subFieldType=sft:fieldType[subField] { checkSubFieldType(#sft); subField.makumbaType = subFieldType; }
                            (sfc:FIELDCOMMENT { subField.description = #sfc.getText(); })?
                            {
                      		// we add the subField to the field
                      			field.addSubfield(subField);
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
    
validationRuleDeclaration
    : rangeRule
	
	{
		checkRuleApplicability();
		mdd.addValidationRule(getCurrentValidationRule());
		#validationRuleDeclaration = getCurrentValidationRule();
	}    
    
    ;

// TODO maybe there's a way to avoid repeating the setting of bounds...    
rangeRule
    : #(vr:VALIDATION fn:FIELDNAME
    	(
		    #( r:RANGE rl:RANGE_FROM ru:RANGE_TO
		    	{
		    		createValidationRule(#vr, #fn.getText(), ValidationType.RANGE);
			    	getCurrentValidationRule().type = ValidationType.RANGE;
			    	getCurrentValidationRule().lowerBound = #rl.getText();
			    	getCurrentValidationRule().upperBound = #ru.getText();
		    	}
		    )
		    |
		    #( LENGTH ll:RANGE_FROM lu:RANGE_TO
		    	{
			    	createValidationRule(#vr, #fn.getText(), ValidationType.LENGTH);
			    	getCurrentValidationRule().type = ValidationType.LENGTH;
			    	getCurrentValidationRule().lowerBound = #ll.getText();
			    	getCurrentValidationRule().upperBound = #lu.getText();
		    	}
		    )
		)

	   m:MESSAGE {getCurrentValidationRule().message = #m.getText();}

	   )
	   
    ;
      