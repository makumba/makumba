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
import org.makumba.*;
import org.makumba.view.FieldFormatter;
import java.util.Dictionary;
import org.makumba.util.HtmlUtils;

/** Default HTML formatting of fields */
public class FieldViewer extends FieldFormatter
{

 final static String defaultEllipsis="...";


 /** 
 * Returns a substring of maximum length by cutting at the end; if cut, an ellipsis is added on the end.
 * Note: uses only J2 1.3 supported functions. An ellipsis is 3 dots (...)
 * TODO:add support for fixedLength=N and fixedLengthAlign=left|center|right, fixedLengthPadChar='.'
 * @param s string to format
 * @param formatParams formatting parameters
 */
 public String formatMaxLengthEllipsis(String s, Dictionary formatParams)
 {
   String prefix="";
   String postfix="";
   String sOut=s;
   int maxLen=getIntParam(formatParams, "maxLength");

   String ellipsis= (String)formatParams.get("maxLengthEllipsis");
   if(ellipsis==null)
     ellipsis=defaultEllipsis;

   int ellipsisLen=getIntParam(formatParams, "maxLengthEllipsisLength");
   if(ellipsisLen==-1) //not specified
      ellipsisLen=ellipsis.length(); //compute from actual ellipsis

   String hoverText= (String)formatParams.get("hoverText");
   if(hoverText==null)
      hoverText="false";

   if(maxLen!=-1 && s.length()>maxLen) //content longer than allowed
    { //shorten the content
      int cutAt=maxLen-ellipsisLen;
      if(cutAt<0) cutAt=0;
      sOut=sOut.substring(0,cutAt);
      postfix=ellipsis;
    }

   if(hoverText.equals("true") || (hoverText.equals("auto") && maxLen!=-1 && s.length()>maxLen) )
    { //add hover text
      prefix="<span title=\""+s.replace('\"','\'')+"\">";
      postfix=postfix+"</span>";
    }

   return prefix+HtmlUtils.string2html(sOut)+postfix;

 }  // end formatMaxLengthEllipsis


 public String formatMaxLengthEllipsis(String txt, String startSeparator, String endSeparator, Dictionary formatParams)
 {
   return formatMaxLengthEllipsis(HtmlUtils.text2html(txt, startSeparator, endSeparator), formatParams);
 }

}
