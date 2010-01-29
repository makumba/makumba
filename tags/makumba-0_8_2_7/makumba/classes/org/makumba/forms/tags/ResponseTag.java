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
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;

import org.makumba.commons.RuntimeWrappedException;
import org.makumba.forms.responder.ResponderFactory;

/**
 * mak:response tag, displaying the response of a form submission.
 * 
 * @author Cristian Bogdan
 * @author Manuel Gay
 * @version $Id$
 */
public class ResponseTag extends javax.servlet.jsp.tagext.TagSupport {

    private static final long serialVersionUID = 1L;

    public int doStartTag() throws JspException {
        try {
            final HttpServletRequest httpServletRequest = (HttpServletRequest) pageContext.getRequest();
            final HttpSession session = httpServletRequest.getSession();
            final String suffix = "_" + httpServletRequest.getRequestURI();

            // check if we came from a form-redirection
            final Object respFromSession = session.getAttribute(ResponderFactory.RESPONSE_STRING_NAME + suffix);
            Object response = httpServletRequest.getAttribute(org.makumba.forms.responder.ResponderFactory.RESPONSE_FORMATTED_STRING_NAME);

            if (response == null && respFromSession != null) {
                // set the attributes from the session to the request
                httpServletRequest.setAttribute(ResponderFactory.RESPONSE_STRING_NAME, respFromSession);
                httpServletRequest.setAttribute(ResponderFactory.RESPONSE_FORMATTED_STRING_NAME,
                    session.getAttribute(ResponderFactory.RESPONSE_FORMATTED_STRING_NAME + suffix));

                // get the response value
                response = httpServletRequest.getAttribute(org.makumba.forms.responder.ResponderFactory.RESPONSE_FORMATTED_STRING_NAME);

                // clear the session values from this form
                session.removeAttribute(ResponderFactory.RESPONSE_STRING_NAME + suffix);
                session.removeAttribute(ResponderFactory.RESPONSE_FORMATTED_STRING_NAME + suffix);
            }

            if (response != null)
                pageContext.getOut().print(response);
        } catch (IOException e) {
            throw new RuntimeWrappedException(e);
        }

        return EVAL_BODY_INCLUDE;
    }
}
