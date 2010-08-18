package org.makumba.providers.query.mql;

import antlr.collections.AST;

/**
 * @author Cristian Bogdan
 */
public class MqlHqlGenerator extends MqlSqlGenerator {

    @Override
    protected void out(AST n) {
        ((MqlNode) n).writeToHql(getText());
    }

    @Override
    protected boolean isHql() {
        return true;
    }

}
