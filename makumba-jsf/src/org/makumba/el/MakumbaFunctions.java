package org.makumba.el;

import javax.el.ELContext;
import javax.faces.context.FacesContext;

/**
 * makumba EL functions
 * 
 * @author manu
 */
public class MakumbaFunctions {

    public static String expr(String expr) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ELContext elCtx = ctx.getELContext();
        System.out.println("MakumbaFunctions.expr() FacesContext renderResponse " + ctx.getRenderResponse());
        System.out.println("MakumbaFunctions.expr() FacesContext responseComplete " + ctx.getResponseComplete());
        System.out.println("MakumbaFunctions.expr() FacesContext currentPhaseId " + ctx.getCurrentPhaseId());

        String component = elCtx.getELResolver().getValue(elCtx, "component", "class").toString();

        return "mak:expr(" + expr + ") nested in component " + component;
    }
}
