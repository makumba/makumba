//////////////////////////////////
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

package org.makumba.controller.http;
import javax.servlet.http.HttpServletRequest;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.PushbackInputStream;
import org.makumba.Text;
import org.makumba.util.BoundaryInputStream;

/* to delete after testing */
/* 
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.text.Format;
import java.text.SimpleDateFormat;
*/
/* - end to delete - */

/** Parse the input stream of a http request as a multipart/form-data. 
 *  Store uploaded files as org.makumba.Text.
 *  Normal http parameters are stored as Text.toString (simple) or Vectors (multiple) 
 */
public class MultipartHttpParameters extends HttpParameters
{
  Hashtable parameters=new Hashtable();

  void computeAtStart(){}
    
  public boolean knownAtStart(String s)
  {
    return parameters.get(s)!=null;  
  } 

  public MultipartHttpParameters(HttpServletRequest req)
  {
    super(req);
    
    System.out.println("\n\n---- testing new code with Boundary ------\n");
    try{  // testing the BIS

	//System.out.println("DEBUG-mul: contentType: ["+request.getContentType()+"]");
	int index_b = request.getContentType().indexOf("boundary=");
	String boundaryWithoutMark = request.getContentType().substring(index_b+"boundary=".length());
	//System.out.println("DEBUG-mul: boundary without -- ["+boundaryWithoutMark+"]");
	//as specified in the RFCs the boundary in the parts has '--' in the beginning
	String boundary = "--".concat(boundaryWithoutMark);
	
	PushbackInputStream pis = new PushbackInputStream(request.getInputStream(), org.makumba.Text.FILE_LIMIT);

	//read the first line that countains just the boundary
	new Text(new BoundaryInputStream(pis, boundary));
	int var1 = pis.read();
	int var2 = pis.read();
	if (var1 != 13 || var2 != 10)
	    System.out.println("*DEBUG-mul: ERROR reading CRLF after the 1st boundary ->["+var1+"]["+var2+"]");

	//go through all the request
	while(pis.available()>0) {
    
	    StringBuffer header = new StringBuffer();
	    int NrBytesRead = 0;
	    boolean emptyLineFound = false;
	    int byteRead; int x;
	    boolean endOfRequest = false;

	    while ( (byteRead = pis.read()) != -1) {
		//---- reading the header -------
		header.append((char)byteRead);
		NrBytesRead++;
		
		//empty line: when we find 13 10 13 10 (CR LF CR LF)
		if (NrBytesRead > 2) {

		    if (byteRead == 13 && (header.charAt(NrBytesRead-2)) == 10 && (header.charAt(NrBytesRead-3)) == 13 ) {
			if ( (x = pis.read()) != 10)
			    throw new org.makumba.MakumbaError("Multipart Not recognized"); //read the ´LF' that comes after the 'CR' (13)
			//System.out.println("DEBUG-mul: ending of the header+empty Char["+
			//(int)header.charAt(NrBytesRead-3)+"]["+(int)header.charAt(NrBytesRead-2)+"] and ["+(int)header.charAt(NrBytesRead-1)+"]["+x+"]");
			
			header.deleteCharAt(NrBytesRead-1); //remove last CR
			header.deleteCharAt(NrBytesRead-2); //remove LF
			header.deleteCharAt(NrBytesRead-3); //remove first CR
			//System.out.println("DEBUG-mul: removed CRLF from the ending of the header");
			System.out.println("DEBUG-mul: header ["+header+"]");
			emptyLineFound = true;
		    }
		}
		if (emptyLineFound) break;
		
		//test if it's the end of the request
		if (NrBytesRead == 2)
		    if (byteRead == '-' && (header.charAt(NrBytesRead-1)) == '-' ) {
			//read the last CR LF after the ending '--'
			pis.read(); pis.read();
			endOfRequest = true;
		    }
	    }//---- end of reading the header

	    if (!endOfRequest) {
		//---- processing the header -----
		String name="";
		int index_h = header.indexOf(" name=\"");
		if (index_h!=-1) {
		    int beginIndex = index_h + " name=\"".length();
		    name = header.substring(beginIndex, header.indexOf("\"", beginIndex));
		} else System.out.println("DEBUG-mul: ------ no \"name\" in the header was found \n\n");
		
		//----------- check headers and save ---------------
		// if it's  a file, we store it as Text

		index_h = header.indexOf("filename=\"");
		if (index_h != -1) {//fileName
		    int beginIndex = index_h + "filename=\"".length();
		    String fileName = header.substring(beginIndex, header.indexOf("\"", beginIndex));

		    // -- remove / and \ that appear in some Operative Systems
		    if (fileName.indexOf("\\") != -1) {
			fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
		    }
		    else if(fileName.indexOf("/") != -1)
			fileName=fileName.substring(fileName.lastIndexOf("/") + 1);

		    parameters.put(name+"_filename", fileName);		    
		    
		    index_h = header.indexOf("Content-Type:");
		    // some browsers Safari (Mac), Netscape in SUNs don't set Content-Type
		    // for some files e.g.: .cab (in both) and .class (in the SUN)
		    if(index_h != -1) {
			beginIndex = index_h + "Content-Type:".length();
			String type= header.substring(beginIndex).trim();
			//System.out.println("DEBUG-mul: Content-Type ["+type+"]");

			parameters.put(name+"_contentType", type);
			
			if(type.indexOf("application/x-macbinary") != -1) {
			    //throw new org.makumba.MakumbaError("upload is not supported for Apple Mac");
			    System.out.println("\n\nDEBUG-mul: -#######- Mac upload -######- \n");
			    // adler: this code was copied from the previous version
			    // it seems to work in the tests I did
			    byte[] temp_buff = new byte[128];
			    pis.read(temp_buff, 0, 128);
			}
			Text contentToSave = new Text(new BoundaryInputStream(pis, boundary));
			int contentSize = contentToSave.length();
// ----------
/*
  Format formatter = new SimpleDateFormat("mm.ss");
  String check_time = formatter.format(new Date());
  System.out.println("DEBUG-mulX: contentSize = "+contentSize);
  OutputStream fileout = new FileOutputStream("public_html/testfiles/n1-"+check_time+"-"+fileName);
  contentToSave.writeTo(fileout);
*/
// ----------

                        parameters.put(name+"_contentLength", new Integer(contentSize));
			parameters.put(name, contentToSave);
		    } else // there is no Content-Type 
			if ( fileName.length() > 0 ) {
			    System.out.println("DEBUG-mul: no Content-Type found but there might be some content");
			    parameters.put(name+"_contentType", "text/plain"); //not sure which type should I put
			    Text contentToSave = new Text(new BoundaryInputStream(pis, boundary));
			    int contentSize = contentToSave.length();

// ----------
/*
  Format formatter = new SimpleDateFormat("mm.ss");
  String check_time = formatter.format(new Date());
  System.out.println("DEBUG-mulX: contentSize = "+contentSize);
  OutputStream fileout = new FileOutputStream("public_html/testfiles/n2-"+check_time+"-"+fileName);
  contentToSave.writeTo(fileout);
*/
// ----------

			    parameters.put(name+"_contentLength", new Integer(contentSize));
			    System.out.println("DEBUG-mul: no content type found but file with size = "+contentSize);
			    parameters.put(name, contentToSave);
			}
			else {
			    // no content type & no filename in the header
			    // not sure when this happens...
			    System.out.println("* * DEBUG-mul: no Content-Type found and no filename * *");
			    parameters.put(name+"_contentType", "");
			    parameters.put(name+"_contentLength", new Integer(0));
			    parameters.put(name, org.makumba.Pointer.NullText);
			}
		    
		} // end if "filename"
		// if it's a string parameter we store it as String
		else addParameter(name, new Text(new BoundaryInputStream(pis, boundary)).toString());
		
	    } // end if (!endOfRequest)
	    
	}//end of while(pis.available())
	
    }
    catch (IllegalArgumentException e) { System.out.println("DEBUG-mul: -- end: nothing to push back");}
    catch(Throwable t){t.printStackTrace();}

    //System.out.println("\n\n\n---- END new code with Boundary ------\n");

  }


  void addParameter(String name, String value)
  {
    Object o= parameters.get(name);
    if(o!=null)
      if(o instanceof Vector)
	((Vector)o).addElement(value);
      else
	{
	  Vector v= new Vector();
	  v.addElement(o);
          v.addElement(value);
          parameters.put(name, v);
	}
    else
      parameters.put(name, value);
  }

  
  /** we compose what we read from the multipart with what we have in the query string.
   * the assumption is that the multipart cannot change during execution, while the query string may change due to e.g. forwards
   */
  public Object getParameter(String s)
  {
    return compose(parameters.get(s), super.getParameter(s));
  }
  
  /** compose two objects, if both are vectors, unite them */
  static Object compose(Object a1, Object a2)
  {
    if(a1==null)
      return a2;
    if(a2==null)
      return a1;

    if(a1 instanceof Vector)
      if(a2 instanceof Vector)
	{
  	  for(Enumeration e= ((Vector)a2).elements(); e.hasMoreElements(); )
	    ((Vector)a1).addElement(e.nextElement());
	  return a1;
	}
      else
	{
	  ((Vector)a1).addElement(a2);
	  return a1; 
	}
    else
      if(a2 instanceof Vector)
	{
	  ((Vector)a2).addElement(a1);
	  return a2; 
	}
      else
	{
	  Vector v= new Vector();
	  v.addElement(a1);
	  v.addElement(a2);
	  return v;
	}
  }
  
}

/* data inside the request
 * - 1st line: boundary CR+LF
 * - header & values CR+LF (e.g. Content-Type: application/octec-stream)
 * - CR+LF
 * - content (related to the header just read)
 * - CR+LF
 * - boundary CR+LF
 * - header... and so forth
 * ...
 * 
 * and after the last boundary you will have '--' with CR+LF
 */
