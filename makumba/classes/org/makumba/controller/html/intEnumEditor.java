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

public class intEnumEditor extends intEditor
{
  public String formatShow(Object o, Dictionary formatParams)
  {
    StringBuffer sb=new StringBuffer();
    sb.append("<select name=\"").append(getInputName(formatParams)).append("\">");
    Enumeration v=getValues();
    Enumeration n=getNames();
    while(v.hasMoreElements())
      {
	Object vl=v.nextElement();
	sb.append("<option value=\"").append(vl).append("\"");
	if(vl.equals(o))
	  sb.append(" selected");
	sb.append(">").append(n.nextElement()).append("</option>");
      }
    sb.append("</select>");
    return sb.toString();
  }
}
