package org.makumba.jsf.update;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.makumba.Transaction;
import org.makumba.jsf.component.MakumbaDataComponent.Util;
import org.makumba.providers.TransactionProvider;

public class MakumbaDataHandler implements DataHandler {

    static final Logger log = java.util.logging.Logger.getLogger("org.makumba.jsf.update");

    private List<ObjectInputValue> values = new ArrayList<ObjectInputValue>();

    @Override
    public void process() {
        Transaction t = null;

        try {
            // TODO list db attribute
            t = TransactionProvider.getInstance().getConnectionToDefault();
            for (ObjectInputValue v : values) {
                v.processAndTreatExceptions(t);
            }

        } finally {
            if (Util.validationFailed() && t != null) {
                System.out.println("aborting due to errors, use <h:messages /> if you can't see them");
                for (FacesMessage m : FacesContext.getCurrentInstance().getMessageList()) {
                    System.out.println(m.getSummary());
                }
                t.rollback();
                FacesContext.getCurrentInstance().renderResponse();
            }
            if (t != null) {
                t.close();
            }
            // clear everything
            values.clear();
        }
    }

    @Override
    public List<ObjectInputValue> getValues() {
        return values;
    }

}
