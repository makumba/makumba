package org.makumba.controller;

import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.makumba.commons.ControllerHandler;
import org.makumba.commons.ServletObjects;

/**
 * ControllerHandler that modifies the response text
 * 
 * @author Manuel Gay
 * @version $Id: ResponseModifierControllerHandler.java,v 1.1 Dec 25, 2009 10:05:55 PM manu Exp $
 */
public class ResponseModifierControllerHandler extends ControllerHandler {
    
    private static ResponseModifierControllerHandler instance;
    
    /** this is not a singleton pattern, it's just a hack to allow tags to get a hand on this handler and influence its behavior **/
    public static ResponseModifierControllerHandler getInstance() {
        return instance;
    }
    
    public ResponseModifierControllerHandler() {
        instance = this;
    }
    
    @Override
    public boolean beforeFilter(ServletRequest request, ServletResponse response, FilterConfig conf,
            ServletObjects httpServletObjects) throws Exception {
        
        // hijack the response output stream so that in afterFilter we can have fun with it
        // HOW??
        
        return true;
    }
    
    @Override
    public void afterFilter(ServletRequest request, ServletResponse response, FilterConfig conf) {
        
        
    }
    
    

}
