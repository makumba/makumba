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

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.makumba.InvalidValueException;
import org.makumba.MakumbaError;
import org.makumba.CompositeValidationException;
import org.makumba.UnauthorizedException;
import org.makumba.analyser.AnalysableTag;
import org.makumba.commons.StringUtils;
import org.makumba.commons.attributes.RequestAttributes;
import org.makumba.controller.DbConnectionProvider;
import org.makumba.devel.TagExceptionServlet;
import org.makumba.forms.responder.Responder;
import org.makumba.forms.responder.ResponderCacheManager;
import org.makumba.forms.responder.ResponderFactory;

/**
 * The filter that controls each makumba HTTP access. Performs login, form response, exception handling.
 * 
 * @author Cristian Bogdan
 * @author Rudolf Mayer
 * @version $Id$ *
 */
public class ControllerFilter implements Filter {
    public static final String MAKUMBA_FORM_VALIDATION_ERRORS = "__makumba__formValidationErrors__";

    public static final String MAKUMBA_FORM_RELOAD = "__makumba__formReload__";

    public static final String ORIGINAL_REQUEST = "org.makumba.originalRequest";

    static FilterConfig conf;

    public void init(FilterConfig c) {
        conf = c;
    }

    private static ThreadLocal<ServletRequest> requestThreadLocal = new ThreadLocal<ServletRequest>();

    private static ResponderFactory factory = ResponderFactory.getInstance();
    
    /**
     * Gets the request
     * 
     * @return The HTTPServletRequest corresponding to the current access
     */
    public static HttpServletRequest getRequest() {
        return (HttpServletRequest) requestThreadLocal.get();
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException,
            java.io.IOException {
        boolean filter = shouldFilter((HttpServletRequest) req);
        requestThreadLocal.set(req);

        // initalises a database pool (one connection per database)
        DbConnectionProvider prov = RequestAttributes.getConnectionProvider((HttpServletRequest) req);

        if (filter) {
            try {
                if(!checkAttributes(req, resp)) return;
                
                Exception e = factory.getResponse((HttpServletRequest) req, (HttpServletResponse) resp);
                if (e instanceof CompositeValidationException) {
                    CompositeValidationException v = (CompositeValidationException) e;
                    req.setAttribute(MAKUMBA_FORM_VALIDATION_ERRORS, v);
                    req.setAttribute(MAKUMBA_FORM_RELOAD, "true");

                    String message;

                    // Check if we shall reload the form page
                    Responder firstResponder = factory.getFirstResponder(req);
                    java.util.logging.Logger.getLogger("org.makumba." + "controller").fine(
                        "Caught a CompositeValidationException, reloading form page: "
                                + firstResponder.getReloadFormOnError());
                    
                    if (firstResponder.getReloadFormOnError()) {
                        
                        final String root = conf.getInitParameter(req.getServerName());
                        HttpServletRequest httpServletRequest = ((HttpServletRequest) getRequest());
                        String originatingPage = httpServletRequest.getParameter(Responder.originatingPageName);
                        String contextPath = httpServletRequest.getContextPath();
                        
                        if (originatingPage.startsWith(contextPath)) {
                            originatingPage = originatingPage.substring(contextPath.length());
                        }
                        if (originatingPage.indexOf("?") > 0) {
                            originatingPage = originatingPage.substring(0, originatingPage.indexOf("?"));
                        }

                        req = getFormReloadRequest(req);
                        resp = getFormReloadResponse(resp, root);

                        java.util.logging.Logger.getLogger("org.makumba." + "controller").fine(
                            "CompositeValidationException: annotating form: " + firstResponder.getShowFormAnnotated());
                        
                        if (firstResponder.getShowFormAnnotated()) {
                            java.util.logging.Logger.getLogger("org.makumba." + "controller").finer(
                                "Processing CompositeValidationException for annotation:\n" + v.toString());
                            // if the form shall be annotated, we need to filter which exceptions can be assigned to
                            // fields, and which not
                            ArrayList unassignedExceptions = factory.getUnassignedExceptions(v,
                                (HttpServletRequest) req);
                            java.util.logging.Logger.getLogger("org.makumba." + "controller").finer(
                                "Exceptions not assigned:\n" + StringUtils.toString(unassignedExceptions));

                            // the messages left unassigned will be shown as the form response
                            message = "";
                            for (Iterator iter = unassignedExceptions.iterator(); iter.hasNext();) {
                                InvalidValueException invEx = (InvalidValueException) iter.next();
                                message += invEx.getMessage() + "<br>";
                            }
                        } else {
                            message = v.toString();
                        }

                    } else { // we do not change the target page
                        message = v.toString();
                    }

                    message = Responder.errorMessage(message);
                    req.setAttribute(ResponderFactory.RESPONSE_STRING_NAME, message);

                } else if (wasException((HttpServletRequest) req))
                    return;
            } finally {
                prov.close();
            } // commit everything after response
        }

        try {
            chain.doFilter(req, resp);
        } catch (AllowedException e) {
        } catch (Throwable e) {
            treatException(e, (HttpServletRequest) req, (HttpServletResponse) resp);
            return;
        } finally {
            prov.close();
        }
    }

    /**
     * Makes a response wrapper, using an anonymous inner class
     * @param resp the original response
     * @param root the server hostname
     * @return a wrapped ServletResponse redirecting to the original page
     */
    private ServletResponse getFormReloadResponse(ServletResponse resp, final String root) {
        resp = new HttpServletResponseWrapper((HttpServletResponse) resp) {
            public void sendRedirect(String s) throws java.io.IOException {
                if (root != null && s.startsWith(root)) {
                    s = s.substring(root.length());
                }
                ((HttpServletResponse) getResponse()).sendRedirect(s);
            }

        };
        return resp;
    }

    /**
     * Makes a request wrapper, using an anonymous inner class.
     * @param req the original request
     * @return a wrapped ServletRequest containing information about the originally submitted page
     */
    private ServletRequest getFormReloadRequest(ServletRequest req) {
        // 
        req = new HttpServletRequestWrapper((HttpServletRequest) req) {

            public String getServletPath() {
                HttpServletRequest httpServletRequest = ((HttpServletRequest) getRequest());
                String originatingPage = httpServletRequest.getParameter(Responder.originatingPageName);
                String contextPath = httpServletRequest.getContextPath();
                if (originatingPage.startsWith(contextPath)) {
                    originatingPage = originatingPage.substring(contextPath.length());
                }
                if (originatingPage.indexOf("?") > 0) {
                    originatingPage = originatingPage.substring(0, originatingPage.indexOf("?"));
                }
                return originatingPage;
            }

            /**
             * We override this method so we can serve the previous request URI. This doesn't have any
             * effect on the URI displayed in the browser, but it makes submitting the form twice
             * possible...
             */
            public String getRequestURI() {
                return getRequest().getParameter(Responder.originatingPageName);
            }
        };
        return req;
    }

    private boolean checkAttributes(ServletRequest req, ServletResponse resp) {
        try {
            RequestAttributes.getAttributes((HttpServletRequest) req);
            return true;
        } catch (Throwable e) {
            treatException(e, (HttpServletRequest) req, (HttpServletResponse) resp);
            return false;
        }
    }

    /**
     * Decides if we filter or not
     * 
     * @param req
     *            the request corresponding to the access
     * @return <code>true</code> if we should filter, <code>false</code> otherwise
     */
    public boolean shouldFilter(HttpServletRequest req) {
        String uri = req.getRequestURI();

        // accesses to the source viewer are not filtered
        if (uri.startsWith("/dataDefinitions") || uri.startsWith("/logic") || uri.startsWith("/classes"))
            return false;
        String file = null;
        try {
            file = new URL(req.getRequestURL().toString()).getFile();
        } catch (java.net.MalformedURLException e) {
        } // can't be

        // JSP and HTML are always filtered
        if (file.endsWith(".jsp") || file.endsWith(".html"))
            return true;

        // JSPX is never filtered
        if (file.endsWith(".jspx"))
            return false;

        // we compute the file that corresponds to the indicated path
        java.io.File f = new java.io.File(conf.getServletContext().getRealPath(req.getRequestURI()));

        // if it's a directory, there will most probably be a redirection, we filter anyway
        if (f.isDirectory())
            return true;

        // if the file does not exist on disk, it means that it's produced dynamically, so we filter
        // it it exists, it's probably an image or a CSS, we don't filter
        return !f.exists();
    }

    public void destroy() {
    }

    /**
     * Treats an exception that occured during the request. Displays the exception and sets an attribute corresponding
     * to it.
     * 
     * @param t
     *            the Throwable corresponding to the exception
     * @param req
     *            the http request corresponding to the access
     * @param resp
     *            the http response corresponding to the access
     */
    static public void treatException(Throwable t, HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("text/html");
        req.setAttribute(javax.servlet.jsp.PageContext.EXCEPTION, t);
        if (req.getAttribute("org.makumba.exceptionTreated") == null
                && !((t instanceof UnauthorizedException) && login(req, resp))) {
            try {
                req.getRequestDispatcher("/servlet/org.makumba.devel.TagExceptionServlet").forward(req, resp);
            }
            // we only catch the improbable ServletException and IOException
            // so if something is rotten in the TagExceptionServlet,
            // tomcat will deal with it
            catch (ServletException se) {
                se.printStackTrace();
                throw new MakumbaError(se);
            } catch (java.io.IOException ioe) {
                ioe.printStackTrace();
                throw new MakumbaError(ioe);
            } catch (java.lang.IllegalStateException ise) { // we get an java.lang.IllegalStateException
                // most likely due to not being able to redirect the page to the error page due to already flushed
                // buffers
                // ==> we display a warning, and display the error message as it would have been on the page
                java.util.logging.Logger.getLogger("org.makumba." + "controller").severe(
                    "Page execution breaks on page '"
                            + req.getServletPath()
                            + "' but the error page can't be displayed due to too small buffer size.\n"
                            + "==> Try increasing the page buffer size by manually increasing the buffer to 16kb (or more) using <%@ page buffer=\"16kb\"%> in the .jsp page\n"
                            + "The makumba error message would have been:\n"
                            + new TagExceptionServlet().getErrorMessage(req));
            } finally {
                AnalysableTag.initializeThread();
            }
        }
        setWasException(req);
        req.setAttribute("org.makumba.exceptionTreated", "yes");
    }

    /**
     * Signals that there was an exception during the request, so some operations can be skipped
     * 
     * @param req
     *            the http request corresponding to the current access
     */
    public static void setWasException(HttpServletRequest req) {
        req.setAttribute("org.makumba.wasException", "yes");
    }

    /**
     * Tests if there was an exception during the request
     * 
     * @param req
     *            the http request corresponding to the current access
     * @return <code>true</code> if there was an exception, <code>false</code> otherwise
     */
    public static boolean wasException(HttpServletRequest req) {
        return "yes".equals(req.getAttribute("org.makumba.wasException"));
    }

    /**
     * Computes the login page from a servletPath
     * 
     * @param servletPath
     *            the path of the servlet we are in
     * @return A String containing the path to the login page
     */
    public static String getLoginPage(String servletPath) {
        String root = conf.getServletContext().getRealPath("/");
        String virtualRoot = "/";
        String login = "/login.jsp";

        java.util.StringTokenizer st = new java.util.StringTokenizer(servletPath, "/");
        while (st.hasMoreElements()) {
            if (new java.io.File(root + "login.jsp").exists())
                login = virtualRoot + "login.jsp";
            String s = st.nextToken() + "/";
            root += s;
            virtualRoot += s;
        }
        if (new java.io.File(root + "login.jsp").exists())
            login = virtualRoot + "login.jsp";
        return login;
    }

    /**
     * Finds the closest login.jsp and forwards to it
     * 
     * @param req
     *            the http request corresponding to the current access
     * @param resp
     *            the http response corresponding to the current access
     */
    protected static boolean login(HttpServletRequest req, HttpServletResponse resp) {
        // the request path may be modified by the filter, we take it as is
        String login = getLoginPage(req.getServletPath());

        if (login == null)
            return false;

        // we will forward to the login page using the original request
        while (req instanceof HttpServletRequestWrapper)
            req = (HttpServletRequest) ((HttpServletRequestWrapper) req).getRequest();

        req.setAttribute(ORIGINAL_REQUEST, req);

        try {
            req.getRequestDispatcher(login).forward(req, resp);
        } catch (Throwable q) {
            q.printStackTrace();
            return false;
        }
        return true;
    }
}
