package org.makumba.list.tags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.jsp.JspException;

import org.makumba.LogicException;
import org.makumba.analyser.MakumbaJspAnalyzer;
import org.makumba.analyser.PageCache;
import org.makumba.commons.MultipleKey;
import org.makumba.list.engine.valuecomputer.ValueComputer;

public class ListGroupTag extends GenericListTag {

    /**
     * 
     */
    private static final long serialVersionUID = 8403662329907300929L;

    private String separator = "";

    public void setSeparator(String s) {
        separator = s;
    }

    @Override
    public boolean canHaveBody() {
        return true;
    }

    @Override
    public void setTagKey(PageCache pageCache) {
        addToParentListKey("listGroup");
    }

    /**
     * Determines whether the tag can have the same key as others in the page
     * 
     * @return <code>true</code> if the tag is allowed to have the same key as others in the page, <code>false</code>
     *         otherwise
     */
    @Override
    public boolean allowsIdenticalKey() {
        return true;
    }

    private static final String CANDIDATE_VALUES = "org.makumba.list.groupCandidates";

    private static final String GROUPABLE = "org.makumba.list.groupableList";

    public static void addCandidateGroupValue(ValueTag valueTag, PageCache pageCache) {
        MultipleKey parentListKey = QueryTag.getParentListKey(valueTag, pageCache);
        @SuppressWarnings("unchecked")
        List<MultipleKey> tags = (List<MultipleKey>) pageCache.retrieve(CANDIDATE_VALUES, parentListKey);
        if (tags == null) {
            tags = new ArrayList<MultipleKey>();
            pageCache.cache(CANDIDATE_VALUES, parentListKey, tags);
        }
        tags.add(valueTag.tagKey);
    }

    @Override
    public void doStartAnalyze(PageCache pageCache) {
        pageCache.cache(GROUPABLE, QueryTag.getParentListKey(this, pageCache), "true");
    }

    @SuppressWarnings("unchecked")
    public static void checkHideGroupHeader(QueryTag list, PageCache pageCache) throws JspException {
        // if not groupable, return
        if (list.distinctData == null) {
            list.distinctDataChanged = true;
            return;
        }

        // eval all candidates, compare with candidate value from same list-tag in
        // if nothing changed print start of comment
        list.distinctDataChanged = false;

        for (MultipleKey k : (List<MultipleKey>) pageCache.retrieve(CANDIDATE_VALUES, list.tagKey)) {
            Object val;
            try {
                val = ((ValueComputer) pageCache.retrieve(MakumbaJspAnalyzer.VALUE_COMPUTERS, k)).getValue(list.getPageContext());
            } catch (LogicException e) {
                throw new JspException(e);
            }
            if (!val.equals(list.distinctData.get(k))) {
                list.distinctDataChanged = true;
            }
            list.distinctData.put(k, val);
        }
        openComment(list);
    }

    public static void checkHideGroupFooter(QueryTag list) throws JspException {
        closeComment(list);
    }

    public static void endList(QueryTag list) {
        list.distinctData = null;
    }

    public static void startList(PageCache pageCache, QueryTag list) {
        if (pageCache.retrieve(GROUPABLE, list.getTagKey()) != null) {
            list.distinctData = new HashMap<MultipleKey, Object>();
        }
    }

    @Override
    public int doAnalyzedStartTag(PageCache pageCache) throws JspException, org.makumba.LogicException {
        QueryTag parentList = (QueryTag) QueryTag.getParentList(this);
        closeComment(parentList);
        if (!parentList.distinctDataChanged) {
            try {
                pageContext.getOut().print(separator);
            } catch (IOException e) {
                throw new JspException(e);
            }
        }
        return EVAL_BODY_INCLUDE;
    }

    @Override
    public int doAnalyzedEndTag(PageCache pageCache) throws JspException {
        openComment((QueryTag) QueryTag.getParentList(this));
        return EVAL_PAGE;
    }

    private static void openComment(QueryTag list) throws JspException {
        if (!list.distinctDataChanged) {
            try {
                list.getPageContext().getOut().print("<!-- ");
            } catch (IOException e) {
                throw new JspException(e);
            }
        }
    }

    private static void closeComment(QueryTag list) throws JspException {
        if (!list.distinctDataChanged) {
            try {
                list.getPageContext().getOut().print(" -->");
            } catch (IOException e) {
                throw new JspException(e);
            }
        }
    }

}
