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
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.ProgrammerError;
import org.makumba.util.MultipleKey;

public class AddTag extends FormTagBase 
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
// for input tags:
  String field = null;
  String multipleSubmitErrorMsg = null;
  
  public void setField(String s) { field=s; }
  public void setMultipleSubmitErrorMsg(String s) { checkNoParent("multipleSubmitErrorMsg"); multipleSubmitErrorMsg=s; }

  /** Set tagKey to uniquely identify this tag. Called at analysis time before doStartAnalyze() and at runtime before doMakumbaStartTag() */
  public void setTagKey(MakumbaJspAnalyzer.PageCache pageCache)
  {
    Object[] keyComponents= {baseObject, field, handler, getParentListKey(null), getClass()};
    tagKey=new MultipleKey(keyComponents);
  }

  public void initialiseState() {
      super.initialiseState();
	  if (multipleSubmitErrorMsg != null) responder.setMultipleSubmitErrorMsg(multipleSubmitErrorMsg);
      if (field != null) responder.setAddField(field);
      if (!"add".equals(getOperation())) 
	responder.setNewType(((NewTag)findParentForm()).type);
  }

  public DataDefinition getDataTypeAtAnalysis(MakumbaJspAnalyzer.PageCache pageCache)
  {
    DataDefinition base= getOperation().equals("add")?pageCache.getQuery(getParentListKey(pageCache)).getLabelType(baseObject):
      ((NewTag)findParentForm()).type;
    if (base == null) { // we could not find the type
        String message = "Could not determine type for specified object '" + baseObject + "'";
        if (baseObject.indexOf('.') != -1) { // the programmer tried to use some sub-pointer here..
            message += " - you cannot specify a sub-pointer in the 'object=' attribute!";
        }
        throw new ProgrammerError(message);
    }    
    FieldDefinition fieldDefinition = base.getFieldDefinition(field);
    if (fieldDefinition == null) { // we used an unknow field
        throw new ProgrammerError("Cannot find field '" + field + " in type " + base + "");
    }
    return fieldDefinition.getSubtable();
  }
  
  String getOperation(){
    FormTagBase parent=findParentForm();
    if((parent instanceof NewTag) && baseObject.equals(parent.formName))
       return "addToNew";
    return "add";
  }
  
  boolean shouldComputeBasePointer(){ return getOperation().equals("add"); }
}


