package org.makumba.el;

import org.makumba.jsf.UIRepeatListComponent;

/**
 * makumba EL functions
 * 
 * @author manu
 * @author cristi
 */
public class MakumbaFunctions {

    public static Object expr(String expr) {
        return UIRepeatListComponent.getCurrentlyRunning().getExpressionValue(expr);
    }
}
