package org.makumba.db.sql;

/** this exception is thrown during operation with PreparedStatements when a lost connection is
 * detected and reconection is succesful */
public class ReconnectedException extends org.makumba.util.RuntimeWrappedException
{
  public ReconnectedException(java.sql.SQLException se){super(se); }
}

