package org.makumba.jsf.component;

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;

import org.makumba.ProgrammerError;

public class DeleteTagHandler extends ComponentHandler {

    public DeleteTagHandler(ComponentConfig config) {
        super(config);
    }

    @Override
    public void onComponentCreated(FaceletContext ctx, UIComponent c, UIComponent parent) {
        if (parent instanceof UICommand) {
            ((UICommand) parent).addActionListener((DeleteComponent) c);
            if (((UICommand) parent).isImmediate()) {
                // TODO maybe we can support this later
                throw new ProgrammerError(
                        "UICommand components having a nested mak:delete cannot be submitted in immediate mode");
            }
        } else {
            throw new ProgrammerError(
                    "mak:delete can only be nested in UICommand subclasses such as h:commandButton and h:commandLink");
        }
    }
}
