package org.makumba;

/** This class is a facade for the internal OQL analyzer. To obtain an instance of this class, use {@link MakumbaSystem#getOQLAnalyzer(java.lang.String)}
 * @since 0.5.5.10
 */
public interface OQLAnalyzer
{
    /** Get the original OQL query that is analyzed by this object 
   */
  String getOQL();

    /** Get the type of the fields between SELECT and FROM 
   * @return a DataDefinition containing in the first field the type and name of the first OQL projection,the second field the type and name of the second OQL projection $2 etc 
   */
  DataDefinition getProjectionType();

    /** Get the type of a label used within the OQL query
   *@return the type of the label as declared in the FROM part of the query
   */
  DataDefinition getLabelType(String labelName);

    /** Get the types of the query parameters, as resulted from the OQL analysis.
   * @return a DataDefinition containing in the first field the type of the OQL parameter $1, the second field the type of the OQL parameter $2 etc 
   */
  org.makumba.DataDefinition getParameterTypes();

    /** Get the total number of OQL parameters in the query; like $1, $2 etc. Note that if e.g. $1 appears twice it will be counted twice 
     @see #parameterAt(int)
   */
  int parameterNumber();
  
    /** Get the number of the parameter mentioned at the position indicated by the given index.
  * OQL parameters may not get mentioned in the order of their $number, e.g. $1 may not appear first in the query, $2 may not appear second in the query, etc
  @see #parameterNumber()
  */
  public int parameterAt(int index); 

}
