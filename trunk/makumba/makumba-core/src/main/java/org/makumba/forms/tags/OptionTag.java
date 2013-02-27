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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;

import org.makumba.FieldDefinition;
import org.makumba.ProgrammerError;
import org.makumba.analyser.PageCache;
import org.makumba.commons.MakumbaJspAnalyzer;
import org.makumba.commons.MultipleKey;
import org.makumba.providers.DataDefinitionProvider;

/**
 * mak:option tag
 * 
 * @author Cristian Bogdan
 * @version $Id$
 */
public class OptionTag extends BasicValueTag implements BodyTag {
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTagKey(PageCache pageCache) {
        expr = valueExprOriginal;
        if (expr == null) {
            expr = "nil";
        }
        // a pretty long key but i can't come with a better idea
        Object[] keyComponents = { expr.trim(), getInput().tagKey, fdp.getParentListKey(this) };
        tagKey = new MultipleKey(keyComponents);
    }

    InputTag getInput() {
        return (InputTag) findAncestorWithClass(this, InputTag.class);
    }

    @Override
    FieldDefinition getTypeFromContext(PageCache pageCache) {
        FieldDefinition t = (FieldDefinition) pageCache.retrieve(MakumbaJspAnalyzer.INPUT_TYPES, getInput().tagKey);

        if (!(t.getType().startsWith("set") || t.getType().startsWith("ptr") || t.getType().contains("Enum"))) {
            throw new ProgrammerError("Only set and pointer <mak:input > can have options inside");
        }

        if (t.getType().startsWith("ptr") || !t.getType().contains("Enum")) {
            return DataDefinitionProvider.getInstance().makeFieldDefinition("dummy",
                "ptr " + t.getForeignTable().getName());
        }
        if (!t.getType().startsWith("set")) {
            return t;
        }
        return t.getSubtable().getFieldDefinition("enum");
    }

    @Override
    public void doStartAnalyze(PageCache pageCache) {
        if (getInput() == null) {
            throw new ProgrammerError("\'option\' tag must be enclosed in a 'input' tag");
        }
        getInput().isChoser = true;
        super.doStartAnalyze(pageCache);
    }

    @Override
    public void doInitBody() {
    }

    BodyContent bodyContent;

    @Override
    public void setBodyContent(BodyContent bc) {
        bodyContent = bc;
    }

    @Override
    public int doAnalyzedStartTag(PageCache pageCache) {
        return EVAL_BODY_BUFFERED;
    }

    /**
     * A value was computed, do what's needed with it, cleanup and return the result of doMakumbaEndTag()
     * 
     * @param val
     *            the computed value
     * @param type
     *            the type of the value
     * @throws JspException
     * @throws {@link LogicException}
     */
    @Override
    int computedValue(Object val, FieldDefinition type) throws JspException, org.makumba.LogicException {
        getInput().checkBodyContentForNonWhitespace();
        String content = bodyContent == null ? "" : bodyContent.getString();
        if (isNull()) {
            val = "";
            getInput().setNullOption(content);
        }
        getInput().choiceSet.add(val, content, false, false);
        valueExprOriginal = dataType = expr = null;
        return EVAL_PAGE;
    }

    @Override
    protected void doAnalyzedCleanup() {
        super.doAnalyzedCleanup();
        bodyContent = null;
    }
}
