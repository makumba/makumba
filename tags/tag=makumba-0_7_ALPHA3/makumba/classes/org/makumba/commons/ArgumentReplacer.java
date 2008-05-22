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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Parses a string and identifies the arguments, to allow operations with them for now, arguments are of the form
 * $javaid[$] but the class can be extended for them to take other forms
 * 
 * @author Cristian Bogdan
 * @version $Id$
 */
public class ArgumentReplacer {
    List<String> text = new ArrayList<String>();

    Map<String, String> argumentNames = new HashMap<String, String>();

    List<String> argumentOrder = new ArrayList<String>();

    /** Gets the arguments list 
     *  @return An Enumeration containing the list of arguments
     */
    public Iterator<String> getArgumentNames() {
        return argumentNames.keySet().iterator();
    }

    /**
     * Replaces the arguments in a dictionary by their equivalent in numbers
     * @param d the dictionary containing the arguments in their original form
     * @return A String with the respective values replaced
     *  */
    public String replaceValues(Map<String, Object> d) {
        StringBuffer sb = new StringBuffer();
        Iterator f = argumentOrder.iterator();
        Iterator e = text.iterator();
        while (true) {
            sb.append(e.next());
            if (f.hasNext()) {
                Object nm = f.next();
                Object o = d.get(nm);
                if (o == null)
                    throw new RuntimeException(nm + " " + d);
                sb.append(o);
            } else
                break;
        }
        return sb.toString();
    }

    /**
     * Makes a list of all arguments and where they are
     * @param s the string containing the arguments
     */
    public ArgumentReplacer(String s) {
        int dollar;
        String prev = "";
        boolean doubledollar;
        int n;
        String argname;

        while (true) {
            dollar = s.indexOf('$');
            if (dollar == -1 || s.length() == dollar + 1) {
                text.add(prev + s);
                break;
            }

            dollar++;
            if ((doubledollar = s.charAt(dollar) == '$') || !(Character.isJavaIdentifierStart(s.charAt(dollar)) || Character.isDigit(s.charAt(dollar)))) {
                prev = s.substring(0, dollar);
                if (doubledollar)
                    dollar++;
                if (s.length() > dollar) {
                    s = s.substring(dollar);
                    continue;
                } else {
                    text.add(prev);
                    break;
                }
            }
            text.add(prev + s.substring(0, dollar - 1));
            prev = "";

            // we allow also '.' in the attribute name, which is needed in search forms for searches on subfields
            // TODO: check if that also works for HQL
            for (n = dollar + 1; n < s.length() && s.charAt(n) != '$' && (Character.isJavaIdentifierPart(s.charAt(n)) || s.charAt(n)=='.'); n++)
                ;
            argname = s.substring(dollar, n);
            if (n < s.length() && s.charAt(n) == '$')
                n++;
            argumentNames.put(argname, "");
            argumentOrder.add(argname);
            if (n < s.length()) {
                s = s.substring(n);
                continue;
            } else {
                text.add("");
                break;
            }
        }
    }
}
