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

import org.makumba.OODB.DatabaseImplementation;

/** Specifies interface for data transformation. 
 Some of your application's class should implement this DataTransformer in order to be used by Makumba during DB operations. 
 <p>
 
 In your db configuration file (eg <code>localhost_mysql_myapp.properties</code>) it should be specified with a line 
 <pre class="example">
insert#<i>makumba.Type</i>=<i>yourClassThatImplementsDataTransformer</i>
</pre>
 to run your transformer (<code>yourClassThatImplementsDataTransformer</code>) on all the records of that makumba type 
(<code><i>makumba.Type</i></code>) before being inserted into the database.

<h3>Example</h3>
 <pre class="example">
insert#<i>general.Person</i>=<i>PersonHook</i>
</pre>

with a simple <code>DataTransformer</code> class <code>PersonHook</code>:
 <pre class="example">
import org.makumba.*;
import java.util.*;

public class PersonHook implements DataTransformer
{
  public boolean transform(Dictionary d, Database db)
  {
    //we might want to make sure a name is all lowercase with capital initial:
    String n= (String)d.get("name");
    if(n!=null && n.trim().length()>2)
    {
      d.put("name", n.substring(0,1).toLowerCase()+n.substring(1).toLowerCase());
      return true
    }
    else {
      return false;
    }
  }
</pre>
Would ensure that application writes only "properly capitalized" names to the DB, wherever in the application objects of type 
<code>general.Person</code> 
would be created.

 */
public interface DataTransformer
{
    /** Performs the data transformation.
	@param d data to transform
	@param db database
	@return true if specified database operation should be done (eg data transformed succesfully or data is valid), false 
otherwise.
    */
    public boolean transform(java.util.Dictionary d, DatabaseImplementation db);
}
