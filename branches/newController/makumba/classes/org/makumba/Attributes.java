package org.makumba;

/** The attributes provided by a makumba environment (eg a http session).
 * Attributes can be referred and assigned to in business logic code. 
 */
public interface Attributes
{
  /** Get the attribute with the given name. 
    @param name the name of the attribute
    @return the attribute value
   *@throws LogicException if a business logic problem occured while trying to determine the attribute
   *@throws AttributeNotFoundException if there was no error but the attribute could not be found.
   */
  public Object getAttribute(String name) throws LogicException;
}
