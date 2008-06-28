package org.makumba.list.tags;

import javax.servlet.jsp.JspException;

import org.makumba.LogicException;
import org.makumba.ProgrammerError;
import org.makumba.analyser.PageCache;
import org.makumba.analyser.TagData;
import org.makumba.commons.MultipleKey;
import org.makumba.forms.tags.SearchTag;
import org.makumba.list.engine.ComposedQuery;

/**
 * This class is a specially tailored mak:list to work on the results from a search form. It provides short-cuts to set
 * the from, variableFrom and where parameters from the request attributes set by the search form. <br>
 * 
 * @see SearchTag
 * @see QueryTag
 * @author Rudolf Mayer
 * @version $Id: ResultListTag.java,v 1.1 Oct 24, 2007 1:04:02 PM mayer Exp $
 */
public class ResultListTag extends QueryTag {
    private static final long serialVersionUID = 1L;

    private String resultsFrom;

    private boolean noResultsPresent = false; // indicates whether we execute the list, or not

    public void setResultsFrom(String s) {
        this.resultsFrom = s;
    }

    @Override
    public int doAnalyzedStartTag(PageCache pageCache) throws LogicException, JspException {
        setFieldsFromSearchFormInfo(pageCache);
        // check whether we have the attributes from the search present, if not, don't process the list
        String[] attributesToCheck = { SearchTag.ATTRIBUTE_NAME_VARIABLE_FROM, SearchTag.ATTRIBUTE_NAME_WHERE };
        for (int i = 0; i < attributesToCheck.length; i++) {
            String thisAttribute = resultsFrom + attributesToCheck[i];
            if (pageContext.getRequest().getAttribute(thisAttribute) == null) {
                this.noResultsPresent = true;
                return SKIP_BODY;
            }
        }
        return super.doAnalyzedStartTag(pageCache);
    }

    @Override
    public void doStartAnalyze(PageCache pageCache) {
        setFieldsFromSearchFormInfo(pageCache);
        super.doStartAnalyze(pageCache);
    }

    /**
     * Sets the from, variableFrom and where parts of the query from the search form information. This method must be
     * called before any of the super class methods to do analysis & execution is invoked.
     */
    private void setFieldsFromSearchFormInfo(PageCache pageCache) {
        TagData tag = (TagData) pageCache.retrieve(TagData.TAG_DATA_CACHE,
            new MultipleKey(new Object[] { resultsFrom }));
        if (tag != null) {
            setFrom(tag.attributes.get("in") + " " + SearchTag.OBJECT_NAME);
            tag.attributes.put("from", queryProps[ComposedQuery.FROM]);
        } else if (queryProps[ComposedQuery.FROM] == null) {
            throw new ProgrammerError(
                    "Could not find search form '"
                            + resultsFrom
                            + "' in this page. Please check the name is correct, or if the search form is on a different page, please specify the type to be searched with the from=\"\" attribute.");
        }
        setVariableFrom("#{" + resultsFrom + SearchTag.ATTRIBUTE_NAME_VARIABLE_FROM + "}");
        setWhere("#{" + resultsFrom + SearchTag.ATTRIBUTE_NAME_WHERE + "}");
        if (tag != null) {
            tag.attributes.put("where", queryProps[ComposedQuery.WHERE]);
            tag.attributes.put("variableFrom", queryProps[ComposedQuery.VARFROM]);
        }
    }

    @Override
    public void setTagKey(PageCache pageCache) {
        setFieldsFromSearchFormInfo(pageCache);
        super.setTagKey(pageCache);
    }

    public int doAnalyzedEndTag(PageCache pageCache) throws JspException {
        if (noResultsPresent) { // no results ==> skip the list
            return SKIP_BODY;
        } else {
            return super.doAnalyzedEndTag(pageCache);
        }
    }

}
