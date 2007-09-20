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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;

import org.makumba.ProgrammerError;
import org.makumba.analyser.PageCache;
import org.makumba.commons.GenericMakumbaTag;

/**
 * mak:action tag
 * @author Cristian Bogdan
 * @version $Id$
 */
public class ActionTag extends GenericMakumbaTag implements BodyTag {

    private static final long serialVersionUID = 1L;

    BodyContent bodyContent;

    public void setBodyContent(BodyContent bc) {
        bodyContent = bc;
    }

    /** 
     * Indicates if the tag needs the page cache
     */
    protected boolean needPageCache() {
        return false;
    }

    /** 
     * This always returns EVAL_BODY_TAG so we make sure {@link #doInitBody()} is called
     * @param pageCache the page cache of the current page
     */
    public int doAnalyzedStartTag(PageCache pageCache) {
        return EVAL_BODY_BUFFERED;
    }

    public void doStartAnalyze(PageCache pageCache) {
        FormTagBase form = (FormTagBase) findAncestorWithClass(this, FormTagBase.class);
        if (form == null)
            throw new ProgrammerError(
                    "\'action\' tag must be enclosed in any kind of 'form' tag or in 'deleteLink' tag");
        form.setAction("dummy");
    }

    public void doInitBody() {
    }

    public int doAnalyzedEndTag(PageCache pageCache) throws JspException {
        FormTagBase form = (FormTagBase) findAncestorWithClass(this, FormTagBase.class);
        form.responder.setAction(bodyContent.getString());
        return EVAL_PAGE;
    }

}