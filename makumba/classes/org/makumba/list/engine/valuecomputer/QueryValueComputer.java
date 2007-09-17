package org.makumba.list.engine.valuecomputer;

import org.makumba.LogicException;
import org.makumba.analyser.PageCache;
import org.makumba.list.engine.ComposedQuery;
import org.makumba.list.engine.QueryExecution;
import org.makumba.list.html.RecordViewer;
import org.makumba.list.tags.MakumbaTag;
import org.makumba.list.tags.QueryTag;
import org.makumba.util.MultipleKey;
import org.makumba.view.RecordFormatter;

/**
 * The ValueComputer of a queryMak:value
 * 
 * @author Cristian Bogdan
 */
public abstract class QueryValueComputer extends ValueComputer {

    /** The key of the generated query */
    MultipleKey queryKey;

    /**
     * Makes a key that adds the given keyDifference to the tagKey of the parentList, and associates with it a subquery
     * of the parentQuery made from the given queryProps.
     * 
     * @param analyzed
     *            the analyzed tag
     * @param keyDifference
     * @param queryProps
     * @param expr
     * @param pageCache
     *            the page cache of the current page
     */
    public void makeQueryAtAnalysis(MakumbaTag analyzed, String keyDifference, String[] queryProps, String expr,
            PageCache pageCache) {
        this.expr = expr;
        parentKey = analyzed.getParentListKey(pageCache);

        queryKey = new MultipleKey(parentKey, keyDifference);

        QueryTag.cacheQuery(pageCache, queryKey, queryProps, parentKey).checkProjectionInteger(expr);
    }

    /** The key of the query in which this value is a projection. Returns queryKey */
    MultipleKey getQueryKey() {
        return queryKey;
    }

    /**
     * If other ValueComputers sharing the same valueQuery did not analyze it yet, we analyze it here.
     * 
     * @param analyzed
     *            the analyzed tag
     * @param pageCache
     *            the page cache of the current page
     */
    public void doEndAnalyze(MakumbaTag analyzed, PageCache pageCache) {
        if (pageCache.retrieve(RecordFormatter.FORMATTERS, queryKey) == null) {
            ComposedQuery myQuery = QueryTag.getQuery(pageCache, queryKey);
            myQuery.analyze();
            pageCache.cache(RecordFormatter.FORMATTERS, queryKey, new RecordViewer(myQuery));
        }
        super.doEndAnalyze(analyzed, pageCache);
    }

    static final Object dummy = new Object();

    /**
     * Obtains the iterationGroupData for the valueQuery
     * 
     * @param running
     *            the tag that is currently running
     * @throws LogicException
     * @return The QueryExecution that will give us the data
     */
    QueryExecution runQuery(MakumbaTag running) throws LogicException {
        QueryExecution ex = QueryExecution.getFor(queryKey, running.getPageContext(), null, null);

        QueryExecution parentEx = QueryExecution.getFor(parentKey, running.getPageContext(), null, null);

        // if the valueQuery's iterationGroup for this parentIteration was not computed, do it now...
        if (parentEx.valueQueryData.get(queryKey) == null) {
            ex.getIterationGroupData();

            // ... and make sure it won't be done this parentIteration again
            parentEx.valueQueryData.put(queryKey, dummy);
        }
        return ex;
    }
}
