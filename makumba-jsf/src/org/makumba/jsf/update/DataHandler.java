package org.makumba.jsf.update;

import java.util.List;

public interface DataHandler {
    public List<ObjectInputValue> getValues();

    public abstract void process();

}