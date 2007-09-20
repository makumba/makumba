package org.makumba.list.engine.valuecomputer;

import org.makumba.LogicException;
import org.makumba.analyser.AnalysableTag;
import org.makumba.analyser.PageCache;
import org.makumba.list.engine.QueryExecution;
import org.makumba.list.tags.MakumbaTag;

/**
 * The manager of a nullableValueQuery
 * 
 * @author Cristian Bogdan
 */
class NullableValueComputer extends QueryValueComputer {

    static final String emptyQueryProps[] = new String[5];

    /**
     * Makes a query that is identical to the parentQuery, but has expr as projection.
     * 
     * @param analyzed
     *            the tag that is analyzed
     * @param nullableExpr
     *            the nullable expression
     * @param expr
     *            the expression we use as projection
     * @param pageCache
     *            the page cache of the current page
     */
    NullableValueComputer(AnalysableTag analyzed, String nullableExpr, String expr, PageCache pageCache) {
        makeQueryAtAnalysis(analyzed, nullableExpr.trim(), emptyQueryProps, expr, pageCache);
    }

    /**
     * Checks if the iterationGroupData is longer than 1, and throws an exception if so. Takes the first result (if any)
     * otherwise.
     * 
     * @param running
     *            the tag that is currently running
     * @throws LogicException
     */
    public Object getValue(MakumbaTag running) throws LogicException {
        QueryExecution ex = runQuery(running);
        int n = ex.dataSize();
        if (n > 1)
            throw new RuntimeException("nullable query with more than one result ??? " + n);
        if (n == 0)
            return null;
        return ex.currentListData().data[projectionIndex];
    }
}