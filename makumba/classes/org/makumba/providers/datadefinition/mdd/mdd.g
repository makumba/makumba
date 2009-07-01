header {
    package org.makumba.providers.datadefinition.mdd;
}

class MDDLexer extends Lexer;

options {
    exportVocab=MDD;
    k = 3;
}
	    
LEFT_PAREN: '(';
RIGHT_PAREN: ')';
LEFT_CUBR: '{';
RIGHT_CUBR: '}';
LEFT_SQBR: '[';
RIGHT_SQBR: ']';

EQ: '=';
LT: '<';
GT: '>';
SQL_NE: "<>";
NE: "!=" | "^=";
LE: "<=";
GE: ">=";

PERCENT: '%';
SEMICOLON: ';';
COLON: ':';
COMMA: ',';
DOT: '.';
QUOTMARK: '"';
EXMARK: '!';
INTMARK: '?';
MINUS: '-';

SUBFIELD
    : '-' '>'
    ;


// we allow identifiers to start with a number
IDENT options { testLiterals=true; }
    : ID_START_LETTER ( ID_LETTER )*
    ;

protected
ID_START_LETTER
    :    'a'..'z'
    |    'A'..'Z'
    |    '_'
    |    '\u0080'..'\ufffe'
    ;

protected
ID_LETTER
    :    ID_START_LETTER
    |    '0'..'9'
    ;
    
POSITIVE_INTEGER
    : ('+')? NUMBER
    ;

NEGATIVE_INTEGER
    : '-' NUMBER
    ;

protected    
NUMBER
    : '0'..'9' ('0'..'9')*
    ;


WHITESPACE
    : (' ' | 't' | 'r' | 'n' | '\t') { $setType(Token.SKIP); }
    ;

// Single-line comments
SL_COMMENT
    :  "#"
        (~('\n'|'\r'))* ('\n'|'\r'('\n')?)
        {$setType(Token.SKIP); newline();}
    ;
    
LINEBREAK
    :   '\n'      { newline(); } // unix
    |   '\r' '\n' { newline(); } // dos
    |   '\r'      { newline(); } // mac
    ;
	
FIELDCOMMENT
	: SEMICOLON (~('\n'|'\r'))* LINEBREAK //('\n'|'\r'('\n')?) {newline();}
	;


MESSAGE
	: COLON (~('\n'|'\r'))* LINEBREAK //('\n'|'\r'('\n')?) {newline();}
	;
	
FUNCTION_BODY
	: LEFT_CUBR (~('}'))* RIGHT_CUBR
	;
	
// string literals
STRING_LITERAL
    :   '"' (ESC|~('"'|'\\'|'\n'|'\r'))* '"'
    ;


// escape sequence -- note that this is protected; it can only be called
// from another lexer rule -- it will not ever directly return a token to
// the parser
// There are various ambiguities hushed in this rule. The optional
// '0'...'9' digit matches should be matched here rather than letting
// them go back to STRING_LITERAL to be matched. ANTLR does the
// right thing by matching immediately; hence, it's ok to shut off
// the FOLLOW ambig warnings.
protected
ESC
    :   '\\'
        (   'n'
        |   'r'
        |   't'
        |   'b'
        |   'f'
        |   '"'
        |   '\''
        |   '\\'
        |   ('u')+ HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
        |   '0'..'3'
            (
                options {
                    warnWhenFollowAmbig = false;
                }
            :   '0'..'7'
                (
                    options {
                        warnWhenFollowAmbig = false;
                    }
                :   '0'..'7'
                )?
            )?
        |   '4'..'7'
            (
                options {
                    warnWhenFollowAmbig = false;
                }
            :   '0'..'7'
            )?
        )
    ;


// hexadecimal digit (again, note it's protected!)
protected
HEX_DIGIT
    :   ('0'..'9'|'A'..'F'|'a'..'f')
    ;


// TODO throw better exceptions when no message for validation rules
class MDDBaseParser extends Parser;

options {
        buildAST=true;
        k = 3;
}

tokens {
    FIELD;
    VALIDATION;
    FUNCTION;
    
	// title field
	// !title=name
	// !title = nameSurname()
    TITLEFIELD;
    TITLEFIELDFIELD;
    TITLEFIELDFUNCTION;
    
    // macro types
	// !type.genTyp=int{"Female"=0, "Male"=1}
    TYPENAME;
    TYPEDEF;
    
    INCLUDED;
    
    // MDD field
    // name = unique char[255] ;some comment
    FIELDNAME;
    MODIFIER;
    FIELDTYPE;
    PATH;
    
    // MDD subfield
    // cars = set
    // cars->brand = char[255]
    PARENTFIELDNAME;
    SUBFIELDNAME;
    SUBFIELDTYPE;
    
    // field types
    CHAR="char";
    INT="int";
    INTENUM;
    INTENUMTEXT;
    INTENUMINDEX;
    CHARENUM;
    CHARENUMELEMENT;
    REAL="real";
    BOOLEAN="boolean";
    TEXT="text";
    BINARY="binary";
    FILE="file";
    DATE="date";
    PTR="ptr";
    SET="set";
    SETCOMPLEX;
    SETINTENUM;
    SETCHARENUM;
    PTRONE;
    UNKNOWN_TYPE; // for type shorthands
    
    //modifiers
    UNIQUE="unique";
    FIXED="fixed";
    
    // field type attributes
    CHAR_LENGTH;
    POINTED_TYPE;
    DEPRECATED="deprecated"; // intEnum
    
    
    // validation rules
    VALIDATIONNAME;
    
    RANGE="range";
    LENGTH="length";
    RANGE_FROM;
    RANGE_TO;
    
    MATCHES="matches";
    COMPARE="compare";
    
    // functions
    
    FUNCTION_ARGUMENT_NAME;
    FUNCTION_ARGUMENT;
    FUNCTION_NAME;
    SESSIONVAR_NAME;
    
    // Literal tokens
	NUM_DOUBLE;
	NUM_FLOAT;
	NUM_LONG;
	TRIPLE_DOT;
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
    
    protected String typeName;
    
    private AST currentField;
    
    protected boolean included = false;

    protected void disableField(AST field) { }
    
    protected AST include(AST type) { return null; }
    
    protected AST includeSubField(AST type, AST parentField) { return null; }
    
    protected AST parseExpression(AST expr) { return null; }
    
    protected void errorNestedSubfield(AST s) {}
    
}

dataDefinition
    : (LINEBREAK!)* (declaration)* EOF!
    ;

declaration
    : fd:fieldDeclaration {currentField = #fd;} (LINEBREAK!)*
    | subFieldDeclaration (LINEBREAK!)*
    | titleDeclaration (LINEBREAK!)*
    | typeDeclaration (LINEBREAK!)*
    | includeDeclaration (LINEBREAK!)*
    | validationRuleDeclaration (LINEBREAK!)*
    | functionDeclaration (LINEBREAK!)*
    ;
    
    
//////////////// FIELD DECLARATION

fieldDeclaration
    : fn:fieldName
      e:EQ^ {#e.setType(FIELD); #e.setText(#fn.getText()); ((MDDAST)#e).wasIncluded = this.included; boolean hasBody = false;}
      (options{greedy=true;}:
          (modifier)* ft:fieldType
          (fieldComment)? {hasBody = true;}
      )?
      {
      	if(!hasBody) {
      	    disableField(#fieldDeclaration);
      	}
      }
    ;
    
    
subFieldDeclaration
    : 
      fn:atom {#fn.setType(PARENTFIELDNAME); } s:SUBFIELD^
      (
          titleDeclaration (fieldComment!)? // allow comment but do not store them
          | validationRuleDeclaration
          | functionDeclaration
          | EXMARK! "include"! EQ! t:type { #subFieldDeclaration = includeSubField(#t, #fn); }
          | subFieldBody
      )
      { // we move the subfield node under the current field node
        currentField.addChild(#subFieldDeclaration); #subFieldDeclaration = null;
      }
        
    ;
    
subFieldBody
	: a:fieldName { #a.setType(SUBFIELDNAME); }
      (EQ! (modifier)* fieldType (fieldComment)?)
    ;
    
fieldName
    : a:atom { #a.setType(FIELDNAME); }
    // this is an ugly workaround to allow "length" as a field name
    | l:LENGTH { #l.setType(FIELDNAME); }
    | c:CHAR { #c.setType(FIELDNAME); }
    ;

fieldType
    : a:atom { #a.setType(UNKNOWN_TYPE); }
    | c:CHAR^ LEFT_SQBR! (l:POSITIVE_INTEGER {#l.setType(CHAR_LENGTH); })? RIGHT_SQBR!
    | INT
    | intEnum
    | charEnum
    | REAL
    | BOOLEAN
    | TEXT
    | BINARY
    | FILE
    | DATE
    | PTR^ (options{greedy=true;}: p:type {#p.setType(POINTED_TYPE);})?
    | SET^ (options{greedy=true;}: s:type {#s.setType(POINTED_TYPE);})?
    | SET! si:intEnum {#si.setType(SETINTENUM);}
    | SET! sc:charEnum {#sc.setType(SETCHARENUM);}
    ;

//int { "aa"=5, "bb"=2 deprecated, "cc"=10}
intEnum
	// intEnum has no function body as such, but the syntax is the same so we use this trick
	: ie:INT^ {#ie.setType(INTENUM);} parsedFunctionBody 
	;

//char { "aa", "bb" deprecated, "cc"}
charEnum
	// charEnum has no function body as such, but the syntax is the same so we use this trick
	: ce:CHAR^ {#ce.setType(CHARENUM);} parsedFunctionBody
	;
	
fieldComment
	: f:FIELDCOMMENT { int k = #f.getText().indexOf(";"); #fieldComment.setText(#f.getText().substring(k+1).trim()); }
	;


errorMessage
	: m:MESSAGE {int k = #m.getText().indexOf(":"); #errorMessage.setText(#m.getText().substring(k+1).trim()); }
	;
    
modifier
    : u:UNIQUE { #u.setType(MODIFIER); }
    | f:FIXED { #f.setType(MODIFIER); }
    | "not" "null" { #modifier = #[MODIFIER, "not null"]; }
    | "not" "empty" { #modifier = #[MODIFIER, "not empty"]; }
    ;
    
// !title = name
titleDeclaration
    : EXMARK! "title"! EQ! t:title
    ;
    
title
    : t:type { #t.setType(TITLEFIELDFIELD);}
    | f:functionCall
    ;
    
includeDeclaration
    : EXMARK! "include"! EQ! t:type { #includeDeclaration = include(#t); }
    ;

// !type.genDef = ...
typeDeclaration
    : EXMARK! "type"! DOT! n:atom { #n.setType(TYPENAME); } EQ! fieldType
    ;
    
    
    
//////////////// VALIDATION RULES

validationRuleDeclaration
	: 	(
			rangeValidationRuleDeclaration
			| uniquenessValidationRuleDeclaration
			| comparisonValidationRuleDeclaration
			| regexValidationRuleDeclaration
		)
		errorMessage
	;
	
comparisonValidationRuleDeclaration
	: COMPARE^ functionArguments functionBody
	
	;
		
rangeValidationRuleDeclaration
	: (RANGE^ | LENGTH^) functionArguments parsedFunctionBody
	;

regexValidationRuleDeclaration
	: MATCHES^ functionArguments functionBody
	;

// unique() {field1, field2} : These need to be unique
uniquenessValidationRuleDeclaration
	: UNIQUE^ LEFT_PAREN! RIGHT_PAREN!
	parsedFunctionBody
	;



//////////////// FUNCTIONS

functionDeclaration
	: (s:atom {#s.setType(SESSIONVAR_NAME);} PERCENT!)? a:atom {#a.setType(FUNCTION_NAME);} d:functionArgumentDeclaration b:functionBody (errorMessage)?
	{ #functionDeclaration = #(#[FUNCTION, "function"], #functionDeclaration); }
	;

functionArgumentDeclaration
	: LEFT_PAREN! (functionArgumentBody)? (COMMA! functionArgumentBody)*  RIGHT_PAREN!
	;

functionArgumentBody
	: fieldType n:atom {#n.setType(FUNCTION_ARGUMENT_NAME); }
	;

functionCall
	: a:atom {#a.setType(FUNCTION_NAME);} functionArguments
	;

functionArguments
	: LEFT_PAREN! (a:atom {#a.setType(FUNCTION_ARGUMENT);} )? (COMMA! b:atom {#b.setType(FUNCTION_ARGUMENT);} )* RIGHT_PAREN!
	;

functionBody
	: b:FUNCTION_BODY
	  { String body = ""; body = #b.getText().substring(1); body = body.substring(0, body.length() - 1); #b.setText(body.trim());}
	;

parsedFunctionBody
	: b:functionBody
	  {
	  	#parsedFunctionBody = parseExpression(#b);
	  }
	;

//////////////// COMMON

// general.Person
type
    : {String type="";} a:atom { type = #a.getText(); } (DOT! b:atom! {type += "." + #b.getText(); } )* { #type.setText(type); #type.setType(PATH); }
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