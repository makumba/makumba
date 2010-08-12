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

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.UPDATE_MODEL_VALUES;
    }

    @Override
    public void beforePhase(PhaseEvent event) {
        ObjectInputValue.dataHandler.set(new MakumbaDataHandler());
    }

    @Override
    public void afterPhase(PhaseEvent event) {
        ObjectInputValue.dataHandler.get().process();
        ObjectInputValue.dataHandler.remove();
    }

}
