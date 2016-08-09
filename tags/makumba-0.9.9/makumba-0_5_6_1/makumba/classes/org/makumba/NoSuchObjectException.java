package org.makumba;

/** This exception occurs when a pointer or a condition is pased to the database for a delete or update operation but the object pointed does not exist in the database (anymore). This is a makumba API user error and it should be fixed, not caught. */
public class NoSuchObjectException extends RuntimeException
{
  /** indicate the missing pointer */
  public NoSuchObjectException(Pointer p) {super(""+p); }
}
