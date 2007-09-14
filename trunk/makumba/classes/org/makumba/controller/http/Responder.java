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

package org.makumba.controller.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Level;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.makumba.AttributeNotFoundException;
import org.makumba.DataDefinition;
import org.makumba.LogicException;
import org.makumba.MakumbaError;
import org.makumba.MakumbaSystem;
import org.makumba.CompositeValidationException;
import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.controller.Logic;
import org.makumba.util.NamedResourceFactory;
import org.makumba.util.NamedResources;

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
     * used to fix the multiple submition bug (#190), it's basicaliy respoder+sessionID. <br>
     * TODO find a meaningful name :)
     */
    public final static String formSessionName = "__makumba__formSession__";

    /** the default label used to store the add and new result, "___mak___edited___" */
    static public final String anonymousResult = "___mak___edited___";

    /** the default response message, "changes done" */
    static public final String defaultMessage = "changes done";

    /** the name of the CGI parameter that passes the base pointer, see {@link #basePointerType}, "__makumba__base__" */
    public final static String basePointerName = "__makumba__base__";

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

    /** the business logic handler, for simple forms */
    protected String handler;

    /**
     * edit, add and delete makumba operations have a special pointer called the base pointer
     */
    protected String basePointerType;

    /** the type where the new operation is made */
    protected String newType;

    /** the field on which the add operation is made */
    protected String addField;

    /** the operation name: add, edit, delete, new, simple */
    protected String operation;

    /** the operation handler, computed from the operation */
    protected ResponderOp op;

    // --------------- form time, responder preparation -------------------
    /** pass the http request, so the responder computes its default controller and database */
    public void setHttpRequest(HttpServletRequest req) throws LogicException {
        controller = RequestAttributes.getAttributes(req).getRequestController();
        database = RequestAttributes.getAttributes(req).getRequestDatabase();
    }

    /** pass the operation */
    public void setOperation(String operation) {
        this.operation = operation;
        op = (ResponderOp) responderOps.get(operation);
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

    /** pass the response handler, if other than the default one */
    public void setHandler(String handler) {
        this.handler = handler;
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

    // --------------- responder caching section ------------------------
    static Hashtable indexedCache = new Hashtable();

    public static String makumbaResponderBaseDirectory;

    static NamedResources cache = new NamedResources("controller.responders", new NamedResourceFactory() {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public Object getHashObject(Object o) {
            return ((Responder) o).responderKey();
        }

        public Object makeResource(Object name, Object hashName) {
            Responder f = (Responder) name;
            f.identity = hashName.hashCode();

            String fileName = validResponderFilename(f.identity);

            if (indexedCache.get(new Integer(f.identity)) == null) { // responder not in cache
                try {
                    if (!new File(fileName).exists()) { // file does not exist
                        f.controllerClassname = f.controller.getClass().getName();
                        ObjectOutputStream objectOut = new ObjectOutputStream(new FileOutputStream(fileName));
                        objectOut.writeObject(f); // we write the responder to disk
                        objectOut.close();
                    }
                } catch (IOException e) {
                    MakumbaSystem.getMakumbaLogger("controller").log(Level.SEVERE,
                        "Error while trying to check for responder on the HDD: could not read from file " + fileName, e);
                }
            }
            indexedCache.put(new Integer(f.identity), name);

            return name;
        }
    });

    abstract protected void postDeserializaton();

    /** a key that should identify this responder among all */
    public String responderKey() {
        return basePointerType + message + multipleSubmitErrorMsg + resultAttribute + database + operation
                + controller.getClass().getName() + handler + addField + newType + reloadFormOnError
                + showFormAnnotated;
    }

    /** get the integer key of this form, and register it if not already registered */
    public int getPrototype() {
        // at this point we should have all data set, so we should be able to verify the responder
        String s = op.verify(this);
        if (s != null)
            throw new MakumbaError("Bad responder configuration " + s);
        return ((Responder) cache.getResource(this)).identity;
    }

    // ------------------ multiple form section
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

    static Integer suffix(String s) {
        int n = s.indexOf(suffixSeparator);
        if (n == -1)
            return ZERO;
        s = s.substring(n + 1);
        n = s.indexOf(suffixSeparator);
        if (n != -1)
            s = s.substring(0, n);
        return new Integer(Integer.parseInt(s));
    }

    static Comparator bySuffix = new Comparator() {
        public int compare(Object o1, Object o2) {
            return suffix((String) o1).compareTo(suffix((String) o2));
        }

        public boolean equals(Object o) {
            return false;
        }
    };

    /**
     * read all responder codes from a request (all code_suffix values of __mak__responder__) and order them by suffix,
     * return the enumeration of responder codes
     */
    static Iterator getResponderCodes(HttpServletRequest req) {
        TreeSet set = new TreeSet(bySuffix);

        Object o = RequestAttributes.getParameters(req).getParameter(responderName);
        if (o != null) {
            if (o instanceof String)
                set.add(o);
            else
                set.addAll((Vector) o);
        }
        return set.iterator();
    }

    // ----------------- response section ------------------
    static public final String RESPONSE_STRING_NAME = "makumba.response";

    static final String resultNamePrefix = "org.makumba.controller.resultOf_";

    private static String validResponderFilename(int responderValue) {
        return new String(makumbaResponderBaseDirectory + "/") + String.valueOf(responderValue).replaceAll("-", "_");
    }

    protected static void setResponderWorkingDir(HttpServletRequest request) {
        // set the correct working directory for the responders
        if (Responder.makumbaResponderBaseDirectory == null) {
            System.out.println("had an empty responder dir - working dir ==> "
                    + request.getSession().getServletContext().getAttribute("javax.servlet.context.tempdir"));
            String baseDir = request.getSession().getServletContext().getAttribute("javax.servlet.context.tempdir")
                    + System.getProperty("file.separator") + "makumba-responders"
                    + System.getProperty("file.separator");
            Responder.makumbaResponderBaseDirectory = baseDir + request.getContextPath();
            if (!new File(Responder.makumbaResponderBaseDirectory).exists()) {
                new File(baseDir).mkdir();
                new File(Responder.makumbaResponderBaseDirectory).mkdir();
            }
            System.out.println("base dir: " + Responder.makumbaResponderBaseDirectory);
        }
    }

    /**
     * This method returns the first responder object found fitting the request. It can be used to retrieve information
     * about the form which is valid for all nested forms, and is used e.g. in {@link ControllerFilter} to find out the
     * value of {@link #getReloadFormOnError()}.
     */
    static Responder getFirstResponder(ServletRequest req) {
        Iterator responderCodes = getResponderCodes((HttpServletRequest) req);
        if (responderCodes.hasNext()) {
            String code = (String) responderCodes.next();
            String suffix = "";
            String parentSuffix = null;
            int n = code.indexOf(suffixSeparator);
            if (n != -1) {
                suffix = code.substring(n);
                parentSuffix = "";
                n = suffix.indexOf(suffixSeparator, 1);
                if (n != -1) {
                    parentSuffix = suffix.substring(n);
                    suffix = suffix.substring(0, n);
                }
            }
            return getResponder(code, suffix, parentSuffix);
        } else {
            return null;
        }
    }

    private static Responder getResponder(String code, String suffix, String parentSuffix) {
        Integer i = new Integer(Integer.parseInt(code));
        Responder fr = ((Responder) indexedCache.get(i));
        String fileName = validResponderFilename(i.intValue());

        // responder check
        if (fr == null) { // we do not have responder in cache --> try to get it from disk
            ObjectInputStream objectIn = null;
            try {
                objectIn = new ObjectInputStream(new FileInputStream(fileName));
                fr = (Responder) objectIn.readObject();
                fr.postDeserializaton();
                fr.controller = Logic.getController(fr.controllerClassname);
            } catch (UnsupportedClassVersionError e) {
                // if we try to read a responder that was written with a different version of the responder class
                // we delete it, and throw an exception
                MakumbaSystem.getMakumbaLogger("controller").log(Level.SEVERE,
                    "Error while trying to check for responder on the HDD: could not read from file " + fileName, e);
                new File(fileName).delete();
                throw new org.makumba.MakumbaError(
                        "Responder cannot be re-used due to Makumba version change! Please reload this page.");
            } catch (InvalidClassException e) {
                // same as above
                MakumbaSystem.getMakumbaLogger("controller").log(Level.SEVERE,
                    "Error while trying to check for responder on the HDD: could not read from file " + fileName, e);
                new File(fileName).delete();
                throw new org.makumba.MakumbaError(
                        "Responder cannot be re-used due to Makumba version change! Please reload this page.");
            } catch (IOException e) {
                MakumbaSystem.getMakumbaLogger("controller").log(Level.SEVERE,
                    "Error while trying to check for responder on the HDD: could not read from file " + fileName, e);
            } catch (ClassNotFoundException e) {
                MakumbaSystem.getMakumbaLogger("controller").log(Level.SEVERE,
                    "Error while trying to check for responder on the HDD: class not found: " + fileName, e);
            } finally {
                if (objectIn != null) {
                    try {
                        objectIn.close();
                    } catch (IOException e1) {
                    }
                }
            }
            if (fr == null) { // we did not find the responder on the disk
                throw new org.makumba.MakumbaError(
                        "Responder cannot be found, probably due to server restart. Please reload this page.");
            }
        }
        // end responder check
        return fr;
    }

    /** respond to a http request */
    static Exception response(HttpServletRequest req, HttpServletResponse resp) {
        setResponderWorkingDir(req);

        if (req.getAttribute(RESPONSE_STRING_NAME) != null)
            return null;
        req.setAttribute(RESPONSE_STRING_NAME, "");
        String message = "";

        for (Iterator responderCodes = getResponderCodes(req); responderCodes.hasNext();) {
            String code = (String) responderCodes.next();
            String responderCode = code;
            String suffix = "";
            String parentSuffix = null;
            int n = code.indexOf(suffixSeparator);
            if (n != -1) {
                responderCode = code.substring(0, n);
                suffix = code.substring(n);
                parentSuffix = "";
                n = suffix.indexOf(suffixSeparator, 1);
                if (n != -1) {
                    parentSuffix = suffix.substring(n);
                    suffix = suffix.substring(0, n);
                }
            }
            Responder fr = getResponder(responderCode, suffix, parentSuffix);

            try {
                // check for multiple submition of forms
                String reqFormSession = (String) RequestAttributes.getParameters(req).getParameter(formSessionName);
                if (fr.multipleSubmitErrorMsg != null && !fr.multipleSubmitErrorMsg.equals("")
                        && reqFormSession != null) {
                    Transaction db = null;
                    try {
                        db = MakumbaSystem.getConnectionTo(RequestAttributes.getAttributes(req).getRequestDatabase());

                        // check to see if the ticket is valid... if it exists in the db
                        Vector v = db.executeQuery(
                            "SELECT ms FROM org.makumba.controller.MultipleSubmit ms WHERE ms.formSession=$1",
                            reqFormSession);
                        if (v.size() == 0) { // the ticket does not exist... error
                            throw new LogicException(fr.multipleSubmitErrorMsg);

                        } else if (v.size() >= 1) { // the ticket exists... continue
                            // garbage collection of old tickets
                            GregorianCalendar c = new GregorianCalendar();
                            c.add(GregorianCalendar.HOUR, -5); // how many hours of history do we want?

                            Object[] params = { reqFormSession, c.getTime() };
                            // delete the currently used ticked and the expired ones
                            db.delete("org.makumba.controller.MultipleSubmit ms",
                                "ms.formSession=$1 OR ms.TS_create<$2", params);
                        }
                    } finally {
                        db.close();
                    }
                }
                // end mulitiple submit check

                Object result = fr.op.respondTo(req, fr, suffix, parentSuffix);
                message = "<font color=green>" + fr.message + "</font>";
                if (result != null) {
                    req.setAttribute(fr.resultAttribute, result);
                    req.setAttribute(resultNamePrefix + suffix, result);
                }
                req.setAttribute("makumba.successfulResponse", "yes");
            } catch (AttributeNotFoundException anfe) {
                // attribute not found is a programmer error and is reported
                ControllerFilter.treatException(anfe, req, resp);
                continue;
            } catch (CompositeValidationException e) {
                req.setAttribute(fr.resultAttribute, Pointer.Null);
                req.setAttribute(resultNamePrefix + suffix, Pointer.Null);
                // we do nothing, cause we will treat that from the ControllerFilter.doFilter
                return e;
            } catch (LogicException e) {
                MakumbaSystem.getMakumbaLogger("logic.error").log(Level.INFO, "error", e);
                message = errorMessage(e);
                req.setAttribute(fr.resultAttribute, Pointer.Null);
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

    /** format an error message */
    public static String errorMessage(Throwable t) {
        return errorMessage(t.getMessage());
    }

    /** format an error message */
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
     * read the data needed for the logic operation, from the http request. org.makumba.controller.html.FormResponder
     * provides an implementation
     */
    public abstract Dictionary getHttpData(HttpServletRequest req, String suffix);

    public abstract ArrayList getUnassignedExceptions(CompositeValidationException e, ArrayList unassignedExceptions,
            HttpServletRequest req, String suffix);

    public static ArrayList getUnassignedExceptions(CompositeValidationException e, HttpServletRequest req) {
        ArrayList unassignedExceptions = e.getExceptions();
        for (Iterator responderCodes = getResponderCodes(req); responderCodes.hasNext();) {
            String responderCode = (String) responderCodes.next();
            String[] suffixes = getSuffixes(responderCode);
            getResponder(responderCode, suffixes[0], suffixes[1]).getUnassignedExceptions(e, unassignedExceptions, req,
                suffixes[0]);
        }
        return unassignedExceptions;
    }

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

    static Hashtable responderOps = new Hashtable();
    static {
        responderOps.put("edit", new ResponderOp() {
            private static final long serialVersionUID = 1L;

            public Object respondTo(HttpServletRequest req, Responder resp, String suffix, String parentSuffix)
                    throws LogicException {
                String handlerName;
                if (resp.handler != null) {
                    handlerName = resp.handler;
                } else {
                    handlerName = "on_edit" + Logic.upperCase(resp.basePointerType);
                }

                return Logic.doEdit(resp.controller, handlerName, resp.basePointerType, resp.getHttpBasePointer(req,
                    suffix), resp.getHttpData(req, suffix), new RequestAttributes(resp.controller, req, resp.database),
                    resp.database, RequestAttributes.getConnectionProvider(req));
            }

            public String verify(Responder resp) {
                return null;
            }
        });

        responderOps.put("simple", new ResponderOp() {
            private static final long serialVersionUID = 1L;

            public Object respondTo(HttpServletRequest req, Responder resp, String suffix, String parentSuffix)
                    throws LogicException {
                return Logic.doOp(resp.controller, resp.handler, resp.getHttpData(req, suffix), new RequestAttributes(
                        resp.controller, req, resp.database), resp.database,
                    RequestAttributes.getConnectionProvider(req));
            }

            public String verify(Responder resp) {
                return null;
            }
        });

        responderOps.put("new", new ResponderOp() {
            private static final long serialVersionUID = 1L;

            public Object respondTo(HttpServletRequest req, Responder resp, String suffix, String parentSuffix)
                    throws LogicException {
                String handlerName;
                if (resp.handler != null) {
                    handlerName = resp.handler;
                } else {
                    handlerName = "on_new" + Logic.upperCase(resp.newType);
                }
                return Logic.doNew(resp.controller, handlerName, resp.newType, resp.getHttpData(req, suffix),
                    new RequestAttributes(resp.controller, req, resp.database), resp.database,
                    RequestAttributes.getConnectionProvider(req));
            }

            public String verify(Responder resp) {
                return null;
            }
        });

        responderOps.put("add", new ResponderOp() {
            private static final long serialVersionUID = 1L;

            public Object respondTo(HttpServletRequest req, Responder resp, String suffix, String parentSuffix)
                    throws LogicException {
                String handlerName;
                if (resp.handler != null) {
                    handlerName = resp.handler;
                } else {
                    handlerName = "on_add" + Logic.upperCase(resp.basePointerType + "->" + resp.addField);
                }
                return Logic.doAdd(resp.controller, handlerName, resp.basePointerType + "->" + resp.addField,
                    resp.getHttpBasePointer(req, suffix), resp.getHttpData(req, suffix), new RequestAttributes(
                            resp.controller, req, resp.database), resp.database,
                    RequestAttributes.getConnectionProvider(req));
            }

            public String verify(Responder resp) {
                return null;
            }
        });

        responderOps.put("addToNew", new ResponderOp() {
            private static final long serialVersionUID = 1L;

            public Object respondTo(HttpServletRequest req, Responder resp, String suffix, String parentSuffix)
                    throws LogicException {
                // get result we got from the new form
                Object resultFromNew = req.getAttribute(resultNamePrefix + parentSuffix);

                // if we got a null response from the new form (possibly from a logic exception thrown by the
                // programmer)
                if (resultFromNew == org.makumba.Pointer.Null) {
                    return org.makumba.Pointer.Null; // we return null here too
                }

                String handlerName;
                if (resp.handler != null) {
                    handlerName = resp.handler;
                } else {
                    System.out.println("resp.basePointerType:" + resp.basePointerType);
                    handlerName = "on_edit" + Logic.upperCase(resp.basePointerType);
                }
                // otherwise, we add to the new object
                return Logic.doAdd(resp.controller, handlerName, resp.newType + "->" + resp.addField,
                    (Pointer) resultFromNew, resp.getHttpData(req, suffix), new RequestAttributes(resp.controller, req,
                            resp.database), resp.database, RequestAttributes.getConnectionProvider(req));
            }

            public String verify(Responder resp) {
                return null;
            }
        });

        responderOps.put("deleteLink", new ResponderOp() {
            private static final long serialVersionUID = 1L;

            public Object respondTo(HttpServletRequest req, Responder resp, String suffix, String parentSuffix)
                    throws LogicException {
                return Logic.doDelete(resp.controller, resp.basePointerType, resp.getHttpBasePointer(req, suffix),
                    new RequestAttributes(resp.controller, req, resp.database), resp.database,
                    RequestAttributes.getConnectionProvider(req));
            }

            public String verify(Responder resp) {
                return null;
            }
        });

        responderOps.put("deleteForm", new ResponderOp() {
            private static final long serialVersionUID = 1L;

            public Object respondTo(HttpServletRequest req, Responder resp, String suffix, String parentSuffix)
                    throws LogicException {
                return Logic.doDelete(resp.controller, resp.basePointerType, resp.getHttpBasePointer(req, suffix),
                    new RequestAttributes(resp.controller, req, resp.database), resp.database,
                    RequestAttributes.getConnectionProvider(req));
            }

            public String verify(Responder resp) {
                return null;
            }
        });
    }
}

/** this class helps to differentiate between the different types of forms */
abstract class ResponderOp implements java.io.Serializable {
    /** respond to the given request, with the data from the given responder, read using the given multiple form suffix */
    public abstract Object respondTo(HttpServletRequest req, Responder resp, String suffix, String parentSuffix)
            throws LogicException;

    /** check the validity of the given responder data, return not-null if there is a problem */
    public abstract String verify(Responder resp);
}
