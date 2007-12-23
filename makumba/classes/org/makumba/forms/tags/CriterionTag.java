package org.makumba.forms.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;
import org.makumba.FieldDefinition;
import org.makumba.LogicException;
import org.makumba.ProgrammerError;
import org.makumba.analyser.PageCache;
import org.makumba.commons.MultipleKey;
import org.makumba.commons.tags.GenericMakumbaTag;
import org.makumba.providers.FormDataProvider;

/**
 * @author Rudolf Mayer
 * @version $Id: CriterionTag.java,v 1.1 Oct 21, 2007 1:37:37 PM rudi Exp $
 */
public class CriterionTag extends GenericMakumbaTag implements BodyTag {
    private static final long serialVersionUID = 1L;

    private static final String[] allowedRanges = { "true", "false" };

    private String isRange;

    private String fields;

    private FieldDefinition fieldDef;

    private BodyContent bodyContent;

    private FormDataProvider fdp;

    public CriterionTag() {
        // TODO move this somewhere else
        try {
            this.fdp = (FormDataProvider) Class.forName("org.makumba.list.ListFormDataProvider").newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public FormTagBase getForm() {
        return (FormTagBase) TagSupport.findAncestorWithClass(this, FormTagBase.class);
    }

    /**
     * Determines the ValueComputer and associates it with the tagKey
     * 
     * @param pageCache
     *            the page cache of the current page
     */
    public void doStartAnalyze(PageCache pageCache) {
        if (fields == null) {
            throw new ProgrammerError("fields attribute is required");
        }
        parseFieldList(pageCache);
        super.doStartAnalyze(pageCache);
    }

    private void parseFieldList(PageCache pageCache) throws ProgrammerError {
        String[] fieldsSplit = getFieldsSplit();
        for (int i = 0; i < fieldsSplit.length; i++) {
            String element = fieldsSplit[i];
            FieldDefinition fd = getForm().fdp.getInputTypeAtAnalysis(this,
                getForm().getDataTypeAtAnalysis(pageCache), element, pageCache);

            // if the fd is not found, the field is not known
            if (fd == null) {
                throw new ProgrammerError("Field '" + element + "' in field list '" + fields + "' is not known.");
            }
            // compare it to the already checked one if the types are equivalent
            if (fieldDef != null) {
                if (fieldDef.getIntegerType() != fd.getIntegerType()) {
                    throw new ProgrammerError("All fields in the field list must be of the same type! Field '"
                            + element + "' with type '" + fd.getType() + "' differs from the previous field '"
                            + fieldDef + "' of type '" + fieldDef.getType() + "'!");
                }
            } else {
                fieldDef = fd;
            }

            // multi-field search is only possible for string, number and date types, not for sets, pointers & enums
            if (fieldsSplit.length > 1 && !(fd.isStringType() || fd.isNumberType() || fd.isDateType())) {
                throw new ProgrammerError(
                        "Multi-field search is only possible for 'char'/'text', 'int'/'real' and 'date' types, given field '"
                                + element + "' is of type '" + fd.getType() + "'!");
            }

        }
    }

    @Override
    public int doAnalyzedEndTag(PageCache pageCache) throws JspException, LogicException {
        getForm().responder.addMultiFieldSearchMapping(getInputName(), getFieldsSplit());
        if (bodyContent != null) {
            try {
                bodyContent.getEnclosingWriter().print(bodyContent.getString());
            } catch (IOException e) {
                throw new JspException(e.toString());
            }
        }
        return super.doAnalyzedEndTag(pageCache);
    }

    /**
     * This always returns EVAL_BODY_TAG so we make sure {@link #doInitBody()} is called
     * 
     * @param pageCache
     *            the page cache of the current page
     */
    @Override
    public int doAnalyzedStartTag(PageCache pageCache) {
        if (fieldDef == null) {
            parseFieldList(pageCache);
        }
        return EVAL_BODY_BUFFERED;
    }

    @Override
    public void doEndAnalyze(PageCache pageCache) {
        super.doEndAnalyze(pageCache);
    }

    public void setFields(String s) {
        this.fields = s;
    }

    public String[] getFieldsSplit() {
        String[] split = fields.split(",");
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].trim();
        }
        return split;
    }

    public void doInitBody() throws JspException {
    }

    public void setBodyContent(BodyContent b) {
        bodyContent = b;
    }

    public FieldDefinition getTypeFromContext(PageCache pageCache) {
        if (fieldDef == null) {
            parseFieldList(pageCache);
        }
        return fdp.getInputTypeAtAnalysis(this, getForm().getDataTypeAtAnalysis(pageCache), getInputName(), pageCache);
    }

    @Override
    public void setTagKey(PageCache pageCache) {
        tagKey = new MultipleKey(new Object[] { getForm().tagKey, id, fields });
    }

    @Override
    public boolean allowsIdenticalKey() {
        return false;
    }

    public String getInputName() {
        if (fields.indexOf(",") == -1) {
            return fields.trim();
        } else {
            return fields.substring(0, fields.indexOf(",")).trim();
        }

    }

    public FieldDefinition getFieldDefinition(PageCache pageCache) {
        parseFieldList(pageCache);
        return fieldDef;
    }

    public boolean isRange() {
        return StringUtils.equals(isRange, "true");
    }

    public void setIsRange(String isRange) {
        if (!org.makumba.commons.StringUtils.equals(isRange, allowedRanges)) {
            throw new ProgrammerError("Invalid value for attribute 'isRange': <" + isRange + ">. Allowed values are "
                    + org.makumba.commons.StringUtils.toString(allowedRanges));
        }
        this.isRange = isRange;
    }
}
