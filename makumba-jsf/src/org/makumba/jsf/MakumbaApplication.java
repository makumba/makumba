package org.makumba.jsf;

import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.ApplicationWrapper;
import javax.faces.application.Resource;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.makumba.jsf.component.MakumbaDataComponent;
import org.makumba.jsf.update.DataHandler;

@SuppressWarnings("deprecation")
/**
 * Decorator for the JSF Application that sets a ComponentDataHandler on all MakumbaDataComponent-s
 * 
 * @author manu
 */
public class MakumbaApplication extends ApplicationWrapper {

    private Application wrapped;

    private DataHandler dataHandler;

    public MakumbaApplication(Application wrapped, DataHandler handler) {
        this.wrapped = wrapped;
        this.dataHandler = handler;
    }

    private UIComponent injectDataHanlder(UIComponent c) {
        if (c instanceof MakumbaDataComponent) {
            ((MakumbaDataComponent) c).setDataHandler(dataHandler);
        }
        return c;
    }

    @Override
    public Application getWrapped() {
        return wrapped;
    }

    @Override
    public UIComponent createComponent(FacesContext context, Resource componentResource) {
        return injectDataHanlder(super.createComponent(context, componentResource));
    }

    @Override
    public UIComponent createComponent(FacesContext context, String componentType, String rendererType) {
        return injectDataHanlder(super.createComponent(context, componentType, rendererType));
    }

    @Override
    public UIComponent createComponent(String componentType) throws FacesException {
        return injectDataHanlder(super.createComponent(componentType));
    }

    @Override
    public UIComponent createComponent(ValueBinding componentBinding, FacesContext context, String componentType)
            throws FacesException {
        return injectDataHanlder(super.createComponent(componentBinding, context, componentType));
    }

    @Override
    public UIComponent createComponent(ValueExpression componentExpression, FacesContext context, String componentType)
            throws FacesException {
        return injectDataHanlder(super.createComponent(componentExpression, context, componentType));
    }

    @Override
    public UIComponent createComponent(ValueExpression componentExpression, FacesContext context, String componentType,
            String rendererType) {
        return injectDataHanlder(super.createComponent(componentExpression, context, componentType, rendererType));
    }

}