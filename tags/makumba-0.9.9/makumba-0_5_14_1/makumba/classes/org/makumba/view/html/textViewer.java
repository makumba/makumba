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

package org.makumba.view.html;
import java.util.Dictionary;

import org.makumba.HtmlUtils;

public class textViewer extends FieldViewer
{
  static String[] params= { "default", "empty", "lineSeparator", "longLineLength", "html"};
  static String[][] paramValues= { null,  null, null,  null, {"true", "false" , "auto"}};

  public String[] getAcceptedParams(){ return params; }
  public String[][] getAcceptedValue(){ return paramValues; }

  static int screenLength=30;

  public String formatNotNull(Object o, Dictionary formatParams) 
  {
    String txt=o.toString();
    String ht= (String)formatParams.get("html");
    if(ht!=null && (ht.equals("true") || ht.equals("auto") && HtmlUtils.detectHtml(txt)))
      return txt;

    String startSeparator="<p>";
    String endSeparator="</p>";
    String s= (String)formatParams.get("lineSeparator");
    if(s!=null){
      startSeparator=s;
      endSeparator="";
    }

    int n= getIntParam(formatParams, "longLineLength");
    if(n==-1)
      n=screenLength;
    
    if( HtmlUtils.maxLineLength(txt) > n)
      // special text formatting
      return HtmlUtils.text2html(txt, startSeparator, endSeparator);
    else if(txt.indexOf('\n')<0)
      // single, short line of text
      return HtmlUtils.string2html(txt);
    else
      // else: text preformatted
      return "<pre style=\"margin:0px\">"+HtmlUtils.string2html(txt)+"</pre>";
  }
  
  
}
