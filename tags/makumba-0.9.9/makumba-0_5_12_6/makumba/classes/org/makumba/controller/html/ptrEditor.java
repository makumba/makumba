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

package org.makumba.controller.html;
import org.makumba.*;
import java.util.*;

public class ptrEditor extends choiceEditor
{
  String db;
  String query;

  public void onStartup(RecordEditor re)
  {
    db=re.database;
    query= "SELECT choice as choice, choice."+getTitleField()+ " as title FROM "+getPointedType().getName()+" choice ORDER BY title";
  }

  public Object getOptions()
  {
    Database dbc= MakumbaSystem.getConnectionTo(db);
    try{
      return dbc.executeQuery(query, null); 
    }finally{dbc.close(); }
  }

  public int getOptionsLength(Object opts){ return ((Vector)opts).size(); }

  public Object getOptionValue(Object options, int i)
  { return ((Dictionary)((Vector)options).elementAt(i)).get("choice"); }

  public String formatOptionValue(Object val)
  { return ((Pointer)val).toExternalForm(); }
  
  public String formatOptionValue(Object opts, int i, Object val)
  { return ((Pointer)val).toExternalForm(); }
  
  public String formatOptionTitle(Object options, int i)
  { return ""+((Dictionary)((Vector)options).elementAt(i)).get("title"); }

  public String getMultiple() { return ""; }
  public boolean isMultiple() { return false; }

  public int getDefaultSize() { return 1; }
}
