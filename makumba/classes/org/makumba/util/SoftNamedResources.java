package org.makumba.util;
import java.util.*;
import java.lang.ref.SoftReference;

/** A NamedResources that keeps its resources as soft references
 *@see org.makumba.util.NamedResources
*/
public class SoftNamedResources extends NamedResources
{
  public SoftNamedResources(String name, NamedResourceFactory f)
    { super(name, f); }

  protected NameValue getNameValue(Object name, Object hash)
  {
    NameValue nv=null;
    SoftReference sr=(SoftReference)values.get(hash);
    final Object h= hash;
    if(sr==null || (nv=(NameValue)sr.get())==null)
      values.put(hash, new SoftReference(nv=new NameValue(name, hash, f))
		 {
		   // possible improvement: rather redefine get(), 
		   // if it returns null, re-make the resource?
		   public void clear()
		     { 
		       super.clear();
		       values.remove(h);
		     }
		 }); 
    return nv;
  }
}
