package org.makumba.util;
import java.util.*;

interface NameValueReturner 
{
  Object getRes();
}

class NameValue implements NameValueReturner, java.io.Serializable
{
  Object value;
  NameValueReturner returner;

  NameValue(Object name, Object hashName, NamedResourceFactory f)
  {
    ProducerReturner pr= new ProducerReturner();
    pr.nv= this;
    pr.f=f;
    pr.name= name;
    pr.hashName= hashName;
    returner= pr;
  }
  
  public Object getRes()
  { 
    return value; 
  }
  
  synchronized Object getResource() { return returner.getRes(); }
}


class ProducerReturner implements NameValueReturner, java.io.Serializable
{
  NameValue nv;
  Object name, hashName;
  NamedResourceFactory f;
  
  public Object getRes()
  {
    nv.returner= new ErrorReturner(name);
    try{
      nv.value= f.makeResource(name, hashName);
      // further calls from this thread (during configure) will return 
      // immediately
      nv.returner= nv;
      f.configureResource(name, hashName, nv.value);
    }catch(RuntimeException e)
      { 
	nv.returner= this;
	throw e; 
      }
    catch(Throwable t)
      {
	nv.returner= this;
	throw new RuntimeWrappedException(t); 
      }

    return nv.value;
  } 
}

class ErrorReturner implements NameValueReturner, java.io.Serializable
{
  Object name;

  ErrorReturner(Object n){ name= n; }

  public Object getRes(){ throw new RuntimeException("Resource attempts to re-make itself: "+name+" . Use the NamedResourceFactory.configure(Object) method"); }
}
