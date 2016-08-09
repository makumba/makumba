package org.makumba;

/** This exception occurs when a field is indicated as being subset or subrecord (eg for the insert method) and it is not. This is a makumba API user error and it should be fixed, not caught. */
public class InvalidFieldTypeException extends RuntimeException
{
  public InvalidFieldTypeException(FieldDefinition f, String expectedType ) 
  {super(f.getDataDefinition().getName()+"->"+f.getName()+" is not a "+expectedType); } 

  public InvalidFieldTypeException(String explanation ) 
  {super(explanation); }
}
