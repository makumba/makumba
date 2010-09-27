package org.makumba.providers.query.mql;

import antlr.ASTFactory;

/** Makumba adaptation of the original class in org.hibernate.hql.ast. 
 * To update, check the differences with the .original file in this repository 
 * */

/**
 * User: Joshua Davis<br>
 * Date: Sep 23, 2005<br>
 * Time: 12:30:01 PM<br>
 */
public class HqlASTFactory extends ASTFactory {

    /**
     * Returns the class for a given token type (a.k.a. AST node type).
     * 
     * @param tokenType
     *            The token type.
     * @return Class - The AST node class to instantiate.
     */
    @Override
    public Class<?> getASTNodeType(int tokenType) {
        return Node.class;
    }
}
