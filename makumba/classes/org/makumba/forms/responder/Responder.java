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

package org.makumba.forms.responder;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.makumba.CompositeValidationException;
import org.makumba.DataDefinition;
import org.makumba.LogicException;
import org.makumba.MakumbaError;
import org.makumba.Pointer;
import org.makumba.commons.attributes.RequestAttributes;
import org.makumba.controller.http.ControllerFilter;

/**
 * A responder is created for each form and stored internally, to respond when the form is submitted. To reduce memory
 * space, identical respodners are stored only once
 */
public abstract class Responder implements java.io.Serializable {
    /**
     * the name of the CGI parameter that passes the responder key, so the responder can be retrieved from the cache,
     * "__makumba__responder__"
     */
    public final static String responderName = "__makumba__responder__";

    /** the URI of the page, "__makumba__originatingPage__" */
    public final static String originatingPageName = "__makumba__originatingPage__";

    /**
     * prevents multiple submition of the same form (bug #190), computes as respoder+sessionID,
     * "__makumba__formSession__"
     */
    public final static String formSessionName = "__makumba__formSession__";

    /** the default label used to store the add and new result, "___mak___edited___" */
    static public final String anonymousResult = "___mak___edited___";

    /** the default response message, "changes done" */
    static public final String defaultMessage = "changes done";

    /** the default response message for search forms, "Search done!" */
    public static final String defaultMessageSearchForm = "Search done!";

    /** the name of the CGI parameter that passes the base pointer, see {@link #basePointerType}, "__makumba__base__" */
    public final static String basePointerName = "__makumba__base__";

    public static final String MATCH_CONTAINS = "contains";

    public static final String MATCH_EQUALS = "equals";

    public static final String MATCH_BEGINS = "begins";

    public static final String MATCH_ENDS = "ends";

    public static final String MATCH_BEFORE = "before";

    public static final String MATCH_AFTER = "after";

    public static final String MATCH_LESS = "lessThan";

    public static final String MATCH_GREATER = "greaterThan";

    public static final String SUFFIX_INPUT_MATCH = "Match";

    protected transient ResponderFactory factory;

    /** the responder key, as computed from the other fields */
    protected int identity;

    /** the controller object, on which the handler method that performs the operation is invoked */
    protected transient Object controller;

    /** store the name of the controller class */
    protected String controllerClassname;

    /** the database in which the operation takes place */
    protected String database;

    /** a response message to be shown in the response page */
    protected String message = defaultMessage;

    /** a response message to be shown when multiple submit occur */
    protected String multipleSubmitErrorMsg;

    /** Stores whether we shall do client-side validation, i.e. with javascript for a {@link FormResponder} */
    protected String clientSideValidation;

    /**
     * Stores whether we shall reload this form on a validation error or not. Used by {@link ControllerFilter} to decide
     * on the action.
     */
    private boolean reloadFormOnError;

    /**
     * Stores whether the form shall be annotated with the validation errors, or not. Used by {@link ControllerFilter}
     * to decide if the error messages shall be shown in the form response or not.
     */
    private boolean showFormAnnotated;

    /** new and add responders set their result to a result attribute */
    protected String resultAttribute = anonymousResult;

    /** the business logic handler, for all types of forms */
    protected String handler;

    /** the business logic after handler, for all types of forms */
    protected String afterHandler;

    /**
     * edit, add and delete makumba operations have a special pointer called the base pointer
     */
    protected String basePointerType;

    /** the type where the search operation is made */
    protected String searchType;

    /** the name of the form we operate on (only needed for search forms). */
    protected String formName;

    /** the type where the new operation is made */
    protected String newType;

    /** the field on which the add operation is made */
    protected String addField;

    /** the operation name: add, edit, delete, new, simple */
    protected String operation;

    /** the operation handler, computed from the operation */
    protected ResponderOperation op;
    
    /** order of the forms in the page **/
    protected List formOrder;

    public String getHandler() {
        return handler;
    }

    public String getAfterHandler() {
        return afterHandler;
    }

    public String getAddField() {
        return addField;
    }

    public String getBasePointerType() {
        return basePointerType;
    }

    public Object getController() {
        return controller;
    }

    public String getDatabase() {
        return database;
    }

    public String getNewType() {
        return newType;
    }

    public String getSearchType() {
        return searchType;
    }

    public String getFormName() {
        return formName;
    }
    

    public List getFormOrder() {
        return formOrder;
    }

    // --------------- form time, responder preparation -------------------
    /** pass the http request, so the responder computes its default controller and database */
    public void setHttpRequest(HttpServletRequest req) throws LogicException {
        controller = RequestAttributes.getAttributes(req).getRequestController();
        database = RequestAttributes.getAttributes(req).getRequestDatabase();
    }

    /** pass the operation **/
    public void setOperation(String operation, ResponderOperation op) {
        this.operation = operation;
        this.op = op;
    }

    /** pass the form response message */
    public void setMessage(String message) {
        this.message = message;
    }

    /** pass the multiple submit response message */
    public void setMultipleSubmitErrorMsg(String multipleSubmitErrorMsg) {
        this.multipleSubmitErrorMsg = multipleSubmitErrorMsg;
    }

    public void setReloadFormOnError(boolean reloadFormOnError) {
        this.reloadFormOnError = reloadFormOnError;
    }

    public boolean getReloadFormOnError() {
        return reloadFormOnError;
    }

    public void setShowFormAnnotated(boolean showFormAnnotated) {
        this.showFormAnnotated = showFormAnnotated;
    }

    public boolean getShowFormAnnotated() {
        return showFormAnnotated;
    }

    public void setClientSideValidation(String clientSideValidation) {
        this.clientSideValidation = clientSideValidation;
    }

    /** pass the response handler, if other than the default one */
    public void setHandler(String handler) {
        this.handler = handler;
    }

    /** pass the response afterHandler, if other than the default one */
    public void setAfterHandler(String afterHandler) {
        this.afterHandler = afterHandler;
    }

    /** pass the base pointer type, needed for the response */
    public void setBasePointerType(String basePointerType) {
        this.basePointerType = basePointerType;
    }

    /** pass the name of the result attribute */
    public void setResultAttribute(String resultAttribute) {
        this.resultAttribute = resultAttribute;
    }

    /** pass the field to which the add operation is made */
    public void setAddField(String s) {
        addField = s;
    }

    /** pass the type on which the new operation is made */
    public void setNewType(DataDefinition dd) {
        newType = dd.getName();
    }

    /** pass the type on which the new operation is made */
    public void setSearchType(DataDefinition dd) {
        searchType = dd.getName();
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }
    
    public void setFormOrder(List formOrder) {
        this.formOrder = formOrder;
    }


    abstract protected void postDeserializaton();

    /** a key that should identify this responder among all */
    public String responderKey() {
        return basePointerType + message + multipleSubmitErrorMsg + resultAttribute + database + operation
                + controller.getClass().getName() + handler + addField + newType + reloadFormOnError
                + showFormAnnotated + clientSideValidation;
    }

    /** get the integer key of this form, and register it if not already registered */
    public int getPrototype() {
        // at this point we should have all data set, so we should be able to verify the responder
        String s = op.verify(this);
        if (s != null)
            throw new MakumbaError("Bad responder configuration " + s);
        return factory.getResponderIdentity(this);
    }

    // ------------------ multiple form section -------------------
    /** the form counter, 0 for the root form, one increment for each subform of this form */
    // NOTE: transient data here is not used during response, only at form building time
    transient int groupCounter = 0;

    /** the form suffix, "" for the root form, _+increment for subforms */
    protected transient String storedSuffix = "";

    protected transient String storedParentSuffix = "";

    protected static final char suffixSeparator = '_';

    /** pass the parent responder */
    public void setParentResponder(Responder resp, Responder root) {
        storedSuffix = "" + suffixSeparator + (++root.groupCounter);
        storedParentSuffix = resp.storedSuffix;
    }

    public String getSuffix() {
        return storedSuffix;
    }

    // ----------------- response section ------------------
 
    /** formats an error message */
    public static String errorMessage(Throwable t) {
        return errorMessage(t.getMessage());
    }

    /** formats an error message */
    public static String errorMessage(String message) {
        return "<font color=red>" + message + "</font>";
    }

    /** reads the HTTP base pointer */
    public Pointer getHttpBasePointer(HttpServletRequest req, String suffix) {
        // for add forms, the result of the enclosing new form may be used
        return new Pointer(basePointerType, (String) RequestAttributes.getParameters(req).getParameter(
            basePointerName + suffix));
    }

    /**
     * Reads the data needed for the logic operation, from the http request. org.makumba.forms.html.FormResponder
     * provides an implementation
     * 
     * @param req
     *            the request corresponding to the current page
     * @param suffix
     *            the responder / form suffix
     * @return a Dicionary holding the data read by the logic
     */
    public abstract Dictionary getHttpData(HttpServletRequest req, String suffix);

    public abstract ArrayList getUnassignedExceptions(CompositeValidationException e, ArrayList unassignedExceptions,
            String suffix);

    public ResponderFactory getFactory() {
        return factory;
    }

    public void setFactory(ResponderFactory factory) {
        this.factory = factory;
    }
}