package org.makumba.db.sql.oql;

/** like operands are surely char */
public class LikeTree extends ComparisonTree
{
  public LikeTree(Object left, int op, Object right){ super(left, op, right); }

  public Object guessParameterType() 
  {
    return "char";
  }
}
