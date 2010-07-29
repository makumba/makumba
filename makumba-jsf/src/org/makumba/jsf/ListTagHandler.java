package org.makumba.jsf;

import javax.faces.component.UIComponent;
import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;
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
    }

    @Override
    public void onComponentCreated(FaceletContext ctx, UIComponent c, UIComponent parent) {
        super.onComponentCreated(ctx, c, parent);
        ((UIRepeatListComponent) c).setPrefix(getTag().getQName().substring(0, getTag().getQName().indexOf(":")));
    }

}