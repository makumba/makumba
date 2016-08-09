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
import java.util.*;
import org.makumba.view.*;
import org.makumba.util.HtmlUtils;

public class charEditor extends FieldEditor
{
  static String[] params= { "type", "size", "maxlength" };
  static String[][] paramValues= { {"text", "password"}, null, null };
  public String[] getAcceptedParams(){ return params; }
  public String[][] getAcceptedValue(){ return paramValues; }

  public String getParams(Dictionary formatParams){ 
    String ret=getIntParamString(formatParams, "size");
    int n=getIntParam(formatParams, "maxlength");
    if(n> getWidth())
      throw new InvalidValueException(this, "invalid too big for maxlength "+n); 
    if(n==-1)
      n=getWidth();
    ret+="maxlength=\""+n+"\" ";
    return ret;
  }

  public String formatNull(Dictionary formatParams) 
  { return "<input name=\""+getInputName(formatParams)+"\" type=\""+getInputType(formatParams)+"\" value=\"\" "+getParams(formatParams)+getExtraFormatting(formatParams)+">"; }

  public String formatNotNull(Object o, Dictionary formatParams) 
  { return "<input name=\""+getInputName(formatParams)+"\" type=\""+getInputType(formatParams)+"\" value=\""+getLiteral(o, formatParams)+"\" "+getParams(formatParams)+
      getExtraFormatting(formatParams)+">"; }


  public String getLiteral(Object o, Dictionary formatParams) 
  {return HtmlUtils.string2html(o.toString()); }

  public String getInputType(Dictionary formatParams)
  {
    String s=(String)formatParams.get("type");
    if(s== null)
      s="text";
    return s;
  }
}
