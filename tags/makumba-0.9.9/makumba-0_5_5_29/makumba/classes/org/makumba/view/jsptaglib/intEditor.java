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

package org.makumba.view.jsptaglib;
import org.makumba.view.*;

public class intEditor extends charEditor
{
  static String[] params= {  "size", "maxlength" };
  static String[][] paramValues= { null, null };
  public String[] getAcceptedParams(){ return params; }
  public String[][] getAcceptedValue(){ return paramValues; }

  public int getWidth() { return 10; }

  public String getLiteral(Object o, java.util.Dictionary formatParams) 
  {
    return o.toString();
  }

  public Object readFrom(HttpParameters par)
  { 
    Object o=par.getParameter(getInputName());
    
    if(o instanceof java.util.Vector)
      { throw new InvalidValueException(this, "multiple value not accepted for integer: "+o); }
    return toInt(o);
  }

}
