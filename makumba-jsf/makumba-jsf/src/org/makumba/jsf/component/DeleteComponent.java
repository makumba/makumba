package org.makumba.jsf.component;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import javax.el.ValueExpression;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.PhaseId;

import org.makumba.Pointer;
import org.makumba.ProgrammerError;
import org.makumba.jsf.update.ObjectInputValue;

public class DeleteComponent extends UIComponentBase implements ActionListener {

    private static final String OBJECT = "object";

    private ThreadLocal<DeleteAction> deferredDeleteActions = new ThreadLocal<DeleteComponent.DeleteAction>();

    @Override
    public String getFamily() {
        return "makumba";
    }

    private Object object;

    public Object getObject() {
        return this.object;
    }

    public void setObject(Object object) {
        // check object expression validity
        if (object instanceof String) {
            throw new ProgrammerError("Invalid EL expression '" + object + "', expecting #{expression}");
        }
        this.object = object;
    }

    @Override
    public void processUpdates(FacesContext context) {
        super.processUpdates(context);

        // FIXME this will not work, because processUpdates is never called, because when the commandButton is pressed
        // with 'immediate' it will never run processUpdates (but it fires the event in apply request values)
        // we keep it for the moment, we can use it somewhere else
        if (deferredDeleteActions.get() != null) {
            ObjectInputValue.makeDeleteInputValue(deferredDeleteActions.get().getLabel(),
                deferredDeleteActions.get().getPtr());
            deferredDeleteActions.remove();
        } else {
            System.out.println("no");
        }
    }

    @Override
    public void processAction(ActionEvent event) throws AbortProcessingException {
        if (event.getSource().equals(this.getParent())) {

            System.out.println("Delete event fired");

            // retrieve the bloody expression
            try {
                PropertyDescriptor[] pd = Introspector.getBeanInfo(this.getClass()).getPropertyDescriptors();
                for (PropertyDescriptor p : pd) {
                    if (p.getName().equals(OBJECT)) {
                        ValueExpression ve = this.getValueExpression(p.getName());
                        if (ve != null) {
                            String label = ve.getExpressionString().substring(2, ve.getExpressionString().length() - 1);
                            if (this.getAttributes().get(OBJECT) instanceof Pointer) {
                                DeleteAction delete = new DeleteAction(label,
                                        (Pointer) this.getAttributes().get(OBJECT));
                                // happens with immediate = true
                                if (this.getFacesContext().getCurrentPhaseId() == PhaseId.APPLY_REQUEST_VALUES) {
                                    this.deferredDeleteActions.set(delete);
                                } else {
                                    ObjectInputValue.makeDeleteInputValue(delete.getLabel(), delete.getPtr());
                                }
                            } else {
                                throw new ProgrammerError("You need to select the object to delete via .id");
                            }
                        }
                    }
                }
            } catch (IntrospectionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            //

        }
    }

    class DeleteAction {
        private String label;

        private Pointer ptr;

        public DeleteAction(String label, Pointer ptr) {
            super();
            this.label = label;
            this.ptr = ptr;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public Pointer getPtr() {
            return ptr;
        }

        public void setPtr(Pointer ptr) {
            this.ptr = ptr;
        }
    }
}
