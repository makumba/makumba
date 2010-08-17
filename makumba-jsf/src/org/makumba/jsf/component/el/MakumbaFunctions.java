package org.makumba.jsf.component.el;

import org.makumba.jsf.MakumbaDataContext;

/**
 * makumba EL functions
 * 
 * @author manu
 * @author cristi
 */
public class MakumbaFunctions {

    public static Object expr(String expr) {
        return MakumbaDataContext.getDataContext().getCurrentList().getExpressionValue(expr);
    }
}
