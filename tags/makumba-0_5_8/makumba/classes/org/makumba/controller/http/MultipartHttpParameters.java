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

package org.makumba.controller.http;
import javax.servlet.http.*;
import java.util.*;

/** Parse the input stream of a http request as a multipart/form-data. 
 * Store uploaded files as org.makumba.Text. Normal http parameters are stored as Strings (simple) or Vectors (multiple) 
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

    byte [] bytes;

    // the index needed to go thru the request
    int index=0; 

    // first we copy the request in an array
    bytes= new byte[request.getContentLength()];

    int bytesRead=0;
    for(int totalRead=0; totalRead < bytes.length; totalRead+=bytesRead )
      try{
      bytesRead=request.getInputStream().read(bytes, totalRead, bytes.length - totalRead);
      if(bytesRead==-1)
	throw new org.makumba.MakumbaError("attempt to read the multipart stream twice during one request");
    }catch(Exception e){throw new org.makumba.MakumbaError(e); }
    
    // now we find out the multipart boundary
    for(; index < bytes.length && bytes[index]!=13; index++);
    
    String boundary= new String(bytes, 0, index-1);
    
    index++;
    
    // now we go thru each section (file or http parameter)
    while(index<bytes.length)
      {
	// determine a header
	int h=index;
	while(bytes[index] != 13 ||  bytes[index + 2] != 13)
	  index++;
	
	String header = new String(bytes, h, index-h);
	index+=4;

	// determine the http parameter name
	String name="";
	int n= header.indexOf(" name"+"=\"");
	if(n!=-1)
	  name=header.substring(n+7, header.indexOf("\"", n+7));

	// read the rest of the section until the boundary
	int j=0;
	int start=index;
	int end=0;
        while(index<bytes.length)
        {
	  if(bytes[index] == (byte)boundary.charAt(j))
            {
	      if(j==boundary.length()-1)
                {
		  end = index - boundary.length() - 1;
		  break;
                }
	      index++;
	      j++;
            } 
	  else
            {
	      index++;
	      j=0;
            }
        } 
	index++;
	
       
	// if it's  a file, we store it as Text
	n=header.indexOf("filename");
	if(n !=-1)
	  {
	    String fn=header.substring(n+10, header.indexOf("\"", n+10));
	    if(fn.indexOf("\\")!=-1)
	      fn=fn.substring(fn.lastIndexOf("\\")+1);
	    else
	      if(fn.indexOf("/")!=-1)
		fn=fn.substring(fn.lastIndexOf("/")+1);
	    parameters.put(name+"_filename", fn);
			   
	    n= header.indexOf("Content-Type:");
	    if(n!=-1)
	      {
		String type= header.substring(n+13).trim();
	    
		parameters.put(name+"_contentType", type);
		if(type.indexOf("application/x-macbinary") !=-1)
		  start+=128;
		parameters.put(name+"_contentLength", new Integer(end-start));
		parameters.put(name, new org.makumba.Text(bytes, start, end-start));
	      }
	    else
	      {
		// no content type -> no content
		parameters.put(name+"_contentType", "");
		parameters.put(name+"_contentLength", new Integer(0));
		parameters.put(name, org.makumba.Pointer.NullText);
	      }
	  }
	else
	  // if it's a string parameter we store it as String
	  addParameter(name, new String(bytes, start, (end- start)));
	
	// are we at the end?
	if((char)bytes[index + 1] == '-')
	  break;
	index+=2;
      }
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
	  ((Vector)o).addElement(value);
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
