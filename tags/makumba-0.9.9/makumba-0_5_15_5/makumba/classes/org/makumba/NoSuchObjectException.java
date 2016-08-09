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

/** This exception occurs when a pointer or a condition is pased to the database for a delete or update operation but the object pointed does not exist in the database (anymore). This is a makumba API user error and it should be fixed, not caught. */
public class NoSuchObjectException extends RuntimeException
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

/** indicate the missing pointer */
  public NoSuchObjectException(Pointer p) {super(""+p); }
}
