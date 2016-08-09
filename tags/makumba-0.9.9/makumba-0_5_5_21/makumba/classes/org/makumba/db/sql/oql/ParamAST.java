package org.makumba.db.sql.oql;
import org.makumba.abstr.*;
import org.makumba.db.Database;

public class ParamAST extends OQLAST
{
  public String toString(){ return "$"+number; }
  public ParamAST(){};

  public String writeInSQLQuery(Database d)
  {
    return "?";
  }

  int number;
}
