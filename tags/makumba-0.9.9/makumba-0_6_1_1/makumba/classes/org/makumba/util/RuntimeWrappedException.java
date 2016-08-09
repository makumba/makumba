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

/** wrap an exception to throw it further as a RuntimeException. stacktraces of this exception will actually print the stracktrace of the wrapped exception */
public class RuntimeWrappedException extends RuntimeException
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
Throwable t;

  /** wrap the given exception */
  public RuntimeWrappedException(Throwable e)
  {
    super(e.toString());
    this.t=e;
  }
  
  /** return the wrapped exception */
  public Throwable getReason() { return t; }

  /** wrappee's stacktrace */
  public void printStackTrace() { t.printStackTrace();  }

  /** wrappee's stacktrace */
  public void printStackTrace(PrintStream ps){ t.printStackTrace(ps);}

  /** wrappee's stacktrace */
  public void printStackTrace(PrintWriter ps){ t.printStackTrace(ps);}
  
  /** toString of the wrapee */
  public String toString(){ return "RuntimeWrappedException: "+t.toString();}
}
