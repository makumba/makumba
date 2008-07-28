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
 * An exception thrown during the execution of some business logic code
 * 
 * @author Cristian Bogdan
 * @author Manuel Gay
 * 
 * @version $Id$
 * */
public class LogicException extends org.makumba.commons.WrappedException {

    private static final long serialVersionUID = 1L;

    public LogicException(Throwable t) {
        super(t, false);
    }

    public LogicException(String s) {
        super(s, false);
    }
    
    public LogicException(Throwable t, boolean isControllerOriginated) {
        super(t, isControllerOriginated);
    }

    public LogicException(String s, boolean isControllerOriginated) {
        super(s, isControllerOriginated);
    }
}
