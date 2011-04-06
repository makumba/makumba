package org.makumba.jsf;

import java.io.Serializable;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import org.makumba.Pointer;
import org.makumba.jsf.component.UIRepeatListComponent;

public class PointerConverter implements Converter, Serializable {

    private static final long serialVersionUID = 3102266240167328526L;

    static final Logger log = java.util.logging.Logger.getLogger("org.makumba.jsf.converter");

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {

        UIRepeatListComponent list = UIRepeatListComponent.findMakListParent(component, true);
        try {
            return list.convertExpression(component, value);
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(component.getClientId(),
                new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage()));
            throw new ConverterException(e.getMessage());
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value instanceof String) {
            return (String) value;
        }
        return ((Pointer) value).toExternalForm();
    }

}
