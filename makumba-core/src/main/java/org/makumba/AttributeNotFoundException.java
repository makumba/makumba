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

package org.makumba;

/**
 * An exception thrown if an attribute is not found
 * 
 * @author Cristian Bogdan
 * @author Manuel Bernhardt <manuel@makumba.org>
 * @version $Id$
 */
public class AttributeNotFoundException extends LogicException {

    private static final long serialVersionUID = 1L;

    /** construct an exception message from the name of the missing attribute, indicates origin */
    public AttributeNotFoundException(String attName, boolean isControllerOriginated) {
        super("attribute not found: " + attName, isControllerOriginated);
    }

    /** construct an exception message from the name of the missing attribute */
    public AttributeNotFoundException(String attName) {
        super("attribute not found: " + attName, false);
    }
}
