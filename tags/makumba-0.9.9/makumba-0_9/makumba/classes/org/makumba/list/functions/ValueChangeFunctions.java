package org.makumba.list.functions;

import javax.servlet.jsp.PageContext;

import org.makumba.LogicException;
import org.makumba.analyser.PageCache;
import org.makumba.commons.MultipleKey;
import org.makumba.commons.StringUtils;
import org.makumba.controller.http.MakumbaJspFactory;
import org.makumba.list.engine.QueryExecution;
import org.makumba.list.tags.QueryTag;

/**
 * Provides the mak:hasValueChanged('expr') and mak:willValueChange('expr') methods.<br/>
 * These methods will tell whether a specific expression has changed resp. will change its value from the previous resp.
 * in the next iteration.
 * 
 * @author Rudolf Mayer
 * @version $Id$
 */
public class ValueChangeFunctions extends GenericListValueFunction {
    private static final long serialVersionUID = 1L;

    @Override
    public void analyze(PageCache pageCache) {
        checkNumberOfArguments(1);
        String expr = StringUtils.removeSingleQuote(elData.getArguments().get(0));
        QueryTag parentList = getEnclosingList();
        registerValueAtParentList(pageCache, expr, parentList);
    }

    @Override
    public String getPrefix() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object resolve(PageContext pc, PageCache pageCache) throws LogicException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String treatExpressionAtAnalysis(String expression) {
        return expression;
    }

    public static boolean hasValueChanged(java.lang.String expr) {
        try {
            PageContext pageContext = MakumbaJspFactory.getPageContext();
            MultipleKey parentListKey = getRunningListKey(pageContext);
            QueryExecution execution = QueryExecution.getFor(parentListKey, pageContext, null, null);
            return execution.hasValueChanged(getValueComputer(expr, pageContext, parentListKey).getProjectionIndex());
        } catch (LogicException e) {
            // TODO proper error handling
            e.printStackTrace();
            return false;
        }
    }

    public static boolean willValueChange(java.lang.String expr) {
        try {
            PageContext pageContext = MakumbaJspFactory.getPageContext();
            MultipleKey parentListKey = getRunningListKey(pageContext);
            QueryExecution execution = QueryExecution.getFor(parentListKey, pageContext, null, null);
            return execution.willValueChange(getValueComputer(expr, pageContext, parentListKey).getProjectionIndex());
        } catch (LogicException e) {
            // TODO proper error handling
            e.printStackTrace();
            return false;
        }
    }
}
