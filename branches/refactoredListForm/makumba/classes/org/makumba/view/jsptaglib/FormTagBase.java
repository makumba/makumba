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

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;

import org.makumba.DataDefinition;
import org.makumba.LogicException;
import org.makumba.MakumbaSystem;
import org.makumba.ProgrammerError;
import org.makumba.analyser.PageCache;
import org.makumba.commons.GenericMakumbaTag;
import org.makumba.controller.html.FormResponder;
import org.makumba.list.ListFormDataProvider;
import org.makumba.util.MultipleKey;
import org.makumba.util.StringUtils;

/**
 * mak:form base tag
 * 
 * @author Cristian Bogdan
 * @author Rudolf Mayery
 * @version $Id$
 * 
 */
public class FormTagBase extends GenericMakumbaTag implements BodyTag {

    private static final long serialVersionUID = 1L;

    // the tag attributes
    public String baseObject = null;

    String handler = null;

    String formMethod = null;

    public String formAction = null;

    String formName = null;

    String formMessage = null;

    FormResponder responder = null;

    long starttime;

    String basePointer = null;

    BodyContent bodyContent = null;

    String annotation;

    private static final String[] validAnnotationParams = new String[] { "none", "before", "after", "both" };

    private static final String[] validClientSideValidationParams = new String[] { "true", "false", "live" };

    String annotationSeparator;

    boolean reloadFormOnError = true;

    private String clientSideValidation = "live";
    
    protected ListFormDataProvider fdp = new ListFormDataProvider();


    public void setBodyContent(BodyContent bc) {
        bodyContent = bc;
    }

    public void doInitBody() {
    }

    // for add, edit, delete
    public void setObject(String s) {
        baseObject = s;
    }

    public void setAction(String s) {
        formAction = s;
    }

    public void setHandler(String s) {
        handler = s;
    }

    public void setMethod(String s) {
        checkNoParent("method");
        formMethod = s;
    }

    public void setName(String s) {
        formName = s;
        extraFormattingParams.put("name", s);
    }

    public void setMessage(String s) {
        checkNoParent("message");
        formMessage = s;
    }

    public void setMultipart() {
        FormTagBase parent = findParentForm();
        if (parent != null) // propagate multipart to the root form
            parent.setMultipart();
        else
            responder.setMultipart(true);
    }

    // additional html attributes:
    public void setTarget(String s) {
        checkNoParent("target");
        extraFormattingParams.put("target", s);
    }

    public void setOnReset(String s) {
        checkNoParent("onReset");
        extraFormattingParams.put("onReset", s);
    }

    public void setOnSubmit(String s) {
        checkNoParent("onSubmit");
        if (clientSideValidation != null) {
            throw new ProgrammerError(
                    "Forms specifying a 'clientSideValidation' attribute cannot provide an 'onSubmit' attribute");
        }
        extraFormattingParams.put("onSubmit", s);
    }

    public void setClientSideValidation(String clientSideValidation) {
        if (!StringUtils.equals(clientSideValidation, validClientSideValidationParams)) {
            throw new ProgrammerError("Invalid value for attribute 'clientSideValidation': <" + clientSideValidation
                    + ">. Allowed values are " + StringUtils.toString(validClientSideValidationParams));
        }
        if (extraFormattingParams.get("onSubmit") != null) {
            throw new ProgrammerError(
                    "Forms specifying a 'clientSideValidation' attribute cannot provide an 'onSubmit' attribute");
        }
        this.clientSideValidation = clientSideValidation;
    }

    public void setAnnotation(String s) {
        checkNoParent("annotation");
        if (!Arrays.asList(validAnnotationParams).contains(s)) {
            throw new ProgrammerError("Invalid value for attribute 'annotation': <" + annotation
                    + ">. Allowed values are " + StringUtils.toString(validAnnotationParams));
        }
        annotation = s;
    }

    public void setAnnotationSeparator(String s) {
        checkNoParent("annotationSeparator");
        annotationSeparator = s;
    }

    public void setReloadFormOnError(String s) {
        checkNoParent("reloadFormOnError");
        if (s != null && s.equals("false")) {
            reloadFormOnError = false;
        } else {
            reloadFormOnError = true;
        }
    }

    /**
     * Gets the name of the operation of the tag based on its classname
     * 
     * @return The name of the operation: Edit, Input, ...
     */
    String getOperation() {
        String classname = getClass().getName();

        if (classname.endsWith("FormTagBase"))
            return "simple";
        int n = classname.lastIndexOf("Tag");
        if (n != classname.length() - 3)
            throw new RuntimeException("the tag class name was expected to end with \'Tag\': " + classname);
        classname = classname.substring(0, n);
        int m = classname.lastIndexOf(".");
        return classname.substring(m + 1).toLowerCase();
    }

    public boolean shouldComputeBasePointer() {
        return baseObject != null;
    }

    /**
     * Sets tagKey to uniquely identify this tag. Called at analysis time before doStartAnalyze() and at runtime before
     * doMakumbaStartTag()
     * 
     * @param pageCache
     *            the page cache of the current page
     */
    public void setTagKey(PageCache pageCache) {
        Object[] keyComponents = { baseObject, handler, fdp.getParentListKey(this), getClass() };
        tagKey = new MultipleKey(keyComponents);
    }

    public static final String BASE_POINTER_TYPES = "org.makumba.basePointerTypes";

    /**
     * {@inheritDoc}
     * 
     * FIXME QueryExecutionProvider should tell us the syntax for the primary key name
     *
     */
    public void doStartAnalyze(PageCache pageCache) {
        if (!shouldComputeBasePointer()) {
            return;
        }
        
        fdp.onFormStartAnalyze(this, pageCache, baseObject);
    }

    /**
     * Finds the parent form
     * 
     * @return The parent form class of this tag
     */
    public FormTagBase findParentForm() {
        return (FormTagBase) findAncestorWithClass(this, FormTagBase.class);
    }

    /**
     * Finds the root form
     * 
     * @return The root form, in case of multiple nested forms
     */
    FormTagBase findRootForm() {
        FormTagBase parent = findParentForm();
        if (parent == null)
            return this;
        return parent.findRootForm();
    }

    /**
     * Generates an error message if this form has no parent form (and hence can't have the given attribute)
     * 
     * @param attrName
     *            the name of the attribute that shouldn't be used
     * @throws ProgrammerError
     */
    void checkNoParent(String attrName) {
        if (findParentForm() != null)
            throw new ProgrammerError("Forms included in other forms cannot have a '" + attrName + "' attribute");
    }

    /**
     * {@inheritDoc}
     */
    public void doEndAnalyze(PageCache pageCache) {
        fdp.onFormEndAnalyze(getTagKey(), pageCache);
        
        if (formAction == null && findParentForm() == null)
            throw new ProgrammerError(
                    "Forms must have either action= defined, or an enclosed <mak:action>...</mak:action>");
        if (findParentForm() != null) {
            if (formAction != null)
                throw new ProgrammerError(
                        "Forms included in other forms cannot have action= defined, or an enclosed <mak:action>...</mak:action>");
        }
        
        if (!shouldComputeBasePointer())
            return;
        
        pageCache.cache(BASE_POINTER_TYPES, tagKey, fdp.getTypeOnEndAnalyze(getTagKey(), pageCache).getPointedType().getName());
    }

    /**
     * {@inheritDoc}
     */
    public void initialiseState() {
        super.initialiseState();

        responder = new FormResponder();
        if (formName != null)
            responder.setResultAttribute(formName);
        if (handler != null)
            responder.setHandler(handler);
        if (formAction != null)
            responder.setAction(formAction);
        if (formMethod != null)
            responder.setMethod(formMethod);
        if (formMessage != null)
            responder.setMessage(formMessage);

        responder.setReloadFormOnError(reloadFormOnError);
        responder.setShowFormAnnotated(StringUtils.equals(annotation, new String[] { "before", "after", "both" }));
        responder.setClientSideValidation(clientSideValidation);

        if (findParentForm() != null)
            responder.setParentResponder(findParentForm().responder, findRootForm().responder);
    }

    /**
     * Sets the responder elements, computes the base pointer if needed
     * 
     * @param pageCache
     *            the page cache of the current page
     * @throws JspException
     * @throws LogicException
     */
    public int doAnalyzedStartTag(PageCache pageCache) throws JspException, LogicException {
        
        fdp.onFormStartTag(getTagKey(), pageCache, pageContext);
        
        responder.setOperation(getOperation());
        responder.setExtraFormatting(extraFormatting);
        responder.setBasePointerType((String) pageCache.retrieve(BASE_POINTER_TYPES, tagKey));

        starttime = new java.util.Date().getTime();

        /** we compute the base pointer */
        if (shouldComputeBasePointer()) {
            basePointer = fdp.computeBasePointer(getTagKey(), pageContext);
        }
        try {
            responder.setHttpRequest((HttpServletRequest) pageContext.getRequest());
        } catch (LogicException e) {
            treatException(e);
        }

        return EVAL_BODY_BUFFERED;
    }

    /**
     * Lets the responder write the pre- and postabmble for the form, and writes the bodyContent inside. Resets all the
     * variables.
     * 
     * @param pageCache
     *            the page cache of the current page
     * @throws JspException
     */
    public int doAnalyzedEndTag(PageCache pageCache) throws JspException {
                
        fdp.onFormEndTag(getTagKey(), pageCache, pageContext);
        
        try {
            StringBuffer sb = new StringBuffer();
            responder.writeFormPreamble(sb, basePointer);
            bodyContent.getEnclosingWriter().print(sb.toString());

            // for a deleteForm, we want to trim the text on the button unless specified otherwise
            // not sure if this implementation is the best possible solution
            if (this instanceof DeleteTag && !((DeleteTag) this).getPreserveWhiteSpace()) {
                bodyContent.getEnclosingWriter().print(bodyContent.getString().trim());
            } else {
                bodyContent.writeOut(bodyContent.getEnclosingWriter());
            }

            if (StringUtils.equals(clientSideValidation, new String[] { "true", "live" })) {
                sb = new StringBuffer();
                responder.writeClientsideValidation(sb);
                bodyContent.getEnclosingWriter().print(sb.toString());
            }

            sb = new StringBuffer();
            responder.writeFormPostamble(sb, basePointer, (HttpServletRequest) pageContext.getRequest());

            bodyContent.getEnclosingWriter().print(sb.toString());
            if (findParentForm() != null)
                MakumbaSystem.getMakumbaLogger("taglib.performance").fine(
                    "form time: " + ((new java.util.Date().getTime() - starttime)));
        } catch (IOException e) {
            throw new JspException(e.toString());
        } finally {
            baseObject = handler = formMethod = formAction = formName = formMessage = basePointer = null;
            responder = null;
            bodyContent = null;
        }
        return EVAL_PAGE;
    }

    /** The default expression for an input tag, if none is indicated */
    public String getDefaultExpr(String fieldName) {
        return null;
    }

    /** The basic data type inside the form. null for generic forms */
    public DataDefinition getDataTypeAtAnalysis(PageCache pageCache) {
        return null;
    }

}
