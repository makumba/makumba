package org.makumba.el;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 * makumba EL functions
 * 
 * @author manu
 */
public class MakumbaFunctions {

    public static String expr(String expr) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        // System.out.println("MakumbaFunctions.expr() " + expr);
        // System.out.println("MakumbaFunctions.expr() FacesContext renderResponse " + ctx.getRenderResponse());
        // System.out.println("MakumbaFunctions.expr() FacesContext responseComplete " + ctx.getResponseComplete());
        // System.out.println("MakumbaFunctions.expr() FacesContext currentPhaseId " + ctx.getCurrentPhaseId());

        // from ze book, page 98
        UIComponent component = ctx.getApplication().evaluateExpressionGet(ctx, "#{component}", UIComponent.class);

        // ELContext elCtx = ctx.getELContext();
        // Object component = elCtx.getELResolver().getValue(elCtx, "component", "class");

        return "mak:expr(" + expr + ") nested in component " + component;
    }
}
