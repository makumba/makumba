package org.makumba.db.sql.oql;
import antlr.collections.AST;
public class Projection
{
  public Projection(AST proj, AST expr, String as)
  { this.proj=(OQLAST)proj; this.expr= (OQLAST)expr; this.as=as; }

  OQLAST proj;
  String as;
  OQLAST expr;
}
