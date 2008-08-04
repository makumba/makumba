package org.makumba.providers.query.mql;

import java.io.PrintWriter;
import org.hibernate.hql.ast.util.ASTPrinter;
import org.hibernate.hql.ast.util.ASTUtil;
import org.makumba.commons.NameResolver;

import antlr.ASTFactory;
import antlr.RecognitionException;
import antlr.SemanticException;
import antlr.collections.AST;

public class MqlSqlWalker extends MqlSqlBaseWalker {
    // TODO:
    // finish subqueries
   
    // TODO: analysis (HqlAnalyzer can be used for now)
    // projections
    // parameters
    // labels
    
    ASTFactory fact ;
    RecognitionException error;
    QueryContext currentContext;
    NameResolver nr;
    private boolean fromEnded;
    static PrintWriter pw = new PrintWriter(System.out);
    static ASTPrinter printer = new ASTPrinter(HqlSqlTokenTypes.class);
 
    protected void pushFromClause(AST fromClause,AST inputFromNode) {
        QueryContext c= new QueryContext(this);
        c.setParent(currentContext);
        currentContext=c;
    }
    
    protected void setFromEnded() throws SemanticException{
        fromEnded=true;
    }
    
    protected void processQuery(AST select,AST query) throws SemanticException { 
        currentContext.close(); 
        currentContext=currentContext.getParent();
    }


    public MqlSqlWalker(NameResolver nr){
        setASTFactory(fact= new MqlSqlASTFactory(this));
        this.nr=nr;
        
    }
    protected AST createFromElement(String path, AST alias, AST propertyFetch) throws SemanticException {
       return currentContext.createFromElement(path, alias, HqlSqlTokenTypes.INNER);
    }
    
    protected void createFromJoinElement(AST path,AST alias,int joinType,AST fetch,AST propertyFetch,AST with) throws SemanticException {
       if(!(path instanceof MqlDotNode))
           throw new SemanticException("can only support dot froms "+path);
       ((MqlDotNode)path).processInFrom();
       currentContext.createFromElement(path.getText(), alias, joinType);
    }

    
    protected AST lookupProperty(AST dot,boolean root,boolean inSelect) throws SemanticException {
        if(error!=null || !fromEnded)
            return dot;
        // root and inSelect are useless due to logicExpr being accepted now in SELECT
        MqlDotNode dotNode= (MqlDotNode)dot;
        dotNode.processInExpression();
        return dot;
    }

    protected void resolve(AST node) throws SemanticException { 
        if(error!=null || !fromEnded)
            return;
        if(node.getType()==HqlSqlTokenTypes.IDENT)
            ((MqlIdentNode)node).resolve();
    }
    protected void setAlias(AST selectExpr, AST ident) {
        // we add the label to the output
        if(error!=null)
            return;
        selectExpr.setNextSibling(ASTUtil.create(fact, HqlSqlTokenTypes.ALIAS_REF, " AS "+ident.getText()));
    }

    
    protected AST generateNamedParameter(AST delimiterNode, AST nameNode) throws SemanticException {
        return ASTUtil.create(fact, MqlSqlWalker.NAMED_PARAM, "?");
    }

    protected AST generatePositionalParameter(AST inputNode) throws SemanticException {
        return ASTUtil.create(fact, MqlSqlWalker.PARAM, "?");
    }
    
    public void reportError(RecognitionException e) {
        if(error==null)
            error=e;
    }

    public void reportError(String s) {
        if(error== null)
            error= new RecognitionException(s);
    }

    public void reportWarning(String s) {
        System.out.println(s);
    }

}
