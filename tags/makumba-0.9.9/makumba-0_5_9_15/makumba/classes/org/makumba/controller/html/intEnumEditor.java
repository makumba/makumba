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
import javax.servlet.*;
import java.util.*;


public class intEnumEditor extends charEnumEditor
{
	
  public Object getOptionValue(Object options, int i)
  { return new Integer(getIntAt(i)); }

  // copy/paste from intEditor.java
  public Object readFrom(org.makumba.controller.http.HttpParameters par, String suffix)
  { 
    Object o=par.getParameter(getInputName(suffix));
    
    if(o instanceof java.util.Vector)
      { throw new InvalidValueException(this, "multiple value not accepted for integer: "+o); }
    return toInt(o);
  }
}

/*	
  public String formatShowHIDE(Object o, Dictionary formatParams)
  {
    // check if the value is to be replaced by 'default' or 'empty'.
    o = toInt( formatValue(o, formatParams) );
    
    StringBuffer sb=new StringBuffer();
    sb.append("<select name=\"").append(getInputName(formatParams))
      .append("\"").append(getExtraFormatting(formatParams)).append(">");
    Enumeration v=getValues();
    Enumeration n=getNames();
    while(v.hasMoreElements())
      {
	Object vl=v.nextElement();
	sb.append("<option value=\"").append(vl).append("\"");
	if(vl.equals(o))
	  sb.append(" selected");
	// FIXME (fred): do string2html on the label (name) ??
	sb.append(">").append(n.nextElement()).append("</option>");
      }
    sb.append("</select>");
    return sb.toString();
  }
  
  public String formatShowHIDE2(Object o, Dictionary formatParams)
  {
    // check if the value is to be replaced by 'default' or 'empty'.
    o = toInt( formatValue(o, formatParams) );

    HtmlChoiceWriter hcw = new HtmlChoiceWriter(getInputName(formatParams));
    hcw.setValues(new EnumerationWrapper( getValues() ));
    hcw.setLabels(new EnumerationWrapper( getNames() ));
    if (o != null) hcw.setSelectedValues(o.toString());
    hcw.setLiteralHtml(getExtraFormatting(formatParams));
    hcw.setMultiple(false);
    hcw.setSize(1);
    // FIXME: set convert2html to true!?
   
    return hcw.getSelectOne();  
  }
*/


