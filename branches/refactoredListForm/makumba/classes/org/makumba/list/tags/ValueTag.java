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

import org.makumba.MakumbaSystem;
import org.makumba.analyser.PageCache;
import org.makumba.list.engine.valuecomputer.ValueComputer;
import org.makumba.view.jsptaglib.MakumbaJspAnalyzer;

/**
 * mak:value tag
 * 
 * @author Cristian Bogdan
 *
 */
public class ValueTag extends MakumbaTag {

    private static final long serialVersionUID = 1L;

    private String expr;

    private String var;

    private String printVar;

    public void setExpr(String expr) {
        this.expr = expr;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setPrintVar(String var) {
        this.printVar = var;
    }
    
    public String getExpr() {
        return expr;
    }

    public String getPrintVar() {
        return printVar;
    }

    public String getVar() {
        return var;
    }

    /**
     * Sets tagKey to uniquely identify this tag. Called at analysis time before doStartAnalyze() and at runtime before
     * doMakumbaStartTag()
     * 
     * @param pageCache
     *            the page cache of the current page
     */
    public void setTagKey(PageCache pageCache) {
        addToParentListKey(expr.trim());
    }

    /** 
     * Determines the ValueComputer and associates it with the tagKey
     * @param pageCache the page cache of the current page
     */
    public void doStartAnalyze(PageCache pageCache) {
        pageCache.cache(MakumbaTag.VALUE_COMPUTERS, tagKey, ValueComputer.getValueComputerAtAnalysis(this, expr, pageCache));
    }

    /** 
     * Tells the ValueComputer to finish analysis, and sets the types for var and printVar.
     * @param pageCache the page cache of the current page
     */
    public void doEndAnalyze(PageCache pageCache) {
        ValueComputer vc = (ValueComputer) pageCache.retrieve(MakumbaTag.VALUE_COMPUTERS, tagKey);
        vc.doEndAnalyze(this, pageCache);

        if (var != null)
            setType(pageCache, var, vc.getType());

        if (printVar != null)
            setType(pageCache, printVar, MakumbaSystem.makeFieldOfType(printVar, "char"));
    }

    /** 
     * Asks the ValueComputer to present the expression
     * @param pageCache the page cache of the current page
     * @throws JspException
     * @throws LogicException
     *  */
    public int doMakumbaStartTag(PageCache pageCache) throws JspException,
            org.makumba.LogicException {
        ((ValueComputer) pageCache.retrieve(MakumbaTag.VALUE_COMPUTERS, tagKey)).print(this, pageCache);

        expr = printVar = var = null;
        return EVAL_BODY_INCLUDE;
    }

    /**
     * Computes a string
     * @return A String holding the value in a form useful for debugging
     */
    public String toString() {
        return "VALUE expr=" + expr + " parameters: " + params;
    }

}
