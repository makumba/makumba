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
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;

import org.makumba.CompositeValidationException;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.InvalidValueException;
import org.makumba.LogicException;
import org.makumba.MakumbaSystem;
import org.makumba.ProgrammerError;
import org.makumba.analyser.AnalysableElement;
import org.makumba.analyser.PageCache;
import org.makumba.analyser.TagData;
import org.makumba.commons.MakumbaJspAnalyzer;
import org.makumba.commons.MultipleKey;
import org.makumba.commons.StringUtils;
import org.makumba.commons.attributes.HttpParameters;
import org.makumba.commons.attributes.PageAttributes;
import org.makumba.commons.attributes.RequestAttributes;
import org.makumba.forms.html.dateEditor;
import org.makumba.forms.responder.ResponseControllerHandler;
import org.makumba.providers.Configuration;

/**
 * mak:input tag
 * 
 * @author Cristian Bogdan
 * @author Rudolf Mayer
 * @author Manuel Gay
 * @version $Id$
 */
public class InputTag extends BasicValueTag implements javax.servlet.jsp.tagext.BodyTag {

    private static final long serialVersionUID = 1L;

    protected String name = null;

    protected String display = null;

    protected String nameVar = null;

    protected String calendarEditorLink = null;

    private String autoComplete = null;

    protected boolean calendarEditor = Configuration.getCalendarEditorDefault();

    protected String nullOption;

    /** input with body, used only for choosers as yet * */
    BodyContent bodyContent = null;

    org.makumba.forms.html.ChoiceSet choiceSet;

    // unused for now, set when we know at analysis that this input has
    // a body and will generate a choser (because it has <mak:option > inside)
    boolean isChoser;

    @Override
    public String toString() {
        return "INPUT name=" + name + " value=" + valueExprOriginal + " dataType=" + dataType + "\n";
    }

    public void setDataType(String dt) {
        this.dataType = dt.trim();
    }

    public void setField(String field) {
        setName(field);
    }

    public void setName(String field) {
        this.name = field.trim();
    }

    public void setDisplay(String d) {
        this.display = d;
    }

    public void setNameVar(String var) {
        this.nameVar = var;
    }

    public void setNullOption(String s) {
        this.nullOption = s;
    }

    public void setClearDefault(String d) {
        params.put("clearDefault", d);
    }

    // Extra html formatting parameters
    public void setAccessKey(String s) {
        extraFormattingParams.put("accessKey", s);
    }

    public void setDisabled(String s) {
        extraFormattingParams.put("disabled", s);
    }

    public void setOnChange(String s) {
        extraFormattingParams.put("onChange", s);
    }

    public void setOnBlur(String s) {
        extraFormattingParams.put("onBlur", s);
    }

    public void setOnFocus(String s) {
        extraFormattingParams.put("onFocus", s);
    }

    public void setOnSelect(String s) {
        extraFormattingParams.put("onSelect", s);
    }

    public void setTabIndex(String s) {
        extraFormattingParams.put("tabIndex", s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTagKey(PageCache pageCache) {
        if (calendarEditorLink == null && pageContext != null) { // initialise default calendar link text
            calendarEditorLink = Configuration.getDefaultCalendarEditorLink(((HttpServletRequest) pageContext.getRequest()).getContextPath());
        }
        expr = valueExprOriginal;
        // FIXME: this fix is rather a quick fix, it does not provide any information about the location of the error
        // it may appear e.g. if you put a mak:input inside a mak:object, but not inside a form
        if (getForm() == null) {
            throw new ProgrammerError("input tag must be enclosed by a form tag");
        }
        if (expr == null) {
            expr = getForm().getDefaultExpr(name);
        }
        Object[] keyComponents = { name, getForm().tagKey };
        tagKey = new MultipleKey(keyComponents);
    }

    @Override
    FieldDefinition getTypeFromContext(PageCache pageCache) {
        return fdp.getInputTypeAtAnalysis(this, getForm().getDataTypeAtAnalysis(pageCache), name, pageCache);
    }

    /**
     * Determines the ValueComputer and associates it with the tagKey
     * 
     * @param pageCache
     *            the page cache of the current page
     */
    @Override
    public void doStartAnalyze(PageCache pageCache) {
        if (name == null) {
            throw new ProgrammerError("name attribute is required");
        }
        if (getForm() == null) {
            throw new ProgrammerError("input tag must be enclosed by a form tag!");
        }
        if (isValue()) {
            // check if the value is the same as a previous form name
            if (getForm().getNestedFormNames(pageCache).keySet().contains(expr)) { // delay the evaluation
                // we delay finding the value for later
                // String tagName = name + getForm().responder.getSuffix();
                getForm().lazyEvaluatedInputs.put(expr, name);
                setDisplay("false"); // we assume that this input does not need to be displayed. FIXME: maybe this
                // should be made explicit as a programmer error
            } else {
                fdp.onNonQueryStartAnalyze(this, isNull(), getForm().getTagKey(), pageCache, expr);
            }
        }
    }

    /**
     * Tells the ValueComputer to finish analysis, and sets the types for var and printVar
     * 
     * @param pageCache
     *            the page cache of the current page
     */
    @Override
    public void doEndAnalyze(PageCache pageCache) {
        if (getForm().lazyEvaluatedInputs.containsKey(expr)) {
            // set the input type as the form type
            TagData t = (TagData) pageCache.retrieve(MakumbaJspAnalyzer.TAG_DATA_CACHE, getForm().getNestedFormNames(
                pageCache).get(expr));
            DataDefinition type = ((FormTagBase) t.tagObject).type;
            pageCache.cache(MakumbaJspAnalyzer.INPUT_TYPES, tagKey,
                type.getFieldDefinition(type.getIndexPointerFieldName()));
            return;
        }

        // if we use printVar="xxx", set the type to char
        if (nameVar != null) {
            setType(pageCache, nameVar, ddp.makeFieldOfType(nameVar, "char"));
        }

        FieldDefinition contextType = getTypeFromContext(pageCache);

        boolean dataTypeIsDate = dataType != null && ddp.makeFieldDefinition("dummyName", dataType) != null
                && ddp.makeFieldDefinition("dummyName", dataType).isDateType();

        boolean contextTypeIsDate = contextType != null && contextType.isDateType();

        // if we have a date type and the calendarEditor is requested, request the inclusion of the needed resources
        if ((dataTypeIsDate || contextTypeIsDate) && calendarEditor
                && !StringUtils.equals(params.get("type"), "hidden")) {
            pageCache.cacheSetValues(NEEDED_RESOURCES,
                MakumbaSystem.getCalendarProvider().getNeededJavaScriptFileNames());
        }

        // if we use the JS set editor, request the inclusion of its resources
        if (StringUtils.equals(params.get("type"), "seteditor")) {
            pageCache.cacheSetValues(NEEDED_RESOURCES, new String[] { "makumbaSetChooser.js" });
            if (org.apache.commons.lang.StringUtils.isBlank(getForm().formName)) {
                throw new ProgrammerError(
                        "For using 'seteditor' input types, you have to give the encosing form a name, using name=\"  \"!");
            }
        }

        // if we use auto-complete, request the inclusion of its resources
        if (this.autoComplete != null && this.autoComplete.equals("true")) {
            pageCache.cacheSetValues(NEEDED_RESOURCES, new String[] { "prototype.js", "scriptaculous.js",
                    "makumba-autocomplete.js" });
        }

        super.doEndAnalyze(pageCache);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialiseState() {
        super.initialiseState();

        // if type is "file", make the form multipart (should this be here or in doMakumbaStartTag() ?)
        if ("file".equals(params.get("type"))) {
            getForm().setMultipart();
        }
    }

    public void setBodyContent(BodyContent bc) {
        bodyContent = bc;
        // for now, only chosers can have body
        choiceSet = new org.makumba.forms.html.ChoiceSet();
    }

    public void doInitBody() {
    }

    @Override
    public int doAnalyzedStartTag(PageCache pageCache) {
        // we do everything in doMakumbaEndTag, to give a chance to the body to set more attributes, etc
        return EVAL_BODY_BUFFERED;
    }

    @Override
    public int doAnalyzedEndTag(PageCache pageCache) throws JspException, LogicException {
        params.put("org.makumba.forms.queryLanguage", MakumbaJspAnalyzer.getQueryLanguage(pageCache));
        FieldDefinition type = (FieldDefinition) pageCache.retrieve(MakumbaJspAnalyzer.INPUT_TYPES, tagKey);

        // for file types, set the form to multi-part
        // FIXME: this check is a bit duplicated to the one in initialiseState(), but we only know the field-type here
        // need to do it here, to have both the type of this field and the responder available
        if (type.isBinaryType() || type.isFileType()) {
            getForm().setMultipart();
        }

        Object val = null;

        // if we are reloading the form page on validation errors, fill form inputs as in the request
        if (StringUtils.equals(pageContext.getRequest().getAttribute(ResponseControllerHandler.MAKUMBA_FORM_RELOAD),
            "true")) {
            String tagName = name + getForm().responder.getSuffix();
            HttpParameters parameters = RequestAttributes.getParameters((HttpServletRequest) pageContext.getRequest());
            if (type.isDateType()) {
                // we need a special treatment for date fields, as they do not come in a single input, but several ones
                val = dateEditor.readFrom(tagName, parameters);
                // if the date is the default value date, set it to null
                if (val.equals(type.getDefaultValue()) && parameters.getParameter(tagName + "_null") != null) {
                    val = null;
                }
            } else { // other types can be handled normally
                val = parameters.getParameter(tagName);
            }
            return computedValue(val, type);
        }

        if (isValue()) {
            // check whether we shall evaluate the value now, or later
            if (!getForm().getNestedFormNames(pageCache).containsKey(expr)) {
                val = fdp.getValue(getTagKey(), getPageContext(), pageCache);
            }
        }

        if (isAttribute()) {
            val = PageAttributes.getAttributes(pageContext).getAttribute(expr.substring(1));
        }

        if (val != null) {
            val = type.checkValue(val);
        }

        // if the value is null ==> check for a default value
        // FIXME: this is a basic implementation, it can only discover attributes, does not value computation yet
        // FIXME: also some of the code below is just repeating the one from above, could be optimised
        String defaultExpr = (String) params.get("default");
        if (val == null && defaultExpr != null && defaultExpr.toString().trim().length() > 0) {
            if (isAttribute(defaultExpr)) {
                val = PageAttributes.getAttributes(pageContext).getAttribute(defaultExpr.substring(1));
            }
            if (isValue(defaultExpr)) {
                // FIXME: actually implement this!
                // for now, we just let this be handled by the various editors, which know how to deal with text and
                // numbers
            }
            if (val != null) {
                val = type.checkValue(val);
            }
        }

        return computedValue(val, type);
    }

    void checkBodyContentForNonWhitespace() throws JspException {
        // if we find non-whitespace text between two options, we insert it in the choices, as "text" (no actual choice)
        if (bodyContent != null && bodyContent.getString().trim().length() > 0) {
            choiceSet.add(null, bodyContent.getString().trim(), false, false);
            try {
                bodyContent.clear();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    /**
     * A value was computed, do what's needed with it, cleanup and return the result of doMakumbaEndTag()
     * 
     * @param val
     *            the computed value
     * @param type
     *            the type of the computed value
     * @throws JspException
     * @throws {@link LogicException}
     */
    @Override
    int computedValue(Object val, FieldDefinition type) throws JspException, LogicException {
        checkBodyContentForNonWhitespace();

        if (choiceSet != null) {
            params.put(org.makumba.forms.html.ChoiceSet.PARAMNAME, choiceSet);
        }

        if ("false".equals(display)) {
            params.put("org.makumba.noDisplay", "dummy");
        }

        if (nullOption != null) {
            // nullOption is only applicable for charEnum and intEnum types
            FieldDefinition fd = getTypeFromContext(AnalysableElement.getPageCache(pageContext, MakumbaJspAnalyzer.getInstance()));
            if (!fd.isEnumType()
                    && !fd.isPointer()
                    && !(this instanceof SearchFieldTag && org.apache.commons.lang.StringUtils.equals(
                        ((SearchFieldTag) this).forceInputStyle, "single"))) {
                throw new ProgrammerError(
                        "Attribute 'nullOption' is only applicable for 'charEnum', 'intEnum' and 'ptr' types, but input '"
                                + fd.getName() + "' is of type '" + fd.getType() + "'!");
            }
            params.put("nullOption", nullOption);
        }

        if (type.isDateType() && !StringUtils.equals(params.get("type"), "hidden")) {
            // for dates (not hidden) we add info about calendarEditor
            if (calendarEditor) {
                params.put("calendarEditor", String.valueOf(calendarEditor));
            }
            if (calendarEditorLink != null) {
                params.put("calendarEditorLink", calendarEditorLink);
            }
        }

        if (autoComplete != null) {
            params.put("autoComplete", autoComplete);
        }

        String formatted = getForm().responder.format(name, type, val, params, extraFormatting.toString(),
            getForm().getFormIdentifier());
        String fieldName = name + getForm().responder.getSuffix();

        if (nameVar != null) {
            getPageContext().setAttribute(nameVar, fieldName);
        }

        if (display == null || !display.equals("false")) {
            try {
                // check for a possible composite validation error set, and do form annotation if needed
                CompositeValidationException errors = (CompositeValidationException) pageContext.getRequest().getAttribute(
                    ResponseControllerHandler.MAKUMBA_FORM_VALIDATION_ERRORS);
                Collection<InvalidValueException> exceptions = null;
                if (errors != null) { // get the exceptions for this field
                    exceptions = errors.getExceptions(fieldName);
                }
                // if requested, do annotation before the field
                if (StringUtils.equalsAny(getForm().annotation, new String[] { "before", "both" })
                        && exceptions != null) {
                    for (InvalidValueException invalidValueException : exceptions) {
                        printAnnotation(fieldName, invalidValueException);
                        if (getForm().annotationSeparator != null) {// print the separator, if existing
                            pageContext.getOut().print(getForm().annotationSeparator);
                        }
                    }
                }
                // print the actual form value
                pageContext.getOut().print(formatted);
                // if requested, do annotation after the field
                if (StringUtils.equalsAny(getForm().annotation, new String[] { "after", "both" }) && exceptions != null) {
                    for (InvalidValueException invalidValueException : exceptions) {
                        if (getForm().annotationSeparator != null) {// print the separator, if existing
                            pageContext.getOut().print(getForm().annotationSeparator);
                        }
                        printAnnotation(fieldName, invalidValueException);
                    }
                }
            } catch (java.io.IOException e) {
                throw new JspException(e.toString());
            }
        }

        name = valueExprOriginal = dataType = display = expr = nameVar = null;
        return EVAL_PAGE;
    }

    private void printAnnotation(String fieldName, InvalidValueException e) throws IOException {
        pageContext.getOut().print("<span class=\"LV_validation_message LV_invalid\">");
        // pageContext.getOut().print("<span class=\"formAnnotation\">");
        pageContext.getOut().print(e.getShortMessage());
        pageContext.getOut().print("</span>");
    }

    public void setCalendarEditorLink(String calendarEditorLink) {
        this.calendarEditorLink = calendarEditorLink;
    }

    public void setAutoComplete(String autoComplete) {
        this.autoComplete = autoComplete;
    }

    public void setCalendarEditor(String calendarEditor) {
        this.calendarEditor = Boolean.parseBoolean(calendarEditor);
    }

    public void setStepSize(String s) {
        params.put("stepSize", s);
    }

    @Override
    protected void doAnalyzedCleanup() {
        super.doAnalyzedCleanup();
        bodyContent = null;
        choiceSet = null;
        name = nameVar = nullOption = display = calendarEditorLink = autoComplete = null;
        calendarEditor = Configuration.getCalendarEditorDefault();
    }

}
