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
import org.makumba.controller.http.Responder;
import org.makumba.controller.http.RequestAttributes;
import org.makumba.abstr.RecordInfo;
import org.makumba.abstr.FieldInfo;

import java.util.Dictionary;
import java.util.Hashtable;
import javax.servlet.http.HttpServletRequest;

public class FormResponder extends Responder
{
  RecordEditor editor;
  
  /** reads the data submitted to the controller by http, also sets the values in the request so they can be retrieved as attributes  */
  public Dictionary getHttpData(HttpServletRequest req, String suffix)
  {
    if(editor!=null)
      return editor.readFrom(req, suffix);
    else
      return new Hashtable(1);
  }

  Hashtable indexes=new Hashtable();
  RecordInfo dd= new RecordInfo();
  int max=0;

  Hashtable fieldParameters= new Hashtable();
  Hashtable fieldNames= new Hashtable();
  
  /** Format a field using the editor, and grow the editor as needed */
  public String format(String fname, Object ftype, Object fval, Dictionary formatParams)
  {
    FieldEditor.setSuffix(formatParams, suffix);
    Integer i=(Integer)indexes.get(fname);
    if(i!=null)
      return editor.format(i.intValue(), fval, formatParams);

    indexes.put(fname, new Integer(max));
    String colName=("col"+max);
    fieldNames.put(colName, fname);
    fieldParameters.put(colName, (Dictionary)((Hashtable)formatParams).clone());
    dd.addField(FieldInfo.getFieldInfo(colName, ftype, true));
    editor= new RecordEditor(dd, fieldNames, database);
    editor.config();
    return editor.format(max++, fval, formatParams);
  }

  public String responderKey()
  {
    return ""+fieldNames+fieldParameters+super.responderKey();
  }

  protected String action;
  protected String method="GET";
  protected boolean multipart;
  StringBuffer extraFormatting;

  public void setAction(String action){ this.action=action; }
  public String getAction(){ return action; }
  public void setMultipart(boolean multipart){ this.multipart=multipart; }
  public void setMethod(String method) {this.method=method; }
  public void setExtraFormatting(StringBuffer extraFormatting)
  { this.extraFormatting=extraFormatting; }

  public void writeFormPreamble(StringBuffer sb, String basePointer) 
  {
    if(operation.equals("delete"))
      {
	String sep=action.indexOf('?')>=0?"&":"?";
	sb.append("<a href=\"")
	  .append(action)
	  .append(sep)
	  .append(basePointerName)
	  .append("=")
	  .append(basePointer)
	  .append('&')
	  .append(responderName)
	  .append("=")
	  .append(getPrototype())
	  .append("\">");
      }
    else
      {
	sb.append("<form action=");
	sb.append("\""+action+"\"");
	sb.append(" method=");
	sb.append("\""+method+"\"");
	if(multipart)
	  sb.append(" enctype=\"multipart/form-data\" ");
	sb.append(extraFormatting);
	sb.append(">");
      }
  }
  
  public void writeFormPostamble(StringBuffer sb, String basePointer) 
  {
    if(operation.equals("delete"))
      {
	sb.append("</a>");
	return;
      }
    if(basePointer!=null)
      writeInput(sb, basePointerName, basePointer);
    writeInput(sb, responderName, ""+getPrototype());
    sb.append("</form>");
  }
  
  void writeInput(StringBuffer sb, String name, String value)
  {
    sb.append("<input type=\"hidden\" name=\"")
      .append(name)
      .append("\" value=\"")
      .append(value)
      .append("\">");
  }

}
