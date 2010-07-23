package org.makumba.jsf;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
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

    public ListTagHandler(ComponentConfig config) {
        super(config);

        FacesContext context = FacesContext.getCurrentInstance();
        Application app = context.getApplication();
        app.addComponent("makumbaList", "org.makumba.jsf.UIRepeatListComponent");

        // example code to add a phase listener dynamically:

        // the trouble with adding the listener here is that it is too late and
        // it will miss events like root system events needed by {@link
        // AnalysisListener}

        // LifecycleFactory f = (LifecycleFactory) FactoryFinder
        // .getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        // for (Iterator<String> i = f.getLifecycleIds(); i.hasNext();) {
        // Lifecycle l = f.getLifecycle(i.next());
        //
        // PhaseListener listener = new ListPhaseListener();
        // l.removePhaseListener(listener);
        // l.addPhaseListener(listener);
        // }
    }

    @Override
    public void onComponentPopulated(FaceletContext ctx, UIComponent c, UIComponent parent) {
        // this method is called after rendering starts but before the
        // rendering finished, so it is a good moment to listen for analysis
        AnalysisListener.install(ctx.getFacesContext());
    }
}