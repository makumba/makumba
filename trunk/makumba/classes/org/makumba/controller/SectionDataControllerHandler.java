package org.makumba.controller;

import java.io.IOException;
import java.util.Map;

import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.makumba.commons.ControllerHandler;
import org.makumba.commons.ServletObjects;
import org.makumba.commons.json.JSONObject;
import org.makumba.list.tags.SectionTag;

/**
 * ControllerHandler that writes the section data as JSON string
 * 
 * @author Manuel Gay
 * @version $Id: ResponseModifierControllerHandler.java,v 1.1 Dec 25, 2009 10:05:55 PM manu Exp $
 */
public class SectionDataControllerHandler extends ControllerHandler {
    
    private static SectionDataControllerHandler instance;
    
    /** this is not a singleton pattern, it's just a hack to allow tags to get a hand on this handler and influence its behavior **/
    public static SectionDataControllerHandler getInstance() {
        return instance;
    }
    
    public SectionDataControllerHandler() {
        instance = this;
    }
    
    @Override
    public boolean beforeFilter(ServletRequest request, ServletResponse response, FilterConfig conf,
            ServletObjects httpServletObjects) throws Exception {
        
        return true;
    }
    
    @Override
    public void afterFilter(ServletRequest request, ServletResponse response, FilterConfig conf) {
        HttpServletRequest req = (HttpServletRequest) request;
        
        String event = request.getParameter(SectionTag.MAKUMBA_EVENT);
        
        if(event != null) {
            
            response.reset();
            response.setContentType("application/json");
            
            // fetch data from request context
            Map<String, String> data = (Map<String, String>) req.getAttribute(SectionTag.MAKUMBA_EVENT + "###" + event);
            try {
                response.getWriter().append(new JSONObject(data).toString());
                response.getWriter().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
