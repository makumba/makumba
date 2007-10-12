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
import java.util.Comparator;
import java.util.Dictionary;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.makumba.AttributeNotFoundException;
import org.makumba.CompositeValidationException;
import org.makumba.DataDefinition;
import org.makumba.LogicException;
import org.makumba.MakumbaError;
import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.commons.Configuration;
import org.makumba.commons.attributes.RequestAttributes;
import org.makumba.controller.http.ControllerFilter;
import org.makumba.providers.TransactionProvider;

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

    // --------------- form time, responder preparation -------------------
    /** pass the http request, so the responder computes its default controller and database */
    public void setHttpRequest(HttpServletRequest req) throws LogicException {
        controller = RequestAttributes.getAttributes(req).getRequestController();
        database = RequestAttributes.getAttributes(req).getRequestDatabase();
    }

    /** pass the operation */
    public void setOperation(String operation) {
        this.operation = operation;
        op = ResponderOperationLocator.locate(operation);
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
        return ((Responder) ResponderCacheManager.cache.getResource(this)).identity;
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

    static Integer ZERO = new Integer(0);

    /**
     * Given a responder code, extracts the suffix
     * 
     * @param code
     *            the responder code
     * @return the responder suffix, ZERO if none found FIXME maybe this goes just 2 levels, so forms in forms in forms
     *         aren't working?
     */
    static Integer suffix(String code) {
        int n = code.indexOf(suffixSeparator);
        if (n == -1)
            return ZERO;
        code = code.substring(n + 1);
        n = code.indexOf(suffixSeparator);
        if (n != -1)
            code = code.substring(0, n);
        return new Integer(Integer.parseInt(code));
    }

    /**
     * Given a responder code, extracts suffix and parentSuffix
     * 
     * @param responderCode
     *            the responder code
     * @return a String[] containing the suffix as first element and the parentSuffix as second element FIXME maybe this
     *         goes just 2 levels, so forms in forms in forms aren't working?
     */
    public static String[] getSuffixes(String responderCode) {
        String suffix = "";
        String parentSuffix = null;
        int n = responderCode.indexOf(suffixSeparator);
        if (n != -1) {
            suffix = responderCode.substring(n);
            parentSuffix = "";
            n = suffix.indexOf(suffixSeparator, 1);
            if (n != -1) {
                parentSuffix = suffix.substring(n);
                suffix = suffix.substring(0, n);
            }
        }
        return new String[] { suffix, parentSuffix };
    }

    /**
     * Reads all responder codes from a request (all code_suffix values of __mak__responder__) and orders them by
     * suffix.
     * 
     * @param req
     *            the request in which we currently are
     * @return the enumeration of responder codes
     */
    static Iterator<Object> getResponderCodes(HttpServletRequest req) {
        TreeSet<Object> set = new TreeSet<Object>(bySuffix);

        Object o = RequestAttributes.getParameters(req).getParameter(responderName);
        if (o != null) {
            if (o instanceof String)
                set.add(o);
            else
                set.addAll((Vector) o);
        }
        return set.iterator();
    }

    /**
     * Simple comparator to be able to sort by suffix
     */
    static Comparator<Object> bySuffix = new Comparator<Object>() {
        public int compare(Object o1, Object o2) {
            return suffix((String) o1).compareTo(suffix((String) o2));
        }

        public boolean equals(Object o) {
            return false;
        }
    };

    // ----------------- response section ------------------
    static public final String RESPONSE_STRING_NAME = "makumba.response";

    public static final String resultNamePrefix = "org.makumba.controller.resultOf_";

    /** respond to a http request */
    public static Exception response(HttpServletRequest req, HttpServletResponse resp) {

        ResponderCacheManager.setResponderWorkingDir(req);

        if (req.getAttribute(RESPONSE_STRING_NAME) != null)
            return null;
        req.setAttribute(RESPONSE_STRING_NAME, "");
        String message = "";

        // we go over all the responders of this page (hold in the request)
        for (Iterator<Object> responderCodes = Responder.getResponderCodes(req); responderCodes.hasNext();) {

            // first we need to retrieve the responder from the cache
            String code = (String) responderCodes.next();
            String suffix = getSuffixes(code)[0];
            String parentSuffix = getSuffixes(code)[1];
            Responder formResponder = ResponderCacheManager.getResponder(code, suffix, parentSuffix);

            try {
                checkMultipleSubmission(req, formResponder);

                // respond, depending on the operation (new, add, edit, delete)
                Object result = formResponder.op.respondTo(req, formResponder, suffix, parentSuffix);

                // display the response message and set attributes
                message = "<font color=green>" + formResponder.message + "</font>";
                if (result != null) {
                    req.setAttribute(formResponder.resultAttribute, result);
                    req.setAttribute(resultNamePrefix + suffix, result);
                }
                req.setAttribute("makumba.successfulResponse", "yes");

            } catch (AttributeNotFoundException anfe) {
                // attribute not found is a programmer error and is reported
                ControllerFilter.treatException(anfe, req, resp);
                continue;
            } catch (CompositeValidationException e) {
                req.setAttribute(formResponder.resultAttribute, Pointer.Null);
                req.setAttribute(resultNamePrefix + suffix, Pointer.Null);
                // we do nothing, cause we will treat that from the ControllerFilter.doFilter
                return e;
            } catch (LogicException e) {
                java.util.logging.Logger.getLogger("org.makumba." + "logic.error").log(Level.INFO, "error", e);
                message = errorMessage(e);
                req.setAttribute(formResponder.resultAttribute, Pointer.Null);
                req.setAttribute(resultNamePrefix + suffix, Pointer.Null);
            } catch (Throwable t) {
                // all included error types should be considered here
                ControllerFilter.treatException(t, req, resp);
            }
            // messages of inner forms are ignored
            if (suffix.equals(""))
                req.setAttribute(RESPONSE_STRING_NAME, message);
        }
        return null;
    }

    /**
     * Checks if a form has been submitted several times.
     * 
     * @param req
     *            the current request
     * @param tp
     *            a TransactionProvider needed to query the database for the form tickets
     * @param fr
     *            the formResponder
     * @throws LogicException
     *             if a form has already been submitted once, throw a LogicException to say so
     */
    private static void checkMultipleSubmission(HttpServletRequest req, Responder fr) throws LogicException {
        String reqFormSession = (String) RequestAttributes.getParameters(req).getParameter(formSessionName);
        if (fr.multipleSubmitErrorMsg != null && !fr.multipleSubmitErrorMsg.equals("") && reqFormSession != null) {
            Transaction db = null;
            try {
                db = new TransactionProvider(new Configuration()).getConnectionTo(RequestAttributes.getAttributes(req).getRequestDatabase());

                // check to see if the ticket is valid... if it exists in the db
                Vector v = db.executeQuery(
                    "SELECT ms FROM org.makumba.controller.MultipleSubmit ms WHERE ms.formSession=$1", reqFormSession);
                if (v.size() == 0) { // the ticket does not exist... error
                    throw new LogicException(fr.multipleSubmitErrorMsg);

                } else if (v.size() >= 1) { // the ticket exists... continue
                    // garbage collection of old tickets
                    GregorianCalendar c = new GregorianCalendar();
                    c.add(GregorianCalendar.HOUR, -5); // how many hours of history do we want?

                    Object[] params = { reqFormSession, c.getTime() };
                    // delete the currently used ticked and the expired ones
                    db.delete("org.makumba.controller.MultipleSubmit ms", "ms.formSession=$1 OR ms.TS_create<$2",
                        params);
                }
            } finally {
                db.close();
            }
        }
    }

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
            HttpServletRequest req, String suffix);

    public static ArrayList getUnassignedExceptions(CompositeValidationException e, HttpServletRequest req) {
        ArrayList unassignedExceptions = e.getExceptions();
        for (Iterator<Object> responderCodes = Responder.getResponderCodes(req); responderCodes.hasNext();) {
            String responderCode = (String) responderCodes.next();
            String[] suffixes = getSuffixes(responderCode);
            ResponderCacheManager.getResponder(responderCode, suffixes[0], suffixes[1]).getUnassignedExceptions(e,
                unassignedExceptions, req, suffixes[0]);
        }
        return unassignedExceptions;
    }
}