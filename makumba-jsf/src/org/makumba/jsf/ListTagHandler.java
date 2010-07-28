package org.makumba.jsf;

import javax.faces.context.FacesContext;
import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.TagHandler;

/**
 * {@link TagHandler} for mak:list
 * 
 * @author manu
 */
public class ListTagHandler extends ComponentHandler {

    public static final String TAG_PREFIX = "org.makumba.tagPrefix";

    public ListTagHandler(ComponentConfig config) {
        super(config);
        // save the prefix for re-use in the list
        // it's safe to do it here as this constructor is only called once, when the view is compiled
        FacesContext.getCurrentInstance().getAttributes().put(TAG_PREFIX,
            getTag().getQName().substring(0, getTag().getQName().indexOf(":")));
    }
}