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

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * A generic Makumba fatal error, due to misconfiguration, bad Data Definition (MDD) syntax, bad OQL syntax, database
 * fatal error, etc. Such an error usually denotes either a configuration mistake from the makumba API user, either a
 * fatal database problem that makes it impossible for the makumba application to work. Like all errors, Makumba errors
 * don't need to be caught, as they occur in "terminal" conditions anyway. Most makumba errors come from exceptions,
 * which can be retrieved calling getReason()
 */
public class MakumbaError extends Error {
    private static final long serialVersionUID = 1L;

    String explanation;

    /** Build a makumba error and associate it with the given reason */
    public MakumbaError(Throwable reason) {
        super(reason);
    }

    /** Build a makumba error and associate it with the given reason and explanation text */
    public MakumbaError(Throwable reason, String expl) {
        super(expl, reason);
        this.explanation = expl;
    }

    /** Build an empty makumba error */
    public MakumbaError() {
    };

    /** Build a makumba error with the given explanation */
    public MakumbaError(String explanation) {
        super(explanation);
    }

   /** Print the stacktrace of the reason exception, if any, otherwise print a normal stack trace */
    public void printStackTrace() {
        if (explanation != null)
            System.out.println(explanation);
        super.printStackTrace();
    }

    /** Print the stacktrace of the reason exception, if any, otherwise print a normal stack trace */
    public void printStackTrace(PrintStream ps) {
            if (explanation != null)
                ps.println(explanation);

            super.printStackTrace(ps);
    }

    /** Print the stacktrace of the reason exception, if any, otherwise print a normal stack trace */
    public void printStackTrace(PrintWriter ps) {
            if (explanation != null)
                ps.println(explanation);
            super.printStackTrace(ps);
    }

    /** A string representation of this error, for debugging */
    public String toString() {
        try {
            return getClass().getName() + ": " + getCause().toString();
        } catch (NullPointerException npe) {
            return super.toString();
        }
    }
}
