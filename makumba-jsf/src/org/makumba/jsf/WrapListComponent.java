/*
 * Created on Jul 23, 2010
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.makumba.jsf;

import java.io.IOException;

import javax.faces.FacesException;
import javax.faces.component.ContextCallback;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesEvent;

import com.sun.faces.facelets.component.UIRepeat1;

public class WrapListComponent extends UINamingContainer {
    UINamingContainer wrapped = new com.sun.faces.facelets.component.UIRepeat1();

    public WrapListComponent() {
        this.setRendererType(wrapped.getRendererType());
        ((UIRepeat1) wrapped).setValue(new Object[] { "a", "b", "c" });
        ((UIRepeat1) wrapped).setVar("myVariable");

    }

    @Override
    public void setParent(UIComponent parent) {
        wrapped.setParent(parent);
        for (UIComponent kid : getChildren()) {
            kid.setParent(wrapped);
        }
    }

    public void setValue(Object o) {
        System.out.println(o);
        ((UIRepeat1) wrapped).setValue(o);
    }

    public void setVar(String s) {
        ((UIRepeat1) wrapped).setVar(s);
    }

    @Override
    public String getFamily() {
        return wrapped.getFamily();
    }

    @Override
    public String getClientId(FacesContext faces) {
        return wrapped.getClientId();
    }

    @Override
    public boolean invokeOnComponent(FacesContext faces, String clientId,
            ContextCallback callback) throws FacesException {
        return wrapped.invokeOnComponent(faces, clientId, callback);
    }

    @Override
    public void processDecodes(FacesContext faces) {
        wrapped.processDecodes(faces);
    }

    @Override
    public void processUpdates(FacesContext faces) {
        wrapped.processUpdates(faces);
    }

    @Override
    public void processValidators(FacesContext faces) {
        wrapped.processValidators(faces);
    }

    @Override
    public void broadcast(FacesEvent event) throws AbortProcessingException {
        wrapped.broadcast(event);
    }

    @Override
    public void queueEvent(FacesEvent event) {
        wrapped.queueEvent(event);
    }

    @Override
    public void restoreState(FacesContext faces, Object object) {
        wrapped.restoreState(faces, object);
    }

    @Override
    public Object saveState(FacesContext faces) {
        return wrapped.saveState(faces);
    }

    @Override
    public void encodeChildren(FacesContext faces) throws IOException {
        wrapped.encodeChildren(faces);
    }

    @Override
    public boolean getRendersChildren() {
        return wrapped.getRendersChildren();
    }
}
