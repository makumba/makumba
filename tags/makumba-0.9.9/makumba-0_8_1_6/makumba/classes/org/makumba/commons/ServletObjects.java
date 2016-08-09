package org.makumba.commons;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * A wrapper to pass on {@link ServletRequest} and {@link ServletResponse}, used on the {@link ControllerHandler}, to
 * allow implementing classes to modify them and be subsequently used in the {@link FilterChain}.
 * 
 * @author Rudolf Mayer
 * @version $Id: ServletObjects.java,v 1.1 May 18, 2008 2:26:49 PM rudi Exp $
 */
public class ServletObjects {
    protected ServletRequest request;

    protected ServletResponse response;

    public ServletObjects(ServletRequest request, ServletResponse response) {
        this.request = request;
        this.response = response;
    }

    public ServletRequest getRequest() {
        return request;
    }

    public ServletResponse getResponse() {
        return response;
    }

    public void setRequest(ServletRequest request) {
        this.request = request;
    }

    public void setResponse(ServletResponse response) {
        this.response = response;
    }
}