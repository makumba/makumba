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
import org.makumba.commons.attributes.RequestAttributes;
import org.makumba.controller.Logic;
import org.makumba.forms.responder.Responder;
import org.makumba.forms.responder.ResponderOperation;

/**
 * mak:editForm tag
 * @author Cristian Bogdan
 * @version $Id$
 */
public class EditTag extends FormTagBase {

    private static final long serialVersionUID = 1L;
    
    // for input tags:
    public String getDefaultExpr(String fieldName) {
        return baseObject + "." + fieldName;
    }

    public DataDefinition getDataTypeAtAnalysis(PageCache pageCache) {
        return fdp.getBasePointerType(this, pageCache, baseObject);
    }
    
    public static ResponderOperation getResponderOperation(String operation) {
        if(operation.equals("edit")) {
            return new ResponderOperation() {
                private static final long serialVersionUID = 1L;

                public Object respondTo(HttpServletRequest req, Responder resp, String suffix, String parentSuffix)
                        throws LogicException {
                    String handlerName;
                    if (resp.getHandler() != null) {
                        handlerName = resp.getHandler();
                    } else {
                        handlerName = "on_edit" + Logic.upperCase(resp.getBasePointerType());
                    }
                    String afterHandlerName;
                    if (resp.getAfterHandler() != null) {
                        afterHandlerName = resp.getAfterHandler();
                    } else {
                        afterHandlerName = "after_edit" + Logic.upperCase(resp.getBasePointerType());
                    }

                    return Logic.doEdit(resp.getController(), handlerName, afterHandlerName, resp.getBasePointerType(),
                        resp.getHttpBasePointer(req, suffix), resp.getHttpData(req, suffix), new RequestAttributes(
                                resp.getController(), req, resp.getDatabase()), resp.getDatabase(),
                        RequestAttributes.getConnectionProvider(req));
                }

                public String verify(Responder resp) {
                    return null;
                }
            };
        }
        return null;
    }
}
