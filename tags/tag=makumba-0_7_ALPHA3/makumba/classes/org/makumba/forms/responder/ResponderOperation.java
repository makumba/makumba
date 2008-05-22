package org.makumba.forms.responder;

import javax.servlet.http.HttpServletRequest;
import org.makumba.LogicException;
import org.makumba.commons.DbConnectionProvider;
import org.makumba.commons.attributes.RequestAttributes;

/**
 * this class helps to differentiate between the different types of forms
 * 
 * @author Cristian Bogdan
 * @author Manuel Gay
 */
public abstract class ResponderOperation implements java.io.Serializable {
    /** respond to the given request, with the data from the given responder, read using the given multiple form suffix */
    public abstract Object respondTo(HttpServletRequest req, Responder resp, String suffix, String parentSuffix)
            throws LogicException;

    /** check the validity of the given responder data, return not-null if there is a problem */
    public abstract String verify(Responder resp);

    /** gets the transaction provider **/
    public DbConnectionProvider getConnectionProvider(HttpServletRequest req, Object controller) {
        return RequestAttributes.getConnectionProvider(req);

    }
}
