package org.makumba.util;
import java.util.*;

/** Keeps a number of identical creation-expensive resources. Avoids resource re-creation/deletion
 * @author cristi
 */
public abstract class ResourcePool
{
  Stack stack= new Stack();
  
  /** re-define this method to express how to create a resource */
  public abstract Object create() throws Exception;

    int number=0;

    Object createAndCount() throws Exception
    {
	number++;
	org.makumba.MakumbaSystem.getMakumbaLogger("util.pool").fine("pool size: "+number);
	return create();
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
    synchronized(stack)
      {
	stack.push(o); 
      }
  }
  
  public void reset()
  {
    stack= new Stack();
  }
}
