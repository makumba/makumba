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

import java.net.MalformedURLException;
import java.net.URL;

/**
 * This is a utility class which simply returns the URL of a java resource.
 * 
 * @author Cristian Bogdan
 * @version $Id$
 */
public class ClassResource {
    public static URL get(String s) {
        /*
        try {
            ShowResources.main(new String[] {"mdd"});
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        */

        String prependToCP = null;
        try {
            prependToCP = System.getProperty("org.makumba.prependToClassPath");
        } catch (SecurityException e) {
            // ignore
        }
        if (prependToCP != null && new java.io.File(prependToCP + "/" + s).exists()) {
            try {
                return new URL("file://" + prependToCP + "/" + s);
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        URL u = null;
        try {
            u = ClassResource.class.getClassLoader().getResource(s);
        } catch (RuntimeException e) {
        }
        if (u == null) {
            u = ClassLoader.getSystemResource(s);
        }

        String addToCP = null;
        try {
            addToCP = System.getProperty("org.makumba.addToClassPath");
        } catch (SecurityException e) {
            // ignore
        }
        if (u == null && addToCP != null && new java.io.File(addToCP + "/" + s).exists()) {
            try {
                return new URL("file://" + addToCP + "/" + s);
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        // new Throwable().printStackTrace();
        return u;
    }
}
