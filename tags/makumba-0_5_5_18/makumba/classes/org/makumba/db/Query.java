package org.makumba.db;
import org.makumba.*;
import java.util.*;

/** A query prepared for execution. Returns all its results at once, in a Vector */
public interface Query
{
  /** Execute the query with the given arguments */
  public Vector execute(Object [] args);

  /** Get the data type returned by the query, as given by its SELECT section */
  public DataDefinition getResultType();

  /** Get the data type of the given label */
  public DataDefinition getLabelType(String label);
}
