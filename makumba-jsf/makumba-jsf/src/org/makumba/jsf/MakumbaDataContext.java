package org.makumba.jsf;

import javax.el.ELResolver;
import javax.faces.context.FacesContext;

import org.makumba.jsf.component.CreateObjectComponent;
import org.makumba.jsf.component.MakumbaDataComponent;
import org.makumba.jsf.component.UIRepeatListComponent;
import org.makumba.jsf.update.DataHandler;

/**
 * Context object allowing makumba {@link ELResolver}-s to communicate with {@link MakumbaDataComponent}-s, so as to
 * share what's the currently running component. The implementation of this context must be threadsafe according to JSF.
 * 
 * @author manu
 */
public class MakumbaDataContext {

    private ThreadLocal<UIRepeatListComponent> currentList = new ThreadLocal<UIRepeatListComponent>();

    private ThreadLocal<CreateObjectComponent> currentCreateObject = new ThreadLocal<CreateObjectComponent>();

    // TODO probably should move to a logic context
    private ThreadLocal<DataHandler> dataHandler = new ThreadLocal<DataHandler>();

    public UIRepeatListComponent getCurrentList() {
        return currentList.get();
    }

    public void setCurrentList(UIRepeatListComponent list) {
        currentList.set(list);
    }

    public CreateObjectComponent getCurrentCreateObject() {
        return currentCreateObject.get();
    }

    public void setCurrentCreateObject(CreateObjectComponent c) {
        currentCreateObject.set(c);
    }

    public void setDataHandler(DataHandler dh) {
        dataHandler.set(dh);
    }

    public DataHandler getDataHandler() {
        return dataHandler.get();
    }

    public void removeDataHandler() {
        dataHandler.remove();
    }

    /**
     * Gets the data context for this request
     * 
     * @return the {@link MakumbaDataContext} for the current request
     */
    public static MakumbaDataContext getDataContext() {
        return (MakumbaDataContext) FacesContext.getCurrentInstance().getELContext().getContext(
            MakumbaDataContext.class);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        currentCreateObject.remove();
        currentList.remove();
        dataHandler.remove();
    }

}
