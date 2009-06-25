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
    
    private FieldNode currentField;
    
    private ValidationRuleNode currentValidationRule;
    
    // ordered map to keep track of fields and handle duplicates, i.e. overriden fields
    private ListOrderedMap fields = new ListOrderedMap();
    
    // set currently analyzed field
    protected void setCurrentField(FieldNode field) { this.currentField = field; }
    
    // get currently analyzed field
    protected FieldNode getCurrentField() { if(this.currentField == null) this.currentField = new FieldNode(mdd, "dummy"); return this.currentField; }
    
    // set makumba type of currently analyzed field
    protected void setCurrentFieldType(FieldType type) { if(this.currentField != null) this.currentField.makumbaType = type; }

    // set type of currently analyzed field if mak type is unknown
    protected void setCurrentFieldTypeUnknown(String type) { if(this.currentField != null) this.currentField.unknownType = type; }
    
    // Check if field name is valid
    protected void checkFieldName(AST fieldName) {}
    
    // Check if type and type attributes are correct
    protected void checkFieldType(AST type) { }
    
    // Check if subfield type is allowed - same as field type but without ptrOne and setComplex
    protected void checkSubFieldType(AST type) { }
    
    // Check if name of parent in subfield is the name of parent
    protected void checkSubFieldName(AST parentName, AST name) { }
    
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

fieldDeclaration
    : #(
            f:FIELD
            fn:FIELDNAME { checkFieldName(#fn); FieldNode field = new FieldNode(mdd, #fn.getText(), #f); setCurrentField(field); }
              (m:MODIFIER { addModifier(field, #m.getText()); })*
              ft:fieldType { checkFieldType(#ft); }
              (fc:FIELDCOMMENT { getCurrentField().description = #fc.getText(); } )?
                ( { MDDNode subFieldDD = field.initSubfield(); }
                  #(
                      sf:SUBFIELD
                      pf:PARENTFIELDNAME { checkSubFieldName(#fn, #pf); }
                      (
                          (t:titleDeclaration { subFieldDD.setTitleField((TitleFieldNode) #t); field.addChild(#t); })
                          |
                          (
                              sfn:SUBFIELDNAME { FieldNode subField = new FieldNode(subFieldDD, #sfn.getText(), #sf); setCurrentField(subField); }
                              (sm:MODIFIER { addModifier(subField, #sm.getText());} )*
                              sft:fieldType { checkSubFieldType(#sft); }
                              (sfc:FIELDCOMMENT { subField.description = #sfc.getText(); })?
                              {
                                  // we add the subField to the field
                                  field.addSubfield(#pf.getText(), subField);
                                  field.addChild(subField);
                              }
                          )
                      )
                   )
                )* {
                      // we set back the current field
                      setCurrentField(field);
                   }
            
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
    
    
fieldType
    :
    { FieldType type = null; } (
      u:UNKNOWN_TYPE { setCurrentFieldTypeUnknown(#u.getText()); } // will need processing afterwards, this happens when dealing with macro types - needs to be stored somehow in the field though!
    | #(CHAR { type = FieldType.CHAR; }
        cl:CHAR_LENGTH { getCurrentField().charLength = Integer.parseInt(#cl.getText()); }
       )
    | INT { type = FieldType.INT; }
    | #(
        INTENUM { type = FieldType.INTENUM; } ( { boolean isDeprecated = false; }
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

	// FIXME it's quite ugly that we copy this, we could try to make a separate subrule but we'd need to somehow return the type
	| #(
    SETINTENUM { type = FieldType.SETINTENUM; } ( { boolean isDeprecated = false; }
             sit:INTENUMTEXT
             sii:INTENUMINDEX
             (sid:DEPRECATED { isDeprecated = true; } )?
             {
                if(isDeprecated) {
                    getCurrentField().addIntEnumValueDeprecated(Integer.parseInt(#sii.getText()), #sit.getText());
                } else {
                    getCurrentField().addIntEnumValue(Integer.parseInt(#sii.getText()), #sit.getText());
                }
             }
            )*
    )
    
    | #(
        CHARENUM { type = FieldType.CHARENUM; } ( { boolean isDeprecated = false; }
                 ee:CHARENUMELEMENT
                 (cd:DEPRECATED { isDeprecated = true; } )?
                 {
                    if(isDeprecated) {
                        getCurrentField().addCharEnumValueDeprecated(#ee.getText());
                    } else {
                        getCurrentField().addCharEnumValue(#ee.getText());
                    }
                 }
                )*
        )
        
    | #(
        SETCHARENUM { type = FieldType.SETCHARENUM; } ( { boolean isDeprecated = false; }
                 see:CHARENUMELEMENT
                 (scd:DEPRECATED { isDeprecated = true; } )?
                 {
                    if(isDeprecated) {
                        getCurrentField().addCharEnumValueDeprecated(#see.getText());
                    } else {
                        getCurrentField().addCharEnumValue(#see.getText());
                    }
                 }
                )*
        )
    
    
	| REAL { type = FieldType.REAL; }
    | BOOLEAN { type = FieldType.BOOLEAN; }
    | TEXT { type = FieldType.TEXT; }
    | BINARY { type = FieldType.BINARY; }
    | FILE { type = FieldType.FILE; }
    | DATE { type = FieldType.DATE; }
    | #(PTR { type = FieldType.PTRONE; #fieldType.setType(PTRONE); } (p:POINTED_TYPE { getCurrentField().pointedType = #p.getText(); #fieldType.setType(PTR); type =FieldType.PTRREL; })? )
    | #(SET { type = FieldType.SETCOMPLEX; #fieldType.setType(SETCOMPLEX); } (s:POINTED_TYPE { getCurrentField().pointedType = #s.getText(); #fieldType.setType(SET); type = FieldType.SET; })? )
    )
    {
        setCurrentFieldType(type);
        ((MDDAST)#fieldType).makumbaType = type;
    }
    ;
    
titleDeclaration
    : tf:TITLEFIELDFIELD { #tf.setType(TITLEFIELD); ((TitleFieldNode)#tf).titleType = FIELD;}
    | tfun:TITLEFIELDFUNCTION { #tfun.setType(TITLEFIELD); ((TitleFieldNode)#tfun).titleType = FUNCTION; }
    ;

typeDeclaration! // we kick out the declaration after registering it
	: name:TYPENAME
	  // dummy field, needed for keeping intEnum values
	  { FieldNode field = new FieldNode(mdd, #name.getText(), #name); setCurrentField(field); }
	  ft:fieldType { checkFieldType(#ft); field.makumbaType = ((MDDAST)#ft).makumbaType; addTypeShorthand(#name, field); }
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
      