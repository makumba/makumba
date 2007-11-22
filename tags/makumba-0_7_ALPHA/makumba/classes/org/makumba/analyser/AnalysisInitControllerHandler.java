package org.makumba.analyser;

import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.makumba.commons.ControllerHandler;

/**
 * This {@link ControllerHandler} ensures that the analysis is initialised correctly at each access
 * 
 * @author Manuel Gay
 * @version $Id: AnalysisInitControllerHandler.java,v 1.1 21.11.2007 11:22:24 Manuel Exp $
 */
public class AnalysisInitControllerHandler extends ControllerHandler {

    @Override
    public boolean beforeFilter(ServletRequest request, ServletResponse response, FilterConfig conf) {

        AnalysableTag.initializeThread();
        return true;
    }
    
    @Override
    public boolean onError(ServletRequest request, ServletResponse response, Throwable e) {
        AnalysableTag.initializeThread();
        return true;
    }
    
}
