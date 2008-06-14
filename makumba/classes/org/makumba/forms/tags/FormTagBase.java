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

package org.makumba.forms.tags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;

import org.makumba.DataDefinition;
import org.makumba.LogicException;
import org.makumba.MakumbaSystem;
import org.makumba.ProgrammerError;
import org.makumba.analyser.PageCache;
import org.makumba.commons.MakumbaJspAnalyzer;
import org.makumba.commons.MakumbaResourceServlet;
import org.makumba.commons.MultipleKey;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.commons.StringUtils;
import org.makumba.commons.attributes.RequestAttributes;
import org.makumba.commons.tags.GenericMakumbaTag;
import org.makumba.controller.Logic;
import org.makumba.forms.responder.FormResponder;
import org.makumba.forms.responder.Responder;
import org.makumba.forms.responder.ResponderFactory;
import org.makumba.forms.responder.ResponderOperation;
import org.makumba.providers.Configuration;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.FormDataProvider;

/**
 * mak:form base tag
 * 
 * @author Cristian Bogdan
 * @author Rudolf Mayery
 * @version $Id$
 */
public class FormTagBase extends GenericMakumbaTag implements BodyTag {

    private static final long serialVersionUID = 1L;

    // the tag attributes
    public String baseObject = null;

    String handler = null;

    String afterHandler = null;

    String formMethod = null;

    public String formAction = null;

    String formName = null;

    String formMessage = null;

    FormResponder responder = null;

    long starttime;

    String basePointer = null;

    BodyContent bodyContent = null;

    String annotation = "after";

    private static final String[] validAnnotationParams = { "none", "before", "after", "both" };

    private static final String[] validClientSideValidationParams = { "true", "false", "live" };

    String annotationSeparator;

    boolean reloadFormOnError = Configuration.getReloadFormOnErrorDefault();

    private String clientSideValidation = Configuration.getClientSideValidationDefault();

    protected FormDataProvider fdp;

    private ResponderFactory responderFactory = ResponderFactory.getInstance();

    // TODO we should be able to specify the DataDefinitionProvider used at the form level or so
    protected DataDefinitionProvider ddp = DataDefinitionProvider.getInstance();

    private Map<MultipleKey, String> responders; // all the form responders in a nested form; only in the root form

    private ArrayList<String> formNames; // the names of all forms in this root form; only in the root form

    public FormTagBase() {
        // TODO move this somewhere else
        try {
            this.fdp = (FormDataProvider) Class.forName("org.makumba.list.ListFormDataProvider").newInstance();
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

    public void setAfterHandler(String s) {
        afterHandler = s;
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
        if (parent != null) {// propagate multipart to the root form
            parent.setMultipart();
        } else {
            responder.setMultipart(true);
        }
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
        checkValidAttributeValues("clientSideValidation", clientSideValidation, validClientSideValidationParams);
        if (extraFormattingParams.get("onSubmit") != null) {
            throw new ProgrammerError(
                    "Forms specifying a 'clientSideValidation' attribute cannot provide an 'onSubmit' attribute");
        }
        this.clientSideValidation = clientSideValidation;
    }

    public void setAnnotation(String s) {
        checkNoParent("annotation");
        checkValidAttributeValues("annotation", s, validAnnotationParams);
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

        if (classname.endsWith("FormTagBase")) {
            return "simple";
        }
        int n = classname.lastIndexOf("Tag");
        if (n != classname.length() - 3) {
            throw new RuntimeException("the tag class name was expected to end with \'Tag\': " + classname);
        }
        classname = classname.substring(0, n);
        int m = classname.lastIndexOf(".");
        return classname.substring(m + 1).toLowerCase();
    }

    /**
     * Indicates whether the base pointer should be computed or not
     * 
     * @return <code>false</code> if we are at runtime (i.e. the baseObject has been set by JSP), <code>true</code>
     *         if we are at analysis time
     */
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
    @Override
    public void setTagKey(PageCache pageCache) {
        Object[] keyComponents = { baseObject, handler, afterHandler, fdp.getParentListKey(this), getClass() };
        tagKey = new MultipleKey(keyComponents);
    }

    public static final String BASE_POINTER_TYPES = "org.makumba.basePointerTypes";

    /**
     * {@inheritDoc} FIXME QueryExecutionProvider should tell us the syntax for the primary key name
     */
    @Override
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
        if (parent == null) {
            return this;
        }
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
        if (findParentForm() != null) {
            throw new ProgrammerError("Forms included in other forms cannot have a '" + attrName + "' attribute");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doEndAnalyze(PageCache pageCache) {
        fdp.onFormEndAnalyze(getTagKey(), pageCache);

        // form action is not needed for search tags
        if (formAction == null && findParentForm() == null && !getOperation().equals("search")) {
            throw new ProgrammerError(
                    "Forms must have either action= defined, or an enclosed <mak:action>...</mak:action>");
        }
        if (findParentForm() != null) {
            if (formAction != null) {
                throw new ProgrammerError(
                        "Forms included in other forms cannot have action= defined, or an enclosed <mak:action>...</mak:action>");
            }
        }
        // add needed resources, stored in cache for this page
        if (StringUtils.equalsAny(clientSideValidation, new String[] { "true", "live" })) {
            pageCache.cacheSetValues(NEEDED_RESOURCES,
                MakumbaSystem.getClientsideValidationProvider().getNeededJavaScriptFileNames());
        }

        if (!shouldComputeBasePointer()) {
            return;
        }

        pageCache.cache(BASE_POINTER_TYPES, tagKey,
            fdp.getTypeOnEndAnalyze(getTagKey(), pageCache).getPointedType().getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialiseState() {
        super.initialiseState();

        responder = responderFactory.createResponder();
        if (formName != null) {
            responder.setResultAttribute(formName);
        }
        if (handler != null) {
            responder.setHandler(handler);
        }
        if (afterHandler != null) {
            responder.setAfterHandler(afterHandler);
        }
        if (formAction != null) {
            responder.setAction(formAction);
        }
        if (formMethod != null) {
            responder.setMethod(formMethod);
        }
        if (formMessage != null) {
            responder.setMessage(formMessage);
        }

        responder.setReloadFormOnError(reloadFormOnError);
        responder.setShowFormAnnotated(StringUtils.equalsAny(annotation, new String[] { "before", "after", "both" }));
        responder.setClientSideValidation(clientSideValidation);

        if (findParentForm() != null) {
            responder.setParentResponder(findParentForm().responder, findRootForm().responder);
        }

        if (findParentForm() == null) { // initialise only for the root form!
            responders = new HashMap<MultipleKey, String>();
            formNames = new ArrayList<String>();
        }
    }

    /**
     * Sets the responder elements, computes the base pointer if needed
     * 
     * @param pageCache
     *            the page cache of the current page
     * @throws JspException
     * @throws LogicException
     */
    @Override
    public int doAnalyzedStartTag(PageCache pageCache) throws JspException, LogicException {

        fdp.onFormStartTag(getTagKey(), pageCache, pageContext);

        responder.setOperation(getOperation(), getResponderOperation(getOperation()));
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
            throw new RuntimeWrappedException(e);
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
    @Override
    public int doAnalyzedEndTag(PageCache pageCache) throws JspException {

        fdp.onFormEndTag(getTagKey(), pageCache, pageContext);

        try {
            StringBuffer sb = new StringBuffer();

            HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
            // if we are at the first form
            if (findParentForm() == null && pageContext.getAttribute("firstFormPassed") == null) {
                // included needed resources
                HashSet<Object> resources = pageCache.retrieveSetValues(NEEDED_RESOURCES);
                if (resources != null) {
                    for (Object object : resources) {
                        String rsc = (request).getContextPath() + "/" + MakumbaResourceServlet.resourceDirectory + "/"
                                + MakumbaResourceServlet.RESOURCE_PATH_JAVASCRIPT + object;
                        sb.append("<script type=\"text/javascript\" src=\"" + rsc + "\">" + "</script>\n");
                    }
                    pageContext.setAttribute("firstFormPassed", Boolean.TRUE);
                }
            }

            responder.writeFormPreamble(sb, basePointer);
            bodyContent.getEnclosingWriter().print(sb.toString());

            // for a deleteForm, we want to trim the text on the button unless specified otherwise
            // not sure if this implementation is the best possible solution
            if (this instanceof DeleteTag && !((DeleteTag) this).getPreserveWhiteSpace()) {
                bodyContent.getEnclosingWriter().print(bodyContent.getString().trim());
            } else {
                bodyContent.writeOut(bodyContent.getEnclosingWriter());
            }

            // write client side validation, but only for edit operations (not search) & not delete links
            if (!getOperation().equals("search") && !(this instanceof DeleteTag)
                    && StringUtils.equalsAny(clientSideValidation, new String[] { "true", "live" })) {
                sb = new StringBuffer();
                responder.writeClientsideValidation(sb);
                bodyContent.getEnclosingWriter().print(sb.toString());
            }

            sb = new StringBuffer();
            responder.writeFormPostamble(sb, basePointer, (HttpServletRequest) pageContext.getRequest());

            bodyContent.getEnclosingWriter().print(sb.toString());
            if (findParentForm() != null) {
                java.util.logging.Logger.getLogger("org.makumba." + "taglib.performance").fine(
                    "form time: " + ((new java.util.Date().getTime() - starttime)));
            }

            // retrieves the form dependency graph from the cache
            // this needs to be the last thing done, so we can retrieve the responder code safely
            MultipleKey[] sortedForms = (MultipleKey[]) pageCache.retrieve(MakumbaJspAnalyzer.DEPENDENCY_CACHE,
                MakumbaJspAnalyzer.DEPENDENCY_CACHE);

            // form order - add the responders & form names
            FormTagBase rootForm = findRootForm();
            if (rootForm.responders == null) {
                findRootForm();
            }
            rootForm.responders.put(this.getTagKey(), responder.getResponderValue());
            rootForm.formNames.add(formName);

            if (findParentForm() == null) { // we are in the end of the root form - all child forms have a responder by
                // now
                // so now we can set the responder order in the responder
                ArrayList<String> responderOrder = new ArrayList<String>();
                int j = 0;
                for (MultipleKey element : sortedForms) {
                    if (responders.get(element) != null) {
                        responderOrder.add(responders.get(element));
                    }
                }
                responder.setResponderOrder(responderOrder);
                responder.setFormNames(formNames);
                // we need to save the responder again to the disc, cause the new fields were not persisted yet
                // FIXME: this might be sub-optimal, but i guess it can only be fixed when the form/responder order
                // detection is done at analysis time, before the responder gets saved to the disc in
                // org.makumba.forms.responder.ResponderCacheManager.NamedResources.makeResource
                responder.saveResponderToDisc();
            }

        } catch (IOException e) {
            throw new JspException(e.toString());
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

    /**
     * Gives the operation associated with this form tag. Each tag should implement its own
     * 
     * @param operation
     *            name of the operation
     * @return a {@link ResponderOperation} object holding the operation information
     */
    public ResponderOperation getResponderOperation(String operation) {
        if (operation.equals("simple")) {
            return simepleOp;
        }
        throw new RuntimeException("Houston, problem");
    }

    private final static ResponderOperation simepleOp = new ResponderOperation() {

        private static final long serialVersionUID = 1L;

        @Override
        public Object respondTo(HttpServletRequest req, Responder resp, String suffix, String parentSuffix)
                throws LogicException {
            return Logic.doOp(resp.getController(), resp.getHandler(), resp.getHttpData(req, suffix),
                new RequestAttributes(resp.getController(), req, resp.getDatabase()), resp.getDatabase(),
                getConnectionProvider(req, resp.getController()));
        }

        @Override
        public String verify(Responder resp) {
            return null;
        }
    };

    @Override
    protected void doAnalyzedCleanup() {
        super.doAnalyzedCleanup();
        afterHandler = annotation = annotationSeparator = baseObject = basePointer = formAction = formMethod = formMessage = formName = handler = null;
        responder = null;
        bodyContent = null;
    }

}
