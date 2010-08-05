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

                if (isCreateObject(config.getTag())) {
                    return "makumbaObject";
                } else {
                    // TODO one-iteration mak:list
                    throw new RuntimeException("Not implemented.");
                }

            }
        });
    }

    @Override
    public void onComponentCreated(FaceletContext ctx, UIComponent c, UIComponent parent) {
        super.onComponentCreated(ctx, c, parent);

        if (isCreateObject(getTag())) {
            // TODO if we are not a NEW, we will be a List, but we need to tell that to the component
            // for this look into the WHERE tagAttribute

            if (c instanceof ObjectComponent) {
                ((ObjectComponent) c).setCreate(isCreateObject(getTag()));
            }
        }

    }

    private static boolean isCreateObject(Tag t) {
        String where = t.getAttributes().get("where").getValue();
        // FIXME this could also be a o.type = 'NEW'
        // FIXME this won't handle #{ValueExpressions}
        return where.indexOf("NEW") > -1;
    }

}
