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

public class NewTag extends FormTagBase
{
  public FormResponder makeResponder() { return new NewResponder(); }

  RecordInfo type;

  public FieldDefinition getDefaultType(String fieldName) 
  {
    return deriveType(type, fieldName);
  }

  public void setType(String s) { type=RecordInfo.getRecordInfo(s); }

  public String getEditedType(){ return type.getName(); }
}

class NewResponder extends FormResponder
{
  public Object respondTo(PageContext pc) throws LogicException
  {
    return Logic.doNew(controller, type, getHttpData(pc), makeAttributes(pc), database);
  }
}
