/*
 * Created on Apr 1, 2011
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.makumba.list.engine;

import org.makumba.UnauthorizedException;
import org.makumba.list.engine.ComposedQueryAuthorization.AuthorizationInfo;

public class QueryUnauthorizedException extends UnauthorizedException {

    private static final long serialVersionUID = 1L;

    // private AuthorizationInfo ai;

    // private Object object;

    public QueryUnauthorizedException(AuthorizationInfo ai, Object object) {
        super(makeMessage(ai, object));
        // this.ai = ai;
        // this.object = object;

    }

    public static String makeMessage(AuthorizationInfo ai, Object object) {
        return "canRead() failed for: " + ai.expr + ", on object " + object + ", message: " + ai.message
                + ", in query: " + ai.query;
    }
}
