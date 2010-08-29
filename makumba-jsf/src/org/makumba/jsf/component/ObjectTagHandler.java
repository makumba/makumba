package org.makumba.jsf.component;


import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletHandler;
import javax.faces.view.facelets.Tag;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagHandlerDelegate;

import org.makumba.jsf.component.MakumbaDataComponent.Util;

/**
 * This ComponentHandler dynamically creates the right component type depending on whether this is a create object or a
 * update object. This can change depending on a request parameter, therefore a {@link TagHandlerDelegate} with the
 * fitting configuration needs to be created / used.
 * 
 * @author manu
 */
public class ObjectTagHandler extends ComponentHandler {

    private TagHandlerDelegate createDelegate;

    private TagHandlerDelegate editDelegate;

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
    protected TagHandlerDelegate getTagHandlerDelegate() {
        if (isCreateObject(tag)) {
            if (createDelegate == null) {
                createDelegate = delegateFactory.createComponentHandlerDelegate(this);
            }
            return createDelegate;
        }
        if (!isCreateObject(tag)) {
            if (editDelegate == null) {
                editDelegate = delegateFactory.createComponentHandlerDelegate(this);
            }
            return editDelegate;
        }
        return null;
    }

    private static boolean isCreateObject(Tag t) {
        TagAttribute where = t.getAttributes().get("where");

        if (where != null) {
            String w = where.getValue();
            return Util.isCreateObject(w);
        }
        return false;
    }
}
