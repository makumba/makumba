package org.makumba.list.tags;

import javax.servlet.jsp.JspException;

import org.apache.commons.lang.StringUtils;
import org.makumba.LogicException;
import org.makumba.ProgrammerError;
import org.makumba.analyser.PageCache;
import org.makumba.analyser.TagData;
import org.makumba.commons.MakumbaJspAnalyzer;
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

    private static final String MODE_SEARCH = "search";

    private static final String MODE_FILTER = "filter";

    private String resultsFrom;

    private String mode = MODE_SEARCH;

    private boolean noResultsPresent = false; // indicates whether we the search form was execute already

    public void setResultsFrom(String s) {
        this.resultsFrom = s;
    }

    @Override
    protected void registerPossibleAttributeValues() {
        registerAttributeValues("mode", MODE_SEARCH, MODE_FILTER);
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setStaticWhere(String s) {
        queryProps[ComposedQuery.STATIC_WHERE] = s;
    }

    @Override
    public int doAnalyzedStartTag(PageCache pageCache) throws LogicException, JspException {
        setFieldsFromSearchFormInfo(pageCache);

        this.noResultsPresent = false;

        // check whether we have the attributes from the search present
        String[] attributesToCheck = { SearchTag.ATTRIBUTE_NAME_VARIABLE_FROM, SearchTag.ATTRIBUTE_NAME_WHERE };
        for (int i = 0; i < attributesToCheck.length; i++) {
            String thisAttribute = resultsFrom + attributesToCheck[i];
            if (pageContext.getRequest().getAttribute(thisAttribute) == null) {
                this.noResultsPresent = true;
                if (mode.equals(MODE_SEARCH)) {// if we do search, we only execute the tag if we already did the search
                    return SKIP_BODY;
                }
            }
        }

        if (mode.equals(MODE_FILTER) && noResultsPresent) {
            // if we filter, but the search hasn't been done yet, we execute an unfiltered list
            // thus, we need to populate the variableFrom & where fields with empty values
            pageContext.setAttribute(resultsFrom + SearchTag.ATTRIBUTE_NAME_VARIABLE_FROM, "");
            pageContext.setAttribute(resultsFrom + SearchTag.ATTRIBUTE_NAME_WHERE, "");
            setFieldsFromSearchFormInfo(pageCache);
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
        TagData tag = (TagData) pageCache.retrieve(MakumbaJspAnalyzer.TAG_DATA_CACHE, new MultipleKey(
                new Object[] { resultsFrom }));
        if (tag != null) {
            setFrom(tag.attributes.get("in") + " "
                    + StringUtils.defaultString(tag.attributes.get("resultLabel"), SearchTag.OBJECT_NAME));
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
        if (noResultsPresent && mode.equals(MODE_SEARCH)) { // no results ==> skip the list in search mode
            return SKIP_BODY;
        } else {
            return super.doAnalyzedEndTag(pageCache);
        }
    }

}
