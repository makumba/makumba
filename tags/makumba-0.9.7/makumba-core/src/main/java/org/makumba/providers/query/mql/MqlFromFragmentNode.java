package org.makumba.providers.query.mql;

import org.makumba.commons.TextList;

import antlr.collections.AST;

public class MqlFromFragmentNode extends MqlNode {

    QueryContext queryContext;

    TextList hqlFrom;

    @Override
    public void writeToHql(TextList tl) {
        if (hqlFrom == null) {
            hqlFrom = queryContext.getFrom(true);
        }
        tl.append(hqlFrom);
    }

    @Override
    public void initialize(AST t) {
        super.initialize(t);
        if (t instanceof MqlFromFragmentNode) {
            MqlFromFragmentNode n = (MqlFromFragmentNode) t;
            this.queryContext = n.queryContext;
            this.hqlFrom = n.hqlFrom;
        }
    }
}
