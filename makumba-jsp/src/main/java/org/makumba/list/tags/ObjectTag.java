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

package org.makumba.list.tags;

import javax.servlet.jsp.JspException;

import org.makumba.LogicException;
import org.makumba.analyser.PageCache;

/**
 * mak:object tag
 * 
 * @author Cristian Bogdan
 * @version $Id$
 */
public class ObjectTag extends QueryTag {
    public ObjectTag() {
        // authorize = "binding";
    }

    private static final long serialVersionUID = 1L;

    @Override
    protected void setNumberOfIterations(int max) throws JspException {
        if (max > 1) {
            throw new MakumbaJspException(this, "Object tag should have only one result");
        }
        super.setNumberOfIterations(max);
    }

    // fake means that the mak:object has no query
    private boolean fake;

    public void setContext(String s) {
        fake = "true".equals(s);
    }

    @Override
    public boolean allowsIdenticalKey() {
        return isFake();
    }

    @Override
    public boolean isFake() {
        return fake && findAncestorWithClass(this, QueryTag.class) != null;
    }

    @Override
    public void doStartAnalyze(PageCache pageCache) {
        if (isFake()) {
            return;
        }
        super.doStartAnalyze(pageCache);
    }

    @Override
    public void doEndAnalyze(PageCache pageCache) {
        if (isFake()) {
            return;
        }
        super.doEndAnalyze(pageCache);
    }

    @Override
    public int doAnalyzedStartTag(PageCache pageCache) throws LogicException, JspException {
        if (isFake()) {
            return EVAL_BODY_INCLUDE;
        }
        return super.doAnalyzedStartTag(pageCache);
    }

    @Override
    public int doAfterBody() throws JspException {
        if (isFake()) {
            return SKIP_BODY;
        }
        return super.doAfterBody();
    }

    @Override
    public int doAnalyzedEndTag(PageCache pageCache) throws JspException {
        if (isFake()) {
            return EVAL_PAGE;
        }
        return super.doAnalyzedEndTag(pageCache);
    }
}
