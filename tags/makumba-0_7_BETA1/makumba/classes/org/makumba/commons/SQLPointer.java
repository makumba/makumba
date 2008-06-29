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

import org.makumba.Pointer;

/**
 * A SQL pointer, represents a pointer as a long, but only an int is needed...
 *
 * @author Cristian Bogdan
 */
public class SQLPointer extends Pointer {

    private static final long serialVersionUID = 1L;

    static long compute(int dbsv, int uid) {
        return (dbsv << MASK_ORDER) + uid;
    }

    public static int getMaskOrder() {
        return MASK_ORDER;
    }

    private SQLPointer(String type) {
        if (type == null)
            throw new NullPointerException();
        this.type = type;
    }

    public SQLPointer(String type, long n) {
        this(type);
        this.n = n;
    }

    public SQLPointer(String type, int dbsv, int uid) {
        this(type);
        if (uid > (1 << MASK_ORDER)) {
            java.util.logging.Logger.getLogger("org.makumba." + "debug.db").finest("p");
            n = uid;
        } else
            n = compute(dbsv, uid);
    }

    public SQLPointer(String type, String s) {
        this(type);
        int separator = s.indexOf(":");
        int dbsv = new Integer(s.substring(0, separator)).intValue();
        int uid = new Integer(s.substring(separator + 1, s.length())).intValue();
        n = compute(dbsv, uid);
    }
}
