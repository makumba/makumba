header {
    package org.makumba.providers.datadefinition.mdd;
}

class MDDExpressionLexer extends Lexer;

options {
    importVocab=MDD;
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

    
LINEBREAK
    :   '\n'      { newline(); } // unix
    |   '\r' '\n' { newline(); } // dos
    |   '\r'      { newline(); } // mac
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




class MDDExpressionParser extends Parser;

options {
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
	
}

expression
	: range
	| fieldList
	| intEnum
	| charEnum
	;
	
intEnum
	: intEnumBody (COMMA! intEnumBody)*
	;

charEnum
	: charEnumBody (COMMA! charEnumBody)*
	;
	
intEnumBody
    : t:STRING_LITERAL {#t.setType(INTENUMTEXT); } EQ! i:number { checkNumber(#i); if(#i != null) #i.setType(INTENUMINDEX); } (DEPRECATED)?
    ;

charEnumBody
	: t:STRING_LITERAL { #t.setType(CHARENUMELEMENT); }
	  (DEPRECATED)?
	;

	
fieldList
	: a:type (COMMA! b:type)*
	;

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


operator
	: EQ | LT | GT | LE | GE | NE | SQL_NE | LIKE
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

atom
    : IDENT
    ;