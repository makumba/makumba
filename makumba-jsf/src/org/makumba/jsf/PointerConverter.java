/*
 * Created on Jul 31, 2010
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.makumba.jsf;

import java.io.Serializable;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.makumba.Pointer;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.el.MakumbaELResolver;

public class PointerConverter implements Converter, Serializable {
    static final Logger log = java.util.logging.Logger.getLogger("org.makumba.jsf.ptrConvert");

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        // log.fine("Resolving  " + value);
        try {

            // FIXME: note that there can be more pointers in the expression, though that is unlikely
            String expr = component.getValueExpression("value").getExpressionString();
            // take away #{ }
            expr = expr.substring(2, expr.length() - 1).trim();

            UIRepeatListComponent list = UIRepeatListComponent.findMakListParent(component, true);
            Pointer ptr = MakumbaELResolver.resolvePointer(value, expr, list);
            System.out.println(UIRepeatListComponent.getCurrentlyRunning().debugIdent() + " " + value + " is "
                    + ptr.toString());
            return ptr;
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeWrappedException(t);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return ((Pointer) value).toExternalForm();
    }
}
