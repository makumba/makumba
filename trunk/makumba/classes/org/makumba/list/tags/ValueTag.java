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

import java.io.IOException;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

import org.apache.commons.lang.StringUtils;
import org.makumba.FieldDefinition;
import org.makumba.LogicException;
import org.makumba.MakumbaError;
import org.makumba.MakumbaSystem;
import org.makumba.Pointer;
import org.makumba.analyser.PageCache;
import org.makumba.commons.MakumbaResourceServlet;
import org.makumba.commons.MultipleKey;
import org.makumba.devel.relations.JSPRelationMiner;
import org.makumba.list.engine.ComposedQuery;
import org.makumba.list.engine.valuecomputer.ValueComputer;

/**
 * mak:value tag
 * 
 * FIXME - "editable" for now only works for OQL, it needs to take into account HQL's ".id"
 * 
 * @author Cristian Bogdan
 * @author Manuel Gay
 * @author Marius Andra
 */
public class ValueTag extends GenericListTag {

    private static final long serialVersionUID = 1L;

    private String expr;

    private String var;

    private String printVar;

    private String editable;

    public void setExpr(String expr) {
        this.expr = expr;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setPrintVar(String var) {
        this.printVar = var;
    }

    public void setEditable(String s) {
        this.editable = s;
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
     * Determines the ValueComputer and caches it with the tagKey
     * 
     * @param pageCache
     *            the page cache of the current page
     */
    public void doStartAnalyze(PageCache pageCache) {

        pageCache.cache(GenericListTag.VALUE_COMPUTERS, tagKey, ValueComputer.getValueComputerAtAnalysis(this,
            QueryTag.getParentListKey(this, pageCache), expr, pageCache));

        if (StringUtils.equals(editable, "true")) {

            // if we want this value to be editable, we also ask for a ValueComputer of the parent object
            String parentExpr = expr.indexOf(".") > 0 ? expr.substring(0, expr.lastIndexOf(".")) : null;

            if (parentExpr != null) {

                // TODO move me to setTagKey
                MultipleKey parentObjectKey = new MultipleKey(tagKey);
                parentObjectKey.remove(tagKey.size() - 1);
                parentObjectKey.add(parentExpr);

                // first try to retrieve the projection, see if it already exists
                Object check = pageCache.retrieve(GenericListTag.VALUE_COMPUTERS, parentObjectKey);
                if (check == null) {

                    ValueComputer vc = ValueComputer.getValueComputerAtAnalysis(this, QueryTag.getParentListKey(this,
                        pageCache), parentExpr, pageCache);
                    pageCache.cache(GenericListTag.VALUE_COMPUTERS, parentObjectKey, vc);

                }
            }
        }
        // if we add a projection to a query, we also cache this so that we know where the projection comes from (for
        // the relation analysis)
        ComposedQuery query = null;
        try {
            query = QueryTag.getQuery(pageCache, QueryTag.getParentListKey(this, pageCache));
        } catch (MakumbaError me) {
            // this happens when there is no query for this mak:value
            // we ignore it, query will stay null anyway
        }

        if (query != null) {
            pageCache.cache(JSPRelationMiner.PROJECTION_ORIGIN_CACHE, new MultipleKey(QueryTag.getParentListKey(this,
                pageCache), expr), tagKey);
        }

    }

    /**
     * Tells the ValueComputer to finish analysis, and sets the types for var and printVar.
     * 
     * @param pageCache
     *            the page cache of the current page
     */
    public void doEndAnalyze(PageCache pageCache) {
        ValueComputer vc = (ValueComputer) pageCache.retrieve(GenericListTag.VALUE_COMPUTERS, tagKey);
        vc.doEndAnalyze(pageCache);

        if (StringUtils.equals(editable, "true")) {
            String parentExpr = expr.indexOf(".") > 0 ? expr.substring(0, expr.lastIndexOf(".")) : null;

            if (parentExpr != null) {
                MultipleKey parentObjectKey = new MultipleKey(tagKey);
                parentObjectKey.remove(tagKey.size() - 1);
                parentObjectKey.add(parentExpr);
                ((ValueComputer) pageCache.retrieve(GenericListTag.VALUE_COMPUTERS, parentObjectKey)).doEndAnalyze(pageCache);

            }

        }

        if (var != null)
            setType(pageCache, var, vc.getType());

        if (printVar != null)
            setType(pageCache, printVar, MakumbaSystem.makeFieldOfType(printVar, "char"));

        // add needed resources, stored in cache for this page
        if (StringUtils.equals(editable, "true")) {
            pageCache.cacheSetValues(NEEDED_RESOURCES, new String[] { "makumba-editinplace.js",
                    "makumbaEditInPlace.css" });
        }

    }

    /**
     * Asks the ValueComputer to present the expression
     * 
     * @param pageCache
     *            the page cache of the current page
     * @throws JspException
     * @throws LogicException
     */
    public int doAnalyzedStartTag(PageCache pageCache) throws JspException, org.makumba.LogicException {

        StringBuffer sb = new StringBuffer();

        if (StringUtils.equals(editable, "true")) {

            HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
            // if we are at the first value
            if (pageContext.getAttribute("firstValuePassed") == null) {
                // included needed resources
                HashSet<Object> resources = pageCache.retrieveSetValues(NEEDED_RESOURCES);
                if (resources != null) {
                    MakumbaResourceServlet.writeResources(sb, (request).getContextPath(), resources);
                    pageContext.setAttribute("firstValuePassed", Boolean.TRUE);
                }
            }

            Integer counter = (Integer) pageContext.getAttribute("org.makumba.valueCounter");
            if (counter == null) {
                counter = 0;
                pageContext.setAttribute("org.makumba.valueCounter", counter);
            }

            // prepare the data needed in order to call the edit-in-place servlet

            String formattedValue = ((ValueComputer) pageCache.retrieve(GenericListTag.VALUE_COMPUTERS, tagKey)).getFormattedValue(
                this, pageCache);

            // this FieldDefinition is not a MDD one, it is computed through the ComputedQuery. so we can only use it
            // for type analysis
            FieldDefinition fd = ((ValueComputer) pageCache.retrieve(GenericListTag.VALUE_COMPUTERS, tagKey)).getType();

            String externalPointer = "";
            Pointer ptr = null;
            String fieldName = expr.substring(expr.lastIndexOf(".") + 1);

            String parentExpr = expr.indexOf(".") > 0 ? expr.substring(0, expr.lastIndexOf(".")) : null;

            if (parentExpr != null) {
                // TODO move me to setTagKey
                MultipleKey parentObjectKey = new MultipleKey(tagKey);
                parentObjectKey.remove(tagKey.size() - 1);
                parentObjectKey.add(parentExpr);
                ptr = (Pointer) ((ValueComputer) pageCache.retrieve(GenericListTag.VALUE_COMPUTERS, parentObjectKey)).getValue(pageContext);
                externalPointer = ptr.toExternalForm();
            }

            sb.append("");
            sb.append("<span id='mak_onview_" + counter + "' class='mak_tolink' onclick=\"eip.turnit('" + counter
                    + "');return false;\">\n");
            sb.append(formattedValue);
            sb.append("</span>");
            sb.append("<span id='mak_onedit_" + counter + "' class='mak_onedit'>\n");
            sb.append("       <form method='post' class='mak_edit' action='" + request.getContextPath()
                    + "/valueEditor?table=" + ptr.getType() + "&field=" + fieldName + "&pointer=" + externalPointer
                    + "'");
            sb.append("onsubmit=\"return eip.AIM.submit(this, {'onComplete' :");
            sb.append("eip.completeCallback}, '" + counter + "')\">");

            // FIXME use the record editor for the inner formatting
            switch (fd.getIntegerType()) {

                case FieldDefinition._char:

                    sb.append("               <input type='hidden' id='mak_edittype_" + counter + "' value='char' />\n");
                    sb.append("               <input type='text' name='value' id='mak_onedit_" + counter
                            + "_text' value='' />\n");
                    sb.append("               <input type='submit' value='X' />\n");
                    sb.append("       </form>\n");
                    sb.append("</span>\n");

                    break;

                case FieldDefinition._text:

                    sb.append("               <input type='hidden' id='mak_edittype_" + counter + "' value='text' />\n");
                    sb.append("               <textarea name='value' id='mak_onedit_" + counter
                            + "_text'></textarea>\n");
                    sb.append("               <input type='submit' value='X' />\n");
                    sb.append("       </form>\n");
                    sb.append("</span>\n");

                    break;

                default:
                    break;
                /*
                 * sb.append("<span id='mak_onedit_3' class='mak_onedit'>");
                 * sb.append("       <form method='post' class='mak_edit' action='valueEditor?asd'");
                 * sb.append("onsubmit=\"return eip.AIM.submit(this, {'onComplete' :");
                 * sb.append("eip.completeCallback}, '3')\">");
                 * sb.append("               <input type='hidden' id='mak_edittype_3' value='select'>");
                 * sb.append("               <select name='value' id='mak_onedit_3_text'>");
                 * sb.append("                       <option value='nr1'>Ajee1</option>");
                 * sb.append("                       <option value='nr2'>Ajee2</option>");
                 * sb.append("                       <option value='nr3'>Ajee3</option>");
                 * sb.append("               </select>"); sb.append("               <input type='submit' value='X' />");
                 * sb.append("       </form>"); sb.append("</span>");
                 */
            }

            pageContext.setAttribute("org.makumba.valueCounter", counter++);

            try {
                getPageContext().getOut().print(sb.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            ((ValueComputer) pageCache.retrieve(GenericListTag.VALUE_COMPUTERS, tagKey)).print(this, pageCache);
        }

        return EVAL_BODY_INCLUDE;
    }

    /**
     * Computes a string
     * 
     * @return A String holding the value in a form useful for debugging
     */
    public String toString() {
        return "VALUE expr=" + expr + " parameters: " + params;
    }

    @Override
    protected void doAnalyzedCleanup() {
        super.doAnalyzedCleanup();
        expr = printVar = var = null;
    }

}
