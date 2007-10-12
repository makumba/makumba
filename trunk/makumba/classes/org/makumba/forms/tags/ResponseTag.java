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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;

/**
 * mak:response tag, displaying the response of a form submission.
 * @author Cristian Bogdan
 * @version $Id$
 */
public class ResponseTag extends javax.servlet.jsp.tagext.TagSupport {
    
    private static final long serialVersionUID = 1L;

    public int doStartTag() throws JspException {
        try {
            Object response = pageContext.getRequest().getAttribute(
                org.makumba.forms.responder.ResponderFactory.RESPONSE_STRING_NAME);

            // response is null only during login, maybe a more strict check should be made
            if (response != null)
                pageContext.getOut().print(response);
        } catch (IOException e) {
            org.makumba.controller.http.ControllerFilter.treatException(e,
                (HttpServletRequest) pageContext.getRequest(), (HttpServletResponse) pageContext.getResponse());
        }

        return EVAL_BODY_INCLUDE;
    }
}
