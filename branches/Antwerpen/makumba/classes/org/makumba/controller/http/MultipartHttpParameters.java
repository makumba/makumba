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
import java.io.PushbackInputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.makumba.MakumbaSystem;
import org.makumba.Text;
import org.makumba.util.BoundaryInputStream;

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
    
    MakumbaSystem.getMakumbaLogger("fileUpload").fine("\n\n---- testing new code with Boundary ------\n");

    int index_b = request.getContentType().indexOf("boundary=");
    String boundaryWithoutMark = request.getContentType().substring(index_b+"boundary=".length());

    //as specified in the RFCs the boundary in the parts has '--' in the beginning
    String boundary = "--".concat(boundaryWithoutMark);

    try {	
	PushbackInputStream pis = new PushbackInputStream(request.getInputStream(), org.makumba.Text.FILE_LIMIT);

	//read the first line that countains just the boundary
	new Text(new BoundaryInputStream(pis, boundary));
	int var1 = pis.read();
	int var2 = pis.read();
	if (var1 != 13 || var2 != 10)
	    MakumbaSystem.getMakumbaLogger("fileUpload").severe("Mul: ERROR reading CRLF after the 1st boundary ["+var1+"]["+var2+"]");

	//go through all the request
	endOfRequest:
	while(true) {
	    
	    StringBuffer headers = new StringBuffer();
	    int NrBytesRead = 0;
	    int byteRead; int x;
	    
	    while(true) {
		if ((byteRead = pis.read()) == -1)
		    break endOfRequest;

		//---- reading the headers -------
		headers.append((char)byteRead);
		NrBytesRead++;
		
		//empty line: when we find 13 10 13 10 (CR LF CR LF)
		if (NrBytesRead > 2) {
		    
		    if (byteRead == 13 && (headers.charAt(NrBytesRead-3)) == 13 ) {
			x = pis.read(); //if  != 10
			// some browsers send CR LF CR CR so no -> throw new org.makumba.MakumbaError("Multipart Not recognized");
			MakumbaSystem.getMakumbaLogger("fileUpload").fine("MUL-endOfHeaders: ["+
									  (int)headers.charAt(NrBytesRead-3)+"|"+
									  (int)headers.charAt(NrBytesRead-2)+"] ["+
									  byteRead+"|"+
									  x+"]");
			headers.deleteCharAt(NrBytesRead-1); //remove last CR
			headers.deleteCharAt(NrBytesRead-2); //remove LF
			headers.deleteCharAt(NrBytesRead-3); //remove first CR
			
			MakumbaSystem.getMakumbaLogger("fileUpload").fine("MUL-headers ["+headers+"]");
			break; // we found the end of the headers
		    }
		}
		
		//test if it's the end of the request
		if (NrBytesRead == 2)
		    if (byteRead == '-' && (headers.charAt(NrBytesRead-1)) == '-' ) {
			//read the last CR LF after the ending '--'
			pis.read(); pis.read();
			break endOfRequest;
		    }
	    } //---- end of reading the headers

	    //---- processing the headers -----
	    String name="";
	    int index_h = headers.indexOf(" name=\"");
	    if (index_h!=-1) {
		int beginIndex = index_h + " name=\"".length();
		name = headers.substring(beginIndex, headers.indexOf("\"", beginIndex));
	    } else MakumbaSystem.getMakumbaLogger("fileUpload").warning("DEBUG-mul: ------ no \"name\" in the headers was found \n\n");
	    
	    //----------- check headers and save ---------------
	    // if it's  a file, we store it as Text
	    
	    index_h = headers.indexOf("filename=\"");
	    if (index_h != -1) {//fileName
		int beginIndex = index_h + "filename=\"".length();
		String fileName = headers.substring(beginIndex, headers.indexOf("\"", beginIndex));
		
		// -- remove / and \ that appear in some Operative Systems
		if (fileName.indexOf("\\") != -1) {
		    fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
		}
		else if(fileName.indexOf("/") != -1)
		    fileName=fileName.substring(fileName.lastIndexOf("/") + 1);
		
		if (fileName.length() > 0) {
		    String type;
		    Text contentToSave;
		    int contentSize;
		    
		    // --- check Content-Type ---
		    index_h = headers.indexOf("Content-Type:");
		    // some browsers Safari (Mac), Netscape in SUNs don't set Content-Type
		    // for some files e.g.: .cab (in both) and .class (in the SUN)
		    if(index_h != -1) {
			beginIndex = index_h + "Content-Type:".length();
			type = headers.substring(beginIndex).trim();
			
			if(type.indexOf("application/x-macbinary") != -1) {
			    MakumbaSystem.getMakumbaLogger("fileUpload").fine("Mac upload: application/x-macbinary");
			    // adler: this code was copied from the previous version
			    // it seems to work in the tests I did
			    byte[] temp_buff = new byte[128];
			    pis.read(temp_buff, 0, 128);
			}
			
		    } else {
			type = "application/octet-stream";
			MakumbaSystem.getMakumbaLogger("fileUpload").warning("no Content-Type found but there might be some content");
		    }
		    
		    // ---- read the content and set parameters
		    contentToSave = new Text(new BoundaryInputStream(pis, boundary));
		    contentSize = contentToSave.length();
		    
		    parameters.put(name+"_contentType", type);
		    parameters.put(name+"_filename", fileName);
		    parameters.put(name+"_contentLength", new Integer(contentSize));
		    parameters.put(name, contentToSave);
		    
		    MakumbaSystem.getMakumbaLogger("fileUpload").fine("Parameters set: contentType="+type
								      +", fileName="+fileName+", contentSize="+contentSize);
		} else { //no file -> nothing to upload
		    Text checkContent = new Text(new BoundaryInputStream(pis, boundary)); //to read the boundary
		    if (checkContent.length() > 0)
			MakumbaSystem.getMakumbaLogger("fileUpload").warning("no filename in the headers but there was some content (size="+checkContent.length()+", that was not put in the parameters");
		    parameters.put(name+"_contentType", "");
		    parameters.put(name+"_filename", "");
		    parameters.put(name+"_contentLength", new Integer(0));
		    parameters.put(name, org.makumba.Pointer.NullText);
		}
	    } else // no filename so it is a string parameter. We store it as String
		addParameter(name, new Text(new BoundaryInputStream(pis, boundary)).toString());
	    
	}//end of while(true)

    } catch(Exception e){throw new org.makumba.MakumbaError(e); }
  }//end of the method MultipartHttpParameters


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
 * - 1st line: boundary + CR+LF
 * - headers & values + CR+LF (e.g. filename="file.doc" Content-Type: application/octec-stream)
 * - CR+LF (Konqueror 3.2.1 sends CR CR)
 * - content (related to the headers just read)
 * - CR+LF
 * - boundary CR+LF
 * - headers... and so forth
 * ...
 * 
 * and after the last boundary you will have '--' with CR+LF
 */
