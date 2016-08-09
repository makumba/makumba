package org.makumba;

/** Information about a makumba data definition as obtained from an MDD file or the structure of an OQL query result.
 * This class is provided for makumba programs to be able to introspect makumba data structures. Such introspection is not needed usually, as the application programmer knows the makumba data structure.
 */
public interface DataDefinition
{
  /** name of this data definition */
  public String getName();

  /** the names of the fields declared in this data definition, in the order of declaration */
  public java.util.Vector getFieldNames();
  
  /** the field with the respective name, null if such a field doesn't exist */
  public FieldDefinition getFieldDefinition(String name);

  /** the field with the respective index, null if such a field doesn't exist */
  public FieldDefinition getFieldDefinition(int n);
  
  /** tells whether this data definition was generated temporarily to depict a query result as opposed to being read from an MDD file */
  public boolean isTemporary();

  /** the title field indicated, or the default one */
  public String getTitleFieldName();

  /** which is the name of the index field (primary key), if any? */
  public String getIndexPointerFieldName();

  /** which is the name of the creation timestamp field, if any? */
  public String getCreationDateFieldName();

  /** which is the name of the modification timestamp field, if any? */
  public String getLastModificationDateFieldName();

  /** If this type is the data pointed to by a 1-1 pointer or subset, return the field definition in the main record, otherwise return null */
  public FieldDefinition getParentField();

  /** which is the name of the set member (Pointer, Character or Integer), for set subtypes */
  public String getSetMemberFieldName() ;
}
