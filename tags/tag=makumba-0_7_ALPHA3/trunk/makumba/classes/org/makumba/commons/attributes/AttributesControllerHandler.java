package org.makumba.commons.attributes;

import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.makumba.commons.ControllerHandler;
import org.makumba.commons.ServletObjects;

public class AttributesControllerHandler extends ControllerHandler {

    @Override
    public boolean beforeFilter(ServletRequest request, ServletResponse response, FilterConfig conf, ServletObjects httpServletObjects) throws Exception {
        RequestAttributes.getAttributes((HttpServletRequest) request);
        return true;
    }

}
