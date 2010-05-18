package org.makumba.list.functions;

import javax.servlet.jsp.PageContext;

import org.makumba.MakumbaError;
import org.makumba.ProgrammerError;
import org.makumba.analyser.AnalysableElement;
import org.makumba.analyser.AnalysableExpression;
import org.makumba.analyser.AnalysableTag;
import org.makumba.analyser.PageCache;
import org.makumba.commons.MakumbaJspAnalyzer;
import org.makumba.commons.MultipleKey;
import org.makumba.commons.StringUtils;
import org.makumba.list.engine.ComposedQuery;
import org.makumba.list.engine.valuecomputer.ValueComputer;
import org.makumba.list.tags.QueryTag;

/**
 * Provides a basic implementation for a EL function that uses a {@link ValueComputer} to compute some value from an
 * expression.
 * 
 * @author Rudolf Mayer
 * @version $Id$
 */
public abstract class GenericListValueFunction extends AnalysableExpression {

    private static final long serialVersionUID = 1L;

    public GenericListValueFunction() {
        super();
    }

    /** Registers this function at the parent/enclosing mak:list/object. */
    protected void registerValueAtParentList(PageCache pageCache, String expr, QueryTag parentList) {
        // analogously to ValueTag, we register a value computer
        ValueComputer vc = ValueComputer.getValueComputerAtAnalysis(true, parentList.getTagKey(), expr, pageCache);
        pageCache.cache(MakumbaJspAnalyzer.VALUE_COMPUTERS, key, vc);

        // FIXME: the following code is similar to ValueTag.doStartAnalyze; unifying might make sense.
        // if we add a projection to a query, we also cache this so that we know where the projection comes from (for
        // the relation analysis)
        ComposedQuery query = null;
        try {
            query = QueryTag.getQuery(pageCache, parentList.getTagKey());
        } catch (MakumbaError me) {
            // this happens when there is no query for this mak:value
            // we ignore it, query will stay null anyway
        }

        if (query != null) {
            pageCache.cache(MakumbaJspAnalyzer.PROJECTION_ORIGIN_CACHE, new MultipleKey(parentList.getTagKey(), expr),
                key);
        }
    }

    /**
     * Tells the ValueComputer to finish analysis.
     * 
     * @param pageCache
     *            the page cache of the current page
     */
    @Override
    public void doEndAnalyze(PageCache pageCache) {
        // analogously to ValueTag, we tell the value computer t a value computer
        ValueComputer vc = (ValueComputer) pageCache.retrieve(MakumbaJspAnalyzer.VALUE_COMPUTERS, key);
        vc.doEndAnalyze(pageCache);
    }

    /** Finds the enclosing mak:list/object */
    protected QueryTag getEnclosingList() {
        final QueryTag parentList = (QueryTag) findParentWithClass(QueryTag.class);
        if (parentList == null) {
            throw new ProgrammerError("Function '" + expression + "' needs to be enclosed in a LIST or OBJECT tag");
        }
        return parentList;
    }

    @Override
    public void setKey(PageCache pageCache) {
        checkNumberOfArguments(1);
        AnalysableTag parentList = getEnclosingList();
        String expr = StringUtils.removeSingleQuote(elData.getArguments().get(0));
        key = computeKey(expr, parentList.getTagKey());
    }

    /** computes the key for this function from the expression and the key of the enclosing mak:list/object */
    protected static MultipleKey computeKey(String expr, MultipleKey parentListKey) {
        return new MultipleKey(parentListKey, expr.trim());
    }

    /** retrieve the value computer for the expression from the pageCache */
    protected static ValueComputer getValueComputer(String expr, PageContext pageContext, MultipleKey parentListKey) {
        PageCache pageCache = AnalysableElement.getPageCache(pageContext, MakumbaJspAnalyzer.getInstance());
        ValueComputer vc = (ValueComputer) pageCache.retrieve(MakumbaJspAnalyzer.VALUE_COMPUTERS, computeKey(expr,
            parentListKey));
        return vc;
    }

    /** retrieve the key of the current mak:list from the stack of lists */
    protected static MultipleKey getRunningListKey(PageContext pageContext) {
        return QueryTag.getRunningQueryTagStack(pageContext).peek();
    }
}