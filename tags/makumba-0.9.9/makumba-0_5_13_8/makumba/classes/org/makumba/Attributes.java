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

/** The attributes provided by a makumba environment (eg a http session).
 * Attributes can be referred and assigned to in business logic code. 
 */
public interface Attributes
{
  /** Get the attribute with the given name. 
    @param name the name of the attribute
    @return the attribute value
   *@throws LogicException if a business logic problem occured while trying to determine the attribute
   *@throws AttributeNotFoundException if there was no error but the attribute could not be found.
   */
  public Object getAttribute(String name) throws LogicException;

  /** Set the value of an attribute 
    @param name the name of the attribute
    @param value the value of the attribute
    @return the old value of the attribue, or null if there was none
    *@throws LogicException if a business logic problem occured while trying to set the attribute (though at present the BL has no supported way to check that
    */
  public Object setAttribute(String name, Object value) throws LogicException;
}
