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
//  $Id: SourceViewControllerHandler.java 3224 2008-10-05 22:32:17Z rosso_nero $
//  $Name$
/////////////////////////////////////
package org.makumba.commons;

import java.util.HashMap;

/**
 * * @author Rudolf Mayer
 * 
 * @version $Id: CollectionUtils.java,v 1.1 Dec 16, 2008 1:51:30 AM rudi Exp $
 */
public class CollectionUtils {

    public static HashMap<String, String> toMap(String[][] array) {
        if (array == null) {
            return null;
        }
        final HashMap<String, String> map = new HashMap<String, String>((int) (array.length * 1.5));
        for (int i = 0; i < array.length; i++) {
            String[] entry = array[i];
            if (entry.length < 2) {
                throw new IllegalArgumentException("Array element " + i + ", '" + entry + "', has a length less than 2");
            }
            map.put(entry[0], entry[1]);
        }
        return map;
    }

}
