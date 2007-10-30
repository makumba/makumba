package org.makumba.commons.attributes;

import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.makumba.LogicException;
import org.makumba.controller.ControllerHandler;

public class AttributesControllerHandler extends ControllerHandler {

    @Override
    public void afterFilter(ServletRequest request, ServletResponse response, FilterConfig conf) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean beforeFilter(ServletRequest request, ServletResponse response, FilterConfig conf) throws Exception {
        if (!checkAttributes(request, response))
            return false;
        
        return true;
    }

    @Override
    public boolean onError(ServletRequest request, ServletResponse response, Throwable e) {
        
        // we simply pass it on
        return true;
        
    }

    private boolean checkAttributes(ServletRequest req, ServletResponse resp) throws LogicException {
        RequestAttributes.getAttributes((HttpServletRequest) req);
        return true;
    }

    @Override
    public void finalize(ServletRequest request, ServletResponse response) {
        
    }

}
