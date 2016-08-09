package org.makumba.list.functions;

import javax.servlet.jsp.PageContext;

import org.makumba.ProgrammerError;
import org.makumba.analyser.AnalysableElement;
import org.makumba.analyser.AnalysableExpression;
import org.makumba.analyser.AnalysableTag;
import org.makumba.analyser.MakumbaJspAnalyzer;
import org.makumba.analyser.PageCache;
import org.makumba.commons.MultipleKey;
import org.makumba.commons.StringUtils;
import org.makumba.list.engine.valuecomputer.ValueComputer;
import org.makumba.list.tags.QueryTag;
import org.makumba.list.tags.ValueTag;

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
        ValueTag.startValueAnalyze(pageCache, key, expr, parentList.getTagKey());
    }

    /**
     * Tells the ValueComputer to finish analysis.
     * 
     * @param pageCache
     *            the page cache of the current page
     */
    @Override
    public void doEndAnalyze(PageCache pageCache) {
        ValueTag.endValueAnalyze(pageCache, key);
    }

    /** Finds the enclosing mak:list/object */
    protected QueryTag getEnclosingList() {
        final QueryTag parentList = QueryTag.findEnclosingList(this);
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
        ValueComputer vc = (ValueComputer) pageCache.retrieve(MakumbaJspAnalyzer.VALUE_COMPUTERS,
            computeKey(expr, parentListKey));
        return vc;
    }

    /** retrieve the key of the current mak:list from the stack of lists */
    protected static MultipleKey getRunningListKey(PageContext pageContext) {
        return QueryTag.getRunningQueryTagStack(pageContext).peek();
    }
}