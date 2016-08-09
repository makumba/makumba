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


/**
 * Wraps an exception to throw it further as a RuntimeException. Stacktraces of this exception will actually print the
 * stracktrace of the wrapped exception.
 * 
 * TODO: this can be done in a more standard way since Java 1.4, so this should be refactored.
 */
public class RuntimeWrappedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** wrap the given exception */
    public RuntimeWrappedException(Throwable e) {
        super(e);
    }
    
    public RuntimeWrappedException(String message) {
        super(message);
    }

}
