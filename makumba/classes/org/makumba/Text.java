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

package org.makumba;
import java.io.*;
import org.makumba.util.LongData;
import org.makumba.util.RuntimeWrappedException;

/** A makumba text, a byte array that can be converted to String when needed. 
 * When texts get big, they are swapped on disk. Text swapping is logged (see {@link java.util.logging.Logger}, {@link org.makumba.MakumbaSystem#setLoggingRoot(java.lang.String)} in the logger <b><code>"abstr.longContent"</code></b> with {@link java.util.logging.Level#FINE} logging level.
 */
public class Text
{
  // first alternative
  InputStream source;
  int len;
  
  // second alternative
  LongData ld;

  public static int FILE_LIMIT;
  static{
    String s= System.getProperty("makumba.long-content");
    if(s!=null)
      FILE_LIMIT=Integer.parseInt(s.trim());
    else
      FILE_LIMIT=32768;
  }

  /** make a Text from the given input stream, with the given length
    The content in the input stream must match the length.
    No diskswap will be attempted. If you wish for the info to be swapped to disk, use the constructor Text(InputStream)*/
  public Text(InputStream is, int length)
  {
    this.source=is;
    this.len=length;
  }

  /** make a Text by reading the input stream. If there is a lot of data, it will be swapped to disk */
  public Text(InputStream is) 
  {
    try{
      ld= new LongData();
      ld.appendFrom(is);
      this.source=ld.getInputStream();
      this.len=ld.getLength();
    }catch(IOException e) { throw new RuntimeWrappedException(e);} 
  }

  /** make a Text from the String. If there is a lot of data, it will be swapped to disk */
  public Text(String s)
  {
    this(s.getBytes());
  }

  /** make a Text from the given byte array. If there is a lot of data, it will be swapped to disk */
  public Text(byte[] b)
  {
    this(b, 0, b.length);
  }

  /** make a Text from the given byte array. If there is a lot of data, it will be swapped to disk */
  public Text(byte[] b, int start, int length)
  {
    len=length;
    try{
      ld= new LongData();
      ld.appendFrom(new ByteArrayInputStream(b, start, length));
      source= ld.getInputStream();
    }catch(IOException e) { throw new RuntimeWrappedException(e); }

  }

  /** Read the text content as a binary stream. Recommended for long content */
  public InputStream toBinaryStream() 
  { 
    if(source==null)
      throw new MakumbaError("texts indicated by stream can only be consumed once");
    if(ld!=null)
      try{
      return ld.getInputStream();
    }catch(IOException e) {throw new RuntimeWrappedException(e); }
    InputStream s=source;
    source=null;
    return s;
  }
  
  public int length(){ return len; }

  /** convenience method for making a text out of another Text, InputStream, String, or byte[] */
  public static Text getText(Object value)
  {
    if(value instanceof Text)
      return (Text)value;
    if(value instanceof java.io.InputStream)
      return new Text((InputStream)value);
    if(value instanceof String)
      return new Text((String)value);
    if(value instanceof byte[])
      return new Text((byte[])value);
    throw new InvalidValueException("unsupported type to make text of "+value.getClass());
  }
  
  /** converts the text content to a  string */
  public String toString() 
  { 
    ByteArrayOutputStream bo= new ByteArrayOutputStream(len<FILE_LIMIT?len:FILE_LIMIT);
    try{
      writeTo(bo);
    }catch(IOException e){ throw new RuntimeWrappedException(e); } 
    return new String(bo.toByteArray());
  } 

  /** write content to the given output stream */
  public void writeTo(OutputStream o) throws IOException
  {
    byte[] b= new byte[len<FILE_LIMIT?len:FILE_LIMIT];
    InputStream is= toBinaryStream();
    
    try{
      int n;
      while((n=is.read(b, 0, b.length))!=-1)
	o.write(b, 0, n);
    }finally{
      o.close();
      is.close();
    }
  }

  /** compare the content to the content of a given stream, and closes the stream */
  public boolean compare(InputStream is) throws IOException
  {
    InputStream is1= toBinaryStream();
    try{
      int n, m;
      int i=0;
      while((n=is.read())!=-1)
	if(n!=(m=is1.read()))
	  {
	    MakumbaSystem.getMakumbaLogger("debug.abstr").severe(m+" "+ n+" "+i);
	    return false;
	  }
      else
	i++;
      return is1.read()==-1;
    }finally{ is.close(); is1.close(); }
  }

  /** Compares the content of objects. */
  public boolean equals(Object other) 
  { 
    if(this.getClass() != other.getClass())
	return false;
    if(this.length()!=((Text)other).length())
	return false;
    try{
	return this.compare( ((Text)other).toBinaryStream() );
    }catch(IOException e){ return false; } 
  } 


}
