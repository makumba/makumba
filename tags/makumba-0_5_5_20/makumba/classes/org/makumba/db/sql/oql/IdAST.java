package org.makumba.db.sql.oql;
import org.makumba.abstr.*;
import org.makumba.db.Database;

public class IdAST extends OQLAST
{
  QueryAST query;
  String field;
  String label;
  FieldInfo fieldInfo;
  String projectionLabel;

  public IdAST(){}
  //  public IdAST(antlr.Token t) { super(t); }

  public String writeInSQLQuery(Database d)
  {
    if(projectionLabel!=null)
      return projectionLabel;
    return label+"."+query.getFieldName(label, field, d);
  }
}
