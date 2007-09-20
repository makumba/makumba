///////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003  http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//  -------------
//  $Id$
//  $Name$
/////////////////////////////////////

package org.makumba.list.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;

import org.makumba.LogicException;
import org.makumba.ProgrammerError;
import org.makumba.analyser.PageCache;
import org.makumba.list.engine.valuecomputer.ValueComputer;
import org.makumba.view.jsptaglib.MakumbaJspAnalyzer;
import org.makumba.view.jsptaglib.MakumbaJspException;

/**
 * If tag will accept test="..." similar to value tag, and will show body only if OQL expression evaluates to true
 * (integer 1).
 * 
 * @author Frederik Habilis
 * @since makumba-0.5.9.11
 * @version $Id$
 */

public class IfTag extends GenericListTag implements BodyTag {

    private static final long serialVersionUID = 1L;

    String testExpr;

    private static final Integer TRUE_INT = new Integer(1);

    public void setTest(String s) {
        this.testExpr = s;
    }

    // these 2 are required to implement BodyTag, no action needed inside.
    public void setBodyContent(BodyContent bc) {
    }

    public void doInitBody() {
    }

    /**
     * Sets tagKey to uniquely identify this tag. Called at analysis time before doStartAnalyze() and at runtime before
     * doMakumbaStartTag()
     * @param pageCache the page cache of the current page
     */
    public void setTagKey(PageCache pageCache) {
        addToParentListKey(testExpr.trim());
    }

    /** 
     * Determines the ValueComputer and associates it with the tagKey .
     * If we use Hibernate we need to adjust the syntax
     * @param pageCache the page cache of the current page
     * 
     * FIXME QueryExecutionProvider should tell us the syntax for the if boolean test
     * 
     */
    public void doStartAnalyze(PageCache pageCache) {
        if((Boolean) pageCache.retrieve(MakumbaJspAnalyzer.QUERY_LANGUAGE, MakumbaJspAnalyzer.QUERY_LANGUAGE).equals("hql")) {
            pageCache.cache(GenericListTag.VALUE_COMPUTERS, tagKey, ValueComputer.getValueComputerAtAnalysis(this, QueryTag.getParentListKey(this, pageCache), new String("case when " + testExpr +" then 1 else 0 end"), pageCache));
        } else {
            pageCache.cache(GenericListTag.VALUE_COMPUTERS, tagKey, ValueComputer.getValueComputerAtAnalysis(this, QueryTag.getParentListKey(this, pageCache), testExpr, pageCache));
        }
    }

    /** 
     * Tells the ValueComputer to finish analysis, check for proper test result type
     * @param pageCache the page cache of the current page
     */
    public void doEndAnalyze(PageCache pageCache) {
        ValueComputer vc = (ValueComputer) pageCache.retrieve(GenericListTag.VALUE_COMPUTERS, tagKey);
        vc.doEndAnalyze(pageCache);
        String type = vc.getType().getDataType();
        if (!"int".equals(type)) {
            throw new ProgrammerError("mak:if test expression must be of type 'int'. In this case [" + this
                    + "], type is " + type);
        }
    }

    /** Asks the ValueComputer to calculate the expression, and SKIP_BODY if false.
     * @param pageCache the page cache of the current page
     * @throws JspException
     * @throws LogicException
     */
    public int doAnalyzedStartTag(PageCache pageCache) throws JspException,
            org.makumba.LogicException {
        Object exprvalue = ((ValueComputer) pageCache.retrieve(GenericListTag.VALUE_COMPUTERS, tagKey)).getValue(this.getPageContext());

        if (exprvalue instanceof Integer) {
            int i = ((Integer) exprvalue).intValue();
            if (i == 1) {
                return EVAL_BODY_INCLUDE;
            } else if (i == 0) {
                return SKIP_BODY;
            }

            // integer value other than 1/0
            throw new MakumbaJspException(this, "test expression in mak:if should result in 0 or 1; result is "
                    + exprvalue);
        }

        // comparison with null, will return null, equivalent to false
        if (exprvalue == null)
            return SKIP_BODY;

        // return value is another type
        throw new MakumbaJspException(this, "test expression in mak:if should result in an Integer, result is "
                + exprvalue);

    }

    public String toString() {
        return "IF test=" + testExpr + " parameters: " + params;
    }
}
