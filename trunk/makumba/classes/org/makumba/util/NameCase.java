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

package org.makumba.util;

public class NameCase {
    public static String transformCase(String s) {
        s = s.trim();
        StringBuffer sb = new StringBuffer();
        boolean wasSpace = true;
        boolean wasSpaceChar = false;
        boolean wasLowerCase = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c == '-') || (c == '\'')) {
                wasSpace = true;
                wasSpaceChar = false;
                wasLowerCase = false;
                sb.append(c);
            } else if (c == ' ')
                if (!wasSpaceChar) {
                    wasSpace = true;
                    wasSpaceChar = true;
                    wasLowerCase = false;
                    sb.append(c);
                } else
                    ;
            else {
                if (wasSpace) {
                    sb.append(Character.toUpperCase(c));
                    wasLowerCase = false;
                } else {
                    if ((wasLowerCase == true) && (Character.toUpperCase(c) == c)) {
                        /* uncomment this if you want BauerPartnerHeimer -> Bauer Partner Heimer */
                        /* sb.append(' '); */
                        sb.append(Character.toUpperCase(c));
                        wasLowerCase = false;
                    } else {
                        sb.append(Character.toLowerCase(c));
                        wasLowerCase = (Character.toLowerCase(c) == c);
                    }
                }
                wasSpace = false;
                wasSpaceChar = false;
            }
        }
        String ret = sb.toString();
        if (!s.equals(ret))
            org.makumba.MakumbaSystem.getMakumbaLogger("import").info("org.makumba.util.NameCase: " + s + " -> " + ret);
        return ret;
    }

    public static void main(String argv[]) {
        System.out.println(transformCase(argv[0]));
    }
}
