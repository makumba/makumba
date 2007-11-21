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

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.makumba.commons.ControllerHandler;
import org.makumba.commons.RuntimeWrappedException;

/**
 * The filter that controls each makumba HTTP access. Performs login, form response, exception handling.
 * 
 * This filter uses a number of {@link ControllerHandler}-s which each serve a specific purpose.
 * 
 * @author Cristian Bogdan
 * @author Manuel Gay
 * @version $Id$ *
 */
public class ControllerFilter implements Filter {
    
    private FilterConfig conf;
    
    private String handlerClasses= 
            "org.makumba.devel.ErrorControllerHandler,"+
            "org.makumba.controller.FilterConditionControllerHandler,"+
            "org.makumba.commons.attributes.DatabaseConnectionControllerHandler,"+ 
            "org.makumba.commons.attributes.AttributesControllerHandler,"+
            "org.makumba.forms.responder.ResponseControllerHandler";
    
    private ArrayList<ControllerHandler> handlers= new ArrayList<ControllerHandler>();
    
    public void init(FilterConfig c) { 
        conf=c;
        String handlerParam= c.getInitParameter("handlerClasses");
        if(handlerParam==null)
            handlerParam=handlerClasses;
        StringTokenizer str= new StringTokenizer(handlerParam, ",");
        
        while(str.hasMoreTokens()){
            try {
                handlers.add((ControllerHandler) Class.forName(str.nextToken().trim()).newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException,
            java.io.IOException {
        int i=0;
        int imax=0;
        try{
            for(; i<handlers.size(); i++)
                if(!handlers.get(i).beforeFilter(req, resp, conf))
                    break;   
            imax=i-1;
            for(i=imax; i>=0; i--)
                handlers.get(i).afterBeforeFilter(req, resp, conf);
            if(imax==handlers.size()-1)
                chain.doFilter(req, resp);

            for(i=imax; i>=0; i--)
                handlers.get(i).afterFilter(req, resp, conf);
        } 
        catch (Throwable t) {
            for(i=imax; i>=0; i--)
                if(!handlers.get(i).onError(req, resp, t))
                    return;
            throw new RuntimeWrappedException(t);
        }   

        finally {
            for(i=0;i<=imax;i++)
                handlers.get(i).finalize(req, resp);           
        }
           
        }

    public void destroy() {
    
    }

}
