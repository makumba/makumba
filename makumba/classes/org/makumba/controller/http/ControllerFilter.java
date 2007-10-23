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

package org.makumba.controller.http;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.makumba.commons.RuntimeWrappedException;
import org.makumba.commons.attributes.AttributesControllerHandler;
import org.makumba.controller.ControllerHandler;
import org.makumba.controller.DatabaseConnectionControllerHandler;
import org.makumba.controller.ErrorControllerHandler;
import org.makumba.controller.FilterConditionControllerHandler;
import org.makumba.forms.responder.ResponseControllerHandler;

/**
 * The filter that controls each makumba HTTP access. Performs login, form response, exception handling.
 * 
 * This filter uses a number of {@link ControllerHandler}-s which each serve a specific purpose.
 * 
 * TODO the different handlers should be invoked in a more generic way
 * TODO there should be a simple mechanism of deactivating one of the handlers, e.g. an invocation list
 * read from the filter configuration and by default calling all the handlers
 * 
 * @author Cristian Bogdan
 * @author Rudolf Mayer
 * @author Manuel Gay
 * @version $Id$ *
 */
public class ControllerFilter implements Filter {
    
    public static final String ORIGINAL_REQUEST = "org.makumba.originalRequest";

    private static FilterConfig conf;
    
    private ControllerHandler error;
    private ControllerHandler filterCondition;
    private ControllerHandler dbConnectionProvider;
    private ControllerHandler attributes;
    private ControllerHandler response;
    
    
    public void init(FilterConfig c) {
        conf = c;
        error = new ErrorControllerHandler();
        filterCondition = new FilterConditionControllerHandler();
        dbConnectionProvider = new DatabaseConnectionControllerHandler();
        attributes = new AttributesControllerHandler();
        response = new ResponseControllerHandler();
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException,
            java.io.IOException {
        
            boolean errorSuccess = true;
            boolean filterConditionSuccess = true;
            boolean dbConnectionProviderSuccess = true;
            boolean attributesSuccess = true;
            boolean responseSuccess = true;
            
            try {
                
                // we run all the beforeFilter methods of the handlers
                
                errorSuccess = error.beforeFilter(req, resp, conf);
                filterConditionSuccess = filterCondition.beforeFilter(req, resp, conf);
                dbConnectionProviderSuccess = dbConnectionProvider.beforeFilter(req, resp, conf);
                attributesSuccess = attributes.beforeFilter(req, resp, conf);
                responseSuccess = response.beforeFilter(req, resp, conf);
                
            } catch (Exception e) {
                // if an exception occurs, we pass it through all the onError methods of the handlers
                // until the exception is handled, we just pass it on
                
                boolean isError = true;
                
                isError = response.onError(req, resp, e);
                if(isError)
                    isError = attributes.onError(req, resp, e);
                if(isError)
                    isError = dbConnectionProvider.onError(req, resp, e);
                if(isError)
                    isError = filterCondition.onError(req, resp, e);
                if(isError)
                    isError = error.onError(req, resp, e);
                
                // if this still is an error we throw it to tomcat
                if(isError)
                    throw new RuntimeWrappedException(e);
                
            } finally {
                // finally we finalize
                response.finalize(req, resp);
                attributes.finalize(req, resp);
                dbConnectionProvider.finalize(req, resp);
                filterCondition.finalize(req, resp);
                error.finalize(req, resp);
            }
            
            if(errorSuccess && filterConditionSuccess && attributesSuccess && responseSuccess && dbConnectionProviderSuccess) {
                chain.doFilter(req, resp);
            }
            if(responseSuccess) {
                response.afterFilter(req, resp, conf);
                response.finalize(req, resp);
            }
            if(attributesSuccess) {
                attributes.afterFilter(req, resp, conf);
                attributes.finalize(req, resp);
            }
            if(dbConnectionProviderSuccess) {
                dbConnectionProvider.afterFilter(req, resp, conf);
                dbConnectionProvider.finalize(req, resp);
            }
            if(filterConditionSuccess) {
                filterCondition.afterFilter(req, resp, conf);
                filterCondition.finalize(req, resp);
            }
            if(errorSuccess) {
                error.afterFilter(req, resp, conf);
                error.finalize(req, resp);
            }
               
        }

    public void destroy() {
    
    }

}
