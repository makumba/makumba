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
//  $Id: FieldCursor.java 1707 2007-09-28 15:35:48Z manuel_gay $
//  $Name$
/////////////////////////////////////

package org.makumba.providers.query.mql;

/**
 * Represents a single argument in an MQL function.
 * 
 * @author Rudolf Mayer
 * @version $Id: MQLFunctionArgument.java,v 1.1 Dec 20, 2008 3:33:06 AM rudi Exp $
 */
class MQLFunctionArgument {
    private boolean multiple;

    private boolean optional;

    private String type;

    public MQLFunctionArgument(String type) {
        this(type, false, false);
    }

    public MQLFunctionArgument(String type, boolean optional, boolean multiple) {
        this.type = type;
        this.optional = optional;
        this.multiple = multiple;
    }

    public String getType() {
        return type;
    }

    public String getTypeNice() {
        return getType().replace("[255]", "");
    }

    /** Whether the argument can be repeated multiple times. */
    public boolean isMultiple() {
        return multiple;
    }

    /** Whether the argument is optional. */
    public boolean isOptional() {
        return optional;
    }

    @Override
    public String toString() {
        String s = getTypeNice();
        if (isOptional()) {
            s = "[" + s + "]";
        }
        if (isMultiple()) {
            s += "+";
        }
        return s;
    }
}