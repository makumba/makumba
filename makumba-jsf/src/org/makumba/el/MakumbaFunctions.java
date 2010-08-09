package org.makumba.jsf.component.el;

import org.makumba.jsf.component.UIRepeatListComponent;

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
