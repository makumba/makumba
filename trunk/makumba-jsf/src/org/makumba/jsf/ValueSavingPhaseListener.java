package org.makumba.jsf;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.el.UpdateValue;
import org.makumba.providers.TransactionProvider;

/**
 * Phase listener that persists all updated and created values to the database
 * 
 * @author manu
 */
public class ValueSavingPhaseListener implements PhaseListener {

    private static final long serialVersionUID = 1L;

    @Override
    public void afterPhase(PhaseEvent event) {

        FacesContext facesContext = event.getFacesContext();
        facesContext.getViewRoot().visitTree(VisitContext.createVisitContext(facesContext), new VisitCallback() {

            @Override
            public VisitResult visit(VisitContext context, UIComponent target) {

                if (target instanceof UIRepeatListComponent) {
                    Map<Pointer, Map<String, UpdateValue>> values = ((UIRepeatListComponent) target).getUpdateValues();

                    if (values != null) {

                        Transaction t = null;

                        try {

                            // todo handle list DB attribute
                            t = TransactionProvider.getInstance().getConnectionToDefault();

                            for (Pointer p : values.keySet()) {
                                Map<String, UpdateValue> u = values.get(p);

                                Dictionary<String, Object> data = new Hashtable<String, Object>();
                                for (UpdateValue v : u.values()) {
                                    data.put(v.getPath(), v.getValue());
                                }

                                t.update(p, data);
                            }

                        } catch (Throwable e) {
                            t.rollback();
                            throw new RuntimeException(e);
                        } finally {
                            if (t != null) {
                                t.close();
                            }
                        }

                    }
                }

                return VisitResult.ACCEPT;
            }
        });

    }

    @Override
    public void beforePhase(PhaseEvent event) {

    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.UPDATE_MODEL_VALUES;
    }

}
