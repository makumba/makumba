header {
    package org.makumba.providers.datadefinition.mdd;
}

class MDDLexer extends Lexer;

options {
    exportVocab=MDD;
    k = 3;
}

WORD
    : ('a'..'z'|'A'..'Z') ('a'..'z'|'A'..'Z'|'0'..'9')*
    ;
    
NUMBER
    : ('0'..'9')*
    ;

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

MESSAGE
	: COLON (~('\n'|'\r'))* ('\n'|'\r'('\n')?) {newline();}
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
    FIELDCOMMENT;
    
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
    REAL="real";
    BOOLEAN="boolean";
    TEXT="text";
    BINARY="binary";
    FILE="file";
    DATE="date";
    PTR="ptr";
    SET="set";
    SETCOMPLEX;
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
    
    protected void transformToFile(AST field) {}

    protected AST include(AST type) { return null; }
    
    protected AST includeSubField(AST type, AST parentField) { return null; }
    
}

dataDefinition
    : (LINEBREAK!)* (declaration)*
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
          (SEMICOLON! fieldComment LINEBREAK!)? {hasBody = true;}
      )?
      {
      	if(!hasBody) {
      	    disableField(#fieldDeclaration);
      	}
      	if(#ft.getType() == FILE) {
      		transformToFile(#fieldDeclaration);
      	}
      }
    ;
    
    
subFieldDeclaration
    : 
      fn:atom {#fn.setType(PARENTFIELDNAME); } s:SUBFIELD^
      (
          titleDeclaration (SEMICOLON! fieldComment! LINEBREAK!)? // allow comment but do not store them
          | EXMARK! "include"! EQUALS! t:type { #subFieldDeclaration = includeSubField(#t, #fn); }
          |
          (
            a:atom { #a.setType(SUBFIELDNAME); }
            EQUALS!
            (modifier)* fieldType
            (SEMICOLON! fieldComment LINEBREAK!)?
          )
      )
      { // we move the subfield node under the current field node
        currentField.addChild(#subFieldDeclaration); #subFieldDeclaration = null;
      }
        
    ;
    
fieldName
    : a:atom { #a.setType(FIELDNAME); }
    ;

fieldType
    : a:atom { #a.setType(UNKNOWN_TYPE); }
    | c:CHAR^ LEFT_SQBR! l:NUMBER {#l.setType(CHAR_LENGTH); } RIGHT_SQBR!
    | i:INT
    | ie:INT^ {#ie.setType(INTENUM);} LEFT_CUBR! intEnumBody (COMMA! intEnumBody)*  RIGHT_CUBR! //int { "aa"=5, "bb"=2 deprecated, "cc"=10}
    | r:REAL
    | BOOLEAN
    | TEXT
    | BINARY
    | FILE
    | DATE
    | PTR^ (options{greedy=true;}: p:type {#p.setType(POINTED_TYPE);})?
    | SET^ (options{greedy=true;}: s:type {#s.setType(POINTED_TYPE);})? 
    ;

intEnumBody
    : QUOTMARK! t:atom {#t.setType(INTENUMTEXT); } QUOTMARK! EQUALS! i:NUMBER {#i.setType(INTENUMINDEX); } (DEPRECATED)?
    ;

// TODO this is clumsy, could maybe be replaced with appropriate lexer syntax, see MESSAGE token definition
fieldComment
	: { String comment=""; }
      (a:atom { comment += #a.getText(); })
      (b:atom { comment += " " + #b.getText(); })*
      { #fieldComment = #[FIELDCOMMENT]; #fieldComment.setText(comment); }
	;
	
errorMessage
	: m:MESSAGE {int k = #m.getText().indexOf(":"); #errorMessage.setText(#m.getText().substring(k+1).trim()); }
	;

//message
//	: { String message=""; }
//      (a:atom { message += #a.getText(); })
//      (b:atom { message += " " + #b.getText(); })*
//      { #message = #[MESSAGE]; #message.setText(message); }
//	;

    
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
    : t:atom { #t.setType(TITLEFIELDFIELD);}
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
        rangeRule //| uniquenessRule | comparisonRule
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

//uniquenessRule
//    :
//    ;
    
//comparisonRule
//    : 
//    ;


//////////////// COMMON

// general.Person
type
    : {String type="";} a:atom { type = #a.getText(); } (DOT! b:atom! {type += "." + #b.getText(); } )* { #type.setText(type); }
    ;

atom
    : WORD
    ;