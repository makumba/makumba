package org.makumba.list.functions;

import javax.servlet.jsp.PageContext;

import org.makumba.LogicException;
import org.makumba.ProgrammerError;
import org.makumba.analyser.AnalysableExpression;
import org.makumba.analyser.PageCache;
import org.makumba.commons.StringUtils;
import org.makumba.list.tags.QueryTag;

/**
 * Represents all types of mak:count() functions, and does analysis on them.<br/>
 * FIXME: maybe {@link QueryTag#count()} and others should move here.
 * 
 * @author Rudolf Mayer
 * @version $Id$
 */
public class CountFunctions extends AnalysableExpression {

    private static final long serialVersionUID = 1L;

    @Override
    public void analyze(PageCache pageCache) {
        // do some validity checking

        if (StringUtils.equalsAny(expression, "count", "maxCount")) {
            checkNumberOfArguments(0);
            // check that count() and maxCount() are inside a list
            if (findParentWithClass(QueryTag.class) == null) {
                throw new ProgrammerError("Function '" + expression + "' needs to be enclosed in a LIST or OBJECT tag");
            }
        } else if (expression.equals("lastCount")) {
            checkNumberOfArguments(0);
            // check that there is a mak:list/object before lastCount()
            if (getElementBefore(pageCache, elData, QueryTag.class) == null) {
                throw new ProgrammerError("Function '" + expression + "' on " + elData.getLocation()
                        + " needs to be *after* a LIST or OBJECT tag");
            }
        } else if (expression.equals("nextCount")) {
            checkNumberOfArguments(0);
            // check that there is a mak:list/object after nextCount()

            // TODO: can't do the check at this stage, as the tags *after* aren't yet known....
            // if (getElementAfter(pageCache, elData, QueryTag.class) == null) {
            // throw new ProgrammerError("Function '" + expression + "' on " + elData.getLocation()
            // + " needs to be *before* a LIST or OBJECT tag");
            // }
        } else if (expression.equals("lastCountById")) {
            checkNumberOfArguments(1);
            // check that the mak:list/object specified exists
            String id = StringUtils.removeSingleQuote(elData.getArguments().get(0));
            checkTagFound(pageCache, "id", id, QueryTag.class);
        }
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
    public void setKey(PageCache pageCache) {
        // TODO Auto-generated method stub
    }

    @Override
    public String treatExpressionAtAnalysis(String expression) {
        return expression;
    }

}
