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

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Wraps an exception to throw it further as a desired exception type. stacktraces of this exception will actually print
 * the stracktrace of the wrapped exception.
 * 
 * @author Cristian Bogdan
 * @author Manuel Gay
 * 
 * @version $Id$
 */


public class WrappedException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    Throwable t;
    private boolean controllerOriginated = false;

    /** wrap the given exception */
    public WrappedException(Throwable e, boolean isControllerOriginated) {
        super(e.toString());
        this.t = e;
        this.controllerOriginated = isControllerOriginated;
    }

    /** wrap the given exception */
    public WrappedException(Throwable e, String s) {
        super(e.toString() + (s != null ? s : ""));
        this.t = e;
    }

    /** wrap nothing */
    public WrappedException() {
    };

    /** wrap nothing, store a message */
    public WrappedException(String s, boolean isControllerOriginated) {
        super(s);
        this.controllerOriginated = isControllerOriginated;
    }

    /** return the wrapped exception */
    public Throwable getReason() {
        return t;
    }
    
    /**
     * checks whether this exception originated in Controller or at the JSP level
     */
    public boolean isControllerOriginated() {
      return controllerOriginated;
    }

    /** print the stacktrace of the wrapped exception */
    public void printStackTrace() {
        try {
            t.printStackTrace();
        } catch (NullPointerException npe) {
            super.printStackTrace();
        }
    }

    /** print the stacktrace of the wrapped exception */
    public void printStackTrace(PrintStream ps) {
        try {
            t.printStackTrace(ps);
        } catch (NullPointerException npe) {
            super.printStackTrace(ps);
        }
    }

    /** print the stacktrace of the wrapped exception */
    public void printStackTrace(PrintWriter ps) {
        try {
            t.printStackTrace(ps);
        } catch (NullPointerException npe) {
            super.printStackTrace(ps);
        }
    }

    /** the class name of this exception, then the toString of the wrapped */
    public String toString() {
        try {
            return getClass().getName() + ": " + t.toString();
        } catch (NullPointerException npe) {
            return super.toString();
        }
    }
}
