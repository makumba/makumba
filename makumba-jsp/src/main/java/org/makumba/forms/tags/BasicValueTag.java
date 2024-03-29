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
//  $Id: BasicValueTag.java 1529 2007-09-13 23:33:10Z rosso_nero $
//  $Name$
/////////////////////////////////////

package org.makumba.forms.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.makumba.FieldDefinition;
import org.makumba.LogicException;
import org.makumba.ProgrammerError;
import org.makumba.analyser.AnalysableTag;
import org.makumba.analyser.MakumbaJspAnalyzer;
import org.makumba.analyser.PageCache;
import org.makumba.commons.attributes.PageAttributes;
import org.makumba.commons.tags.FormDataProvider;
import org.makumba.commons.tags.GenericMakumbaTag;
import org.makumba.providers.DataDefinitionProvider;

/**
 * This is a a base class for InputTag and OptionTag but may be used for other tags that need to compute a value in
 * similar manner (value="$attribute" or value="OQL expr").
 * 
 * @author Cristian Bogdan
 * @author Manuel Bernhardt <manuel@makumba.org>
 * @version $Id: BasicValueTag.java 1529 2007-09-13 23:33:10Z rosso_nero $
 */
public abstract class BasicValueTag extends GenericMakumbaTag {
    private static final long serialVersionUID = 1L;

    // TODO we should be able to specify the DataDefinitionProvider used at the form level or so
    protected DataDefinitionProvider ddp = DataDefinitionProvider.getInstance();

    String valueExprOriginal = null;

    /* Cannot be set here, subclasses who need it will set it */
    String dataType = null;

    public String expr = null;

    protected FormDataProvider fdp;

    public BasicValueTag() {
        // TODO move this somewhere else
        try {
            this.fdp = (FormDataProvider) Class.forName("org.makumba.list.tags.ListFormDataProvider").newInstance();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setValue(String value) {
        this.valueExprOriginal = value.trim();
    }

    public FormTagBase getForm() {
        return (FormTagBase) TagSupport.findAncestorWithClass(this, FormTagBase.class);
    }

    /**
     * Indicates if the expression is null
     * 
     * @return <code>true</code> if the expression is null, <code>false</code> otherwise
     */
    public boolean isNull() {
        return expr == null || expr.trim().length() == 0 || expr.trim().equals("nil");
    }

    boolean isValue() {
        return isValue(expr);
    }

    /**
     * Indicates if the given expression is a value
     * 
     * @return <code>true</code> if the expression doesn't start with '$' and is not null, <code>false</code> otherwise
     */
    protected boolean isValue(String exprexpression) {
        return exprexpression != null && !exprexpression.startsWith("$") && !isNull();
    }

    /**
     * Indicates if the given expression is an attribute
     * 
     * @return <code>true</code> if the expression starts with '$', <code>false</code> otherwise
     */
    protected boolean isAttribute() {
        return isAttribute(expr);
    }

    /**
     * Determines the ValueComputer and associates it with the tagKey
     * 
     * @param pageCache
     *            the page cache of the current page
     */
    @Override
    public void doStartAnalyze(PageCache pageCache) {
        if (isValue()) {
            fdp.onBasicValueStartAnalyze(this, isNull(), getForm().getTagKey(), pageCache, expr);
        }
    }

    public String checkPtrExpr(String expr2, PageCache pageCache) {
        return expr2;
    }

    abstract FieldDefinition getTypeFromContext(PageCache pageCache);

    /**
     * Tells the ValueComputer to finish analysis, and sets the types for var and printVar.
     * 
     * @param pageCache
     *            the page cache of the current page
     */
    @Override
    public void doEndAnalyze(PageCache pageCache) {
        FieldDefinition contextType = getTypeFromContext(pageCache);

        FieldDefinition dataTypeInfo = null;
        FieldDefinition type = null;

        if (dataType != null) {
            dataTypeInfo = ddp.makeFieldDefinition("dummyName", dataType);
            if (contextType != null && !contextType.isAssignableFrom(dataTypeInfo)) {
                throw new ProgrammerError("declared data type '" + dataType
                        + "' not compatible with the type computed from context '" + contextType + "'");
            }
        }

        if (isValue()) {
            type = fdp.onBasicValueEndAnalyze(getTagKey(), pageCache);
        }
        if (isAttribute()) {
            Object[] obj = (Object[]) pageCache.retrieve(AnalysableTag.TYPES, expr.substring(1));
            if (obj != null) {
                type = (FieldDefinition) obj[0];
            }
        }
        String fieldName = "";
        if (this instanceof InputTag) {
            fieldName = "Field <" + ((InputTag) this).name + ">: ";
        }

        if (type != null && dataTypeInfo != null && !dataTypeInfo.isAssignableFrom(type)) {
            throw new ProgrammerError(
                    fieldName
                            + "computed type for INPUT is different from the indicated dataType. The dataType is indicated to '"
                            + dataType + "' type computed is '" + type + "'");
        }

        if (type != null && contextType != null && !contextType.isAssignableFrom(type)) {
            String contextTypeStr = contextType.getType();
            String typeStr = type.getType();

            // if we deal with pointers, also indicate what they point to
            if (contextTypeStr.equals("ptr") && typeStr.equals("ptr")) {
                contextTypeStr += " " + contextType.getSubtable().getName();
                typeStr += " " + type.getSubtable().getName();
            }

            throw new ProgrammerError(
                    fieldName
                            + "The computed type is different from the type resulting from the analysis of the form. The context type was determined to '"
                            + contextTypeStr + "', type computed is '" + typeStr + "'");

        }

        if (type == null && contextType == null && dataTypeInfo == null) {
            throw new ProgrammerError(fieldName
                    + "cannot determine input type. Please specify the type using dataType=...");
        }

        // we give priority to the type as computed from the form
        if (contextType == null) {
            contextType = dataTypeInfo != null ? dataTypeInfo : type;
        }

        pageCache.cache(MakumbaJspAnalyzer.INPUT_TYPES, tagKey, contextType);
    }

    @Override
    public int doAnalyzedEndTag(PageCache pageCache) throws JspException, LogicException {
        params.put("org.makumba.forms.queryLanguage", MakumbaJspAnalyzer.getQueryLanguage(pageCache));
        FieldDefinition type = (FieldDefinition) pageCache.retrieve(MakumbaJspAnalyzer.INPUT_TYPES, tagKey);
        Object val = null;

        if (isValue()) {
            val = fdp.getValue(getTagKey(), getPageContext(), pageCache);
        }

        if (isAttribute()) {
            val = PageAttributes.getAttributes(pageContext).getAttribute(expr.substring(1));
        }

        if (val != null) {
            val = type.checkValue(val);
        }

        return computedValue(val, type);
    }

    /**
     * A value was computed, do what's needed with it, cleanup and return the result of doMakumbaEndTag()
     * 
     * @param o
     *            the value
     * @param type
     *            the type of the data
     */
    abstract int computedValue(Object o, FieldDefinition type) throws JspException, LogicException;

    @Override
    protected void doAnalyzedCleanup() {
        super.doAnalyzedCleanup();
        expr = valueExprOriginal = dataType = null;
    }
}
