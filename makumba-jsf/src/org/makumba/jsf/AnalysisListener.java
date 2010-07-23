/*
 * Created on Jul 23, 2010
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.makumba.jsf;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.event.PreRenderViewEvent;

/**
 * When the root of a view becomes available, this listener will determine the
 * moment when all its children are in place, and invoke the analysis on all
 * mak:list root tags.
 * 
 * To install the listener on a context, call {@link #install(FacesContext)}.
 * The context is expected to have a root by that time.
 * 
 * @author cristi
 */
class AnalysisListener implements ComponentSystemEventListener {

    private static final String GUARD = "org.makumba.jsf.analysisListener";
    static private final AnalysisListener signleton = new AnalysisListener();

    /**
     * Install the listener for the given context. This method does nothing if
     * the listener is already installed on that context.
     * 
     * @param context
     *            a FacesContext that is known to have a root view
     * */
    static void install(FacesContext context) {
        if (context.getViewRoot().getAttributes().get(GUARD) == null) {
            context.getViewRoot().subscribeToEvent(
            // TODO: I have no clue which event is better to listen for.
                    // Maybe there are more.
                    // In any case, a _concrete_ event class is required
                    // here.
                    // ComponentSystemEvent will not do
                    PreRenderViewEvent.class, signleton);
            context.getViewRoot().getAttributes().put(GUARD, "");
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processEvent(ComponentSystemEvent ev)
            throws AbortProcessingException {
        analyzeRootMakLists((UIComponent) ev.getSource());
    }

    /** recursive search for root mak:lists. call their analyze() methods */
    private static void analyzeRootMakLists(UIComponent comp) {
        if (comp instanceof UIRepeatListComponent)
            ((UIRepeatListComponent) comp).analyze();
        else
            for (UIComponent kid : comp.getChildren())
                analyzeRootMakLists(kid);
    }

}