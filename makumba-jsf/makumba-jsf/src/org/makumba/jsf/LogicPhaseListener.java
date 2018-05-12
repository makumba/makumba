package org.makumba.jsf;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;

import org.makumba.LogicException;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.commons.attributes.RequestAttributes;

/**
 * Detect when to apply Logic and authentication/authorization.
 * 
 * @author cristi
 */
public class LogicPhaseListener implements PhaseListener {

    private static final long serialVersionUID = 1L;

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

    @Override
    public void beforePhase(PhaseEvent event) {
        System.out.println("before " + event.getPhaseId());
        if (event.getPhaseId() == PhaseId.RESTORE_VIEW) {
            try {
                RequestAttributes.getAttributes((HttpServletRequest) event.getFacesContext().getExternalContext().getRequest());
            } catch (LogicException e) {
                throw new RuntimeWrappedException(e);
            }
        }
    }

    @Override
    public void afterPhase(PhaseEvent event) {
    }
}