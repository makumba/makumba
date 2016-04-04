package org.makumba.list.tags;

import javax.servlet.jsp.JspException;

import org.makumba.analyser.MakumbaJspAnalyzer;
import org.makumba.analyser.PageCache;
import org.makumba.commons.MultipleKey;
import org.makumba.list.engine.valuecomputer.ValueComputer;

public class ListGroupTag extends GenericListTag {

    /**
     * 
     */
    private static final long serialVersionUID = 8403662329907300929L;

    private String on = null;

    public void setOn(String s) {
        on = s;
    }

    @Override
    public boolean canHaveBody() {
        return true;
    }

    @Override
    public void setTagKey(PageCache pageCache) {
        addToParentListKey("listGroup" + on);
    }

    /**
     * Determines whether the tag can have the same key as others in the page
     * 
     * @return <code>true</code> if the tag is allowed to have the same key as others in the page, <code>false</code>
     *         otherwise
     */
    @Override
    public boolean allowsIdenticalKey() {
        return false;
    }

    @Override
    public void doStartAnalyze(PageCache pageCache) {
        ValueTag.startValueAnalyze(pageCache, tagKey, on, QueryTag.getParentListKey(this, pageCache));
    }

    @Override
    public void doEndAnalyze(PageCache pageCache) {
        ValueTag.endValueAnalyze(pageCache, tagKey);
    }

    @Override
    public int doAnalyzedStartTag(PageCache pageCache) throws JspException, org.makumba.LogicException {
        MultipleKey parentListKey = QueryTag.getParentListKey(this, pageCache);
        QueryExecution ex = QueryExecution.getFor(parentListKey, pageContext, null, null, null);
        ValueComputer vc = (ValueComputer) pageCache.retrieve(MakumbaJspAnalyzer.VALUE_COMPUTERS, tagKey);
        if (!ex.hasValueChanged(vc.getProjectionIndex()))
            return SKIP_BODY;

        return EVAL_BODY_INCLUDE;
    }

    @Override
    public int doAnalyzedEndTag(PageCache pageCache) throws JspException {
        return EVAL_PAGE;
    }

}
