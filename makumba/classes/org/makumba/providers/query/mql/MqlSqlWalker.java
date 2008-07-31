package org.makumba.providers.query.mql;

import java.util.Properties;

import org.hibernate.hql.ast.util.ASTUtil;
import org.makumba.commons.NameResolver;

import antlr.ASTFactory;
import antlr.RecognitionException;
import antlr.SemanticException;
import antlr.collections.AST;

public class MqlSqlWalker extends MqlSqlBaseWalker {
    ASTFactory fact = new MqlSqlASTFactory();
    RecognitionException error;
    QueryContext currentContext;
    NameResolver nr;
    
    protected void pushFromClause(AST fromClause,AST inputFromNode) {
        QueryContext c= new QueryContext(this);
        c.setParent(currentContext);
        currentContext=c;
    }
    
    protected void processQuery(AST select,AST query) throws SemanticException { 
        currentContext.close();
        currentContext=currentContext.getParent();
    }


    public MqlSqlWalker(){
        setASTFactory(fact);
        String databaseProperties =  "test/localhost_mysql_makumba.properties";
        Properties p = new Properties();
        try {
            p.load(org.makumba.commons.ClassResource.get(databaseProperties).openStream());
        } catch (Exception e) {
            throw new org.makumba.ConfigFileError(databaseProperties);
        }

        nr = new NameResolver(p);

    }
    protected AST createFromElement(String path, AST alias, AST propertyFetch) throws SemanticException {
       return currentContext.createFromElement(path, alias);
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
