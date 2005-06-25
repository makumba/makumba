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
import org.makumba.MakumbaSystem;
import org.makumba.util.MultipleKey;

public class NewTag extends FormTagBase
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
// for input tags:
  DataDefinition type = null;
  String multipleSubmitMsg = null;

  public void setType(String s) { type=MakumbaSystem.getDataDefinition(s); }
  public void setMultipleSubmitMsg(String s) { checkNoParent("multipleSubmitMsg"); multipleSubmitMsg=s; }

  /** Set tagKey to uniquely identify this tag. Called at analysis time before doStartAnalyze() and at runtime before doMakumbaStartTag() */
  public void setTagKey(MakumbaJspAnalyzer.PageCache pageCache)
  {
    Object keyComponents[]= {type.getName(), handler,  getParentListKey(null), getClass()};
    tagKey=new MultipleKey(keyComponents);
  }
  
  public void initialiseState() {
      super.initialiseState();
      if (type != null) responder.setNewType(type);
	  if (multipleSubmitMsg != null) responder.setMultipleSubmitMsg(multipleSubmitMsg);
  }


  public DataDefinition getDataTypeAtAnalysis(MakumbaJspAnalyzer.PageCache pageCache) 
  {
    return type;
  }
}

