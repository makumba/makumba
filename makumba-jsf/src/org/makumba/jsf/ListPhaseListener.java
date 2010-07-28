package org.makumba.jsf;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

/**
 * A phase listener example. It needs a component listener (see {@link AnalysisListener}) to actually know when analysis
 * can start, but it may be useful for other purposes. TODO: Currently this is not in use as it needs a faces-config
 * declaration which we can do without by installing the AnalysisListener some other way.
 * 
 * @author cristi
 */
public class ListPhaseListener implements PhaseListener {
    @Override
    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

    @Override
    public void beforePhase(PhaseEvent event) {
        System.out.println("before " + event.getPhaseId());
    }

    @Override
    public void afterPhase(PhaseEvent event) {
        // System.out.println("after " + event.getPhaseId());

        if (event.getPhaseId().equals(PhaseId.RESTORE_VIEW)) {
            // at this point (and also at the start of RENDER_RESPONSE) the view
            // exists but it has no children
            // therefore we need a listener that tells us when the view is ready
            AnalysisListener.install(event.getFacesContext());
        }
    }
}