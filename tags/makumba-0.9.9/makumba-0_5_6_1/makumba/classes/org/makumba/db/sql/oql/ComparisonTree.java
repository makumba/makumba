package org.makumba.db.sql.oql;
import org.makumba.abstr.FieldInfo;
import org.makumba.Pointer;
import java.util.*;

/** comparison operations have operands of the same type */
public class ComparisonTree extends AnalysisTree
{
  public ComparisonTree(Object left, int op, Object right){ super(left, op, right); }

  public Object guessParameterType(Object otherOperandType) 
  {
    return otherOperandType;
  }

  public void negociateOperandTypes(Object t1, Object t2)
       throws antlr.RecognitionException
  {
    if(right.makumbaType!=null && right.makumbaType.equals("null"))
      return;
    if(checkAssign(left, right) || checkAssign(right, left))
      return;
    super.negociateOperandTypes(t1, t2);
  }

  boolean checkAssign(AnalysisTree a1, AnalysisTree a2)
       throws antlr.RecognitionException
  {
    if(!(a1.makumbaType instanceof FieldInfo))
      return false;

    if(a2.leaf==null )
      return false;
    String s= a2.leaf.getText();

    if(a2.leaf.makumbaType.equals("char") || a2.leaf.makumbaType.equals("date"))
      s=s.substring(1, s.length()-1);

    Object o= null;
    try{
      o= ((FieldInfo)a1.makumbaType).checkValue(s);
    }catch(org.makumba.InvalidValueException e)
      { throw new antlr.SemanticException(e.getMessage());}
    if(o instanceof Pointer)
      { o= new Long(((Pointer)o).longValue()); }
    if(o instanceof Number)
      {
	a2.leaf.setText(o.toString());   
	a2.leaf.makumbaType="int";
      }
    else
      a2.leaf.setText("\""+o+"\"");
    return true;
  }
}
