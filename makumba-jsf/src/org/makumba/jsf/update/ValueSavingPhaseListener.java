package org.makumba.jsf.update;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;

import javax.el.ELException;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.makumba.DataDefinition;
import org.makumba.InvalidValueException;
import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.commons.GraphTS;
import org.makumba.jsf.ComponentDataHandler;
import org.makumba.jsf.component.MakumbaDataComponent;
import org.makumba.providers.TransactionProvider;

/**
 * Phase listener that persists all updated and created values to the database.<br>
 * 
 * @author manu
 */
public class ValueSavingPhaseListener implements PhaseListener, ComponentDataHandler {

    private static final long serialVersionUID = 1L;

    private ThreadLocal<Stack<String>> dataComponentStack = new ThreadLocal<Stack<String>>() {
        @Override
        protected java.util.Stack<String> initialValue() {
            return new Stack<String>();
        };
    };

    private ThreadLocal<GraphTS<String>> componentToplogyGraph = new ThreadLocal<GraphTS<String>>() {
        @Override
        protected GraphTS<String> initialValue() {
            return new GraphTS<String>();
        }
    };

    private ThreadLocal<HashMap<String, ArrayList<InputValue>>> valuesSet = new ThreadLocal<HashMap<String, ArrayList<InputValue>>>() {
        @Override
        protected HashMap<String, ArrayList<InputValue>> initialValue() {
            return new HashMap<String, ArrayList<InputValue>>();
        };
    };

    /* (non-Javadoc)
     * @see org.makumba.jsf.DataHandler#pushDataComponent(org.makumba.jsf.MakumbaDataComponent)
     */
    @Override
    public void pushDataComponent(MakumbaDataComponent c) {
        Stack<String> s = dataComponentStack.get();

        String parentKey = null;
        if (!s.isEmpty()) {
            parentKey = s.peek();
        }
        // we push the key and not the component to the stack as the getKey() may change (e.g. in the iterating list)
        s.push(c.getKey());

        // update the topology graph
        componentToplogyGraph.get().addVertex(c.getKey());
        if (parentKey != null) {
            componentToplogyGraph.get().addEdge(c.getKey(), parentKey);
        }

    }

    /* (non-Javadoc)
     * @see org.makumba.jsf.DataHandler#popDataComponent()
     */
    @Override
    public void popDataComponent() {
        Stack<String> s = dataComponentStack.get();
        if (!s.isEmpty()) {
            s.pop();
        }
    }

    @Override
    public void afterPhase(PhaseEvent event) {

        GraphTS<String> topologyGraph = this.componentToplogyGraph.get();
        topologyGraph.topo();
        System.out.println(topologyGraph.getSortedKeys());

        Transaction t = null;

        try {

            // TODO handle list DB attribute
            t = TransactionProvider.getInstance().getConnectionToDefault();

            for (String componentKey : topologyGraph.getSortedKeys()) {
                // we execute the actions inside of the context of one component, as this one has an impact on the order
                // in which inserts and updates need to be executed
                ArrayList<InputValue> values = this.valuesSet.get().get(componentKey);
                if (values != null && values.size() > 0) {

                    Map<DataDefinition, Dictionary<String, Object>> creates = new HashMap<DataDefinition, Dictionary<String, Object>>();
                    Map<Pointer, Dictionary<String, Object>> updates = new HashMap<Pointer, Dictionary<String, Object>>();

                    for (InputValue v : values) {
                        // FIXME in full postback all update values are transmitted here
                        // we should only update if the value did change
                        treatValue(v, creates, updates);
                    }

                    // execute the actions grouped by component
                    // TODO invoke makumba BL here instead of doing the operation directly

                    for (DataDefinition key : creates.keySet()) {
                        t.insert(key.getName(), creates.get(key));
                    }
                    for (Pointer key : updates.keySet()) {
                        t.update(key, updates.get(key));
                    }

                    t.commit();
                }
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
            dataComponentStack.get().clear();
            componentToplogyGraph.get().init();
            valuesSet.get().clear();
        }

    }

    private void treatValue(InputValue v, Map<DataDefinition, Dictionary<String, Object>> creates,
            Map<Pointer, Dictionary<String, Object>> updates) {
        switch (v.getCommand()) {
            case CREATE:

                // group by type
                Dictionary<String, Object> createData = creates.get(v.getType());
                if (createData == null) {
                    createData = new Hashtable<String, Object>();
                    creates.put(v.getType(), createData);
                }
                createData.put(v.getPath(), v.getValue());

                break;
            case UPDATE:

                // group by pointer
                Dictionary<String, Object> updateData = updates.get(v.getPointer());
                if (updateData == null) {
                    updateData = new Hashtable<String, Object>();
                    updateData.put(v.getPath(), v.getValue());
                    updates.put(v.getPointer(), updateData);
                }

                break;
        }
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

    @Override
    public void addInputValue(MakumbaDataComponent c, InputValue v) {
        Map<String, ArrayList<InputValue>> values = this.valuesSet.get();
        ArrayList<InputValue> list = values.get(c.getKey());
        if (list == null) {
            list = new ArrayList<InputValue>();
            values.put(c.getKey(), list);
        }
        list.add(v);
    }

    @Override
    public void addObjectInputValue(MakumbaDataComponent c, ObjectInputValue v) {
        // TODO Auto-generated method stub

    }

}
