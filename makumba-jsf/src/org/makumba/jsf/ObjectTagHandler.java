package org.makumba.jsf;

import javax.faces.component.UIComponent;
import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletHandler;
import javax.faces.view.facelets.Tag;

public class ObjectTagHandler extends ComponentHandler {

    public ObjectTagHandler(final ComponentConfig config) {
        super(new ComponentConfig() {

            @Override
            public String getTagId() {
                return config.getTagId();
            }

            @Override
            public Tag getTag() {
                return config.getTag();
            }

            @Override
            public FaceletHandler getNextHandler() {
                return config.getNextHandler();
            }

            @Override
            public String getRendererType() {
                return config.getRendererType();
            }

            @Override
            public String getComponentType() {
                // TODO depending on whether we have a NEW or not return different components
                return "makumbaObject";
            }
        });
    }

    @Override
    public void onComponentCreated(FaceletContext ctx, UIComponent c, UIComponent parent) {
        super.onComponentCreated(ctx, c, parent);

        // TODO if we are not a NEW, we will be a List, but we need to tell that to the component
        // for this look into the WHERE tagAttribute

    }

}
