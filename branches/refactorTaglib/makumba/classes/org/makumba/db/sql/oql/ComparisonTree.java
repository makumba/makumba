///////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003  http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//  -------------
//  $Id$
//  $Name$
/////////////////////////////////////

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
    if(t1.equals("timestamp") && t2.equals("datetime")
       ||t2.equals("timestamp") && t1.equals("datetime") )
      return;

    if(right.makumbaType!=null && right.makumbaType.equals("nil"))
      return;
    if(checkAssign(left, right) || checkAssign(right, left))
      return;
    super.negociateOperandTypes(t1, t2);
  }

  /** assume that a2 is a constant and check if it's compatible with a1 */
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
