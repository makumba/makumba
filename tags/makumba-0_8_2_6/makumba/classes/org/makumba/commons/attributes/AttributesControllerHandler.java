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
//  $Id: NullObject.java 3694 2009-02-11 00:41:05Z rosso_nero $
//  $Name$
/////////////////////////////////////

package org.makumba.commons.attributes;

import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.makumba.commons.ControllerHandler;
import org.makumba.commons.ServletObjects;

/**
 * @author
 * @version $Id: AttributesControllerHandler.java,v 1.1 Feb 11, 2009 1:41:26 AM rudi Exp $
 */
public class AttributesControllerHandler extends ControllerHandler {

    @Override
    public boolean beforeFilter(ServletRequest request, ServletResponse response, FilterConfig conf,
            ServletObjects httpServletObjects) throws Exception {
        RequestAttributes.getAttributes((HttpServletRequest) request);
        return true;
    }

}
