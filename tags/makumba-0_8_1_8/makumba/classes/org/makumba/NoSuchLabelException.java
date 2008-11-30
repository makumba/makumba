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
//  $Id: NoSuchFieldException.java 1211 2006-01-20 03:12:11Z manuel_gay $
//  $Name$
/////////////////////////////////////

package org.makumba;

/**
 * * This exception occurs when a label is used, but does not exist. This is a programmer error, it should be fixed, not
 * caught,
 * 
 * @author Rudolf Mayer
 * @version $Id: NoSuchLabelException.java,v 1.1 Jun 11, 2008 12:39:28 PM rudi Exp $
 */
public class NoSuchLabelException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public NoSuchLabelException(String message) {
        super(message);
    }
}
