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
import org.makumba.util.HtmlUtils;

public class textEditor extends FieldEditor
{
  static String[] params= { "type", "rows", "cols" };
  static String[][] paramValues= { {"textarea", "file" }, null, null };
  public String[] getAcceptedParams(){ return params; }
  public String[][] getAcceptedValue(){ return paramValues; }

  public String getParams(Dictionary formatParams){ 
    return getIntParamString(formatParams, "rows") + getIntParamString(formatParams, "cols");
  }

  public String formatNull(Dictionary formatParams) 
  { 
    if(isTextArea(formatParams))
      return "<TEXTAREA name=\""+getInputName(formatParams)+"\" "+getParams(formatParams)+" ></TEXTAREA>"; 
    else
      return fileInput(formatParams);
  }

  public String formatNotNull(Object o, Dictionary formatParams) 
  { 
    if(isTextArea(formatParams))
      return "<TEXTAREA name=\""+getInputName(formatParams)+"\" "+getParams(formatParams)+" >"+HtmlUtils.string2html(o.toString())+"</TEXTAREA>"; 
    else
      return fileInput(formatParams);
  }
  
  String fileInput(Dictionary formatParams)
  {
    return "<INPUT name=\""+getInputName(formatParams)+"\" type=\"file\">"; 
  }

  boolean isTextArea(Dictionary formatParams)
  {
    String s=(String)formatParams.get("type");
    if(s== null)
      return true;
    return s.equals("textarea");
  }
}
