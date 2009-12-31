///////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003  http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//  -------------
//  $Id$
//  $Name$
/////////////////////////////////////

package org.makumba.commons;

import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.makumba.controller.http.ControllerFilter;

/**
 * This is an abstraction of the filter mechanism. It emulates the behaviour of a filter so that different handlers can
 * be defined which perform specific operations, without having to create real filters.
 * 
 * @author Manuel Gay
 * @version $Id$
 */
public abstract class ControllerHandler {
    /**
     * Performs an operation before the doFilterChain() method is called
     * 
     * @return <code>true</code> if the operation worked out successfully, <code>false</code> otherwise
     * @throws Exception
     */
    public boolean beforeFilter(ServletRequest request, ServletResponse response, FilterConfig conf,
            ServletObjects httpServletObjects) throws Exception {
        return true;
    }

    /**
     * Performs an operation before the doFilterChain() method is called, but after all
     * {@link #beforeFilter(ServletRequest, ServletResponse, FilterConfig, ServletObjects)} methods of all controller
     * handlers registered in {@link ControllerFilter} have been called
     */
    public void afterBeforeFilter(ServletRequest request, ServletResponse response, FilterConfig conf) throws Exception {
    }

    /**
     * Performs an operation after the doFilterChain() method is called
     */
    public void afterFilter(ServletRequest request, ServletResponse response, FilterConfig conf) {
    }

    /**
     * Performs an operation when an error occurs
     * 
     * @return <code>true</code> if this still is an error, <code>false</code> if it was handled
     */
    public boolean onError(ServletRequest request, ServletResponse response, Throwable e, FilterConfig conf) {
        return true;
    }

    /**
     * Performs cleanup operations after the filter operation is executed
     */
    public void finalize(ServletRequest request, ServletResponse response) {
    }

}
