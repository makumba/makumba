package org.makumba.util;
import java.util.*;

/** Keeps a number of identical creation-expensive resources. Avoids resource re-creation/deletion
 * @author cristi
 */
public abstract class ResourcePool
{
  Stack stack= new Stack();
    
    // we keep a reference to all our resources here
    Vector all= new Vector();

  /** re-define this method to express how to create a resource */
  public abstract Object create() throws Exception;

    Object createAndCount() throws Exception
    {
	Object o= create();
	all.addElement(o);
	org.makumba.MakumbaSystem.getMakumbaLogger("util.pool").fine("pool size: "+ all.size());
	return o;	
    }

  /** initialize the pool with n resources */
  public void init(int n) throws Exception
  {
    for(;n>0;n--)
      put(createAndCount());
  }
  
  /** get one resource */
  public Object get() throws Exception
  {
    synchronized(stack)
      {
	if(stack.isEmpty())
	    return createAndCount();
	return stack.pop();
      }
  }
  
  /** put back one resource */
  public void put(Object o)
  { 
      // FIXME: this leaves a door open for resources to be added to the pool
      // without having been created by the pool. 
      // we may want this or we may not. 
      // if we want it, these resources should be added to "all"
      // if not, they should be rejected (and then "all" should probably be a hashmap or so
    synchronized(stack)
      {
	stack.push(o); 
      }
  }
  
  public void reset()
  {
    stack= new Stack();
    all= new Vector();
  }
}
