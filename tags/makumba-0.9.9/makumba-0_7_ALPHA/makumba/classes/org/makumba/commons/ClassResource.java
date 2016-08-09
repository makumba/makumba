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

import java.net.URL;

/**
 * This is a utility class which simply returns the URL of a java resource.
 * @author Cristian Bogdan
 * @version $Id$
 */
public class ClassResource {
    public static URL get(String s) {
        URL u = null;
        try {
            u = ClassResource.class.getClassLoader().getResource(s);
        } catch (RuntimeException e) {
        }
        if (u == null)
            u = ClassLoader.getSystemResource(s);
        // new Throwable().printStackTrace();
        return u;
    }
}
