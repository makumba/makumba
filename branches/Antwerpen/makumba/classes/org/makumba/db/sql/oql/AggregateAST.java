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
import org.makumba.db.Database;

public class AggregateAST extends OQLAST
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
OQLAST expr;

  public AggregateAST(){}
  //  public IdAST(antlr.Token t) { super(t); }

    public void setExpr(OQLAST e){ expr=e; }

    public String writeInSQLQuery(Database d)
    {
	return getText()+expr.writeInSQLQuery(d)+")";
    }
    
    public Object getMakumbaType() throws antlr.RecognitionException
    {
	Object o= expr.getMakumbaType();
	String os= ""+o;
	if(getText().startsWith("max")||getText().startsWith("min"))
	    {
		if(os.startsWith("int") ||os.startsWith("real") || os.startsWith("date") || os.startsWith("ptr") || os.startsWith("char") || os.startsWith("text"))
		    return o;
		throw new antlr.SemanticException("cannot min() or max() a "+os);
	    }

	if(getText().startsWith("sum"))
	    {
		if(os.startsWith("int"))
		    return "int";
		if(os.startsWith("real"))
		    return "real";
		throw new antlr.SemanticException("cannot sum() a "+os);
	   } 

	if(getText().startsWith("avg"))
	    {
		if(os.startsWith("int") || os.startsWith("real"))
		    return "real";
		throw new antlr.SemanticException("cannot avg() a "+os);
	   } 

	throw new antlr.SemanticException("aggregate expressions can be sum, min, max, avg");
    }

}
