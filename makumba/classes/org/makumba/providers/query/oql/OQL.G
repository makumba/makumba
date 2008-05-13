
/* ====================================================================
 * OQL Sample Grammar for Object Data Management Group (ODMG) 
 *
 * Copyright (c) 1999 Micro Data Base Systems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. All advertising materials mentioning features or use of this
 *    software must display the following acknowledgment:
 *    "This product includes software developed by Micro Data Base Systems, Inc.
 *    (http://www.mdbs.com) for the use of the Object Data Management Group
 *    (http://www.odmg.org/)."
 *
 * 4. The names "mdbs" and "Micro Data Base Systems" must not be used to
 *    endorse or promote products derived from this software without
 *    prior written permission. For written permission, please contact
 *    info@mdbs.com.
 *
 * 5. Products derived from this software may not be called "mdbs"
 *    nor may "mdbs" appear in their names without prior written
 *    permission of Micro Data Base Systems, Inc. 
 *
 * 6. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by Micro Data Base Systems, Inc.
 *    (http://www.mdbs.com) for the use of the Object Data Management Group
 *    (http://www.odmg.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY MICRO DATA BASE SYSTEMS, INC. ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL MICRO DATA BASE SYSTEMS, INC. OR
 * ITS ASSOCIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */




/*
**  oql.g
**
** Built with Antlr 2.5
**   java antlr.Tool oql.g
*/
header {package metadata.db.oql; }

options {
    language="Java";       // cristi, 20001027, was "Cpp"
}

class OQLLexer extends Lexer ;
options {
    k=2;
    charVocabulary = '\3'..'\377';
    testLiterals=false;     // don't automatically test for literals
    caseSensitive=false;            // cristi, 20001027, was "true"
    caseSensitiveLiterals=false;    // cristi, 20001027, was "true"
}

        /* punctuation */
TOK_RPAREN
    options {
        paraphrase = "right parenthesis";
    }                       :       ')'             ;
TOK_LPAREN
    options {
        paraphrase = "left parenthesis";
    }                       :       '('             ;
TOK_COMMA
    options {
        paraphrase = "comma";
    }                       :       ','             ;
TOK_SEMIC
    options {
        paraphrase = "semicolon";
    }                       :       ';'             ;

TOK_DOTDOT  :   ".." ;
TOK_COLON   :   ':' ;
// protected - removed to fix bug with '.' 10/4/99
TOK_DOT
    options {
        paraphrase = "dot";
    }                       :       '.'             ;
TOK_INDIRECT
    options {
        paraphrase = "dot";
    }                       :       '-' '>'         { $setType(TOK_DOT); } ;
TOK_CONCAT
    options {
        paraphrase = "operator";
    }                       :       '|' '|'         ;
TOK_EQ
    options {
        paraphrase = "comparison operator";
    }                       :       '='             ;
TOK_PLUS
    options {
        paraphrase = "operator";
    }                       :       '+'             ;
TOK_MINUS
    options {
        paraphrase = "operator";
    }                       :       '-'             ;
TOK_SLASH
    options {
        paraphrase = "operator";
    }                       :       '/'             ;
TOK_STAR
    options {
        paraphrase = "operator";
    }                       :       '*'             ;
TOK_LE
    options {
        paraphrase = "comparison operator";
    }                       :       '<' '='         ;
TOK_GE
    options {
        paraphrase = "comparison operator";
    }                       :       '>' '='         ;
TOK_NE
    options {
        paraphrase = "comparison operator";
    }                       :       '<' '>'         ;
TOK_LT
    options {
        paraphrase = "comparison operator";
    }                       :       '<'             ;
TOK_GT
    options {
        paraphrase = "comparison operator";
    }                       :       '>'             ;
TOK_LBRACK
    options {
        paraphrase = "left bracket";
    }                       :       '['             ;
TOK_RBRACK
    options {
        paraphrase = "right bracket";
    }                       :       ']'             ;
TOK_DOLLAR
    :       '$' ;

/*
 * Names
 */
protected
NameFirstCharacter:
        ( 'a'..'z' | '_' )
    ;

protected
NameCharacter:
        ( 'a'..'z' | '_' | '0'..'9' )
    ;

Identifier
        options {testLiterals=true;}
            :

        NameFirstCharacter
        ( NameCharacter )*
    ;

/* Numbers */
protected
TOK_UNSIGNED_INTEGER :
        '0'..'9'
    ;

// a couple protected methods to assist in matching floating point numbers
protected
TOK_APPROXIMATE_NUMERIC_LITERAL
        :       'e' ('+'|'-')? ('0'..'9')+
	;


// a numeric literal
TOK_EXACT_NUMERIC_LITERAL
    options {
        paraphrase = "numeric value";
    }                       :

            '.'
            (
                TOK_UNSIGNED_INTEGER
            )+
            { _ttype = TOK_EXACT_NUMERIC_LITERAL; }

            (
                TOK_APPROXIMATE_NUMERIC_LITERAL
                { _ttype = TOK_APPROXIMATE_NUMERIC_LITERAL; }
            )?

        |   (
                TOK_UNSIGNED_INTEGER
            )+
            { _ttype = TOK_UNSIGNED_INTEGER; }

            // only check to see if it's a float if looks like decimal so far
            (
                '.'
                (
                    TOK_UNSIGNED_INTEGER
                )*
                { _ttype = TOK_EXACT_NUMERIC_LITERAL; }
                (TOK_APPROXIMATE_NUMERIC_LITERAL { _ttype = TOK_APPROXIMATE_NUMERIC_LITERAL; } )?

            |    TOK_APPROXIMATE_NUMERIC_LITERAL { _ttype = TOK_APPROXIMATE_NUMERIC_LITERAL; }
            )? // cristi, 20001027, ? was missing
	;


/*
** Define a lexical rule to handle strings.
*/
CharLiteral
    options {
        paraphrase = "character string";
    }                       :
        '\''!
        (
            '\'' '\''           { $setText("'"); }
        |   '\n'                { newline(); }
        |   ~( '\'' | '\n' )
        )*
        '\''!
    ;

StringLiteral
    options {
        paraphrase = "character string";
    }                       :
        '"'!
        (
           '\\' '"'            { $setText("\""); }
        |   '\n'                { newline(); }
        |   ~( '\"' | '\n' )   // cristi, 20001028, was '\'' instead of '\"'
        )*
        '"'!
    ;



/*
** Define white space so that we can throw out blanks
*/
WhiteSpace :
        (
            ' '
        |   '\t'
        |   '\r'
        )   { $setType(Token.SKIP); }
    ;

NewLine :
        '\n'    { newline(); $setType(Token.SKIP); }
    ;

/*
** Define a lexical rule to handle line comments.
*/
CommentLine :
        '/'! '/'!
        (
            ~'\n'!
        )*
        '\n'!   { newline(); $setType(Token.SKIP); }
    ;

/*
** Define a lexical rule to handle block comments.
*/
MultiLineComment :
        "/*"
        (
            { LA(2)!='/' }? '*'
        |   '\n' { newline(); }
        |   ~('*'|'\n')
        )*
        "*/"
        { $setType(Token.SKIP); }
    ;

class OQLParser extends Parser ;
options {
    k = 2;
    codeGenMakeSwitchThreshold = 3;
    codeGenBitsetTestThreshold = 4;
    buildAST = true;	// uses CommonAST by default
}

queryProgram:

        (
            (
                declaration
            |   query
            )
            ( TOK_SEMIC! )?
        )+
        EOF
    ;

declaration :

            defineQuery
        |   importQuery
        |   undefineQuery

    ;

importQuery :

        "import"^
        labelIdentifier
        (
            TOK_DOT
            labelIdentifier
        )*

        (
            "as"^ labelIdentifier
        )?

    ;

defineQuery :

        "define" ( "query" )?
        labelIdentifier

        (
            TOK_LPAREN
            type
            labelIdentifier
            (
                TOK_COMMA
                type
                labelIdentifier
            )*
            TOK_RPAREN
        )?

        "as"
        query
    ;

undefineQuery :

        "undefine"
        ( "query" )?
        labelIdentifier
    ;

query :

        (
            selectExpr
        |   expr
        )
    ;

selectExpr :

        "select"^

        (
            options {
                warnWhenFollowAmbig = false;
            } :

            "distinct"
        )?

        projectionAttributes

        fromClause 
        ( whereClause )?
        ( groupClause )?
        ( orderClause )?
    ;

fromClause :

        "from"^ iteratorDef
        (
            TOK_COMMA!
            iteratorDef
        )*
    ;

iteratorDef :

        (
            options {
                warnWhenFollowAmbig = false;
            } :

            labelIdentifier "in" expr // cristi: here we could have an inversion in the tree and ignore "in"

        |   expr
            (
                ( "as"! )?
                labelIdentifier
            )?
        )
    ;

whereClause :

        "where"^ expr
    ;

projectionAttributes :

        (
            projection
            (
                TOK_COMMA!
                projection
            )*
        |   TOK_STAR
        )
    ;

projection :

        expr
        (
            ( "as" )?
            labelIdentifier
        )?
    ;


groupClause :

        "group"^ "by"! groupColumn
        (
            TOK_COMMA! groupColumn
        )*

        (
            "having"
            expr
        )?
    ;

groupColumn :

        fieldList
    ;

orderClause :

        "order"^ "by"!
        sortCriterion
        (
            TOK_COMMA! sortCriterion
        )*
    ;

sortCriterion :

        expr
        (
            "asc"
        |   "desc"
        )?
    ;

expr :

        (
            TOK_LPAREN
            type
            TOK_RPAREN
        )*
        orExpr
    ;


orExpr :

        andExpr
        (
            "or"^
            andExpr
        )*
    ;

andExpr :

        quantifierExpr
        (
            "and"^
            quantifierExpr
        )*
    ;

quantifierExpr :

        (
            equalityExpr

        |   "for"^ "all"! labelIdentifier
            "in"  expr
            TOK_COLON equalityExpr

        |   "exists"^ labelIdentifier
            "in" expr
            TOK_COLON equalityExpr

        )
    ;

equalityExpr :

        relationalExpr
        (
            (
                TOK_EQ^
            |   TOK_NE^
            )

            (
                // Simple comparison of expressions
                relationalExpr

                // Comparison of queries
            |   (
                    "all"^
                |   "any"^
                |   "some"^
                )
                relationalExpr
            )

            // Like comparison
        |   "like"^ relationalExpr

        )*
    ;

relationalExpr :

        additiveExpr
        (
            (
                TOK_LT^
            |   TOK_GT^
            |   TOK_LE^ 
            |   TOK_GE^
            )

            (
                // Simple comparison of expressions
                additiveExpr

                // Comparison of queries
            |   (
                    "all"^
                |   "any"^
                |   "some"^
                )
                additiveExpr
            )
        )*
    ;

additiveExpr :

        multiplicativeExpr
        (
            (
                TOK_PLUS^
            |   TOK_MINUS^
            |   TOK_CONCAT^
            |   "union"^
            |   "except"^
            )
            multiplicativeExpr
        )*
    ;

multiplicativeExpr :

        inExpr
        (
            (
                TOK_STAR^
            |   TOK_SLASH^
            |   "mod"^
            |   "intersect"^
            )
            inExpr
        )*
    ;

inExpr :

        unaryExpr
        (
            "in"^ unaryExpr
        )?
    ;

unaryExpr :

        (
            (
                TOK_PLUS^
            |   TOK_MINUS^
            |   "abs"^
            |   "not"^
            )
        )*
        postfixExpr

    ;

postfixExpr :

        primaryExpr
        (
            TOK_LBRACK! index TOK_RBRACK!

        |   ( TOK_DOT^ | TOK_INDIRECT^ ) labelIdentifier
            (
                argList

            |   /* NOTHING */

            )
        )*
    ;

index :

        // single element
        expr
        (
            // multiple elements

            (
                TOK_COMMA
                expr
            )+

            // Range of elements
        |   TOK_COLON expr

        |   /* NOTHING */

        )
    ;

primaryExpr :

        (
            conversionExpr
        |   collectionExpr
        |   aggregateExpr
        |   functionExpr
        |   undefinedExpr
        |   objectConstruction
        |   structConstruction
        |   collectionConstruction
        |   labelIdentifier
            (
                argList
            |   /* NOTHING */
            )
        |   TOK_DOLLAR TOK_UNSIGNED_INTEGER
        |   literal
        |   TOK_LPAREN! query TOK_RPAREN!
        )
    ;

argList :

        TOK_LPAREN^
        (
            expr
            (
                TOK_COMMA!
                expr
            )*
        )?
        TOK_RPAREN
    ;

conversionExpr :

        (
            "listtoset"
        |   "element"
        |   "distinct"
        |   "flatten"
        )
        TOK_LPAREN query TOK_RPAREN
    ;

collectionExpr :

        (
            "first"^
        |   "last"^
        |   "unique"^
        |   "exists"^
        )
        TOK_LPAREN! query TOK_RPAREN!

    ;

aggregateExpr :

        (
            (
                "sum"^
            |   "min"^
            |   "max"^
            |   "avg"^
            )
            TOK_LPAREN! query TOK_RPAREN!

        |   "count"^
            TOK_LPAREN!
            (
                query
            |   TOK_STAR
            )
            TOK_RPAREN!
        )
    ;

functionExpr :
		(
			"lower"^
		|	"upper"^
		|	"trim"^
		|	"ltrim"^
		|	"rtrim"^
		|	"char"^
		|	"ascii"^
		)
		TOK_LPAREN! query TOK_RPAREN!
	;
	
undefinedExpr :

        (
            "is_undefined"^
        |   "is_defined"^
        )
        TOK_LPAREN!
        query
        TOK_RPAREN!
    ;

objectConstruction :

        typeIdentifier
        TOK_LPAREN
        fieldList
        TOK_RPAREN
    ;

structConstruction :

        "struct"^
        TOK_LPAREN!
        fieldList
        TOK_RPAREN!
    ;

fieldList :

        labelIdentifier TOK_COLON! expr
        (
            TOK_COMMA!
            labelIdentifier
            TOK_COLON! expr
        )*   // cristi 20001028, * was missing
    ;

collectionConstruction :
        (
            (
                "array"
            |   "set"
            |   "bag"
            )
            TOK_LPAREN
            (
                expr
                (
                    TOK_COMMA expr
                )*
            )?
            TOK_RPAREN

        |   "list"
            TOK_LPAREN
            (
                expr
                (
                    TOK_DOTDOT expr
                |   (
                        TOK_COMMA expr
                    )*
                )
            )?
            TOK_RPAREN
        )
    ;

type :

            ( "unsigned" )?
            (
                "short"
            |   "long"
            )
        |   "long" "long"
        |   "float"
        |   "double"
        |   "char"
        |   "string"
        |   "boolean"
        |   "octet"
        |   "enum"
            (
                typeIdentifier
                TOK_DOT
            )?
            labelIdentifier
        |   "date"
        |   "time"
        |   "interval"
        |   "timestamp"
        |   "set" TOK_LT type TOK_GT
        |   "bag" TOK_LT type TOK_GT
        |   "list" TOK_LT type TOK_GT
        |   "array" TOK_LT type TOK_GT
        |   "dictionary" TOK_LT type TOK_COMMA type TOK_GT
        |   typeIdentifier

    ;

literal :

            objectLiteral
        |   booleanLiteral
        |   longLiteral
        |   doubleLiteral
        |   charLiteral
        |   stringLiteral
        |   dateLiteral
        |   timeLiteral
        |   timestampLiteral
    ;

objectLiteral :

        "nil"
    ;

booleanLiteral :

        (
            "true"
        |   "false"
        )
    ;

longLiteral :

            TOK_UNSIGNED_INTEGER
    ;

doubleLiteral :
        (
            TOK_APPROXIMATE_NUMERIC_LITERAL
        |   TOK_EXACT_NUMERIC_LITERAL
        )
    ;

charLiteral :

        CharLiteral
    ;


stringLiteral :

        StringLiteral
    ;

dateLiteral :

        "date" StringLiteral
    ;

timeLiteral :

        "time" StringLiteral
    ;

timestampLiteral :

        "timestamp" StringLiteral
    ;

labelIdentifier :

        Identifier
    ;

typeIdentifier :

        Identifier
    ;
