package org.makumba.util;

import java.io.*;

/** this class accumulates a stream of data, and if it gets long, it automatically saves it in a file */
public class LongData 
{
  int length=0;

  LongDataStrategy ds= new EmptyStrategy();

  public LongData(){}

  public LongData(InputStream is) throws IOException { appendFrom(new InputStreamReader(is));  }

  public void appendFrom(Reader r) throws IOException { 
    char[]buffer= new char[org.makumba.Text.FILE_LIMIT];
    int n;
    while((n=r.read(buffer, 0, buffer.length))!=-1)
      {
	byte[] b=new String(buffer, 0, n).getBytes();
	ds.append(b, 0, b.length);
	length+=b.length;
      }
  }

  public void appendFrom(InputStream i) throws IOException 
  {
    byte[]buffer= new byte[org.makumba.Text.FILE_LIMIT];
    int n;
    while((n=i.read(buffer, 0, buffer.length))!=-1)
      {
	ds.append(buffer, 0, n);
	length+=n;
      }
  }

  public int getLength(){  return length; }

  public InputStream getInputStream() throws IOException{  return ds.getInputStream(); }

  interface LongDataStrategy
  {
    public InputStream getInputStream() throws IOException ;
    public void append(byte[] b, int start, int len) throws IOException;
  }

  class EmptyStrategy implements LongDataStrategy
  {
    public InputStream getInputStream() throws IOException 
    {
      return new ByteArrayInputStream(new byte[0], 0, 0);
    }

    public void append(byte[] b, int start, int len) throws IOException
    {
      if(length+len>= org.makumba.Text.FILE_LIMIT)
	ds= new FileStrategy();
      else
	ds= new DataStrategy(len);
      ds.append(b, start, len);  
    }
  }

  class DataStrategy implements LongDataStrategy
  {
    ByteArrayOutputStream bout;
    DataStrategy(int n){ bout= new ByteArrayOutputStream(n); }

    public InputStream getInputStream() throws IOException 
    { return new ByteArrayInputStream(bout.toByteArray(), 0, bout.size()); }
    
    public void append(byte[] b, int start, int len) throws IOException
      {
	if(length+len>= org.makumba.Text.FILE_LIMIT)
	  {
	    ds= new FileStrategy();
	    ds.append(bout.toByteArray(), 0, bout.size());
	    ds.append(b, start, len);
	    bout=null;
	  }
	else
	  bout.write(b, start, len);
      }
  }

  class FileStrategy implements LongDataStrategy
  {
    File temp;
    OutputStream out;

    FileStrategy() throws IOException
      {
	temp= File.createTempFile("makumbaLongContent", ".bin", null);
	temp.deleteOnExit();
	org.makumba.MakumbaSystem.getMakumbaLogger("util.longContent").fine("writing to "+temp);

	out= new BufferedOutputStream(new FileOutputStream(temp), org.makumba.Text.FILE_LIMIT);
      }
    
    public void append(byte b[], int start, int len) throws IOException
    {
      out.write(b, start, len);
    }
    
    public InputStream getInputStream() throws IOException
    {
      out.close();
      return new BufferedInputStream(new FileInputStream(temp));
    }

    protected void finalize()
    {
      temp.delete();
    }
  }
}
