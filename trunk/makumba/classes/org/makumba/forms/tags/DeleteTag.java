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

import org.makumba.LogicException;
import org.makumba.ProgrammerError;
import org.makumba.commons.attributes.RequestAttributes;
import org.makumba.controller.Logic;
import org.makumba.forms.responder.Responder;
import org.makumba.forms.responder.ResponderOperation;

/**
 * mak:delete tag
 * 
 * @author Cristian Bogdan
 * @version $Id$
 */
public class DeleteTag extends EditTag {

    private static final long serialVersionUID = 1L;

    // no input tags should be allowed

    String widget;

    private boolean preserveWhiteSpace = false;

    public void setWidget(String w) {
        if (w.equals("") || w.equals("link") || w.equals("button")) {
            widget = w;
        } else {
            throw new ProgrammerError(
                    "Wrong 'widget' attribute value for mak:delete. Valid options are 'button' and 'link'.");
        }
    }

    public void setPreserveWhitespace(String s) {
        this.preserveWhiteSpace = (s != null && s.equals("true"));
    }

    String getOperation() {
        // FIXME
        if (widget == null || widget.equals("") || widget.equals("link")) {
            return "deleteLink";
        } else {
            return "deleteForm";
        }
    }

    public boolean getPreserveWhiteSpace() {
        return preserveWhiteSpace;
    }
    
    public static ResponderOperation getResponderOperation(String operation) {
        if(operation.equals("deleteLink")) {
            return new ResponderOperation() {
                private static final long serialVersionUID = 1L;

                public Object respondTo(HttpServletRequest req, Responder resp, String suffix, String parentSuffix)
                        throws LogicException {
                    return Logic.doDelete(resp.getController(), resp.getBasePointerType(), resp.getHttpBasePointer(req, suffix),
                        new RequestAttributes(resp.getController(), req, resp.getDatabase()), resp.getDatabase(),
                        RequestAttributes.getConnectionProvider(req));
                }

                public String verify(Responder resp) {
                    return null;
                }
            };
        } else if(operation.equals("deleteForm")) {
            return new ResponderOperation() {
                
                private static final long serialVersionUID = 1L;
    
                public Object respondTo(HttpServletRequest req, Responder resp, String suffix, String parentSuffix)
                        throws LogicException {
                    return Logic.doDelete(resp.getController(), resp.getBasePointerType(), resp.getHttpBasePointer(req, suffix),
                        new RequestAttributes(resp.getController(), req, resp.getDatabase()), resp.getDatabase(),
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
