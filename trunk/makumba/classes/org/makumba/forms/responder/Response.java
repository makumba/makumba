package org.makumba.forms.responder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This object holds data necessary for responding to a form. It is created by a Responder and used by the
 * ControllerFilter.
 * 
 * @author Manuel Gay
 * @version $Id: Response.java,v 1.1 03.10.2007 17:58:45 Manuel Exp $
 */
public class Response {
    
    private HttpServletRequest request;
    
    private HttpServletResponse response;
    
    private Exception exception;
    
    private String message;

    public Exception getException() {
        return exception;
    }

    public void setException(Exception e) {
        this.exception = e;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest req) {
        this.request = req;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public void setResponse(HttpServletResponse resp) {
        this.response = resp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
