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
import org.makumba.*;
import java.util.*;

public abstract class choiceEditor extends FieldEditor
{
  public abstract String formatOptionValue(Object opts, int i, Object val);

  public abstract Object getOptions();

  public abstract int getOptionsLength(Object opts);

  public abstract Object getOptionValue(Object options, int i);
  
  public abstract String formatOptionTitle(Object options, int i);

  public abstract String getMultiple();

  public abstract int getDefaultSize();


  static String[] params= { "size" };
  static String[][] paramValues= { null };
  public String[] getAcceptedParams(){ return params; }
  public String[][] getAcceptedValue(){ return paramValues; }

  // height? orderBy? where?
  public String format(Object o, Dictionary formatParams) 
  {
    boolean hidden= "hidden".equals(formatParams.get("type"));
    StringBuffer sb= new StringBuffer();
    if(!hidden)
      {
	sb.append("<select name=\"").append(getInputName(formatParams))
	  .append("\"").append(getExtraFormatting(formatParams));
	sb.append(getMultiple());
	int size=getIntParam(formatParams,"size");
	if(size==-1)
	  size=getDefaultSize();
	sb.append(" size=\""+size+"\"");
	sb.append(">");
      }
    
    Vector value;
    o = getValueOrDefault(o, formatParams);
    if(o instanceof Vector)
      value=(Vector)o;
    else 
      {
	value=new Vector(1);
	if(o!=null)
	  value.addElement(o);
      }

    if(!hidden)
      {
	Object opt=getOptions(); 
	
	for(int i=0; i<getOptionsLength(opt); i++)
	  {
	    Object val=getOptionValue(opt, i);
	    
	    sb.append("<option value=\"").append(formatOptionValue(opt, i, val)).append("\" ");
	    for(Enumeration f= value.elements(); f.hasMoreElements(); )
	      if(val.equals(f.nextElement()))
		sb.append("selected ");
	    sb.append(">");
	    sb.append(formatOptionTitle(opt, i)).append("</option>");
	  }
	sb.append("</select>");
      }
    else
      {
	for(Enumeration f= value.elements(); f.hasMoreElements(); )
	  {
	    Object val=f.nextElement();
	    sb.append("<input type=\"hidden\" name=\"").append(getInputName(formatParams)).append("\" value=\"").append(formatOptionValue(null, 0, val)).append("\">");
	  }
      }
    return sb.toString();
  }

  /** Return value if not null, or finds the default option and returns it as a Vector. */
  public Object getValueOrDefault (Object o, Dictionary formatParams) {
     if (o == null || ( o instanceof Vector && ((Vector)o).size()==0)  ) {
         String nullReplacer = (String) formatParams.get("default");
         if (nullReplacer != null) { 
             Vector v = new Vector(); 
             v.add(nullReplacer);
             return v; 
         }
     }
     return o;
  }
  
}
