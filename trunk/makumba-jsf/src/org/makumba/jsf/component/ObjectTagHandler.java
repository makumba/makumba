package org.makumba.jsf.component;

import javax.faces.component.UIComponent;
import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletHandler;
import javax.faces.view.facelets.Tag;
import javax.faces.view.facelets.TagAttribute;

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
                    return "makumbaCreateObject";
                } else {
                    return "makumbaObject";
                }

            }
        });
    }

    @Override
    public void onComponentCreated(FaceletContext ctx, UIComponent c, UIComponent parent) {
        super.onComponentCreated(ctx, c, parent);

        if (!isCreateObject(getTag())) {
            ((UIRepeatListComponent) c).setObject(true);
        }

    }

    private static boolean isCreateObject(Tag t) {
        TagAttribute where = t.getAttributes().get("where");
        // FIXME this could also be a o.type = 'NEW'
        // FIXME this won't handle #{ValueExpressions}
        return where != null && where.getValue().indexOf("NEW") > -1;
    }

}
