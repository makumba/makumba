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
//  $Id: RequestAttributes.java 1707 2007-09-28 15:35:48Z manuel_gay $
//  $Name$
/////////////////////////////////////

package org.makumba.commons.attributes;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.makumba.AttributeNotFoundException;
import org.makumba.Attributes;
import org.makumba.LogicException;
import org.makumba.Pointer;
import org.makumba.Text;
import org.makumba.UnauthenticatedException;
import org.makumba.UnauthorizedException;
import org.makumba.commons.DbConnectionProvider;
import org.makumba.controller.Logic;
import org.makumba.controller.MakumbaActorHashMap;
import org.makumba.forms.responder.ResponderFactory;
import org.makumba.providers.TransactionProvider;

/**
 * Implementation of the Makumba {@link Attributes}
 * 
 * @author Cristian Bogdan
 * @version $Id: RequestAttributes.java 1707 2007-09-28 15:35:48Z manuel_gay $
 */
public class RequestAttributes implements Attributes {
    public static final String PARAMETERS_NAME = "makumba.parameters";

    public static final String ATTRIBUTES_NAME = "makumba.attributes";

    public static final String CONTROLLER_NAME = "makumba.controller";

    final static Logger logger = java.util.logging.Logger.getLogger("org.makumba.controller");

    HttpServletRequest request;

    Object controller;

    private TransactionProvider tp = TransactionProvider.getInstance();

    public String getRequestDatabase() {
        return tp.getDefaultDataSourceName();
    }

    public Object getRequestController() {
        return controller;
    }

    public static RequestAttributes getAttributes(HttpServletRequest req) throws LogicException {
        if (req.getAttribute(ATTRIBUTES_NAME) == null) {
            req.setAttribute(ATTRIBUTES_NAME, new RequestAttributes(req));
            setFormRedirectionResponseAttributes(req);
        }
        return (RequestAttributes) req.getAttribute(ATTRIBUTES_NAME);
    }

    static void setFormRedirectionResponseAttributes(HttpServletRequest req) {
        // check if we came from a form-redirection, and move info from the session to the request
        final HttpServletRequest httpServletRequest = req;
        final HttpSession session = httpServletRequest.getSession();
        final String suffix = "_" + httpServletRequest.getRequestURI();

        final Object respFromSession = session.getAttribute(ResponderFactory.RESPONSE_STRING_NAME + suffix);
        Object response = httpServletRequest.getAttribute(org.makumba.forms.responder.ResponderFactory.RESPONSE_FORMATTED_STRING_NAME);

        logger.fine("respFromSession: " + respFromSession + ", response: " + response);
        if (response == null && respFromSession != null) {
            // set the attributes from the session to the request, clear the session values from this form
            for (String attr : ResponderFactory.RESPONSE_ATTRIBUTE_NAMES) {
                httpServletRequest.setAttribute(attr, session.getAttribute(attr + suffix));
                logger.fine("Setting '" + attr + "' value: '" + req.getAttribute(attr + suffix) + "'.");
                session.removeAttribute(attr + suffix);
            }
        }
    }

    RequestAttributes(HttpServletRequest req) throws LogicException {
        this(Logic.getLogic(req.getServletPath()), req, null);
    }

    public RequestAttributes(Object controller, HttpServletRequest req, String db) throws LogicException {
        if (db == null)
            db = getRequestDatabase();
        this.request = req;
        this.controller = controller;

        if (req.getAttribute(CONTROLLER_NAME + controller.getClass().getName()) == null) {
            req.setAttribute(CONTROLLER_NAME + controller.getClass().getName(), controller);
            try {
                getConnectionProvider(req).setContext(this);
                getConnectionProvider(req).setTransactionProvider(Logic.getTransactionProvider(controller));

                Logic.doInit(req.getServletPath(), this, db, getConnectionProvider(req));
                Logic.doInit(controller, this, db, getConnectionProvider(req));
            } catch (UnauthorizedException e) {
                // if we are not in the login page
                if (!req.getServletPath().endsWith("login.jsp"))
                    throw e;
            }
        }
    }

    static final public String PROVIDER_ATTRIBUTE = "org.makumba.providerAttribute";

    /**
     * Gives a provider to get connection to the database
     * 
     * @param req
     *            the http request corresponding to the current access
     * @return A {@link DbConnectionProvider} providing the database connection service
     */
    public static DbConnectionProvider getConnectionProvider(HttpServletRequest req) {
        DbConnectionProvider prov = (DbConnectionProvider) req.getAttribute(PROVIDER_ATTRIBUTE);
        if (prov == null) {
            prov = new DbConnectionProvider();
            req.setAttribute(PROVIDER_ATTRIBUTE, prov);
        }
        return prov;
    }

    public static HttpParameters getParameters(HttpServletRequest req) {
        if (req.getAttribute(PARAMETERS_NAME) == null)
            req.setAttribute(PARAMETERS_NAME, makeParameters(req));
        return (HttpParameters) req.getAttribute(PARAMETERS_NAME);
    }

    public static HttpParameters makeParameters(HttpServletRequest req) {
        if (req.getContentType() != null && req.getContentType().indexOf("multipart") != -1)
            return new MultipartHttpParameters(req);
        return new HttpParameters(req);
    }

    static public void setAttribute(HttpServletRequest req, String var, Object o) {
        if (o != null) {
            req.setAttribute(var, o);
            req.removeAttribute(var + "_null");
        } else {
            req.removeAttribute(var);
            req.setAttribute(var + "_null", "null");
        }
    }

    public static final Object notFound = "not found";

    /**
     * @see org.makumba.Attributes#setAttribute(java.lang.String, java.lang.Object)
     */
    public Object setAttribute(String s, Object o) {
        String snull = s + "_null";
        HttpSession ss = request.getSession(true);

        Object value = ss.getAttribute(s);
        ss.setAttribute(s, o);
        if (o == null)
            ss.setAttribute(snull, "null");
        else
            ss.removeAttribute(snull);
        return value;
    }

    /**
     * @see org.makumba.Attributes#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String s) throws LogicException {
        request.getSession(true).removeAttribute(s);
    }

    /**
     * @see org.makumba.Attributes#hasAttribute(java.lang.String)
     */
    public boolean hasAttribute(String s) {
        try {
            return (checkSessionForAttribute(s) != RequestAttributes.notFound
                    || checkServletLoginForAttribute(s) != RequestAttributes.notFound
                    || checkLogicForAttribute(s) != RequestAttributes.notFound || checkParameterForAttribute(s) != RequestAttributes.notFound);
        } catch (LogicException e) {
            return false;
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String s = "Makumba Atributes:\n";
        s += "\tSession: {";
        HttpSession ss = request.getSession(true);
        Enumeration enumSession = ss.getAttributeNames();
        while (enumSession.hasMoreElements()) {
            String key = (String) enumSession.nextElement();
            s += key + "=";
            Object value = ss.getAttribute(key);
            s = printElement(s, value);
            if (enumSession.hasMoreElements()) {
                s += ", ";
            }
        }
        s += "}\n";

        Enumeration enumRequest = request.getAttributeNames();
        s += "\tRequest: {";
        while (enumRequest.hasMoreElements()) {
            String key = (String) enumRequest.nextElement();
            s += key + "=";
            Object value = request.getAttribute(key);
            s = printElement(s, value);
            if (enumRequest.hasMoreElements()) {
                s += ", ";
            }
        }
        s += "}\n";

        Enumeration enumParams = request.getParameterNames();
        s += "\tParameters: {";
        while (enumParams.hasMoreElements()) {
            String key = (String) enumParams.nextElement();
            s += key + "=" + request.getParameter(key);
            if (enumParams.hasMoreElements()) {
                s += ", ";
            }
        }
        s += "}";
        return s;
    }

    private String printElement(String s, Object value) {
        if (value instanceof RequestAttributes) { // don't print if type is from this class --> avoid endless loop
            s += value.getClass();
        } else if (value instanceof Text) {
            s += ((Text) value).toShortString(100);
        } else {
            s += value;
        }
        return s;
    }

    /**
     * @see org.makumba.Attributes#getAttribute(java.lang.String)
     */
    public Object getAttribute(String s) throws LogicException {
        Object o = checkSessionForAttribute(s);
        if (o != notFound)
            return o;

        o = checkServletLoginForAttribute(s);
        if (o != notFound)
            return o;

        o = checkLogicForAttribute(s);
        if (o != notFound)
            return o;

        o = checkParameterForAttribute(s);
        if (o != notFound)
            return o;

        throw new AttributeNotFoundException(s, true);
    }

    public Object checkSessionForAttribute(String s) {
        String snull = s + "_null";
        HttpSession ss = request.getSession(true);

        Object value = ss.getAttribute(s);
        if (value != null)
            return value;
        if (ss.getAttribute(snull) != null)
            return null;
        value = request.getAttribute(s);
        if (value != null)
            return value;
        if (request.getAttribute(snull) != null)
            return null;
        return notFound;
    }

    public Object checkServletLoginForAttribute(String s) {
        if (request.getRemoteUser() != null && request.isUserInRole(s)) {
            request.getSession(true).setAttribute(s, request.getRemoteUser());
            return request.getRemoteUser();
        }
        return notFound;
    }

    public Object checkLogicForAttribute(String s) throws LogicException {
        HttpSession ss = request.getSession(true);
        boolean nullValue = false;
        Object value = null;
        try {
            value = Logic.getAttribute(getRequestController(), s, this, getRequestDatabase(),
                getConnectionProvider(request));
            if (value instanceof MakumbaActorHashMap) {
                MakumbaActorHashMap mp = (MakumbaActorHashMap) value;
                value = mp.get(s);
                for (Map.Entry<String, Object> entr : mp.entrySet()) {
                    if (entr.getKey().equals(s))
                        value = entr.getValue();
                    if (entr.getValue() == Pointer.Null)
                        removeFromSession(entr.getKey(), ss);
                    else
                        ss.setAttribute(entr.getKey(), entr.getValue());
                }
            }
            if (value == null || value == Pointer.Null) {
                nullValue = true;
            }
        } catch (NoSuchMethodException e) {
        } catch (UnauthenticatedException ue) {
            ue.setAttributeName(s);
            throw ue;
        }
        // FIXME: should check HTTP argument illegalities

        if (value != null) {
            ss.setAttribute(s, value);
            return value;
        }
        if (nullValue) {
            removeFromSession(s, ss);
            return null;
        }
        return notFound;
    }

    private void removeFromSession(String s, HttpSession ss) {
        String snull = s + "_null";
        ss.removeAttribute(s);
        ss.setAttribute(snull, "x");
    }

    public Object checkParameterForAttribute(String s) {
        Object value = getParameters(request).getParameter(s);
        if (value != null)
            return value;
        return notFound;
    }

    /**
     * Computes a Map that holds all Attributes (meaning, session attributes, request attributes and parameters) FIXME
     * should also take into account all the rest (BL, login, ...)
     */
    public Map<String, Object> toMap() {
        HttpSession ss = request.getSession(true);
        Enumeration<String> enumSession = ss.getAttributeNames();
        Enumeration<String> enumRequest = request.getAttributeNames();
        Enumeration<String> enumParams = request.getParameterNames();

        Map<String, Object> allAttributes = new HashMap<String, Object>();
        while (enumSession.hasMoreElements()) {
            String attrName = enumSession.nextElement();
            allAttributes.put(attrName, ss.getAttribute(attrName));
        }
        while (enumRequest.hasMoreElements()) {
            String attrName = enumRequest.nextElement();
            allAttributes.put(attrName, ss.getAttribute(attrName));
        }
        while (enumParams.hasMoreElements()) {
            String attrName = enumParams.nextElement();
            allAttributes.put(attrName, ss.getAttribute(attrName));
        }

        return allAttributes;
    }
}
