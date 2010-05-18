package org.makumba.list.functions;

import javax.servlet.jsp.PageContext;

import org.makumba.LogicException;
import org.makumba.analyser.PageCache;
import org.makumba.commons.MultipleKey;
import org.makumba.commons.StringUtils;
import org.makumba.controller.http.MakumbaJspFactory;
import org.makumba.list.engine.valuecomputer.ValueComputer;
import org.makumba.list.tags.ValueTag;

/**
 * Implements a mak:value() function, similar to the mak:value tag ({@link ValueTag}). The advantage of the function is
 * that it can be used inside JSTL/EL statements, e.g. a c:if. The function provides just basic value computing, but not
 * the advanced formatting that the {@link ValueTag} provides.
 * 
 * @author Rudolf Mayer
 * @version $Id$
 */
public class ValueFunction extends GenericListValueFunction {

    private static final long serialVersionUID = 1L;

    @Override
    public void analyze(PageCache pageCache) {
        checkNumberOfArguments(1);
        String expr = StringUtils.removeSingleQuote(elData.getArguments().get(0));
        registerValueAtParentList(pageCache, expr, getEnclosingList());
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

    public static Object value(String expr) {
        // resolving the value expression is relatively easy, and similar to ValueTag
        //
        // 1. we need to get the key from the parent mak:list/object
        // as the EL function is static, we rely on QueryTag setting the key of the current list to the pageContext
        //
        // 2. then get the pageCache, and simply retrieve the value computer for the key combined of parentList and expr
        //

        PageContext pageContext = MakumbaJspFactory.getPageContext();
        MultipleKey parentListKey = getRunningListKey(pageContext);
        ValueComputer vc = getValueComputer(expr, pageContext, parentListKey);

        if (vc != null) {
            try {
                return vc.getValue(pageContext);
            } catch (LogicException e) {
                // TODO: some error handling...
                e.printStackTrace();
                return null;
            }
        } else {
            // TODO: some error handling...
            return null;
        }
    }

}
