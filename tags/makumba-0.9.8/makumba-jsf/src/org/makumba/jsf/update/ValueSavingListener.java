package org.makumba.jsf.update;

import javax.el.ELContextEvent;
import javax.el.ELContextListener;
import javax.faces.application.Application;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.faces.event.PostConstructApplicationEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;

import org.makumba.jsf.MakumbaDataContext;

/**
 * Listener that takes care of the initialization of the objects useful for the communication between makumba
 * ELResolvers and components, and of the processing of data actions (create, update, delete, ...) after
 * UPDATE_MODEL_VALUES
 * 
 * @author manu
 */
public class ValueSavingListener implements PhaseListener, ELContextListener, SystemEventListener {

    private static final long serialVersionUID = 2154307482562822044L;

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

    @Override
    public void beforePhase(PhaseEvent event) {

        // create the data handler before phase 4
        if (event.getPhaseId() == PhaseId.UPDATE_MODEL_VALUES) {
            getDataContext(event).setDataHandler(new MakumbaDataHandler());
        }
    }

    @Override
    public void afterPhase(PhaseEvent event) {
        if (event.getPhaseId() == PhaseId.UPDATE_MODEL_VALUES || event.getPhaseId() == PhaseId.INVOKE_APPLICATION) {
            getDataContext(event).getDataHandler().process();
        }

        // remove data handler at the end
        if (event.getPhaseId() == PhaseId.INVOKE_APPLICATION) {
            getDataContext(event).removeDataHandler();
        }

    }

    @Override
    public void contextCreated(ELContextEvent ece) {
        // register EL contexts here
        ece.getELContext().putContext(MakumbaDataContext.class, new MakumbaDataContext());
    }

    @Override
    public boolean isListenerForSource(Object source) {
        return source instanceof Application;
    }

    @Override
    public void processEvent(SystemEvent event) throws AbortProcessingException {
        if (event instanceof PostConstructApplicationEvent) {
            ((PostConstructApplicationEvent) event).getApplication().addELContextListener(this);
        }
    }

    private MakumbaDataContext getDataContext(PhaseEvent event) {
        return (MakumbaDataContext) event.getFacesContext().getELContext().getContext(MakumbaDataContext.class);
    }

}
