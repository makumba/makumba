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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

/**
 * Helper class to work with http parameters
 * 
 * @author Cristian Bogdan
 * @version $Id: HttpParameters.java 1402 2007-07-25 11:52:28Z manuel_gay $
 */
public class HttpParameters {
    HttpServletRequest request;

    Hashtable<Object, Object> atStart;

    public boolean knownAtStart(String s) {
        return atStart.get(s) != null;
    }

    public HttpParameters(HttpServletRequest req) {
        request = req;
        computeAtStart();
    }

    void computeAtStart() {
        atStart = new Hashtable<Object, Object>();
        Object dummy = new Object();
        for (Enumeration e = request.getParameterNames(); e.hasMoreElements();)
            atStart.put(e.nextElement(), dummy);
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
        String[] param = request.getParameterValues(s);
        if (param == null)
            return null;

        if (param.length == 1)
            value = param[0];
        else {
            Vector<String> v = new java.util.Vector<String>();
            value = v;
            for (int i = 0; i < param.length; i++)
                v.addElement(param[i]);
        }
        // request.setAttribute(s, value);

        return value;
    }

    public String toString() {
        return request.getParameterMap().toString();
    }

}