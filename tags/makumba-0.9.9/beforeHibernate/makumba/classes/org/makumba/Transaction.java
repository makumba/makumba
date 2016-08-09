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

/** This class models operations with a database.  To obtain such an object, use methods from {@link MakumbaSystem}. <p>
  Stricly speaking this class represents a database connection (later on, transaction). Obtaining more such objects for the same database configurations will result in opening more connections. Connections must be given back to the system using the {@link #close()} method. That will be done automatically by the object's finalizer. In makumba business logic, connections passed to the BL methods are automatically closed by the system after the BL operations (including eventual automatic DB acceses) were completed. To open a "sibling" of a connection <i>conn</i> of this type, use MakumbaSystem.getConnectionTo(<i>conn</i>.getName()). In most cases, you will have to close the sibling yourself.<p>
 * At the level of this API, data is represented as java.util.Dictionary, both for reading and writing. Most methods throw {@link DBError} if a fatal database error occurs. If the connection to the database is lost, an attempt is made to reconnect before throwing a {@link DBError}.<P>
 * All methods throw subclasses of either Error or RuntimeException, so nothing needs to be caught explicitely.
 * @see org.makumba.MakumbaSystem#getDefaultDatabaseName()
 * @see org.makumba.MakumbaSystem#getDefaultDatabaseName(java.lang.String)
 * @see org.makumba.MakumbaSystem#getConnectionTo(java.lang.String)
 * @since makumba-0.5
 */
public interface Transaction extends Database
{
	
}



