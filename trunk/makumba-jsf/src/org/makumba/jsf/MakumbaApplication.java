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

@SuppressWarnings("deprecation")
/**
 * Decorator for the JSF Application that sets a ComponentDataHandler on all MakumbaDataComponent-s
 * 
 * @author manu
 */
public class MakumbaApplication extends ApplicationWrapper {

    private Application wrapped;

    private ComponentDataHandler componentDataHandler;

    public MakumbaApplication(Application wrapped, ComponentDataHandler handler) {
        this.wrapped = wrapped;
        this.componentDataHandler = handler;
    }

    private UIComponent injectDataHanlder(UIComponent c) {
        if (c instanceof MakumbaDataComponent) {
            ((MakumbaDataComponent) c).setDataHandler(componentDataHandler);
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