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

/** Specifies interface for data transformation. 
 Some of your application's class should implement this DataTransformer in order to be used my Makumba during DB operations. 
 <p>
 
 In your db configuration file (eg <code>localhost_mysql_myapp.properties</code>) it should be specified with a line 
 <pre>insert#<i>makumba.Type</i>=<i>yourClassThatImplementsDataTransformer</i></pre>
 to run your transformer (<code>yourClassThatImplementsDataTransformer</code>) on all the records of that makumba type 
(<code><i>makumba.Type</i></code>) before being inserted into the database.

<p> eg:
 <pre>insert#<i>general.Person</i>=<i>org.eu.best.PersonHook</i></pre>
 */
public interface DataTransformer
{
    /** Performs the data transformation.
	@param d data to transform
	@param db database
	@return true if specified database operation should be done (eg data transformed succesfully or data is valid), false 
otherwise.
    */
    public boolean transform(java.util.Dictionary d, Database db);
}
