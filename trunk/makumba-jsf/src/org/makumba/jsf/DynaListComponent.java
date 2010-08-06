/*
 * Created on Jul 23, 2010
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.makumba.jsf;

import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.FaceletContext;

import com.sun.faces.facelets.component.UIRepeat1;

public class DynaListComponent extends UINamingContainer {
    UIComponent wrapped;
    Object value;
    private String var;

    public void populated(FaceletContext ctx, UIComponent parent) {
        FacesContext faces = ctx.getFacesContext();
        wrapped = faces.getApplication().createComponent("facelets.ui.Repeat");
        wrapped.setParent(this);
        wrapped.getChildren().addAll(getChildren());
        getChildren().clear();
        getChildren().add(wrapped);
        ((UIRepeat1) wrapped).setValue(value);
        ((UIRepeat1) wrapped).setVar(var);
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setVar(String var) {
        this.var = var;
    }

    @Override
    public String getFamily() {
        return "makumba";
    }

}
