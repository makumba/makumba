package org.makumba.util;

/** This models a factory for named resources. Normally a subclass should just implement <a href= "#makeResource(java.lang.Object)">makeResource(name)</a>, which is called whenever the <a href= util.NamedResources.html#_top_>NamedResources</a> is requested an object that it doesn't hold. Still, there are some special cases:<ul>
<li> the <a href= util.NamedResources.html#_top_>NamedResources</a> keep objects in a hashtable. If the key for that hashtable corresponding to a certain name is not the name itself, the <a href="#getHashObject(java.lang.Object)">getHashObject(name)</a> method should be redefined. 
<li> if the process of constructing the resource needs the hash key, the <a href= "#makeResource(java.lang.Object, java.lang.Object)">makeResource(name, hashName)</a> method should be redefined, instead of  <a href= "#makeResource(java.lang.Object)">makeResource(name)</a>
<li> if the process of constructing the resource needs other resources, that in turn might need the resource which is just built, these resources should not be retreived in the makeResource() methods, since that would cause infinite loops. makeResource() should just build the resource, and  <a href= "#configureResource(java.lang.Object, java.lang.Object, java.lang.Object)">configureResource(name, hashName, resource)</a> should be redefined to do further resource adjustments.
</ul>

 */
public abstract class NamedResourceFactory implements java.io.Serializable
{
  protected Object supplementary;

  /** This method should make the resource with the given name.
   *
   * All exceptions thrown here will be caught and thrown further as 
   * RuntimeWrappedExceptions.
   * This method should not lead to the request of the same resource
   * from the NamedResources (by e.g. requesting another resource that 
   * needs to refer this resource). Such actions should be performed in
   * configureResource()
   */
  protected Object makeResource(Object name) 
       throws Throwable
  { throw new RuntimeException("should be redefined");}

  /** If the hash object for the resource is different from the name, 
   * this is the method used to build the named resource. 
   * All exceptions throws here will be caught and thrown further as 
   * RuntimeWrappedExceptions.
   * This method should not lead to the request of the same resource
   * from the NamedResources (by e.g. requesting another resource that 
   * needs to refer this resource). Such actions should be performed in
   * configureResource()
   */
  protected Object makeResource(Object name, Object hashName) 
       throws Throwable
  { return makeResource(name); }

  /** this method builds the hash object from the name of the object. By defaault, it returns the name */
  protected Object getHashObject(Object name)
       throws Throwable
  { return name;}

  /** This method is called immediately after the resource is built, but 
   * before making it accessible to other threads, and before the resource 
   * being returned to the client that requested it. 
   */
  protected void configureResource(Object name, Object hashName, Object resource)
       throws Throwable 
  {}

}
