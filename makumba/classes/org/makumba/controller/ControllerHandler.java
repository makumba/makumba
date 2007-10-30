package org.makumba.controller;

import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * This is an abstraction of the filter mechanism. It emulates the behaviour of a filter so that different handlers can
 * be defined which perform specific operations, without having to create real filters.
 * 
 * @author Manuel Gay
 * @version $Id: ControllerHandler.java,v 1.1 22.10.2007 13:38:15 Manuel Exp $
 */
public abstract class ControllerHandler {
    
    /**
     * Performs an operation before the doFilterChain() method is called
     * @return <code>true</code> if the operation worked out successfully, <code>false</code> otherwise
     * @throws Exception
     */
    public abstract boolean beforeFilter(ServletRequest request, ServletResponse response, FilterConfig conf) throws Exception;
    
    /**
     * Performs an operation after the doFilterChain() method is called
     */
    public abstract void afterFilter(ServletRequest request, ServletResponse response, FilterConfig conf);
    
    /**
     * Performs an operation when an error occurs
     * @return <code>true</code> if this still is an error, <code>false</code> if it was handled
     */
    public abstract boolean onError(ServletRequest request, ServletResponse response, Throwable e);
    
    /**
     * Performs cleanup operations after the filter operation is executed
     */
    public abstract void finalize(ServletRequest request, ServletResponse response);

}
