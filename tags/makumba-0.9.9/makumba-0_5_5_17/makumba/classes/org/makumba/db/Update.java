package org.makumba.db;

/** A delete, or update, prepared for execution */
public interface Update
{
  /** Execute the query with the given arguments */
  public int execute(Object [] args);
}
