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

package org.makumba.forms.tags;

import javax.servlet.http.HttpServletRequest;

import org.makumba.DataDefinition;
import org.makumba.LogicException;
import org.makumba.analyser.PageCache;
import org.makumba.commons.MultipleKey;
import org.makumba.commons.attributes.RequestAttributes;
import org.makumba.controller.Logic;
import org.makumba.forms.responder.Responder;
import org.makumba.forms.responder.ResponderOperation;

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
        type = ddp.getDataDefinition(s);
    }

    public void setMultipleSubmitErrorMsg(String s) {
        checkNoParent("multipleSubmitErrorMsg");
        multipleSubmitErrorMsg = s;
    }

    /**
     * {@inheritDoc}
     */
    public void setTagKey(PageCache pageCache) {
        Object keyComponents[] = { type.getName(), handler, afterHandler, fdp.getParentListKey(this), formName, getClass() };
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
    
    @Override
    public ResponderOperation getResponderOperation(String operation) {
        if(operation.equals("new")) {
            return newOp ;
        }
        return null;
    }
    
    private final static ResponderOperation newOp = new ResponderOperation() {
        private static final long serialVersionUID = 1L;

        public Object respondTo(HttpServletRequest req, Responder resp, String suffix, String parentSuffix)
                throws LogicException {
            String handlerName;
            if (resp.getHandler()!= null) {
                handlerName = resp.getHandler();
            } else {
                handlerName = "on_new" + Logic.upperCase(resp.getNewType());
            }
            String afterHandlerName;
            if (resp.getAfterHandler() != null) {
                afterHandlerName = resp.getAfterHandler();
            } else {
                afterHandlerName = "after_new" + Logic.upperCase(resp.getNewType());
            }
            return Logic.doNew(resp.getController(), handlerName, afterHandlerName, resp.getNewType(), resp.getHttpData(req,
                suffix), new RequestAttributes(resp.getController(), req, resp.getDatabase()), resp.getDatabase(),
                RequestAttributes.getConnectionProvider(req));
        }

        public String verify(Responder resp) {
            return null;
        }
    };
    
    
    @Override
    protected void doAnalyzedCleanup(){
        super.doAnalyzedCleanup();
        type= null; 
        multipleSubmitErrorMsg= null;
    }
}
