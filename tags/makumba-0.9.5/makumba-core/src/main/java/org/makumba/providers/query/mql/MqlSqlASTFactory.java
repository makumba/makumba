package org.makumba.providers.query.mql;

import java.lang.reflect.Constructor;

import antlr.ASTFactory;
import antlr.Token;
import antlr.collections.AST;

public class MqlSqlASTFactory extends ASTFactory {

    private MqlSqlWalker walker;

    public MqlSqlASTFactory(MqlSqlWalker mqlSqlWalker) {
        walker = mqlSqlWalker;
    }

    @Override
    public Class<? extends MqlNode> getASTNodeType(int tokenType) {
        switch (tokenType) {
            case HqlSqlTokenTypes.IDENT:
                return MqlIdentNode.class;
            case HqlSqlTokenTypes.DOT:
                return MqlDotNode.class;
            case HqlSqlTokenTypes.EQ:
            case HqlSqlTokenTypes.NE:
            case HqlSqlTokenTypes.LE:
            case HqlSqlTokenTypes.LT:
            case HqlSqlTokenTypes.GE:
            case HqlSqlTokenTypes.GT:
            case HqlSqlTokenTypes.LIKE:
            case HqlSqlTokenTypes.NOT_LIKE:
                return MqlComparisonNode.class;
            case HqlSqlTokenTypes.PLUS:
            case HqlSqlTokenTypes.MINUS:
            case HqlSqlTokenTypes.STAR:
            case HqlSqlTokenTypes.DIV:
                return MqlAritmeticNode.class;
            case HqlSqlTokenTypes.AND:
            case HqlSqlTokenTypes.OR:
                return MqlLogicalNode.class;
            case HqlSqlTokenTypes.FROM_FRAGMENT:
                return MqlFromFragmentNode.class;
            default:
                return MqlNode.class;
        }
    }

    @Override
    protected AST createUsingCtor(Token token, String className) {
        // FIXME/TODO: why is this method overriding? the only difference seems to be in
        // ASTFActory: c = Utils.loadClass(className);
        // MqlSqlASTFactory: c = Class.forName(className);
        Class<?> c;
        AST t;
        try {
            c = Class.forName(className);
            Class<?>[] tokenArgType = new Class[] { antlr.Token.class };
            Constructor<?> ctor = c.getConstructor(tokenArgType);
            if (ctor != null) {
                t = (AST) ctor.newInstance(new Object[] { token }); // make a new one
                initializeNode(t);
            } else {
                // just do the regular thing if you can't find the ctor
                // Your AST must have default ctor to use this.
                t = create(c);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid class or can't make instance, " + className);
        }
        return t;
    }

    /**
     * Actually instantiate the AST node.
     * 
     * @param c
     *            The class to instantiate.
     * @return The instantiated and initialized node.
     */
    @SuppressWarnings("unchecked")
    @Override
    protected AST create(Class c) {
        AST t;
        try {
            t = (AST) c.newInstance(); // make a new one
            initializeNode(t);
        } catch (Exception e) {
            error("Can't create AST Node " + c.getName());
            return null;
        }
        return t;
    }

    private void initializeNode(AST t) {
        if (t instanceof MqlNode) {
            ((MqlNode) t).setWalker(walker);
        }

    }

}
