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

package org.makumba.view.jsptaglib;
import org.makumba.FieldDefinition;
import org.makumba.DataDefinition;
import org.makumba.util.MultipleKey;

public class AddTag extends FormTagBase 
{
  // for input tags:
  String field = null;

  public void setField(String s) { field=s; }

  /** Set tagKey to uniquely identify this tag. Called at analysis time before doStartAnalyze() and at runtime before doMakumbaStartTag() */
  public void setTagKey()
  {
    Object[] keyComponents= {baseObject, field, handler, getParentListKey(), getClass()};
    tagKey=new MultipleKey(keyComponents);
  }

  public void initialiseState() {
      super.initialiseState();
      if (field != null) responder.setAddField(field);
  }

  public DataDefinition getDataType()
  {
    return pageCache.getQuery(getParentListKey()).getLabelType(baseObject).getFieldDefinition(field).getSubtype();
  }
}


