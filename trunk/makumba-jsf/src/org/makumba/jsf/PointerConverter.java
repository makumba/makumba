/*
 * Created on Jul 31, 2010
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.makumba.jsf;

import java.io.Serializable;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import org.makumba.Pointer;

public class PointerConverter implements Converter, Serializable {
    static final Logger log = java.util.logging.Logger.getLogger("org.makumba.jsf.ptrConvert");

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {

        UIRepeatListComponent list = UIRepeatListComponent.findMakListParent(component, true);
        try {
            return list.convertAndValidateExpression(component, value);
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(component.getClientId(),
                new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage()));
            throw new ConverterException(e.getMessage());
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return ((Pointer) value).toExternalForm();
    }

}
