package org.makumba.controller.http;
import javax.servlet.http.*;
import java.util.*;

/** Parse the input stream of a http request as a multipart/form-data. 
 * Store uploaded files as org.makumba.Text. Normal http parameters are stored as Strings (simple) or Vectors (multiple) 
 */
public class MultipartHttpParameters extends HttpParameters
{
  Hashtable parameters=new Hashtable();

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
		String type= header.substring(n+13);
	    
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
    
    // now we take http parameters from the query string and put them together with the others
    for(Enumeration e= request.getParameterNames(); e.hasMoreElements(); )
      {
	String nm=(String)e.nextElement();
	String [] val=request.getParameterValues(nm);
	for(int i=0; i<val.length; i++)
	  addParameter(nm, val[i]);
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
  
  public Object getParameter(String s)
  {
    Object value= parameters.get(s);
    
    if(value==null)
      return null;

    //    request.setAttribute(s, value);
    return value;
  }

}
