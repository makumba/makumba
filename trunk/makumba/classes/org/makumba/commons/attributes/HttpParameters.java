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
//  $Id: HttpParameters.java 1402 2007-07-25 11:52:28Z manuel_gay $
//  $Name$
/////////////////////////////////////

package org.makumba.commons.attributes;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

/**
 * Helper class to work with http parameters
 * 
 * @author Cristian Bogdan
 * @author Rudolf Mayer
 * @author Manuel Gay
 * @version $Id: HttpParameters.java 1402 2007-07-25 11:52:28Z manuel_gay $
 */
public class HttpParameters {

    HttpServletRequest request;

    Hashtable<Object, Object> atStart;

    Map<?, ?> reloadedParameters = null;

    public boolean knownAtStart(String s) {
        return atStart.get(s) != null;
    }

    public HttpParameters(HttpServletRequest req) {
        request = req;
        computeAtStart();
    }

    public HttpParameters(HttpServletRequest req, Map<?, ?> additionalParams) {
        request = req;
        reloadedParameters = additionalParams;
        computeAtStart();
        atStart.putAll(additionalParams);
    }

    void computeAtStart() {
        atStart = new Hashtable<Object, Object>();
        Object dummy = new Object();
        for (Enumeration<?> e = request.getParameterNames(); e.hasMoreElements();) {
            atStart.put(e.nextElement(), dummy);
        }
    }

    /**
     * Gets a http parameter. If there are more values for the parameter, places them into a Vector
     * 
     * @param s
     *            the name of the parameter
     * @return A String containing the value of the parameter in case of a unique value, a Vector otherwise
     */
    public Object getParameter(String s) {
        Object value = null;
        ArrayList<String> param = new ArrayList<String>();

        String[] params = request.getParameterValues(s);
        if (params != null) {
            for (String p : params) {
                param.add(p);
            }
        }

        // we add the reloaded parameters only if there is no value for it in the existing session
        // otherwise we might set a parameter with multiple values whereas it really should have only one
        // this is especially the case for URL GET parameters
        if (reloadedParameters != null && param.size() == 0) {
            if (reloadedParameters.get(s) instanceof String) {
                param.add((String) reloadedParameters.get(s));
            } else if (reloadedParameters.get(s) instanceof String[]) {
                String[] paramValues = (String[]) reloadedParameters.get(s);
                for (String v : paramValues) {
                    param.add(v);
                }
            }

        }

        if (param.size() == 0) {
            return null;
        }

        if (param.size() == 1) {
            value = param.get(0);
        } else {
            Vector<String> v = new java.util.Vector<String>();
            value = v;
            for (int i = 0; i < param.size(); i++) {
                v.addElement(param.get(i));
            }
        }
        // request.setAttribute(s, value);

        return value;
    }

    public ArrayList<String> getParametersStartingWith(String s) {
        ArrayList<String> result = new ArrayList<String>();
        Enumeration<?> parameterNames = request.getParameterNames();

        while (parameterNames.hasMoreElements()) {
            String param = (String) parameterNames.nextElement();
            if (param.startsWith(s)) {
                result.add(param);
            }
        }

        // we add the reloaded parameters only if there is no value for it in the existing session
        // otherwise we might set a parameter with multiple values whereas it really should have only one
        // this is especially the case for URL GET parameters
        if (reloadedParameters != null) {
            Iterator<?> i = reloadedParameters.keySet().iterator();
            while (i.hasNext()) {
                String param = (String) i.next();
                if (param.startsWith(s) && !result.contains(param)) {
                    result.add(param);
                }
            }
        }

        return result;
    }

    @Override
    public String toString() {
        if (reloadedParameters == null) {
            return request.getParameterMap().toString();
        }
        return "page parameters: " + request.getParameterMap().toString() + ", reloaded parameters: "
                + reloadedParameters.toString();
    }

}