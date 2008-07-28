package org.makumba.commons.attributes;

import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.makumba.commons.ControllerHandler;
import org.makumba.commons.ServletObjects;

public class DatabaseConnectionControllerHandler extends ControllerHandler {

    @Override
    public boolean beforeFilter(ServletRequest request, ServletResponse response, FilterConfig conf, ServletObjects httpServletObjects) throws Exception {
        // we make sure it's initialized, this is not actually needed
        RequestAttributes.getConnectionProvider((HttpServletRequest) request);
        return true;
    }


    @Override
    public void afterBeforeFilter(ServletRequest request, ServletResponse response, FilterConfig conf) {
        finalize(request, response);
    }

    @Override
    public void finalize(ServletRequest request, ServletResponse response) {
        RequestAttributes.getConnectionProvider((HttpServletRequest) request).close();
    }

}
