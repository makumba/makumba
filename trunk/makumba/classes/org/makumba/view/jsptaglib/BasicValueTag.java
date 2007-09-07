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

package org.makumba.view.jsptaglib;

import java.sql.Timestamp;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.makumba.FieldDefinition;
import org.makumba.LogicException;
import org.makumba.MakumbaSystem;
import org.makumba.ProgrammerError;
import org.makumba.controller.http.ControllerFilter;
import org.makumba.controller.jsp.PageAttributes;
import org.makumba.util.MultipleKey;
import org.makumba.util.StringUtils;
import org.makumba.view.jsptaglib.MakumbaJspAnalyzer.PageCache;

/**
 * This is a a base class for InputTag and OptionTag but may be used for other tags that need to compute a value in
 * similar manner (value="$attribute" or value="OQL expr").
 * 
 * @author Cristian Bogdan
 * @version $Id$
 */
public abstract class BasicValueTag extends MakumbaTag {
    
    String valueExprOriginal = null;

    /* Cannot be set here, subclasses who need it will set it */
    String dataType = null;

    String expr = null;

    public void setValue(String value) {
        this.valueExprOriginal = value.trim();
    }

    FormTagBase getForm() {
        return (FormTagBase) TagSupport.findAncestorWithClass(this, FormTagBase.class);
    }

    /**
     * Indicates if the expression is null
     * @return <code>true</code> if the expression is null, <code>false</code> otherwise
     */
    boolean isNull() {
        return expr == null || expr.trim().length() == 0 || expr.trim().equals("nil");
    }

    /**
     * Indicates if the expression is a value
     * @return <code>true</code> if the expression doesn't start with '$' and is not null, <code>false</code> otherwise
     */
    boolean isValue() {
        return expr != null && !expr.startsWith("$") && !isNull();
    }

    /**
     * Indicates if the expression is an attribute
     * @return <code>true</code> if the expression starts with '$', <code>false</code> otherwise
     */
    boolean isAttribute() {
        return expr != null && expr.startsWith("$");
    }

    /**
     * Gets the key of the parentList
     * @param pageCache the page cache of the current page
     * @return The MultipleKey that identifies the parent list, <code>null</code> if there's no parent list
     */
    public MultipleKey getParentListKey(MakumbaJspAnalyzer.PageCache pageCache) {
        MultipleKey k = super.getParentListKey(pageCache);
        if (k != null)
            return k;
        if (isNull())
            return null;

        /* we don't have a query around us, so we must make a dummy query for computing the value via the database */
        getForm().cacheDummyQueryAtAnalysis(pageCache);
        return getForm().tagKey;
    }

    /** 
     * Determines the ValueComputer and associates it with the tagKey
     * @param pageCache the page cache of the current page
     */
    public void doStartAnalyze(MakumbaJspAnalyzer.PageCache pageCache) {
        if (isValue()) {
            pageCache.valueComputers.put(tagKey, ValueComputer.getValueComputerAtAnalysis(this, checkPtrExpr(expr,
                    pageCache), pageCache));
        }
    }

    protected String checkPtrExpr(String expr2, PageCache pageCache) {
        return expr2;
    }

    abstract FieldDefinition getTypeFromContext(MakumbaJspAnalyzer.PageCache pageCache);

    /** 
     * Tells the ValueComputer to finish analysis, and sets the types for var and printVar.
     * @param pageCache the page cache of the current page
     */
    public void doEndAnalyze(MakumbaJspAnalyzer.PageCache pageCache) {
        FieldDefinition contextType = getTypeFromContext(pageCache);

        FieldDefinition dataTypeInfo = null;
        FieldDefinition type = null;

        if (dataType != null) {
            dataTypeInfo = MakumbaSystem.makeFieldDefinition("dummyName", dataType);
            if (contextType != null && !contextType.isAssignableFrom(dataTypeInfo))
                throw new ProgrammerError("declared data type '" + dataType
                        + "' not compatible with the type computed from context '" + contextType + "'");
        }

        if (isValue()) {
            ValueComputer vc = (ValueComputer) pageCache.valueComputers.get(tagKey);
            vc.doEndAnalyze(this, pageCache);
            type = vc.type;
        }
        if (isAttribute())
            type = (FieldDefinition) pageCache.types.get(expr.substring(1));

        if (type != null && dataTypeInfo != null && !dataTypeInfo.isAssignableFrom(type))
            throw new ProgrammerError(
                    "computed type for INPUT is different from the indicated dataType. The dataType is indicated to '"
                            + dataType + "' type computed is '" + type + "'");

        if (type != null && contextType != null && !contextType.isAssignableFrom(type))
            throw new ProgrammerError(
                    "computed type is different from the type resulting from form analysis. The context type was determined to '"
                            + contextType + "', type computed is '" + type + "'");

        if (type == null && contextType == null && dataTypeInfo == null)
            throw new ProgrammerError("cannot determine input type. Please specify the type using dataType=...");

        // we give priority to the type as computed from the form
        if (contextType == null)
            contextType = dataTypeInfo != null ? dataTypeInfo : type;

        pageCache.inputTypes.put(tagKey, contextType);
    }

    public int doMakumbaEndTag(MakumbaJspAnalyzer.PageCache pageCache) throws JspException, LogicException {
        FieldDefinition type = (FieldDefinition) pageCache.inputTypes.get(tagKey);
        Object val = null;
        
        // if we are reloading the form page on validation errors, fill form inputs as in the request
        if (this instanceof InputTag
                && StringUtils.equals(pageContext.getRequest().getAttribute(ControllerFilter.MAKUMBA_FORM_RELOAD), "true")) {
            String tagName = ((InputTag) this).name + getForm().responder.getSuffix();
            if (type.getIntegerType() == FieldDefinition._date) { 
                // we need a special treatment for date fields, as they do not come in a single input, but several ones
                // there might be a way to reuse code from DateEditor, but not sure if/how
                
                // the order of the values is {day, month, year, hour, minute, second}
                // year is default to 1900, cause we substract 1900 afterwards
                int[] values = new int[] { 1, 0, 1900, 0, 0, 0 };
                for (int i = 0; i < values.length; i++) {
                    try {
                        values[i] = Integer.parseInt(pageContext.getRequest().getParameter(tagName + "_" + i));
                    } catch (NumberFormatException e) {
                    }
                }
                // we substract 1900 from the year value, as this is how the Timestamp constructor works
                val = new Timestamp(values[2]-1900,values[1],values[0],values[3],values[4],values[5],0);
            } else { // other types can be handled normally
                val = pageContext.getRequest().getParameter(tagName);
            }
            return computedValue(val, type);
        }

        if (isValue())
            val = ((ValueComputer) getPageCache(pageContext).valueComputers.get(tagKey)).getValue(this);

        if (isAttribute()) {
            val = PageAttributes.getAttributes(pageContext).getAttribute(expr.substring(1));
        }

        if (val != null)
            val = type.checkValue(val);

        return computedValue(val, type);
    }

    /** 
     * A value was computed, do what's needed with it, cleanup and return the result of doMakumbaEndTag()
     * @param o the value
     * @param type the type of the data
     */
    abstract int computedValue(Object o, FieldDefinition type) throws JspException, LogicException;
}
