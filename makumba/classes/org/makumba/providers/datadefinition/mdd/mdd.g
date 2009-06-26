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
EQUALS: '=';
PERCENT: '%';
SEMICOLON: ';';
COLON: ':';
COMMA: ',';
DOT: '.';
QUOTMARK: '"';
EXMARK: '!';
INTMARK: '?';

SUBFIELD
    : '-' '>'
    ;


// from the Java example grammar

protected ID_START:
        '_' | '$' | 
        (            
            {Character.isLetter(LA(1))}? 
            ~(
                '_' | '$' | '/' | '*' | '0'..'9' | 
                '.' | '\'' | '\\' | '"' | '\t' | '\n' | 
                '\r' | ' ' | '\f' | '(' | ')' |
                '{' | '}' | '[' | ']'| ';' | ',' | '=' |
                '+' | '~' | '&' | '<' | '>' | '-' | '!' |
                '^' | '%' | ':' | '?' | '|'| '@'
            )
        )
    ;

protected ID_PART :
        '_' | '$' | 
        (
            {Character.isLetterOrDigit(LA(1))}?
            ~(
                '_' | '$' | '/' | '*' | 
                '.' | '\'' | '\\' | '"' | '\t' | '\n' | 
                '\r' | ' ' | '\f' | '(' | ')' |
                '{' | '}' | '[' | ']'| ';' | ',' | '=' |
                '+' | '~' | '&' | '<' | '>' | '-' | '!' |
                '^' | '%' | ':' | '?' | '|' | '@'
            )
        )
    ;


// an identifier. Note that testLiterals is set to true! This means
// that after we match the rule, we look in the literals table to see
// if it's a literal or really an identifer
IDENT
	options {testLiterals=true;}
	:	ID_START (ID_PART)*
	;


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
		
protected
ESC
	:	'\\'
		(	'n'
		|	'r'
		|	't'
		|	'b'
		|	'f'
		|	'"'
		|	'\''
		|	'\\'
		|	'0'..'3'
			(
				options {
					warnWhenFollowAmbig = false;
				}
			:	'0'..'7'
				(
					options {
						warnWhenFollowAmbig = false;
					}
				:	'0'..'7'
				)?
			)?
		|	'4'..'7'
			(
				options {
					warnWhenFollowAmbig = false;
				}
			:	'0'..'7'
			)?
		)
	;


// TODO throw better exceptions when no message for validation rules
class MDDBaseParser extends Parser;

options {
        buildAST=true;
        k = 3;
}

tokens {
    FIELD<AST=org.makumba.providers.datadefinition.mdd.FieldNode>;
    VALIDATION<AST=org.makumba.providers.datadefinition.mdd.ValidationRuleNode>;
    FUNCTION<AST=org.makumba.providers.datadefinition.mdd.FunctionNode>;

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
    
    COMPARISON;
    
    
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
    
    private AST currentField;

    protected void disableField(AST field) { }
    
    protected AST include(AST type) { return null; }
    
    protected AST includeSubField(AST type, AST parentField) { return null; }
    
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
    
    ;
    
    
//////////////// FIELD DECLARATION

fieldDeclaration
    : fn:fieldName
      EQUALS^ {#EQUALS.setType(FIELD); #EQUALS.setText(#fn.getText()); boolean hasBody = false;}
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
          | EXMARK! "include"! EQUALS! t:type { #subFieldDeclaration = includeSubField(#t, #fn); }
          | subFieldBody
      )
      { // we move the subfield node under the current field node
        currentField.addChild(#subFieldDeclaration); #subFieldDeclaration = null;
      }
        
    ;
    
subFieldBody
	: a:atom { #a.setType(SUBFIELDNAME); }
      EQUALS!
      (modifier)* fieldType
      (fieldComment)?
    ;
    
fieldName
    : a:atom { #a.setType(FIELDNAME); }
    // this is an ugly workaround to allow "length" as a field name
    | l:LENGTH { #l.setType(FIELDNAME); }
    ;

fieldType
    : a:atom { #a.setType(UNKNOWN_TYPE); }
    | c:CHAR^ LEFT_SQBR! l:NUMBER {#l.setType(CHAR_LENGTH); } RIGHT_SQBR!
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

intEnum
	: ie:INT^ {#ie.setType(INTENUM);} LEFT_CUBR! intEnumBody (COMMA! intEnumBody)*  RIGHT_CUBR! //int { "aa"=5, "bb"=2 deprecated, "cc"=10}
	;

intEnumBody
    : QUOTMARK! t:atom {#t.setType(INTENUMTEXT); } QUOTMARK! EQUALS! i:NUMBER {#i.setType(INTENUMINDEX); } (DEPRECATED)?
    ;

charEnum
	: ce:CHAR^ {#ce.setType(CHARENUM);} LEFT_CUBR! charEnumBody (COMMA! charEnumBody)*  RIGHT_CUBR! //char { "aa", "bb" deprecated, "cc"}
	;
	
charEnumBody
	: QUOTMARK! t:atom {#t.setType(CHARENUMELEMENT); } QUOTMARK! (DEPRECATED)?
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
    : EXMARK! "title"! EQUALS! t:title
    ;
    
title
    : t:type { #t.setType(TITLEFIELDFIELD);}
    // TODO add function here as well
    ;
    
includeDeclaration
    : EXMARK! "include"! EQUALS! t:type { #includeDeclaration = include(#t); }
    ;

// !type.genDef = ...
typeDeclaration
    : EXMARK! "type"! DOT! n:atom { #n.setType(TYPENAME); } EQUALS! fieldType
    ;
    
    
    
//////////////// VALIDATION RULES

validationRuleDeclaration
    : a:atom { #a.setType(FIELDNAME); }
      p:PERCENT^ {#p.setType(VALIDATION);}
      // SIMPLE VALIDATION RULES
      (
       rangeRule
       | regExpRule
      )
      errorMessage
      
    ;
    
// name%length = [1..?]
// age%range = [18..99]
rangeRule
    : (RANGE^ | LENGTH^)
      EQUALS!
      LEFT_SQBR!
      f:rangeBound {#f.setType(RANGE_FROM);} DOT! DOT! t:rangeBound {#t.setType(RANGE_TO);}
      RIGHT_SQBR!
    ;

// [1..?] [?..5]
rangeBound
    : n:NUMBER | m:INTMARK
    ;
    
// name4%matches = "http://.+" : the homepage must start with http://
regExpRule
    : MATCHES^
      EQUALS!

      errorMessage
    ;

//uniquenessRule
//    :
//    ;
    
//comparisonRule
//    : 
//    ;


//////////////// COMMON

// general.Person
type
    : {String type="";} a:atom { type = #a.getText(); } (DOT! b:atom! {type += "." + #b.getText(); } )* { #type.setText(type); System.out.println(type);}
    ;

atom
    : IDENT
    ;