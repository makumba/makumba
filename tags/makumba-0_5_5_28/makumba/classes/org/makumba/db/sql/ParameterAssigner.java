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

package org.makumba.db.sql;
import java.sql.*;
import java.util.*;
import org.makumba.*;

// wrong dependencies
import org.makumba.abstr.*;
import org.makumba.db.*;

/** this class takes parameters passed to an OQL query and transmits them to the corresponding PreparedStatement. The order in the two is different, because OQL paramters are numbered. Also, strict type checking is performed for the parameters */
public class ParameterAssigner 
{
  RecordManager paramHandler;
  OQLAnalyzer tree;

  ParameterAssigner(DBConnection dbc, OQLAnalyzer tree)
  {
    this.tree=tree;
    if(tree.parameterNumber()>0)
      paramHandler=(RecordManager)dbc.getHostDatabase().getTable((RecordInfo)tree.getParameterTypes());
  }
  static final Object[] empty=new Object[0];

  public String assignParameters(PreparedStatement ps, Object[] args)
       throws SQLException
  {
    if(tree.parameterNumber()==0)
      return null;
    try{
      Hashtable correct=new Hashtable();
      Hashtable errors=new Hashtable();
      for(int i=0; i< tree.parameterNumber(); i++)
	{
	  FieldManager fm=(FieldManager)(paramHandler.getFieldHandler("param"+i));
	  Integer para= new Integer(tree.parameterAt(i));
	  String spara="$"+para;
	  Object value= args[para.intValue()-1];
	  
	  try{
	    value=fm.checkValue(value);
	  }
	  catch(InvalidValueException e)
	    {
	      // we have a wrong value, we pass something instead and we remember that there is a problem. 
	      // if there is no correct value for this argument, we'll throw an exception later
	      if(correct.get(spara)==null)
		errors.put(spara, e);
	      if(value==Pointer.Null || value==Pointer.NullInteger ||value==Pointer.NullString || value==Pointer.NullText ||value==Pointer.NullSet ||value== Pointer.NullDate)
		fm.setNullArgument(ps, i+1);
	      else
		ps.setObject(i+1, value);
	      continue;
	    }
	  correct.put(spara, para);
	  errors.remove(spara);
	  
	  fm.setUpdateArgument(ps, i+1, value);
	}
      if(errors.size()>0)
	{
	  String s="";
	  for(Enumeration e= errors.keys(); e.hasMoreElements(); )
	    {
	      Object o=e.nextElement();
	      s+="\nargument: "+o+"; exception:\n"+errors.get(o);
	    }
	  return s;
	}
    }catch(ArrayIndexOutOfBoundsException ae){ throw new org.makumba.MakumbaError("wrong number of arguments to query "); }   
    return null;
  }
}
