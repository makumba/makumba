//* This is a very simple translator OQL-SQL. Due to that, it has great 
//* limitations but it is sufficient for current Makumba purposes
//* Lines that begin with //* are Makumba grammar simplifications
//* This is a translator, not a compiler. Semantical checks are not done unless
//* they are needed for translation purposes. The rest will be done at SQL 
//* engine level.

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

header {package org.makumba.db.sql.oql; }
options {
    language="Java";
}

class OQLLexer extends Lexer ;
options {
    k=2;
    charVocabulary = '\3'..'\377';
    testLiterals=false;     // don't automatically test for literals
    caseSensitive=false;
    caseSensitiveLiterals=false;
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
        '\''//!
        (
            '\'' '\''           { $setText("'"); }
        |   '\n'                { newline(); }
        |   ~( '\'' | '\n' )
        )*
        '\''//!
    ;

StringLiteral
    options {
        paraphrase = "character string";
    }                       :
        '"'//!
        (
            '\\' '"'            { $setText("\""); }
        |   '\n'                { newline(); }
        |   ~( '\"' | '\n' )   // cristi, 20001028, was '\'' instead of '\"'
        )*
        '"'//!
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
    defaultErrorHandler=false;
}

{
	QueryAST currentQuery=null;
	AST lastEQop=null;
	String is=null;
	int lastAdditive;
	int lastMultiplicative;
	QueryAST currentQuery() throws antlr.RecognitionException
	{ if(currentQuery!=null) return currentQuery; 
	  throw new antlr.RecognitionException("wrong expression, probably an operator is missing (AND, OR, +, -, *, /)");
	}
}

queryProgram :

        (
            (
                declaration
            |   query
            )
            ( TOK_SEMIC )?
        )+
        EOF
    ;

declaration :

            defineQuery
        |   importQuery
        |   undefineQuery

    ;

importQuery :

        "import"
        labelIdentifier
        (
            TOK_DOT
            labelIdentifier
        )*

        (
            "as" labelIdentifier
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

        s:"select"^<AST=org.makumba.db.sql.oql.QueryAST>
	{ #s.setSuperQuery(currentQuery); currentQuery=#s; }

        (
            options {
                warnWhenFollowAmbig = false;
            } :

            "distinct"
        )?

        projectionAttributes

        ( fromClause )?
        ( whereClause )?
        ( groupClause )?
        ( orderClause )? 
	{ currentQuery= #s.getSuperQuery(); #s.prepare(); }
    ;

fromClause :

        "from" iteratorDef {currentQuery().setFromAST(#fromClause); }
        (
            TOK_COMMA
            iteratorDef
        )*
    ;

iteratorDef :

        (
            options {
                warnWhenFollowAmbig = false;
            } :

//* makumba simplification, we only allow simple a.b.c stuff here
//*            labelIdentifier "in" expr

            lbl:labelIdentifier "in" mi:makumbaIdentifier 
		{ currentQuery().addFrom(#mi.getText(), #lbl.getText()); }

//*       |   expr

        |   mi1:makumbaIdentifier

//* compulsory for now            (
                ( "as" )?
                lbl1: labelIdentifier 
		{ currentQuery().addFrom(#mi1.getText(), #lbl1.getText()); }
//*             )?
        )
    ;

makumbaIdentifier!:
	id:Identifier 
          {String s=id.getText(); } 
	   ((TOK_DOT|TOK_INDIRECT) id1:Identifier {s+="."+id1.getText();} )* 
	{ #makumbaIdentifier=#[Identifier, s]; }
	;

whereClause :

        "where" expr {currentQuery().setWhereAST(#whereClause);} 
    ;

projectionAttributes :

	//* either we have only one identifier.*, either we have normal multi-column projection
	TOK_LPAR labelIdentifier TOK_DOT TOK_STAR TOK_LRAR 
 |
//*        (
            projection 
            (
                TOK_COMMA
                projection 
            )*
//*        |   TOK_STAR
//*       )
//*	|	
//*	lb:labelIdentifier TOK_INDIRECT TOK_STAR { currentQuery().setOneProjection((#lb).getText());}
    ;

projection :

	{String label=""; }
        e:expr 
        (
            ( a:"as" {#a.setText(" AS");})?
            lb:	labelIdentifier { label=#lb.getText(); #lb.setText(" "+label); }
        )?
	{
	  
		currentQuery().addProjection(new Projection(#projection, #e, label));
	}
    ;

groupClause :

        "group" "by" groupColumn
        (
            TOK_COMMA groupColumn
        )*

        (
            h:"having" {#h.setText(" HAVING "); }
            expr
        )? 
	{ currentQuery().setGroupAST(#groupClause); }
    ;

groupColumn :
	expr
   	//* the original grammar had something else here:
	//**     fieldList
    ;

orderClause :

        "order" "by"
        sortCriterion
        (
            TOK_COMMA sortCriterion
        )*
	{ currentQuery().setOrderAST(#orderClause); }	
    ;

sortCriterion :

        expr
        (
            "asc"
        |   "desc"
        )?
    ;

expr :

//        (
//            TOK_LPAREN
//           type
//            TOK_RPAREN
//        )*
        or:orExpr
	{ currentQuery().addExpression(#or); }
    ;


orExpr :

        and:andExpr 	
        (
            "or"
            and2:andExpr  { ((OQLAST)#orExpr).tree= new AnalysisTree(#orExpr, AnalysisTree.OR, #and2);}
        )*
    ;

andExpr :

        q: quantifierExpr 
        (
            "and"
            q2:quantifierExpr { ((OQLAST)#andExpr).tree= new AnalysisTree(#andExpr, AnalysisTree.AND, #q2);}
        )*
    ;

quantifierExpr :

        (
            eq:equalityExpr 

	// unsupported in the tree 
        |   "for" "all" labelIdentifier
            "in"  expr
            TOK_COLON equalityExpr

	// unsupported in the tree
        |   "exists" labelIdentifier
            "in" expr
            TOK_COLON equalityExpr 
        )
    ;

equalityExpr :

        rel:relationalExpr 
        (
            (
                eq:TOK_EQ { lastEQop= #eq; is="is "; } 
            |   neq:TOK_NE{ lastEQop= #neq;is="is not ";} 
            )

            (
                // Simple comparison of expressions
                rel2: relationalExpr  { ((OQLAST)#equalityExpr).tree= new ComparisonTree(#equalityExpr, AnalysisTree.SIM_COMP, #rel2);}


                // Comparison of queries,  unsupported in the tree
            |   (
                    "all"
                |   "any"
                |   "some"
                )
                relationalExpr 
            )

            // Like comparison
        |   "like" rel3: relationalExpr { ((OQLAST)#equalityExpr).tree= new LikeTree(#equalityExpr, AnalysisTree.LIKE, #rel3);}

        )*
    ;

relationalExpr :

        add: additiveExpr 
        (
            (
                TOK_LT
            |   TOK_GT
            |   TOK_LE
            |   TOK_GE
            )

            (
                // Simple comparison of expressions
                add2:additiveExpr  { ((OQLAST)#relationalExpr).tree= new ComparisonTree(#relationalExpr, AnalysisTree.ASIM_COMP, #add2);}

                // Comparison of queries, unsupported yet in the tree
            |   (
                    "all"
                |   "any"
                |   "some"
                )
                additiveExpr  
            )
        )*
    ;

additiveExpr :

        mul: multiplicativeExpr 
        (
            (
                TOK_PLUS { lastAdditive= AnalysisTree.ADD; }
            |   TOK_MINUS { lastAdditive= AnalysisTree.ADD; }
            |   TOK_CONCAT { lastAdditive= AnalysisTree.CONCAT; }
            |   "union" { lastAdditive= AnalysisTree.UNION; }
            |   "except" { lastAdditive= AnalysisTree.EXCEPT; }
            )
            mul2: multiplicativeExpr { if(#additiveExpr!=null)((OQLAST)#additiveExpr).tree= new AdditiveTree(#additiveExpr, lastAdditive, #mul2);}
        )*
    ;

multiplicativeExpr :

        in:inExpr 
        (
            (
                TOK_STAR { lastMultiplicative= AnalysisTree.MUL; }
            |   TOK_SLASH { lastMultiplicative= AnalysisTree.MUL; }
            |   "mod" { lastMultiplicative= AnalysisTree.MUL; }
            |   "intersect" { lastMultiplicative= AnalysisTree.INTERSECT; }
            )
            in2: inExpr { if(#multiplicativeExpr!=null)((OQLAST)#multiplicativeExpr).tree= new AnalysisTree(#multiplicativeExpr, lastMultiplicative, #in2);}
        )*
    ;

inExpr :

        un: unaryExpr 
        (
            "in" un2:unaryExpr 
	{ if(#inExpr!=null)((OQLAST)#inExpr).tree= new InTree(#inExpr, #un2);}
        )?
    ;

unaryExpr :

        (
            (
                TOK_PLUS    
            |   TOK_MINUS
            |   "abs"
            |   "not"
            )
        )*
        po: postfixExpr 

    ;

//* indexes, method calls and fields of expression objects are not in makumba yet
postfixExpr :

        pr: primaryExpr 
//*       (
//*            TOK_LBRACK index TOK_RBRACK

//*        |   ( TOK_DOT | TOK_INDIRECT ) labelIdentifier
//*            (
//*                argList
//*            |   /* NOTHING */
//*            )
//*        )*
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
// unsupported in the tree
//        | conversionExpr
//        | collectionExpr  
//        | objectConstruction
//        | structConstruction
// end of unsupported

	   undefinedExpr  

        |   collectionConstruction{ ((OQLAST)#primaryExpr).makumbaType="inSet";} 
        |   a:aggregateExpr  
	|   !mi: makumbaIdentifier
	{ 
		#primaryExpr=new IdAST();
		#primaryExpr.setText((#mi).getText()); 
		currentQuery().addExpressionIdentifier(#primaryExpr);
	}

 //*      |   labelIdentifier
 //*           (
 //*               argList
 //*           |   /* NOTHING */
 //*           )
       |   !TOK_DOLLAR n:TOK_UNSIGNED_INTEGER 
	{
		ParamAST p= new ParamAST();
		#primaryExpr= p;
		p.number= Integer.parseInt((#n).getText());  
		currentQuery().addParameter(p);
	}
       |   lit: literal 
       |   TOK_LPAREN q:query TOK_RPAREN 
	{
		OQLAST q= (OQLAST)#q;	
		OQLAST p= (OQLAST)#primaryExpr;
	 	if(q.tree==null)
		  p.tree=new AnalysisTree(q);
    		else
	          p.tree= q.tree;
	}		
       )
    ;

argList :

        TOK_LPAREN
        (
            expr
            (
                TOK_COMMA
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
            "first"
        |   "last"
        |   "unique"  
        |   "exists"  
        )
        TOK_LPAREN query TOK_RPAREN

    ;

aggregateExpr :

        (
            (
                s:"sum"{#s.setText("sum("); }
            |   mn:"min"{#mn.setText("min("); }
            |   mx:"max"{#mx.setText("max("); }
            |   av:"avg"{#av.setText("avg("); }
            )
            l:TOK_LPAREN {#l.setText("");} q:query TOK_RPAREN  

	{
        AggregateAST ag= new AggregateAST();
        ag.setText(#aggregateExpr.getText());
		ag.setExpr((OQLAST)#q);
        #aggregateExpr=ag;
	}

        |   c:"count" {#c.setText("count("); }
            lp:TOK_LPAREN{#lp.setText("");}
            (
                query
            |   TOK_STAR
            )
            TOK_RPAREN   { ((OQLAST)#aggregateExpr).makumbaType="int";}
        )
    ;

undefinedExpr :
	//* we take away is_defined and is_undefined and we replace them with SQL is null and is not null...
	{String s; }
        (
            undef:"is_undefined" !{s=" is null"; }
        |   def:"is_defined"!{s=" is not null"; }
        )
   	TOK_LPAREN 
        query
        rp:TOK_RPAREN
	{ #rp.setText(") "+s); }
    ;

objectConstruction :

        typeIdentifier
        TOK_LPAREN
        fieldList
        TOK_RPAREN
    ;

structConstruction :

        "struct"
        TOK_LPAREN
        fieldList
        TOK_RPAREN
    ;

fieldList :

        labelIdentifier TOK_COLON expr
        (
            TOK_COMMA
            labelIdentifier
            TOK_COLON expr
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
        |   booleanLiteral { ((OQLAST)#literal).makumbaType="int";} 
        |   longLiteral  { ((OQLAST)#literal).makumbaType="int";} 
        |   doubleLiteral
        |   charLiteral  { ((OQLAST)#literal).makumbaType="char";}  
        |   stringLiteral { ((OQLAST)#literal).makumbaType="char";} 
        |   dateLiteral   { ((OQLAST)#literal).makumbaType="datetime";} 
        |   timeLiteral  { ((OQLAST)#literal).makumbaType="datetime";} 
        |   timestampLiteral  { ((OQLAST)#literal).makumbaType="date";} 
    ;

objectLiteral :

        "nil" 
	{if(lastEQop!=null)
		lastEQop.setText(is); 
	#objectLiteral.setText("null"); 
	((OQLAST)#objectLiteral).makumbaType="nil";
	}
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

dateLiteral !:

        "date" sl:StringLiteral 
	{
	   #dateLiteral=(#sl);
	   #sl.setText(#sl.getText().replace('\"', '\''));
	}
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
