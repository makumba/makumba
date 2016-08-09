package org.makumba.providers.query.mql;

import org.hibernate.hql.ast.util.ASTUtil;

import antlr.ASTFactory;
import antlr.RecognitionException;
import antlr.SemanticException;
import antlr.collections.AST;

public class MqlSqlWalker extends MqlSqlBaseWalker {
    ASTFactory fact = new MqlSqlASTFactory();
    RecognitionException error;
    
    public MqlSqlWalker(){
        setASTFactory(fact);
    }
    protected AST createFromElement(String path, AST alias, AST propertyFetch) throws SemanticException {
        AST ast = ASTUtil.create(fact, FROM_FRAGMENT, path+" "+alias);
        return ast;
    }
    
    public void reportError(RecognitionException e) {
       error=e;
    }

    public void reportError(String s) {
        error= new RecognitionException(s);
    }

    public void reportWarning(String s) {
        System.out.println(s);
    }

}
