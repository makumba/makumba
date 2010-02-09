package org.makumba.forms.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.TagSupport;

import org.makumba.LogicException;
import org.makumba.ProgrammerError;
import org.makumba.analyser.PageCache;
import org.makumba.commons.tags.GenericMakumbaTag;

/**
 * mak:submit tag, to submit forms. Very simple helper tag that submits the enclosing form, or if not enclosed in a
 * form, submits the form indicated by name. Can render either as a link or as a button.
 * 
 * @author Manuel Gay
 * @version $Id: SubmitTag.java,v 1.1 Jan 1, 2010 2:28:47 PM manu Exp $
 */
public class SubmitTag extends GenericMakumbaTag implements BodyTag {

    private static final long serialVersionUID = -5459766319927481205L;

    private static final String BUTTON = "button";

    private static final String LINK = "link";

    private BodyContent bc;

    private String form;

    private String widget;

    private boolean hasBody = false;

    public void setForm(String form) {
        this.form = form;
    }

    public void setWidget(String widget) {
        this.widget = widget;
    }

    @Override
    public boolean canHaveBody() {
        return true;
    }

    public void doInitBody() throws JspException {
        this.hasBody = true;
    }

    public void setBodyContent(BodyContent b) {
        this.bc = b;
    }

    private FormTagBase getForm() {
        return (FormTagBase) TagSupport.findAncestorWithClass(this, FormTagBase.class);
    }

    @Override
    public void doStartAnalyze(PageCache pageCache) {
        if (getForm() == null) {
            throw new ProgrammerError("mak:submit has to be inside of a form tag");
        }
        if (widget != null && !(widget.equals(LINK) || widget.equals(BUTTON))) {
            throw new ProgrammerError("the 'widget' attribute only takes the values 'link' and 'button'");
        }
    }

    @Override
    public int doAnalyzedStartTag(PageCache pageCache) throws LogicException, JspException {
        return EVAL_BODY_BUFFERED;
    }

    @Override
    public int doAnalyzedEndTag(PageCache pageCache) throws LogicException, JspException {

        if (widget == null) {
            widget = BUTTON;
        }

        // FIXME internationalization
        String text = "Submit";
        if (hasBody) {
            text = bc.getString();
        }

        try {
            if (widget.equals(BUTTON)) {
                pageContext.getOut().append("<input class=\"makSubmitButton\" type=\"submit\" value=\"").append(text).append(
                    "\" />");
            } else if (widget.equals(LINK)) {
                pageContext.getOut().append("<a class=\"makSubmitLink\" href=\"#\" onClick=\"").append(
                    getForm().getSubmitJavascriptCall(null, getForm().triggerEvent != null)).append("\">").append(text).append("</a>");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return super.doAnalyzedStartTag(pageCache);
    }

}
