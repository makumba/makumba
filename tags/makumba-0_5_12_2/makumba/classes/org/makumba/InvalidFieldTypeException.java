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

/** This exception occurs when a field is indicated as being subset or subrecord (eg for the insert method) and it is not. This is a makumba API user error and it should be fixed, not caught. */
public class InvalidFieldTypeException extends RuntimeException
{
  public InvalidFieldTypeException(FieldDefinition f, String expectedType ) 
  {super(f.getDataDefinition().getName()+"->"+f.getName()+" is not a "+expectedType); } 

  public InvalidFieldTypeException(String explanation ) 
  {super(explanation); }
}
