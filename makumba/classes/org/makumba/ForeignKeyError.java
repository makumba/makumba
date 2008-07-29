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
//  $Id: DBError.java 1605 2007-09-17 20:33:41Z rosso_nero $
//  $Name$
/////////////////////////////////////

package org.makumba;

/**
 * @author Rudolf Mayer
 * @version $Id: ForeignKeyError.java,v 1.1 Jul 30, 2008 12:55:56 AM rudi Exp $
 */
public class ForeignKeyError extends DBError {
    private static final long serialVersionUID = 1L;

    public ForeignKeyError(java.sql.SQLException se) {
        super("Foreign Key exception. " + se.getMessage());
    }

    public ForeignKeyError(String msg) {
        super("Foreign Key exception. " + msg);
    }
}