package org.makumba.jsf;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.TagHandler;

/**
 * {@link TagHandler} for mak:list, may be needed later on for doing more
 * advanced stuff
 * 
 * @author manu
 * 
 */
public class ListTagHandler extends ComponentHandler {

    public ListTagHandler(ComponentConfig config) {
        super(config);
        FacesContext context = FacesContext.getCurrentInstance();
        Application app = context.getApplication();
        app.addComponent("makumbaList", "org.makumba.jsf.ListComponent");
    }

}
