package org.makumba.forms.tags;

import javax.servlet.jsp.JspException;

import org.apache.commons.lang.StringUtils;
import org.makumba.FieldDefinition;
import org.makumba.LogicException;
import org.makumba.ProgrammerError;
import org.makumba.analyser.PageCache;
import org.makumba.commons.MultipleKey;

/**
 * This tag implements an input field to be used within search forms. It slighlty changes some of the functionality of
 * {@link InputTag}, e.g. among others:
 * <ul>
 * <li>Not needing the name field, but getting the name & type of the input from the first argument in the fields
 * attribute of {@link CriterionTag}, using {@link CriterionTag#getInputName()}</>
 * <li>Allows to change a single select input to be transformed to a multiple select using the 'selectMultiple'
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

    private static final String[] allowedSelectTypes = { "true", "false" };

    private static final long serialVersionUID = 1L;

    private String selectMultiple = null;

    private String role = null;

    public void setSelectMultiple(String selectMultiple) {
        if (!getForm().getOperation().equals("search")) {
            throw new ProgrammerError("'selectMultiple' attribute is only valid inside Makumba Search Forms!");
        }
        if (!org.makumba.commons.StringUtils.equals(selectMultiple, allowedSelectTypes)) {
            throw new ProgrammerError("Invalid value for attribute 'selectMultiple': <" + selectMultiple
                    + ">. Allowed values are " + org.makumba.commons.StringUtils.toString(allowedSelectTypes));
        }
        this.selectMultiple = selectMultiple;
        params.put("selectMultiple", selectMultiple);
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
        // select multiple is only allowed for single-select inputs
        if (selectMultiple != null && fd != null && !(fd.isEnumType() || fd.isPointer())) {
            throw new ProgrammerError(
                    "'selectMultiple' attribute is only valid for 'ptr' and 'intEnum'/'charEnum' types, field is of type '"
                            + fd.getType() + "'!");
        }

        if (isValue()) {
            fdp.onNonQueryStartAnalyze(this, isNull(), getForm().getTagKey(), pageCache, expr);
        }
        if (StringUtils.equals(selectMultiple, "true") && nullOption != null) {
            throw new ProgrammerError("'selectMultiple' attribute cannot be used in combination with 'nullOption'");
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
        tagKey = new MultipleKey(new Object[] { getCriterionTag().tagKey, id, role });
    }

    public void setRole(String role) {
        if (!org.makumba.commons.StringUtils.equals(role, allowedRoles)) {
            throw new ProgrammerError("Invalid value for attribute 'role': <" + role + ">. Allowed values are "
                    + org.makumba.commons.StringUtils.toString(allowedRoles));
        }
        // role can be used only within a range type criterion tag
        if (!getCriterionTag().isRange()) {
            throw new ProgrammerError("'role' can only be specified if the criterion specific range='true'! ");
        }
        this.role = role;
    }

    @Override
    public boolean allowsIdenticalKey() {
        return false;
    }

}
