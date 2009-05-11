// fix the parser!

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

// Grouping
LEFT_PAREN: '(';
RIGHT_PAREN: ')';
LEFT_CUBR: '{';
RIGHT_CUBR: '}';
LEFT_SQBR: '[';
RIGHT_SQBR: ']';

COMMENT: '#';

EQUALS: '=';
PERCENT: '%';
SEMICOLON: ';';
COMMA: ',';
DOT: '.';
QUOTMARK: '"';
EXMARK: '!';

SUBFIELD
    : '-' '>'
    ;

WHITESPACE
    : (' ' | 't' | 'r' | 'n' | '\t') { $setType(Token.SKIP); }
    ;
        
    
LINEBREAK
    :   '\n'      { newline(); } // unix
    |   '\r' '\n' { newline(); } // dos
    |   '\r'      { newline(); } // mac
    ;


class MDDParser extends Parser;

options {
        buildAST=true;
        k = 3;
}

tokens {
    FIELD<AST=org.makumba.providers.datadefinition.mdd.FieldNode>;
    VALIDATION<AST=org.makumba.providers.datadefinition.mdd.ValidationNode>;
    FUNCTION<AST=org.makumba.providers.datadefinition.mdd.FunctionNode>;

    TITLEFIELD;
    TITLEFIELDFIELD;
    TITLEFIELDFUNCTION;
    
    INCLUDED;
    
    // MDD structure
    FIELDNAME;
    MODIFIER;
    FIELDTYPE;
    FIELDCOMMENT;
    PARENTFIELDNAME;
    SUBFIELDNAME;
    SUBFIELDTYPE;
    
    
    TYPENAME;
    TYPEDEF;
    
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
    
}

dataDefinition
    : (declaration)*
    ;

declaration
    : fd:fieldDeclaration {currentField = #fd;} (LINEBREAK!)*
    | subFieldDeclaration (LINEBREAK!)*
    | titleDeclaration (LINEBREAK!)*
    | typeDeclaration (LINEBREAK!)*
    | includeDeclaration (LINEBREAK!)*
    
    ;

fieldDeclaration
    : fn:fieldName
      EQUALS^ {#EQUALS.setType(FIELD); #EQUALS.setText(#fn.getText()); }
      (modifier)* fieldType
      (SEMICOLON! fieldComment LINEBREAK!)?
    ;
    
    
subFieldDeclaration
    : 
      fn:atom {#fn.setType(PARENTFIELDNAME); } SUBFIELD^
      (
          titleDeclaration (SEMICOLON! fieldComment! LINEBREAK!)? // allow comment but do not store them
          |
          (
            subFieldName
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

fieldComment
    : { String comment=""; }
      (a:atom { comment += #a.getText(); })
      (b:atom { comment += " " + #b.getText(); })*
      { #fieldComment = #[FIELDCOMMENT]; #fieldComment.setText(comment); }
    ;


fieldCommentNew
    : { String comment=""; }
      (a:atom { comment += #a.getText() + " "; })* LINEBREAK!
      { #fieldCommentNew = #[FIELDCOMMENT]; #fieldCommentNew.setText(comment); }
    ;

subFieldName
    : a:atom {#a.setType(SUBFIELDNAME); }
    ;
    
modifier
    : u:UNIQUE { #u.setType(MODIFIER); }
    | f:FIXED { #f.setType(MODIFIER); }
    | "not" "null" { #modifier = #[MODIFIER, "not null"]; }
    | "not" "empty" { #modifier = #[MODIFIER, "not empty"]; }
    ;
    
titleDeclaration
    : EXMARK! "title"! EQUALS! t:title
    ;
    
// !title = name
title
    : t:atom { #t.setType(TITLEFIELDFIELD);}
    // TODO add function here as well
    ;
    
includeDeclaration
    : EXMARK! "include"! EQUALS! t:type {#t.setType(INCLUDED); } // TODO call parsing of idd and add resulting AST as result here
    ;

// !type.genDef = ...
typeDeclaration
    : EXMARK! "type"! DOT! n:atom { #n.setType(TYPENAME); } EQUALS! fieldType
    ;

// general.Person
type
    : {String type="";} a:atom { type = #a.getText(); } (DOT! b:atom! {type += "." + #b.getText(); } )* { #type.setText(type); }
    ;

atom
    : WORD
    ;