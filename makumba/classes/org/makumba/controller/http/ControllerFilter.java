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
 * TODO there should be a simple mechanism of deactivating one of the handlers, e.g. an invocation list
 * read from the filter configuration and by default calling all the handlers
 * 
 * @author Cristian Bogdan
 * @author Rudolf Mayer
 * @author Manuel Gay
 * @version $Id$ *
 */
public class ControllerFilter implements Filter {
    
    private FilterConfig conf;
    
    private ControllerHandler handlers[]={
            new ErrorControllerHandler(), 
            new FilterConditionControllerHandler(), 
            new DatabaseConnectionControllerHandler(), 
            new AttributesControllerHandler(),
            new ResponseControllerHandler()
    };
    
    
    
    public void init(FilterConfig c) { conf=c;}

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException,
            java.io.IOException {
        int i=0;
        int imax=0;
        try{
            for(; i<handlers.length; i++)
                if(!handlers[i].beforeFilter(req, resp, conf))
                    break;   
            imax=i-1;
            for(i=imax; i>=0; i--)
                handlers[i].afterBeforeFilter(req, resp, conf);
            if(imax==handlers.length-1)
                chain.doFilter(req, resp);

            for(i=imax; i>=0; i--)
                handlers[i].afterFilter(req, resp, conf);
        } 
        catch (Throwable t) {
            for(i=imax; i>=0; i--)
                if(!handlers[i].onError(req, resp, t))
                    return;
            throw new RuntimeWrappedException(t);
        }   

        finally {
            for(i=0;i<=imax;i++)
                handlers[i].finalize(req, resp);           
        }
           
        }

    public void destroy() {
    
    }

}
