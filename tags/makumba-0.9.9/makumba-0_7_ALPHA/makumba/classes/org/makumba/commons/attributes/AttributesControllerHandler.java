package org.makumba.commons.attributes;

import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.makumba.LogicException;
import org.makumba.commons.ControllerHandler;

public class AttributesControllerHandler extends ControllerHandler {

    @Override
    public boolean beforeFilter(ServletRequest request, ServletResponse response, FilterConfig conf) throws Exception {
        RequestAttributes.getAttributes((HttpServletRequest) request);
        return true;
    }

}
