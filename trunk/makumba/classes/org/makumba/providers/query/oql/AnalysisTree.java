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

package org.makumba.providers.query.oql;
import org.makumba.FieldDefinition;

public class AnalysisTree
{
  public static final int OR=1;
  public static final int AND=2;
  public static final int SIM_COMP=3;
  public static final int ASIM_COMP=4;
  public static final int LIKE=5;
  public static final int ADD=6;
  public static final int MUL=7;
  public static final int CONCAT=9;
  public static final int UNION=9;
  public static final int EXCEPT=9;
  public static final int INTERSECT=9;
  public static final int IN=10;
  
  OQLAST leaf;
  Object makumbaType;
  int op;
  AnalysisTree left, right;
  
  public AnalysisTree(OQLAST leaf) { this.leaf=leaf; }
  
  public AnalysisTree(Object left, int op, Object right)
  {
    this.left=getTree(left);
    this.right=getTree(right);
    this.op=op;
  }
  
  AnalysisTree getTree(Object o) 
  {
    if(o instanceof AnalysisTree)
      return (AnalysisTree)o;
    OQLAST ast= (OQLAST)o;
    if(ast.tree==null)
      return new AnalysisTree(ast);
    return ast.tree;
  }

  public String toString()
  {
    if(left==null)
      return leaf.toString();
    return left+" "+op+" "+right;
  }

  public Object getMakumbaType() throws antlr.RecognitionException
  {
    if(makumbaType==null)
      computeType();
    return makumbaType;
  }

  public void computeType() throws antlr.RecognitionException
  {
    if(leaf!=null)
      {
	makumbaType=leaf.makumbaType;
	return;
      }
    left.computeType();
    right.computeType();
    
    adjustType(left, right);
    adjustType(right, left);
    checkOperandTypes();
    makumbaType= computeTypeFromOperands();
  }

  void adjustType(AnalysisTree t1, AnalysisTree t2)
  {
    if(t1.makumbaType==null)
      {
	if(!(t1.leaf instanceof ParamAST))
	  throw new org.makumba.MakumbaError("no makumba type assigned for "+t1.leaf.getClass()+ " "+t1.leaf);
	if(t2.makumbaType!=null)
	  t1.leaf.makumbaType=t1.makumbaType= guessParameterType(t2.makumbaType);
	else
	  t1.leaf.makumbaType= t1.makumbaType=guessParameterType();
      }
  }

  // assumes int expressions
  public Object computeTypeFromOperands() { return "int"; }

  // assumes int parameters
  public Object guessParameterType(Object otherOperandType)  { return "int";  }

  // assumes int parameters
  public Object guessParameterType() {  return "int";  }

  public void checkOperandTypes() 
       throws antlr.RecognitionException
  {
    checkOperandTypes(left.makumbaType, right.makumbaType);
  }


  // assumes symetric operations
  public void checkOperandTypes(Object t1, Object t2)
       throws antlr.RecognitionException
  { 
    if(t1 instanceof FieldDefinition)
      t1=((FieldDefinition)t1).getDataType();
    if(t2 instanceof FieldDefinition)
      t2=((FieldDefinition)t2).getDataType();

    if(!t1.equals(t2))
      negociateOperandTypes(t1, t2);
  }
  
  public void negociateOperandTypes(Object t1, Object t2)
       throws antlr.RecognitionException
  {
    throw new antlr.SemanticException("Operand type check failed:\n\t\'"+left+"\' has type <"+t1+"> and \'"+right+"\' has type <"+t2+">");
  }
}

