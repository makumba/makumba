package org.makumba.db.sql.oql;

/** additive operations take their type from any of the operands, and have operands of the same type */
public class AdditiveTree extends AnalysisTree
{
  public AdditiveTree(Object left, int op, Object right){ super(left, op, right); }

  public Object computeTypeFromOperands() { return left.makumbaType; }

  public Object guessParameterType(Object otherOperandType) 
  {
    return otherOperandType;
  }
}
