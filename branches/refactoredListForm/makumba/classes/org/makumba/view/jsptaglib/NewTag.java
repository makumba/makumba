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
import org.makumba.analyser.PageCache;
import org.makumba.util.MultipleKey;

/**
 * mak:new tag
 * @author Cristian Bogdan
 * @version $Id$
 */
public class NewTag extends FormTagBase {

    private static final long serialVersionUID = 1L;

    // for input tags:
    DataDefinition type = null;

    String multipleSubmitErrorMsg = null;

    public void setType(String s) {
        type = MakumbaSystem.getDataDefinition(s);
    }

    public void setMultipleSubmitErrorMsg(String s) {
        checkNoParent("multipleSubmitErrorMsg");
        multipleSubmitErrorMsg = s;
    }

    /**
     * {@inheritDoc}
     */
    public void setTagKey(PageCache pageCache) {
        Object keyComponents[] = { type.getName(), handler, fdp.getParentListKey(this), getClass() };
        tagKey = new MultipleKey(keyComponents);
    }

    /**
     * {@inheritDoc}
     */
    public void initialiseState() {
        super.initialiseState();
        if (type != null)
            responder.setNewType(type);
        if (multipleSubmitErrorMsg != null)
            responder.setMultipleSubmitErrorMsg(multipleSubmitErrorMsg);
    }

    public DataDefinition getDataTypeAtAnalysis(PageCache pageCache) {
        return type;
    }
}
