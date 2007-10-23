package org.makumba.controller;

import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.makumba.commons.attributes.RequestAttributes;

public class DatabaseConnectionControllerHandler extends ControllerHandler {
    
    private DbConnectionProvider dbcp;

    @Override
    public void afterFilter(ServletRequest request, ServletResponse response, FilterConfig conf) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean beforeFilter(ServletRequest request, ServletResponse response, FilterConfig conf) throws Exception {
        // initalises a database pool (one connection per database) needed for the response (BL execution) and the attributes (BL attribute lookup)
        dbcp = RequestAttributes.getConnectionProvider((HttpServletRequest) request);
        return true;
    }

    @Override
    public void finalize(ServletRequest request, ServletResponse response) {
        dbcp.close();
        
    }

    @Override
    public boolean onError(ServletRequest request, ServletResponse response, Exception e) {
        // we simply forward it
        return true;
    }

}
