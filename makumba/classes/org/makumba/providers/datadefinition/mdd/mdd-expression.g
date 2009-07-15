header {
    package org.makumba.providers.datadefinition.mdd;
}

class MDDExpressionBaseParser extends Parser;

options {
		importVocab=MDD;
        buildAST=true;
        k = 3;
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
    
    private void checkNumber(AST n) {
    	if(n == null)
    	   reportError("Incorrect value for number");
    }
    
    protected void assignPart(ComparisonExpressionNode ce, AST part) {};
    
    private String removeQuotation(String s) {
    	return s.substring(1, s.length() -1);
    }
	
}

expression
	: range
	| fieldList
	| intEnum
	| charEnum
	| comparisonExpression
	;
	
	
	
//////// COMPARISON EXPRESSION

comparisonExpression
	: {ComparisonExpressionNode ce = new ComparisonExpressionNode();} lhs:comparisonPart o:operator {ce.setOperatorType(#o.getType());} rhs:comparisonPart
		{	assignPart(ce, #lhs);
			assignPart(ce, #rhs);
			ce.setType(COMPARE_EXPRESSION);
			ce.setText(#lhs.getText() + " " + #o.getText() + " " + #rhs.getText());
			#comparisonExpression = ce;
		}
	;
	
comparisonPart
	: t:type
	| n:number
	| df:dateFunction
	| u:upperFunction
	| l:lowerFunction
	| d:dateConstant
	;

// here we pass only the type name of the argument, with the function as type
upperFunction
	: UPPER! LEFT_PAREN! t:type RIGHT_PAREN!
		{#upperFunction.setText(#t.getText()); #upperFunction.setType(UPPER);}
	;

// here we pass only the type name of the argument, with the function as type
lowerFunction
	: LOWER! LEFT_PAREN! t:type RIGHT_PAREN!
		{#lowerFunction.setText(#t.getText()); #lowerFunction.setType(LOWER);}
	;

dateConstant
	: NOW | TODAY;

dateFunction
	: DATE^ LEFT_PAREN! dateFunctionArgument (COMMA! dateFunctionArgument)* RIGHT_PAREN!
	;
	
dateFunctionArgument
	: dateFunctionArgumentMember
		(
			(PLUS^ | MINUS^) dateFunctionArgumentMember
		)?
	;
	
dateFunctionArgumentMember
	: number
	| dateConstant
	;


//////// ENUMERATOR BODIES

intEnum
	: intEnumBody (COMMA! intEnumBody)*
	;

charEnum
	: charEnumBody (COMMA! charEnumBody)*
	;
	
intEnumBody
    : t:STRING_LITERAL {#t.setType(INTENUMTEXT); #t.setText(removeQuotation(#t.getText())); }
      EQ!
      i:number { checkNumber(#i); if(#i != null) #i.setType(INTENUMINDEX); }
      (DEPRECATED)?
    ;

charEnumBody
	: t:STRING_LITERAL { #t.setType(CHARENUMELEMENT); #t.setText(removeQuotation(#t.getText())); }
	  (DEPRECATED)?
	;


//////// RANGE DEFINITION

// name%length = [1..?]
// age%range = [18..99]
range
    : 
      f:rangeBound {#f.setType(RANGE_FROM);} DOT! DOT! t:rangeBound {#t.setType(RANGE_TO);}
    ;

// [1..?] [?..5]
rangeBound
    : n:POSITIVE_INTEGER | m:INTMARK
    ;
    
//////// MISC

fieldList
	: type (COMMA! type)*
	;

//////////////// COMMON

// general.Person
// general.Person->extraData
type
    : {String type="";} a:atom {type+=#a.getText();}
    	(
    		  (DOT! {type += ".";} | SUBFIELD! {type += "->";})
    		  (b:atom! {type += #b.getText(); } | k:keyword! {type += #k.getText(); }) 
    	)*
    	{ #type.setText(type); #type.setType(PATH); }
    ;
    
keyword
    : FILE
    ;
    

number
    : POSITIVE_INTEGER | NEGATIVE_INTEGER
      {checkNumber(#number);}
    ;

operator
	: EQ | LT | GT | LE | GE | NE | SQL_NE | LIKE
	;

atom
    : IDENT
    ;