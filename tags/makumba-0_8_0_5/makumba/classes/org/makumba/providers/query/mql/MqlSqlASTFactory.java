package org.makumba.providers.query.mql;

import java.lang.reflect.Constructor;

import org.hibernate.hql.antlr.HqlSqlTokenTypes;

import antlr.ASTFactory;
import antlr.Token;
import antlr.collections.AST;

public class MqlSqlASTFactory extends ASTFactory  {
    
    @Override
    public Class getASTNodeType(int tokenType) {
        switch ( tokenType ) {
            case HqlSqlTokenTypes.FROM_FRAGMENT:
                return FromPath.class;
            default:
                return antlr.CommonAST.class;
        }
    }
    
    @Override
    protected AST createUsingCtor(Token token, String className) {
        Class c;
        AST t;
        try {
            c = Class.forName( className );
            Class[] tokenArgType = new Class[]{antlr.Token.class};
            Constructor ctor = c.getConstructor( tokenArgType );
            if ( ctor != null ) {
                t = ( AST ) ctor.newInstance( new Object[]{token} ); // make a new one
             //   initializeSqlNode( t );
            }
            else {
                // just do the regular thing if you can't find the ctor
                // Your AST must have default ctor to use this.
                t = create( c );
            }
        }
        catch ( Exception e ) {
            throw new IllegalArgumentException( "Invalid class or can't make instance, " + className );
        }
        return t;
    }
    
    /**
     * Actually instantiate the AST node.
     *
     * @param c The class to instantiate.
     * @return The instantiated and initialized node.
     */
    @Override
    protected AST create(Class c) {
        AST t;
        try {
            t = ( AST ) c.newInstance(); // make a new one
            //initializeSqlNode( t );
        }
        catch ( Exception e ) {
            error( "Can't create AST Node " + c.getName() );
            return null;
        }
        return t;
    }

}
