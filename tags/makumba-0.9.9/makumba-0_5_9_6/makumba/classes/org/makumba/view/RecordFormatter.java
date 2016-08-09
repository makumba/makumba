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

package org.makumba.view;
import org.makumba.abstr.*;
import java.util.*;

public class RecordFormatter extends RecordHandler
{
  public RecordFormatter(RecordInfo ri) 
  {
    super(ri); 
  }
  
  public RecordFormatter(ComposedQuery q) 
  {
    super((RecordInfo)q.getResultType()); 
    for(int i=0; i<handlerOrder.size(); i++)
      ((FieldFormatter)handlerOrder.elementAt(i)).initExpr(q.getProjectionAt(i));
  }

  public RecordFormatter(RecordInfo ri, java.util.Hashtable names) 
  {
    super(ri);
    for(int i=0; i<handlerOrder.size(); i++)
      {
	FieldFormatter ff= (FieldFormatter)handlerOrder.elementAt(i);
	ff.initExpr((String)names.get(ff.getName()));
      }
  }

  protected String applyParameters(FieldFormatter ff, Dictionary formatParams, String s)
  { return s; }

  public String format(int i, Object value, Dictionary formatParams)
  {
    FieldFormatter ff= (FieldFormatter)handlerOrder.elementAt(i); 
    ff.checkParams(formatParams);
    return applyParameters(ff, formatParams, ff.format(value, formatParams));
  }
}
