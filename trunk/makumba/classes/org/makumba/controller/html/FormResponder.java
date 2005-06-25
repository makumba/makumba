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
import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;

import org.makumba.DataDefinition;
import org.makumba.Database;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaSystem;
import org.makumba.controller.http.Responder;

public class FormResponder extends Responder
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
  DataDefinition dd= MakumbaSystem.getTemporaryDataDefinition("Form responder"); // TODO: more precise name
  int max=0;

  Hashtable fieldParameters= new Hashtable();
  Hashtable fieldNames= new Hashtable();
  
  /** Format a field using the editor, and grow the editor as needed */
  public String format(String fname, FieldDefinition ftype, Object fval, Dictionary formatParams, String extraFormatting)
  {
    Dictionary paramCopy= (Dictionary)((Hashtable)formatParams).clone();
    FieldEditor.setSuffix(paramCopy, storedSuffix);
    FieldEditor.setExtraFormatting(paramCopy, extraFormatting);

    boolean display= (formatParams.get("org.makumba.noDisplay")==null);
    Integer i=(Integer)indexes.get(fname);
    if(i!=null)
      return display?editor.format(i.intValue(), fval, paramCopy):"";

    indexes.put(fname, new Integer(max));
    String colName=("col"+max);
    fieldNames.put(colName, fname);
    fieldParameters.put(colName, formatParams);
    dd.addField(MakumbaSystem.makeFieldWithName(colName, ftype));
    editor= new RecordEditor(dd, fieldNames, database);
    editor.config();
    max++;
    return display?editor.format(max-1, fval, paramCopy):"";
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
     if(!storedSuffix.equals(""))
       // no preamble for non-root forms (forms included in other forms)
       return;
     String sep=action.indexOf('?')>=0?"&":"?";	
     if(operation.equals("deleteLink"))
       {

 	// a root deleteLink
   	
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
 	  .append("\" ")
 	  .append(extraFormatting)
 	  .append(">");
 	
     
     
     }
 	
       
     else if(operation.equals("deleteForm")){
     	sb.append("<form action=");
     	sb.append("\""+action);
 		sb.append(sep);
 		sb.append(basePointerName);
 		sb.append("=");
 		sb.append(basePointer);
 		sb.append('&');
 		sb.append(responderName);
 		sb.append("=");
 		sb.append(getPrototype()+"\"");
     	
     	sb.append(" method=");
     	sb.append("\""+method+"\"");
     	if(multipart)
     	  sb.append(" enctype=\"multipart/form-data\" ");
     	sb.append(extraFormatting);
     	sb.append(">");
     	
     	
     	sb.append("<input type=\"submit\" ");
     	sb.append("value=\"");
     
     
     }
     else
       {
 	// a root form, translates into an HTML form
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
   
  public void writeFormPostamble(StringBuffer sb, String basePointer, String session) 
  {
  	if(storedSuffix.equals("") && operation.equals("deleteLink"))
  	{
  		// a root deleteLink
  		sb.append("</a>");
  		return;
  	}else if(storedSuffix.equals("") && operation.equals("deleteForm")){
  		sb.append("\"/>");
  	}
  	
  	if(basePointer!=null)
  		writeInput(sb, basePointerName, basePointer, storedSuffix);
	
	String responderValue = getPrototype()+storedSuffix+storedParentSuffix;
	String formSessionValue = responderValue + session; //gets the formSession value
	
	//writes the hidden fields
  	writeInput(sb, responderName, responderValue, "");
	if (multipleSubmitMsg != null && !multipleSubmitMsg.equals("")) {
		sb.append('\n');
		writeInput(sb, formSessionName, formSessionValue, "");	
		
		//insert the formSession into the database
		Database db = MakumbaSystem.getConnectionTo(database);
		Dictionary p = new Hashtable();
		p.put("formSession", formSessionValue);
		db.insert("org.makumba.controller.MultipleSubmit", p);
		db.close();
	}
	
	if(storedSuffix.equals(""))
  		// a root form
  		sb.append("\n</form>");
  }




  void writeInput(StringBuffer sb, String name, String value, String suffix)
  {
    sb.append("<input type=\"hidden\" name=\"")
      .append(name)
      .append(suffix)
      .append("\" value=\"")
      .append(value)
      .append("\">");
  }

  protected void postDeserializaton() {
      if (editor != null) {
          editor.initFormatters();
      }
  }

}
