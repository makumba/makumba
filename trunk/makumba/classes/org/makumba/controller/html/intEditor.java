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

package org.makumba.controller.html;
import org.makumba.view.*;
import java.util.Dictionary;

public class intEditor extends charEditor
{
  static String[] __params= { "default", "empty", "size", "maxlength" };
  static String[][] __paramValues= { null, null, null, null };
  public String[] getAcceptedParams(){ return __params; }
  public String[][] getAcceptedValue(){ return __paramValues; }

  public int getWidth() { return 10; }

  
  /** Formats the value to appear in an input statement. */
  public String formatValue(Object o, Dictionary formatParams) {
     // note: only diff with charEditor.formatValue is not calling string2html
     //       maybe can just as well not redefine this method?
     String s = (o == null)? null : o.toString();
     return resetValueFormat(s, formatParams);
  }
  
  public Object readFrom(org.makumba.controller.http.HttpParameters par, String suffix)
  { 
    Object o=par.getParameter(getInputName(suffix));
    
    if(o instanceof java.util.Vector)
      { throw new InvalidValueException(this, "multiple value not accepted for integer: "+o); }
    return toInt(o);
  }

}
