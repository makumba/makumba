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
import org.makumba.FieldDefinition;
import org.makumba.LogicException;
import org.makumba.Pointer;
import org.makumba.ProgrammerError;
import org.makumba.analyser.PageCache;
import org.makumba.commons.MultipleKey;
import org.makumba.commons.attributes.RequestAttributes;
import org.makumba.controller.Logic;
import org.makumba.forms.responder.Responder;
import org.makumba.forms.responder.ResponderFactory;
import org.makumba.forms.responder.ResponderOperation;

/**
 * mak:addForm tag
 * 
 * @author Cristian Bogdan
 * @version $Id$
 */
public class AddTag extends FormTagBase {

    private static final long serialVersionUID = 1L;

    // for input tags:
    String field = null;

    String multipleSubmitErrorMsg = null;

    public void setField(String s) {
        field = s;
    }

    public void setMultipleSubmitErrorMsg(String s) {
        checkNoParent("multipleSubmitErrorMsg");
        multipleSubmitErrorMsg = s;
    }

    /**
     * Sets tagKey to uniquely identify this tag. Called at analysis time before doStartAnalyze() and at runtime before
     * doMakumbaStartTag()
     * 
     * @param pageCache
     *            the page cache of the current page
     */
    public void setTagKey(PageCache pageCache) {
        Object[] keyComponents = { baseObject, field, handler, afterHandler, fdp.getParentListKey(this), formName, getClass() };
        tagKey = new MultipleKey(keyComponents);
    }

    /**
     * Inherited
     */
    public void initialiseState() {
        super.initialiseState();
        if (multipleSubmitErrorMsg != null)
            responder.setMultipleSubmitErrorMsg(multipleSubmitErrorMsg);
        if (field != null)
            responder.setAddField(field);
        if (!"add".equals(getOperation()))
            responder.setNewType(((NewTag) findParentForm()).type);
    }

    /**
     * Tries to figure out the type of the object to which we want to add some data
     * 
     * @param pageCache
     *            the page cache of the current page
     * @return A DataDefinition corresponding to the type of object to which we want to add something
     */
    public DataDefinition getDataTypeAtAnalysis(PageCache pageCache) {
        DataDefinition base = getOperation().equals("add") ? fdp.getBasePointerType(this, pageCache, baseObject)
                : ((NewTag) findParentForm()).type;
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

    /**
     * Figures out the operation
     * 
     * @return 'addNew' if we are inside of a newForm, 'add' otherwise
     */
    String getOperation() {
        FormTagBase parent = findParentForm();
        if ((parent instanceof NewTag) && baseObject.equals(parent.formName))
            return "addToNew";
        return "add";
    }

    public boolean shouldComputeBasePointer() {
        return getOperation().equals("add");
    }

    @Override
    public ResponderOperation getResponderOperation(String operation) {

        // if this is a simple addForm
        if (operation.equals("add")) {
            return addOp;

        } else if (operation.equals("addToNew")) {
            return addToNewOp;
        }
        throw new RuntimeException("Houston, we have a problem");

    }
    
    private final static ResponderOperation addToNewOp = new ResponderOperation() {
        private static final long serialVersionUID = 1L;

        public Object respondTo(HttpServletRequest req, Responder resp, String suffix, String parentSuffix)
                throws LogicException {
            // get result we got from the new form
            Object resultFromNew = req.getAttribute(ResponderFactory.resultNamePrefix + parentSuffix);

            // if we got a null response from the new form (possibly from a logic exception thrown by the
            // programmer)
            if (resultFromNew == org.makumba.Pointer.Null) {
                return org.makumba.Pointer.Null; // we return null here too
            }

            String handlerName;
            if (resp.getHandler() != null) {
                handlerName = resp.getHandler();
            } else {
                handlerName = "on_add" + Logic.upperCase(resp.getBasePointerType());
            }
            String afterHandlerName;
            if (resp.getAfterHandler() != null) {
                afterHandlerName = resp.getAfterHandler();
            } else {
                afterHandlerName = "after_add" + Logic.upperCase(resp.getBasePointerType());
            }

            // otherwise, we add to the new object
            return Logic.doAdd(resp.getController(), handlerName, afterHandlerName, resp.getNewType() + "->"
                    + resp.getAddField(), (Pointer) resultFromNew, resp.getHttpData(req, suffix),
                new RequestAttributes(resp.getController(), req, resp.getDatabase()), resp.getDatabase(),
                getConnectionProvider(req, resp.getController()));
        }

        public String verify(Responder resp) {
            return null;
        }
    };

    private final static ResponderOperation addOp = new ResponderOperation() {
        private static final long serialVersionUID = 1L;

        public Object respondTo(HttpServletRequest req, Responder resp, String suffix, String parentSuffix)
                throws LogicException {
            String handlerName;
            if (resp.getHandler() != null) {
                handlerName = resp.getHandler();
            } else {
                handlerName = "on_add" + Logic.upperCase(resp.getBasePointerType() + "->" + resp.getAddField());
            }
            String afterHandlerName;
            if (resp.getAfterHandler() != null) {
                afterHandlerName = resp.getAfterHandler();
            } else {
                afterHandlerName = "after_add" + Logic.upperCase(resp.getBasePointerType() + "->" + resp.getAddField());
            }
            return Logic.doAdd(resp.getController(), handlerName, afterHandlerName, resp.getBasePointerType() + "->"
                    + resp.getAddField(), resp.getHttpBasePointer(req, suffix), resp.getHttpData(req, suffix),
                new RequestAttributes(resp.getController(), req, resp.getDatabase()), resp.getDatabase(),
                getConnectionProvider(req, resp.getController()));
        }

        public String verify(Responder resp) {
            return null;
        }
    };

    @Override
    protected void doAnalyzedCleanup() {
        super.doAnalyzedCleanup();
        field = multipleSubmitErrorMsg = null;
    }
}
