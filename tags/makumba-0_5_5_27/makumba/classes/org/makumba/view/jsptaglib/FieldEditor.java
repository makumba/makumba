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
import javax.servlet.*;
import java.util.*;

public class FieldEditor extends FieldViewer
{
  static String[] params={"type"};
  static String[][] paramValues={{"hidden"}};
  public String[] getAcceptedParams(){ return params; }
  public String[][] getAcceptedValue(){ return paramValues; }

  public void checkParam(String name, String val)
  {
    if(name.equals("type") && val.equals("hidden"))
      return;
    super.checkParam(name, val);
  }

  public String format(Object o, Dictionary formatParams)
  {
    String s=(String)formatParams.get("type");
    if(s!=null && s.equals("hidden"))
      return formatHidden(o, formatParams);
    return formatShow(o, formatParams);
  }

  public String formatShow(Object o, Dictionary formatParams)
  {
    // this will call formatNull and formatNotNull which should be redefined
    // or, the entire formatShow should be redefined
    return super.format(o, formatParams);
  }

  public String formatHidden(Object o, Dictionary formatParams)
  {
    return "<input type=\"hidden\" name=\""+getInputName()+"\" value=\""+formatHiddenValue(o, formatParams)+"\">";
  }

  public String formatHiddenValue(Object o, Dictionary formatParams)
  {
    if(o==null || o.equals(getNull()))
      return super.formatNull(formatParams);
    return super.formatNotNull(o, formatParams);
  }

  public void onStartup(RecordEditor re){}

  public String getInputName(){ return getExpr(); }

  public Object readFrom(HttpParameters p) 
  {
    return p.getParameter(getInputName());
  }

  protected Integer toInt(Object o)
  {
    String s=(""+o).trim();
    if(s.length()==0)
      return null;
    try{ 
      return new Integer(Integer.parseInt(s));
    }catch(NumberFormatException e) { throw new InvalidValueException(this, "invalid integer: "+o); }
  }

}
