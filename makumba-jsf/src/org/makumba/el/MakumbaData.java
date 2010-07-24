package org.makumba.el;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Wrapper for makumba data, used by the {@link MakumbaELResolver} to identify its data
 * 
 * @author manu
 */
public class MakumbaData {

    private static final long serialVersionUID = 1L;

    private Map<String, Object> wrapped;

    public MakumbaData() {
        wrapped = new LinkedHashMap<String, Object>();
    }

    public Map<String, Object> getWrapped() {
        return wrapped;
    }

}
