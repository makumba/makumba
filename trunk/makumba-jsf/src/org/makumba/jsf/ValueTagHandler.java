package org.makumba.jsf;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.TagHandler;

/**
 * {@link TagHandler} for mak:value tag
 * 
 * @author manu
 */
public class ValueTagHandler extends ComponentHandler {

    public ValueTagHandler(ComponentConfig config) {
        super(config);

        FacesContext context = FacesContext.getCurrentInstance();
        Application app = context.getApplication();
        app.addComponent("makumbaValue", "org.makumba.jsf.ValueComponent");
    }

}
