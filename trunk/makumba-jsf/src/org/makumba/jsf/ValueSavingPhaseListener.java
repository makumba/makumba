package org.makumba.jsf;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import javax.el.ELException;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.makumba.DataDefinition;
import org.makumba.InvalidValueException;
import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.el.CreateValue;
import org.makumba.el.UpdateValue;
import org.makumba.providers.TransactionProvider;

/**
 * Phase listener that persists all updated and created values to the database.<br>
 * TODO topological sort<br>
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

                            // TODO handle list DB attribute
                            t = TransactionProvider.getInstance().getConnectionToDefault();

                            for (Pointer p : values.keySet()) {
                                Map<String, UpdateValue> u = values.get(p);

                                Dictionary<String, Object> data = new Hashtable<String, Object>();
                                for (UpdateValue v : u.values()) {
                                    data.put(v.getPath(), v.getValue());
                                }

                                t.update(p, data);
                            }

                            ((UIRepeatListComponent) target).getUpdateValues().clear();
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

                if (target instanceof CreateObjectComponent) {
                    Map<DataDefinition, Map<String, CreateValue>> values = ((CreateObjectComponent) target).getCreateValues();

                    if (values != null) {

                        Transaction t = null;

                        try {

                            // TODO handle list DB attribute
                            t = TransactionProvider.getInstance().getConnectionToDefault();

                            for (DataDefinition type : values.keySet()) {
                                Map<String, CreateValue> u = values.get(type);

                                Dictionary<String, Object> data = new Hashtable<String, Object>();
                                for (CreateValue v : u.values()) {
                                    data.put(v.getPath(), v.getValue());
                                }

                                t.insert(type.getName(), data);
                            }

                        } catch (InvalidValueException e) {
                            t.rollback();
                            // TODO: store the type (MDD) in the InvalidValueException
                            // TODO: store the offending value in the IVE
                            // TODO: from the type, and the field name, find the label.ptr.field that edits such a field
                            // after that, find the clientId of the UIInput(s) that produced the value
                            // for each such input, register a message

                            // TODO: if the above is hard, at least include the clientId of the form, as below.
                            FacesContext.getCurrentInstance().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage()));
                            throw new ELException(e);
                        } catch (Throwable e) {
                            t.rollback();
                            // TODO: we cannot detect which field provoked this, but we could insert the clientId of the
                            // form
                            FacesContext.getCurrentInstance().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage()));
                            throw new ELException(e);
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
        // TODO: i think we can perform multi-field validation here (or after PROCESS_VALIDATION)
        // if that fails, we can simply add messages to the FacesContext, and throw an exception

    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.UPDATE_MODEL_VALUES;
    }

}
