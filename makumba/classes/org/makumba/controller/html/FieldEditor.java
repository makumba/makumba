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
import org.makumba.ProgrammerError;
import org.makumba.view.*;
import javax.servlet.*;
import java.util.*;

public class FieldEditor extends org.makumba.view.FieldFormatter
{
  static String[] params={"default", "empty", "type"};
  static String[][] paramValues={null, null, {"hidden"}};
  public String[] getAcceptedParams(){ return params; }
  public String[][] getAcceptedValue(){ return paramValues; }

  static final String suffixName="org.makumba.editorSuffix";

  public static String getSuffix(Dictionary formatParams) { return (String)formatParams.get(suffixName); }
  public static void setSuffix(Dictionary formatParams, String suffix) { formatParams.put(suffixName, suffix); }

  public void checkParam(String name, String val)
  {
    if(name.equals(extraFormattingParam))
      return;
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
    return "<input type=\"hidden\" name=\"" + getInputName(formatParams) + "\" value=\"" + 
            formatHiddenValue(o, formatParams) + "\" " + getExtraFormatting(formatParams) +">";
  }

  /** Formats the value to appear in hidden input statement. */
  public String formatHiddenValue(Object o, Dictionary formatParams) {
     // default : same treatment as formatting for normal input.
     return formatValue(o, formatParams);
  }

  /** Formats the value to appear in an input statement. */
  public String formatValue(Object o, Dictionary formatParams) {
      // return super.format(o, formatParams);
      throw new ProgrammerError("If this method is needed, overload it in the inheriting class");
  }

  public void onStartup(RecordEditor re){}

  public String getInputName(Dictionary formatParams){ return getInputName(getSuffix(formatParams)); }

  public String getInputName(String suffix){ return getExpr()+suffix; }

  public static final String extraFormattingParam="makumba.extraFormatting";

  public String getExtraFormatting(Dictionary formatParams)
  {
    return (String)formatParams.get(extraFormattingParam);
  }

  public Object readFrom(org.makumba.controller.http.HttpParameters p, String suffix) 
  {
    return p.getParameter(getInputName(suffix));
  }

  protected Integer toInt(Object o)
  {
    if(o==null)
      return null;
    String s=(""+o).trim();
    if(s.length()==0)
      return null;
    try{ 
      return new Integer(Integer.parseInt(s));
    }catch(NumberFormatException e) { throw new InvalidValueException(this, "invalid integer: "+o); }
  }

  protected Double toReal(Object o)
  {
    if(o==null)
      return null;
    String s=(""+o).trim();
    if(s.length()==0)
      return null;
    try{  return new Double(Double.parseDouble(s));}
    catch(NumberFormatException e) 
     {  try{ return new Double(Double.parseDouble(s.replace(',','.')) );  }
	catch(NumberFormatException e2) 	
	{ try{ return new Double(Double.parseDouble(s.replace('.',',')) );}
	  catch(NumberFormatException e3) 
	  {  throw new InvalidValueException(this, "invalid real: "+o); }
	}
     }
  }

}
