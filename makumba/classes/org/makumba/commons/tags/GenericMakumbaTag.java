package org.makumba.commons.tags;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;

import org.makumba.FieldDefinition;
import org.makumba.ProgrammerError;
import org.makumba.analyser.AnalysableTag;
import org.makumba.analyser.PageCache;

public class GenericMakumbaTag extends AnalysableTag {
    
    private static final long serialVersionUID = 1L;

    /** Tag parameters */
    public Hashtable<String, Object> params = new Hashtable<String, Object>(7); // holds certain 'normal' tag attributes

    public Map<String, String> extraFormattingParams = new HashMap<String, String>(7); // container for html formatting params

    /** Extra html formatting, copied verbatim to the output */
    public StringBuffer extraFormatting;

    /**
     * Resets and initialises the tag's state, to work in a tag pool. See bug 583. If this method is overriden in a
     * child class, the child's method must call super.initaliseState().
     */
    public void initialiseState() {
        extraFormatting = new StringBuffer();
    
        for (Iterator it = extraFormattingParams.entrySet().iterator(); it.hasNext();) {
            Map.Entry me = (Map.Entry) it.next();
            extraFormatting.append(" ").append(me.getKey()).append("=\"").append(me.getValue()).append("\" ");
        }
    }
    
    @Override
    public int doStartTag() throws JspException {
        initialiseState();
        return super.doStartTag();
    }
    
    @Override
    public int doEndTag() throws JspException {
        int doEndTag = super.doEndTag();
        params.clear();
        extraFormattingParams.clear();
        extraFormatting = null;
        return doEndTag;
    }
    
    @Override
    protected void treatException(Throwable t) throws JspException {
        if (pageContext == null)
            throw (JspException) t;

        org.makumba.controller.http.ControllerFilter.treatException(t, (HttpServletRequest) pageContext.getRequest(),
            (HttpServletResponse) pageContext.getResponse());
    }

    public void setStyleId(String s) {
        extraFormattingParams.put("id", s);
    }

    public void setStyleClass(String s) {
        extraFormattingParams.put("class", s);
    }

    public void setStyle(String s) {
        extraFormattingParams.put("style", s);
    }

    public void setTitle(String s) {
        extraFormattingParams.put("title", s);
    }

    public void setOnClick(String s) {
        extraFormattingParams.put("onClick", s);
    }

    public void setOnDblClick(String s) {
        extraFormattingParams.put("onDblClick", s);
    }

    public void setOnKeyDown(String s) {
        extraFormattingParams.put("onKeyDown", s);
    }

    public void setOnKeyUp(String s) {
        extraFormattingParams.put("onKeyUp", s);
    }

    public void setOnKeyPress(String s) {
        extraFormattingParams.put("onKeyPress", s);
    }

    public void setOnMouseDown(String s) {
        extraFormattingParams.put("onMouseDown", s);
    }

    public void setOnMouseUp(String s) {
        extraFormattingParams.put("onMouseUp", s);
    }

    public void setOnMouseMove(String s) {
        extraFormattingParams.put("onMouseMove", s);
    }

    public void setOnMouseOut(String s) {
        extraFormattingParams.put("onMouseOut", s);
    }

    public void setOnMouseOver(String s) {
        extraFormattingParams.put("onMouseOver", s);
    }

    public void setUrlEncode(String s) {
        params.put("urlEncode", s);
    }

    public void setHtml(String s) {
        params.put("html", s);
    }

    public void setFormat(String s) {
        params.put("format", s);
    }

    public void setType(String s) {
        params.put("type", s);
    }

    public void setSize(String s) {
        params.put("size", s);
    }

    public void setMaxlength(String s) {
        params.put("maxlength", s);
    }

    public void setMaxLength(String s) {
        params.put("maxLength", s);
    }

    public void setEllipsis(String s) {
        params.put("ellipsis", s);
    }

    public void setEllipsisLength(String s) {
        params.put("ellipsisLength", s);
    }

    public void setAddTitle(String s) {
        params.put("addTitle", s);
    }

    public void setRows(String s) {
        params.put("rows", s);
    }

    public void setCols(String s) {
        params.put("cols", s);
    }

    public void setLineSeparator(String s) {
        params.put("lineSeparator", s);
    }

    public void setLongLineLength(String s) {
        params.put("longLineLength", s);
    }

    public void setDefault(String s) {
        params.put("default", s);
    }

    public void setEmpty(String s) {
        params.put("empty", s);
    }

    public void setLabelSeparator(String s) {
        params.put("labelSeparator", s);
    }

    public void setElementSeparator(String s) {
        params.put("elementSeparator", s);
    }

    public String toString() {
        return getClass().getName() + " " + params + "\n" + getPageTextInfo();
    }

    public Hashtable getParams() {
        return params;
    }

    /**
     * Sets the type identified by the key of a tag
     * 
     * @param key
     *            the key of the tag
     * @param value
     *            the field definition containing the type
     * @param t
     *            the MakumbaTag
     */
    protected void setType(PageCache pc, String key, FieldDefinition value) {
        Object[] val1 = (Object[]) pc.retrieve(TYPES, key);
        FieldDefinition fd = null;
    
        if (val1 != null)
            fd = (FieldDefinition) val1[0];
        // if we get nil here, we keep the previous, richer type information
        if (fd != null && value.getType().equals("nil"))
            return;
    
        AnalysableTag.analyzedTag.set(tagData);
        if (fd != null && !value.isAssignableFrom(fd))
            throw new ProgrammerError("Attribute type changing within the page: in tag\n"
                    + ((AnalysableTag) val1[1]).getTagText() + " attribute " + key + " was determined to have type " + fd
                    + " and the from this tag results the incompatible type " + value);
        AnalysableTag.analyzedTag.set(null);
    
        Object[] val2 = { value, this };
        pc.cache(TYPES, key, val2);
    }



}
