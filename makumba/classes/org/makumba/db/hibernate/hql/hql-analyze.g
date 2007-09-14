header
{
// $Id$
package org.makumba.db.hibernate.hql;
import org.hibernate.hql.antlr.HqlTokenTypes;
}

/**
 * Hibernate Query Language Type Analyzer.<br>
 * Inspired from hql-sql.g by Joshua Davis
 * @author Cristian Bogdan
 */
class HqlAnalyzeBaseWalker extends TreeParser;

options
{
	// Note: importVocab and exportVocab cause ANTLR to share the token type numbers between the
	// two grammars.  This means that the token type constants from the source tree are the same
	// as those in the target tree.  If this is not the case, tree translation can result in
	// token types from the *source* tree being present in the target tree.
	importVocab=Hql;        // import definitions from "Hql"
	exportVocab=HqlAnalyze;     // Call the resulting definitions "HqlAnalyze"
	buildAST=true;
}

tokens
{
	FROM_FRAGMENT;	// A fragment of SQL that represents a table reference in a FROM clause.
	IMPLIED_FROM;	// An implied FROM element.
	JOIN_FRAGMENT;	// A JOIN fragment.
	SELECT_CLAUSE;
	LEFT_OUTER;
	RIGHT_OUTER;
	ALIAS_REF;      // An IDENT that is a reference to an entity via it's alias.
	PROPERTY_REF;   // A DOT that is a reference to a property in an entity.
	SQL_TOKEN;      // A chunk of SQL that is 'rendered' already.
	SELECT_COLUMNS; // A chunk of SQL representing a bunch of select columns.
	SELECT_EXPR;    // A select expression, generated from a FROM element.
	THETA_JOINS;	// Root of theta join condition subtree.
	FILTERS;		// Root of the filters condition subtree.
	METHOD_NAME;    // An IDENT that is a method name.
	NAMED_PARAM;    // A named parameter (:foo).
	BOGUS;          // Used for error state detection, etc.
}

// -- Declarations --
{
	
			  ObjectType typeComputer;
	
  AST deriveArithmethicExpr(AST ae) throws antlr.RecognitionException { 
  			    return ae; 
	  	 }
	  	 
	  	AST deriveLogicalExpr(AST le) throws antlr.RecognitionException{  return le; 	  	 }
	  	 
		  AST deriveParamExpr(AST pe) throws antlr.RecognitionException{  return pe; 	  	 }
		  
		  AST deriveFunctionCallExpr(AST fc, AST e) throws antlr.RecognitionException{  return fc; 	  	 }
	
	  java.util.Map aliasTypes= new java.util.HashMap();
	  
	  java.util.Stack stackAliases = new java.util.Stack();
	  
		  void setAliasType(AST alias, String path) throws antlr.RecognitionException{}
 
  void getReturnTypes(AST a, java.util.Stack stackAliases) throws antlr.RecognitionException { }
		  		

	private int level = 0;
	private boolean inSelect = false;
	private boolean inFunctionCall = false;
	private boolean inCase = false;
	private boolean inFrom = false;
	private int statementType;
	private String statementTypeName;
	// Note: currentClauseType tracks the current clause within the current
	// statement, regardless of level; currentTopLevelClauseType, on the other
	// hand, tracks the current clause within the top (or primary) statement.
	// Thus, currentTopLevelClauseType ignores the clauses from any subqueries.
	private int currentClauseType;
	private int currentTopLevelClauseType;

	public final boolean isSubQuery() {
		return level > 1;
	}

	public final boolean isInFrom() {
		return inFrom;
	}

	public final boolean isInFunctionCall() {
		return inFunctionCall;
	}
	
	public final boolean isInSelect() {
		return inSelect;
	}

	public final boolean isInCase() {
		return inCase;
	}

	public final int getStatementType() {
		return statementType;
	}

	public final int getCurrentClauseType() {
		return currentClauseType;
	}

	public final int getCurrentTopLevelClauseType() {
		return currentTopLevelClauseType;
	}

	public final boolean isComparativeExpressionClause() {
		// Note: once we add support for "JOIN ... ON ...",
		// the ON clause needs to get included here
	    return getCurrentClauseType() == WHERE ||
	            getCurrentClauseType() == WITH ||
	            isInCase();
	}

	public final boolean isSelectStatement() {
		return statementType == SELECT;
	}

void beforeStatement(String statementName, int statementType) {
		inFunctionCall = false;
		level++;
		
		if ( level == 1 ) {
			this.statementTypeName = statementName;
			this.statementType = statementType;
		
		}
		/* *** if ( log.isDebugEnabled() ) {
			log.debug( statementName + " << begin [level=" + level + ", statement=" + this.statementTypeName + "]" );
		}*/
	}

	private void beforeStatementCompletion(String statementName) {
		/* *** if ( log.isDebugEnabled() ) {
			log.debug( statementName + " : finishing up [level=" + level + ", statement=" + statementTypeName + "]" );
		}*/
	}

	void afterStatementCompletion(String statementName) {
		/* *** if ( log.isDebugEnabled() ) {
			log.debug( statementName + " >> end [level=" + level + ", statement=" + statementTypeName + "]" );
		}*/
		level--;
	}

	private void handleClauseStart(int clauseType) {
		currentClauseType = clauseType;
		if ( level == 1 ) {
			currentTopLevelClauseType = clauseType;
		}
	}
	
			protected void setAlias(AST se, AST i) {}
	}



// The main statement rule.
statement
	: selectStatement | updateStatement | deleteStatement | insertStatement
	;

selectStatement
	: query
	;

// Cannot use just the fromElement rule here in the update and delete queries
// because fromElement essentially relies on a FromClause already having been
// built :(
updateStatement!
	: #( u:UPDATE { beforeStatement( "update", UPDATE ); } (v:VERSIONED)? f:fromClause s:setClause (w:whereClause)? ) {
		#updateStatement = #(#u, #f, #s, #w);
		//*** ignored
		}
	;

deleteStatement
	: #( DELETE { beforeStatement( "delete", DELETE ); } fromClause (whereClause)? ) {
		//*** ignored
		}
	;

insertStatement
	// currently only "INSERT ... SELECT ..." statements supported;
	// do we also need support for "INSERT ... VALUES ..."?
	//
	: #( INSERT { beforeStatement( "insert", INSERT ); } intoClause query ) {
//*** ignored
}
	;

intoClause! {
		String p = null;
	}
	: #( INTO { handleClauseStart( INTO ); } (p=path) ps:insertablePropertySpec ) {}
	;

insertablePropertySpec
	: #( RANGE (IDENT)+ )
	;

setClause
	: #( SET { handleClauseStart( SET ); } (assignment)* )
	;

assignment
	: #( EQ (p:propertyRef) (newValue) ) {}
	;

// For now, just use expr.  Revisit after ejb3 solidifies this.
newValue
	: expr | query
	;

// The query / subquery rule. Pops the current 'from node' context 
// (list of aliases).
query!
	: #( QUERY { beforeStatement( "select", SELECT ); }
			// The first phase places the FROM first to make processing the SELECT simpler.
			#(SELECT_FROM
				f:fromClause
				(s:selectClause)?
			)
			(w:whereClause)?
			(g:groupClause)?
			(o:orderClause)?
		) {
		// Antlr note: #x_in refers to the input AST, #x refers to the output AST
		#query = #([SELECT,"SELECT"], #s, #f, #w, #g, #o);
//		***beforeStatementCompletion( "select" );
		//processQuery( #s, #query );
		//afterStatementCompletion( "select" );
	}
	;

orderClause
	: #(ORDER { handleClauseStart( ORDER ); } orderExprs)
	;

orderExprs
	: expr ( ASCENDING | DESCENDING )? (orderExprs)?
	;

groupClause
	: #(GROUP { handleClauseStart( GROUP ); } (expr)+ ( #(HAVING logicalExpr) )? )
	;

selectClause!
	: #(SELECT { handleClauseStart( SELECT ); /* ***beforeSelectClause();*/ } (d:DISTINCT)? x:selectExprList ) {
		#selectClause = #([SELECT_CLAUSE,"{select clause}"], #d, #x);
		getReturnTypes(		#selectClause, stackAliases);
		//***analysis finished, here we should copy stuff somewhere (List (ordered), ...)
	}
	;

selectExprList {
		boolean oldInSelect = inSelect;
		inSelect = true;
	}
	: ( selectExpr | aliasedSelectExpr )+ {
		inSelect = oldInSelect;
	}
	;

aliasedSelectExpr!
	: #(AS se:selectExpr i:identifier) {
	   setAlias(#se,#i);
		#aliasedSelectExpr = #se;
	}
	;

selectExpr
	: p:propertyRef					{ /* *** resolveSelectExpression(#p); */ }
	| #(ALL ar2:aliasRef) 			{/* *** resolveSelectExpression(#ar2); */ #selectExpr = #ar2; }
	| #(OBJECT ar3:aliasRef)		{/* *** resolveSelectExpression(#ar3);*/ #selectExpr = #ar3; }
	| con:constructor 				{ /* *** processConstructor(#con)*/ ; }
	| functionCall
	| count { #selectExpr = new ExprTypeAST(ExprTypeAST.INT);}
	| collectionFunction			// elements() or indices()
	| literal //***already done
	| are:arithmeticExpr { #selectExpr= deriveArithmethicExpr(#are); }
		| logicalExpr
	;

count
	: #(COUNT ( DISTINCT | ALL )? ( aggregateExpr | ROW_STAR ) )
	;

constructor
	{ String className = null; }
	: #(CONSTRUCTOR className=path ( selectExpr | aliasedSelectExpr )* )
	;

aggregateExpr
	: expr //p:propertyRef { resolve(#p); }
	| collectionFunction
	;

// Establishes the list of aliases being used by this query.
fromClause {
		// NOTE: This references the INPUT AST! (see http://www.antlr.org/doc/trees.html#Action%20Translation)
		// the ouput AST (#fromClause) has not been built yet.
	// 	prepareFromClauseInputTree(#fromClause_in);
	}
	: #(f:FROM { /* *** pushFromClause(#fromClause,f); handleClauseStart( FROM );*/ } fromElementList )
	;

fromElementList {
		boolean oldInFrom = inFrom;
		inFrom = true;
		}
	: (fromElement)+ {
		inFrom = oldInFrom;
		}
	;

fromElement!{
	String p;
} 
	// A simple class name, alias element.
	: #(RANGE p=path (a:ALIAS)? (pf:FETCH)? ) {
		setAliasType(#a, p);
	}
	| je:joinElement 
	// A from element created due to filter compilation
	| fe:FILTER_ENTITY a3:ALIAS {
		// *** #fromElement = createFromFilterElement(fe,a3);
	}
	;

joinElement! 
		: #(JOIN (joinType)? (FETCH)? ref:propertyRef (a:ALIAS)? (FETCH)? (WITH)? ) {
			  setAliasType(#a, ((ObjectTypeAST)#ref).getObjectType());
}
	;

// Returns an node type integer that represents the join type
// tokens.
joinType 
	: ( (LEFT | RIGHT) (OUTER)? ) 
	| FULL
	| INNER 
	;

// Matches a path and returns the normalized string for the path (usually
// fully qualified a class name).
path returns [String p] {
	p = "???";
	String x = "?x?";
	}
	: a:identifier { p = a.getText(); }
	| #(DOT x=path y:identifier) {
			StringBuffer buf = new StringBuffer();
			buf.append(x).append(".").append(y.getText());
			p = buf.toString();
		}
	;

// Returns a path as a single identifier node.
pathAsIdent {
    String text = "?text?";
    }
    : text=path {
        #pathAsIdent = #([IDENT,text]);
    }
    ;

whereClause
	: #(w:WHERE { handleClauseStart( WHERE ); } b:logicalExpr ) {
		// Use the *output* AST for the boolean expression!
		#whereClause = #(w , #b);
	}
	;

logicalExpr
	: #(AND logicalExpr logicalExpr)
	| #(OR logicalExpr logicalExpr)
	| #(NOT logicalExpr)
	| co:comparisonExpr { #logicalExpr =  deriveLogicalExpr(#co); }
	;

// TODO: Add any other comparison operators here.
comparisonExpr
	: #(EQ exprOrSubquery exprOrSubquery)
	| #(NE exprOrSubquery exprOrSubquery)
	| #(LT exprOrSubquery exprOrSubquery)
	| #(GT exprOrSubquery exprOrSubquery)
	| #(LE exprOrSubquery exprOrSubquery)
	| #(GE exprOrSubquery exprOrSubquery)
	| #(LIKE expr expr ( #(ESCAPE expr) )? )
	| #(NOT_LIKE expr expr ( #(ESCAPE expr) )? )
	| #(BETWEEN expr expr expr)
	| #(NOT_BETWEEN expr expr expr)
	| #(IN inLhs inRhs )
	| #(NOT_IN inLhs inRhs )
	| #(IS_NULL expr)
//	| #(IS_TRUE expr)
//	| #(IS_FALSE expr)
	| #(IS_NOT_NULL expr)
	| #(EXISTS ( expr | collectionFunctionOrSubselect ) )
	;

inRhs
	: #(IN_LIST ( collectionFunctionOrSubselect | ( (expr)* ) ) )
	;

inLhs
	: expr
//	| #( VECTOR_EXPR (expr)* )
	;

exprOrSubquery
	: expr
	| query
	| #(ANY collectionFunctionOrSubselect)
	| #(ALL collectionFunctionOrSubselect)
	| #(SOME collectionFunctionOrSubselect)
	;
	
collectionFunctionOrSubselect
	: collectionFunction
	| query
	;
	
expr
	: ae:addrExpr [ true ] { /* ***resolve(#ae);*/ }	// Resolve the top level 'address expression'
	| #( VECTOR_EXPR (expr)* )
	| constant
	| are:arithmeticExpr { #expr= deriveArithmethicExpr(#are); }
	| functionCall							// Function call, not in the SELECT clause.
	| par:parameter { #expr= deriveParamExpr(#par); }
	| count										 { #expr = new ExprTypeAST(ExprTypeAST.INT);} // Count, not in the SELECT clause.
	;

arithmeticExpr
	: #(PLUS expr expr) 
	| #(MINUS expr expr)
	| #(DIV expr expr)
	| #(STAR expr expr)
//	| #(CONCAT expr (expr)+ )
	| #(UNARY_MINUS expr)
	| caseExpr
	;

caseExpr
	: #(CASE { inCase = true; } (#(WHEN logicalExpr expr))+ (#(ELSE expr))?) { inCase = false; }
	| #(CASE2 { inCase = true; } expr (#(WHEN expr expr))+ (#(ELSE expr))?) { inCase = false; }
	;

//TODO: I don't think we need this anymore .. how is it different to 
//      maxelements, etc, which are handled by functionCall
collectionFunction
	: #(e:ELEMENTS {inFunctionCall=true;} p1:propertyRef { /* ***resolve(#p1);*/ } ) 
		{ /* *** processFunction(#e,inSelect);*/ } {inFunctionCall=false;}
	| #(i:INDICES {inFunctionCall=true;} p2:propertyRef { /* *** resolve(#p2);*/ } ) 
		{ /* ***processFunction(#i,inSelect);*/ } {inFunctionCall=false;}
	;

functionCall
	: #(METHOD_CALL  {inFunctionCall=true;} p:pathAsIdent ( #(e:EXPR_LIST (expr)* ) )? )
		{ #functionCall = deriveFunctionCallExpr(#p, #e); } {inFunctionCall=false;}
	| #(AGGREGATE aggregateExpr )
	;

constant
	: literal
	| NULL {#constant= new ExprTypeAST(ExprTypeAST.NULL); } 
	| TRUE {#constant= new ExprTypeAST(ExprTypeAST.BOOLEAN); } 
	| FALSE { #constant= new ExprTypeAST(ExprTypeAST.BOOLEAN);  }
	;

literal
	: NUM_INT {#literal= new ExprTypeAST(ExprTypeAST.INT); } 
	| NUM_FLOAT {#literal= new ExprTypeAST(ExprTypeAST.FLOAT); } 
	| NUM_LONG {#literal= new ExprTypeAST(ExprTypeAST.LONG); } 
	| NUM_DOUBLE  {#literal= new ExprTypeAST(ExprTypeAST.DOUBLE); } 
	| QUOTED_STRING {#literal= new ExprTypeAST(ExprTypeAST.STRING); } 
	;

identifier
	: (IDENT | WEIRD_IDENT)
	;

addrExpr! [ boolean root ]
	: #(d:DOT lhs:addrExprLhs rhs:propertyName )	{
		// This gives lookupProperty() a chance to transform the tree 
		// to process collection properties (.elements, etc).
		#addrExpr = new ObjectTypeAST(#lhs, #rhs, aliasTypes, typeComputer);
//		#addrExpr = lookupProperty(#addrExpr,root,false);
	}
	| #(i:INDEX_OP lhs2:addrExprLhs rhs2:expr)	{
		#addrExpr = #(#i, #lhs2, #rhs2);
// *** 		processIndex(#addrExpr);
	}
	| p:identifier {
		#addrExpr = #p;
		// *** resolve(#addrExpr);
		// *** setRoot(#p);
	}
	;

addrExprLhs
	: addrExpr [ false ]
	;

propertyName
	: identifier
	| CLASS
	| ELEMENTS
	| INDICES
	;

propertyRef
	: #(DOT lhs:propertyRefLhs rhs:propertyName )	{
#propertyRef = new ObjectTypeAST(#lhs, #rhs, aliasTypes, typeComputer);
		}
	|
	p:identifier {
		
		#propertyRef = new ObjectTypeAST(#p, aliasTypes);
		// In many cases, things other than property-refs are recognized
		// by this propertyRef rule.  Some of those I have seen:
		//  1) select-clause from-aliases
		//  2) sql-functions
		/* ***
		
		if ( isNonQualifiedPropertyRef(#p) ) {
			#propertyRef = lookupNonQualifiedProperty(#p);
		}
		else {
			resolve(#p);
			#propertyRef = #p;
		}
		*/
	}
	;

propertyRefLhs
	: propertyRef
	;

aliasRef!
	: i:identifier {
		#aliasRef = #([ALIAS_REF,i.getText()]);	// Create an ALIAS_REF node instead of an IDENT node.
// *** 		lookupAlias(#aliasRef);
		}
	;

parameter!
	: #(COLON a:identifier) {
			// Create a NAMED_PARAM node instead of (COLON IDENT).
			#parameter = #([NAMED_PARAM,a.getText()]);
			/* *** namedParameter(#parameter);*/ 
		}
	| #(p:PARAM (n:NUM_INT)?) {
			if ( n != null ) {
				// An ejb3-style "positional parameter", which we handle internally as a named-param
				#parameter = #([NAMED_PARAM,n.getText()]);
/* *** 				namedParameter(#parameter);*/
			}
			else {
				#parameter = #([PARAM,"?"]);
// *** 				positionalParameter(#parameter);
			}
		}
	;

numericInteger
	: NUM_INT
	;