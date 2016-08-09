package org.makumba.util;
import java.net.URL;

public class ClassResource
{
  public static URL get(String s)
  {
    URL u=null;
    try{
      u=ClassResource.class.getClassLoader().getResource(s);
    }catch(RuntimeException e){}
    if(u==null)
      u= ClassLoader.getSystemResource(s);
    //    new Throwable().printStackTrace();
    return u;
  }
}
