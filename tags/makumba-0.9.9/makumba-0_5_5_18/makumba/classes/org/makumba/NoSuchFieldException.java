package org.makumba;

/** This exception occurs when a field of a makumba type is mentioned but does not exist. This is a programmer error, it should be fixed, not caught */
public class NoSuchFieldException extends RuntimeException
{
  /** indicate the missing pointer */
  public NoSuchFieldException(DataDefinition dd, String message) 
  {
    super("type "+dd.getName()+" : "+message);
  }
}
