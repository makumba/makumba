header {
    package org.makumba.providers.datadefinition.mdd;
}

class MDDLexer extends Lexer;

IDENT
: ('a'..'z'|'A'..'Z') ('a'..'z'|'A'..'Z'|'0'..'9')*
;  

// Grouping
LEFT_PAREN: '(';
RIGHT_PAREN: ')';
LEFT_BR: '{';
RIGHT_BR: '}';

FIELDCOMMENT: ';';
COMMENT: '#';

EQUALS: '=';
PERCENT: '%';

SUBFIELD
    : '-' '>'
    ;

WHITESPACE
    : (' ' | 't' | 'r' | 'n') { $setType(Token.SKIP); }
    ;



class MDDParser extends Parser;

options {
        buildAST=true;
}


declaration
    : fieldDeclaration
    ;

fieldDeclaration
    : fn:fieldName EQUALS fd:fieldDefinition
    ;
    
fieldName
    : atom
    ;

fieldDefinition
    : (atom)*
    ;

atom
    : Id:IDENT
    ;