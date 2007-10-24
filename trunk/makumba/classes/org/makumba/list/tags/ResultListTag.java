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
 * FIXME: it does not work yet...
 * 
 * @see SearchTag
 * @see QueryTag
 * @author Rudolf Mayer
 * @version $Id: ResultListTag.java,v 1.1 Oct 24, 2007 1:04:02 PM mayer Exp $
 */
public class ResultListTag extends QueryTag {
    private static final long serialVersionUID = 1L;

    private String resultsFrom;

    public void setResultsFrom(String s) {
        this.resultsFrom = s;
    }

    @Override
    public int doAnalyzedEndTag(PageCache pageCache) throws JspException {
        return super.doAnalyzedEndTag(pageCache);
    }

    @Override
    public int doAnalyzedStartTag(PageCache pageCache) throws LogicException, JspException {
        setFieldsFromSearchFormInfo(pageCache);
        return super.doAnalyzedStartTag(pageCache);
    }

    @Override
    public void doEndAnalyze(PageCache pageCache) {
        super.doEndAnalyze(pageCache);
    }

    @Override
    public void doStartAnalyze(PageCache pageCache) {
        setFieldsFromSearchFormInfo(pageCache);
        super.doStartAnalyze(pageCache);
    }

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
        setVariableFrom("#{" + resultsFrom + "VariableFrom" + "}");
        setWhere("#{" + resultsFrom + "Where" + "}");
        if (tag != null) {
            tag.attributes.put("from", queryProps[ComposedQuery.WHERE]);
            tag.attributes.put("from", queryProps[ComposedQuery.VARFROM]);
        }
    }

    @Override
    public void setTagKey(PageCache pageCache) {
        // super.setTagKey(pageCache);
        setFieldsFromSearchFormInfo(pageCache);
        super.setTagKey(pageCache);
    }

}
