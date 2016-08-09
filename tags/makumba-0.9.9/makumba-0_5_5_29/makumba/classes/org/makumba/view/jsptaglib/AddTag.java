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
import org.makumba.view.*;

import org.makumba.*;
import javax.servlet.jsp.*;
import org.makumba.abstr.*;

public class AddTag extends FormTagBase 
{
  public boolean canComputeTypeFromEnclosingQuery() 
  { return true; }

  public FieldDefinition computeTypeFromEnclosingQuery(QueryStrategy qs, String fieldName) 
  {
    if(qs.knewProjectionAtStart(enclosingLabel)==-1)
      return null;
    DataDefinition dd= ((FieldInfo)qs.query.getLabelType(enclosingLabel).getFieldDefinition(field)).getPointedType();
    return deriveType(dd, fieldName);
  }

  public FormResponder makeResponder() { return new AddResponder(); }

  String field;

  public void setField(String s) { field=s; }

  public String getEditedType()
  {
    if(responder.pointerType==null) // not set yet
      return null;
    return responder.pointerType+"->"+field; 
  }
}

class AddResponder extends FormResponder
{
  public Object respondTo(PageContext pc) throws LogicException
  {
    return Logic.doAdd(controller, type, getHttpBasePointer(pc), getHttpData(pc), makeAttributes(pc), database);
  }
}


