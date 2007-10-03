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
    
    private HttpServletRequest req;
    
    private HttpServletResponse resp;
    
    private Exception e;

    public Exception getE() {
        return e;
    }

    public void setE(Exception e) {
        this.e = e;
    }

    public HttpServletRequest getReq() {
        return req;
    }

    public void setReq(HttpServletRequest req) {
        this.req = req;
    }

    public HttpServletResponse getResp() {
        return resp;
    }

    public void setResp(HttpServletResponse resp) {
        this.resp = resp;
    }

}
