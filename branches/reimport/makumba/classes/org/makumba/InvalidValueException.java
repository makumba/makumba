package org.makumba;

/** This exception occurs when an invalid value is passed to a field */
public class InvalidValueException extends RuntimeException
{
  public InvalidValueException(String message)
  {
    super(message);
  }

  public InvalidValueException(String field, String message)
  {
    super("Invalid value for "+field+": "+message);
  }

  public InvalidValueException(FieldDefinition fi, String message)
  {
    this(fi.getDataDefinition().getName()+"#"+fi.getName(), message);
  }

  /** form an exception message from the required type and the pointer that doesn't respect it */
  public InvalidValueException(FieldDefinition fi, Class requiredClass, Object value)
  {
    this(fi, "Required Java type:"+requiredClass.getName()+" ; given value: "+value+" of type "+value.getClass().getName());
  }


  /** form an exception message from the required type and the pointer that doesn't respect it */
  public InvalidValueException(FieldDefinition fi, String requiredType, Pointer wrongPointer)
  {
    this(fi, "Required poiter type:"+requiredType+" ; given value: "+wrongPointer);
  }

  /** form an exception message from the compared pointer and the pointer that doesn't match its type */
  public InvalidValueException(Pointer comparedPointer, Pointer wrongPointer)
  {
    super("Compared pointer: "+comparedPointer+" ; given value: "+wrongPointer);
  }
}
