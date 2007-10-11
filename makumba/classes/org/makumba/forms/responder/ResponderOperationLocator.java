package org.makumba.forms.responder;

import java.util.HashMap;
import java.util.Map;

import org.makumba.forms.tags.AddTag;
import org.makumba.forms.tags.DeleteTag;
import org.makumba.forms.tags.EditTag;
import org.makumba.forms.tags.FormTagBase;
import org.makumba.forms.tags.NewTag;

/**
 * This class is a locator for responder operations, based on the name of the operation. For the moment it only locates
 * well-known internal Makumba forms operations, but in the future this might become the basis for locating custom user
 * operations.
 * 
 * TODO this is really stupid code, because we know exactly what we're looking for. instead of defining a
 * whole ResponderOperation object, the FormTag classes should define an execution frame (with their particular
 * behaviour), and some object (ResponderOperationFactory) would compute the ResponderOperation based on what the form
 * tag classes give it plus what it knows from other interested parties (e.g. the Logic) who would just register some
 * methods to the factory.
 * 
 * @author Manuel Gay
 * @version $Id: ResponderOperationLocation.java,v 1.1 11.10.2007 16:10:44 Manuel Exp $
 */
public class ResponderOperationLocator {

    private static Map<String, ResponderOperation> responderOperations = new HashMap<String, ResponderOperation>();

    /**
     * Initalises the locator. In our simple case, just places the responder operations in a map for further retrival
     */
    private static void init() {
        responderOperations.put("new", NewTag.getResponderOperation("new"));
        responderOperations.put("edit", EditTag.getResponderOperation("edit"));
        responderOperations.put("add", AddTag.getResponderOperation("add"));
        responderOperations.put("addToNew", AddTag.getResponderOperation("addToNew"));
        responderOperations.put("simple", FormTagBase.getResponderOperation("simple"));
        responderOperations.put("deleteLink", DeleteTag.getResponderOperation("deleteLink"));
        responderOperations.put("deleteForm", DeleteTag.getResponderOperation("deleteForm"));
    }

    static {
        init();
    }

    /**
     * Locates a responder operation based on the name of the operation.
     * 
     * @param operation
     *            the name of the operation
     * @return a {@link ResponderOperation} object corresponding to the requested operation
     */
    public static ResponderOperation locate(String operation) {

        // since we know exactly where each operation is performed, we don't need to do anything very special
        return responderOperations.get(operation);
    }

}
