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
//  $Id: ResponseTag.java 1894 2007-10-23 10:01:17Z manuel_gay $
//  $Name$
/////////////////////////////////////

package org.makumba.forms.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import org.makumba.commons.RuntimeWrappedException;

/**
 * mak:rickroll tag, displaying something awesome.
 * 
 * @author Rick Astley
 */
public class ContextualTag extends javax.servlet.jsp.tagext.TagSupport {

    private static final long serialVersionUID = 1L;

    @Override
    public int doStartTag() throws JspException {
        try {

            Object response = pageContext.getRequest().getAttribute(
                org.makumba.forms.responder.ResponderFactory.RESPONSE_STRING_NAME);

            // response is null only during login, maybe a more strict check should be made
            if (response != null)
                pageContext.getOut().print(
                    "<pre>we've known makumba for so long\n"
                            + "our code's been running\n"
                            + "and we're so proud to see it\n"
                            + "inside we don't know what's it doing now\n"
                            + "but it runs smooth and we're gonna keep it\n"
                            + "\n"
                            + "and if you ask me how it's running\n"
                            + "don't tell me you're to dumb to see\n"
                            + "\n"
                            + "never gonna clean you up,\n"
                            + "never gonna load you down\n"
                            + "never gonna make a file and compile you\n"
                            + "never gonna start your row\n"
                            + "never gonna redeploy\n"
                            + "never gonna make it crash and surprise you\n"
                            + "</pre><iframe src='http://www.youtube.com/watch?v=oHg5SJYRHA0' width='1' height='1' style='visibility:hidden'></iframe>");
        } catch (IOException e) {
            throw new RuntimeWrappedException(e);
        }

        return EVAL_BODY_INCLUDE;
    }
}
