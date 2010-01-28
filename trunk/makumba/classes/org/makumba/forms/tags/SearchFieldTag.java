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
//  $Id: InputTag.java 3027 2008-08-07 09:20:20Z rosso_nero $
//  $Name$
/////////////////////////////////////

package org.makumba.forms.tags;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

import org.apache.commons.lang.StringUtils;
import org.makumba.FieldDefinition;
import org.makumba.LogicException;
import org.makumba.ProgrammerError;
import org.makumba.analyser.PageCache;
import org.makumba.commons.MultipleKey;
import org.makumba.providers.Configuration;

/**
 * This tag implements an input field to be used within search forms. It slightly changes some of the functionality of
 * {@link InputTag}, e.g. among others:
 * <ul>
 * <li>Not needing the name field, but getting the name & type of the input from the first argument in the fields
 * attribute of {@link CriterionTag}, using {@link CriterionTag#getInputName()}</>
 * <li>Allows to change a select input to be forced to a single/multiple select using the 'forceInputStyle'
 * attribute.</li>
 * <li>Allows for range searching, triggered by the the 'role' attribute, requiring 'isRange' attribute in
 * {@link CriterionTag} to be set to 'true'</li>
 * </ul>
 * 
 * @author Rudolf Mayer
 * @version $Id: SearchFieldTag.java,v 1.1 Oct 21, 2007 1:29:06 PM rudi Exp $
 */
public class SearchFieldTag extends InputTag {
    private static final String[] allowedRoles = { "rangeBegin", "rangeEnd" };

    public static final String[] allowedSelectTypes = { "single", "multiple", "input" };

    private static final long serialVersionUID = 1L;

    protected String forceInputStyle = null;

    private String role = null;

    public void setForceInputStyle(String forceInputStyle) {
        if (!getForm().getOperation().equals("search")) {
            throw new ProgrammerError("'forceInputStyle' attribute is only valid inside Makumba Search Forms!");
        }
        this.forceInputStyle = forceInputStyle;
        params.put("forceInputStyle", forceInputStyle);
    }

    protected CriterionTag getCriterionTag() {
        return (CriterionTag) findAncestorWithClass(this, CriterionTag.class);
    }

    /**
     * Determines the ValueComputer and associates it with the tagKey
     * 
     * @param pageCache
     *            the page cache of the current page
     */
    public void doStartAnalyze(PageCache pageCache) {
        if (getCriterionTag() == null) {
            throw new ProgrammerError("\'criterionInput\' tag must be enclosed in a 'criterion' tag");
        }

        // need to get the input name from the surrounding criterion tag, so analysis in InputTag.doEndAnalyze works
        name = getCriterionTag().getInputName();

        FieldDefinition fd = getCriterionTag().getFieldDefinition(pageCache);
        // forceInputStyle 'multiple' is only allowed for single-select inputs
        if (StringUtils.equals("forceInputStyle", "multiple") && fd != null && !(fd.isEnumType() || fd.isPointer())) {
            throw new ProgrammerError(
                    "'forceInputStyle' attribute with value 'multiple' is only valid for 'ptr' and 'intEnum'/'charEnum' types, field is of type '"
                            + fd.getType() + "'!");
        }
        if (StringUtils.equals("forceInputStyle", "single") && fd != null && !(fd.isSetType())) {
            throw new ProgrammerError(
                    "'forceInputStyle' attribute with value 'single' is only valid for 'set' and 'setIntEnum'/'setCharEnum' types, field is of type '"
                            + fd.getType() + "'!");
        }

        if (isValue()) {
            fdp.onNonQueryStartAnalyze(this, isNull(), getForm().getTagKey(), pageCache, expr);
        }
        if (StringUtils.equals(forceInputStyle, "multiple") && nullOption != null) {
            throw new ProgrammerError("'forceInputStyle' attribute with value 'multiple' cannot be used in combination with 'nullOption'");
        }
    }

    @Override
    public int doAnalyzedEndTag(PageCache pageCache) throws JspException, LogicException {
        // need to get the input name from the surrounding criterion tag
        name = getCriterionTag().getInputName();
        if (StringUtils.equals(role, "rangeEnd")) {
            name += "RangeEnd";
        }
        getForm().responder.addMultiFieldSearchMapping(name, getCriterionTag().getFieldsSplit());
        return super.doAnalyzedEndTag(pageCache);
    }

    @Override
    public void setTagKey(PageCache pageCache) {
        if (calendarEditorLink == null && pageContext != null) { // initialise default calendar link text
            calendarEditorLink = Configuration.getDefaultCalendarEditorLink(((HttpServletRequest) pageContext.getRequest()).getContextPath());
        }
        tagKey = new MultipleKey(new Object[] { getCriterionTag().tagKey, id, role, getClass() });
    }

    public void setRole(String role) {
        // role can be used only within a range type criterion tag
        if (!getCriterionTag().isRange()) {
            throw new ProgrammerError("'role' can only be specified if the criterion specified range='true'! ");
        }
        this.role = role;
    }

    @Override
    public boolean allowsIdenticalKey() {
        return false;
    }
    
    @Override
    protected void registerPossibleAttributeValues() {
        registerAttributeValues("forceInputStyle", allowedSelectTypes);
        registerAttributeValues("role", allowedRoles);
    }
}
