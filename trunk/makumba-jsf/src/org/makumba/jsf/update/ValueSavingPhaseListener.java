package org.makumba.jsf.update;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

/**
 * Phase listener that persists all updated and created values to the database.<br>
 * 
 * @author manu
 */
public class ValueSavingPhaseListener implements PhaseListener {

    private static final long serialVersionUID = 2154307482562822044L;

    private DataHandler d;

    public ValueSavingPhaseListener(DataHandler d) {
        this.d = d;
    }

    @Override
    public void afterPhase(PhaseEvent event) {
        d.process();
    }

    @Override
    public void beforePhase(PhaseEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.UPDATE_MODEL_VALUES;
    }

}
