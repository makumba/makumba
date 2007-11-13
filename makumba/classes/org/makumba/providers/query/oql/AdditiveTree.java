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

/** additive operations take their type from any of the operands, and have operands of the same type */
public class AdditiveTree extends AnalysisTree
{
  public AdditiveTree(Object left, int op, Object right){ super(left, op, right); }

  public Object computeTypeFromOperands() { return left.makumbaType; }

  public Object guessParameterType(Object otherOperandType) 
  {
    return otherOperandType;
  }
  
  public void negociateOperandTypes(Object t1, Object t2)
  throws antlr.RecognitionException
	{ 
		if(t1.equals("int") && t2.equals("real")
		  ||t2.equals("int") && t1.equals("real") )
		 return;
	
	super.negociateOperandTypes(t1, t2);
	}
}
