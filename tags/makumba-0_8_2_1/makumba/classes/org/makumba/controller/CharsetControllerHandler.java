package org.makumba.controller;

import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.makumba.commons.ControllerHandler;
import org.makumba.commons.ServletObjects;
import org.makumba.providers.Configuration;

/**
 * Sets the utf8 charset encoding if enabled in the datasource configuration
 * @author Marius Andra
 * @author Manuel Gay
 * @version $Id: CharsetControllerHandler.java,v 1.1 Sep 23, 2009 3:55:49 PM manu Exp $
 */
public class CharsetControllerHandler extends ControllerHandler {
    
    private String encoding = Configuration.getDefaultDataSourceConfiguration().get("encoding");
    
    @Override
    public boolean beforeFilter(ServletRequest request, ServletResponse response, FilterConfig conf,
            ServletObjects httpServletObjects) throws Exception {
        
        if (encoding != null && encoding.equalsIgnoreCase("utf8")) {
            request.setCharacterEncoding("UTF-8");
            response.setContentType("text/html;charset=UTF-8");
        }

        return true;
        
    }

}
