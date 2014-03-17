package org.makumba.devel;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.makumba.MakumbaError;
import org.makumba.UnauthenticatedException;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.commons.http.ControllerHandler;
import org.makumba.commons.http.ServletObjects;
import org.makumba.controller.MakumbaResponseWrapper;
import org.makumba.list.tags.SectionTag;

/**
 * This handler lets the request go to the filter chain and then catches all kind of exceptions after it. The exceptions
 * are then handled by the {@link ErrorFormatter}. It should be the first one of the filter handlers. If disabled, the
 * "raw" exception is thrown. TODO the login should not be triggered directly in here, but somewhere else
 * 
 * @author Manuel Bernhardt <manuel@makumba.org>
 * @author Rudolf Mayer
 * @author Filip Kis
 * @version $Id: ErrorFilter.java,v 1.1 12.10.2007 18:17:00 Manuel Exp $
 */
public class ErrorControllerHandler extends ControllerHandler {

    public static final String ORIGINAL_REQUEST = "org.makumba.originalRequest";

    @Override
    public boolean beforeFilter(ServletRequest request, ServletResponse response, FilterConfig conf,
            ServletObjects httpServletObjects) {

        if (wasException((HttpServletRequest) request)) {
            return false;
        }

        return true;
    }

    @Override
    public boolean onError(ServletRequest request, ServletResponse response, Throwable e, FilterConfig conf) {
        treatException(e, (HttpServletRequest) request, (HttpServletResponse) response, conf);
        return false;
    }

    /**
     * Treats an exception that occurred during the request. Displays the exception and sets an attribute corresponding
     * to it. The exception is displayed either in custom "error.jsp" set by the user in the folder of the page that
     * throw an exception or any parent root. In this case, three attributes (mak_error_title, mak_error_description ,
     * mak_error_realpath) are set so that the user can place use them in the custom page freely. If the custom page is
     * not found the error is show in the TagExceptionFilter.
     * 
     * @param t
     *            the Throwable corresponding to the exception
     * @param req
     *            the http request corresponding to the access
     * @param resp
     *            the http response corresponding to the access
     */
    public void treatException(Throwable t, HttpServletRequest req, HttpServletResponse resp, FilterConfig conf) {
        if (SectionTag.getEvent(req) != null) {
            t.printStackTrace();
            resp.setStatus(500);
            try {
                ((MakumbaResponseWrapper) resp).outputOn();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        resp.setContentType("text/html");
        // sometimes tomcat wraps the RuntimeWrappedException in a JasperException (which extends ServletException)
        if (t instanceof ServletException && ((ServletException) t).getRootCause() instanceof RuntimeWrappedException) {
            t = ((ServletException) t).getRootCause();
        }
        if (t instanceof RuntimeWrappedException) {
            t = ((RuntimeWrappedException) t).getCause();
        }
        req.setAttribute(javax.servlet.jsp.PageContext.EXCEPTION, t);

        if (req.getAttribute("org.makumba.exceptionTreated") == null
                && !(t instanceof UnauthenticatedException && login(req, resp))) {
            try {

                String errorPage = getPage(req, req.getServletPath(), "error.jsp");

                if (errorPage != null) {
                    StringWriter sw = new StringWriter();
                    PrintWriter wr = new PrintWriter(sw);
                    ErrorFormatter ef = new ErrorFormatter(req, req.getSession().getServletContext(), wr, false);
                    req.setAttribute("mak_error_title", ef.getTitle());
                    req.setAttribute("mak_error_description", sw.toString());
                    req.setAttribute("mak_error_realpath",
                        new File(req.getSession().getServletContext().getRealPath("/")).getCanonicalPath());
                    // FIXME:see if error code thrown gives problems to tests
                    // resp.setStatus(500);
                    req.getRequestDispatcher(errorPage).forward(req, resp);
                } else {
                    // code copied from TagExceptionServlet
                    resp.setContentType("text/html");
                    // FIXME:see if error code thrown gives problems to tests
                    // resp.setStatus(500);
                    new ErrorFormatter(req, conf.getServletContext(), resp.getWriter(), true);
                    resp.getWriter().flush();
                    // req.getRequestDispatcher("/servlet/org.makumba.devel.TagExceptionServlet").forward(req, resp);
                }
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
                // FIXME: most probably this message is tomcat specific
                if (ise.getMessage().startsWith("getOutputStream() has already been called for this response")) {
                    // we have called response.getOutputStream(), or similar, in the JSP page
                    // however, we still have some output in the page, even if it is just a space
                    java.util.logging.Logger.getLogger("org.makumba.controller").severe(
                        "There is an error on page '"
                                + req.getServletPath()
                                + "'. You have called 'response.getOutputStream()' or 'response.getWriter()' in the page, maybe to write a binary response, but you still have some non-Java-scriplet code, e.g. HTML, or even just a single whitespace.\n"
                                + "Remove all of these to get rid of this error message"
                                + new ErrorFormatter().getErrorMessage(req));
                } else {
                    // most likely due to not being able to redirect the page to the error page due to already flushed
                    // buffers
                    // ==> we display a warning, and display the error message as it would have been on the page
                    java.util.logging.Logger.getLogger("org.makumba.controller").severe(
                        "Page execution breaks on page '"
                                + req.getServletPath()
                                + "' but the error page can't be displayed due to too small buffer size.\n"
                                + "==> Try increasing the page buffer size by manually increasing the buffer to 16kb (or more) using <%@ page buffer=\"16kb\"%> in the .jsp page\n"
                                + "The makumba error message would have been:\n"
                                + new ErrorFormatter().getErrorMessage(req));
                }
            }
        }
        setWasException(req);
        req.setAttribute("org.makumba.exceptionTreated", "yes");
    }

    /**
     * Computes the login page from a servletPath
     * 
     * @param req
     *            the http request corresponding to the current access
     * @param servletPath
     *            the path of the servlet we are in
     * @return A String containing the path to the login page
     */
    public static String getLoginPage(HttpServletRequest req, String servletPath) {
        return getPage(req, servletPath, "login.jsp");
    }

    /**
     * Computes any page from a servletPath, used to compute login, error or any other default page
     * 
     * @param req
     *            the http request corresponding to the current access
     * @param servletPath
     *            the path of the servlet we are in
     * @param pageName
     *            the name of the page we are looking for
     * @return
     */

    public static String getPage(HttpServletRequest req, String servletPath, String pageName) {

        // FIXME: This doesn't work in the webapps, it returns the path of the last context
        // alphabetically and not the one you are in:
        // String root = conf.getServletContext().getRealPath("/");
        String root = req.getSession().getServletContext().getRealPath("/");

        String virtualRoot = "/";
        String page = null;

        java.util.StringTokenizer st = new java.util.StringTokenizer(servletPath, "/");
        while (st.hasMoreElements()) {
            if (new java.io.File(root + pageName).exists()) {
                page = virtualRoot + pageName;
            }
            String s = st.nextToken() + "/";
            root += s;
            virtualRoot += s;
        }
        if (new java.io.File(root + pageName).exists()) {
            page = virtualRoot + pageName;
        }
        return page;
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
        String login = getLoginPage(req, req.getServletPath());

        if (login == null) {
            return false;
        }

        // we will forward to the login page using the original request
        while (req instanceof HttpServletRequestWrapper) {
            req = (HttpServletRequest) ((HttpServletRequestWrapper) req).getRequest();
        }

        req.setAttribute(ORIGINAL_REQUEST, req);

        try {
            req.getRequestDispatcher(login).forward(req, resp);
        } catch (Throwable q) {
            q.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Signals that there was an exception during the request, so some operations can be skipped
     * 
     * @param req
     *            the http request corresponding to the current access
     */
    public void setWasException(HttpServletRequest req) {
        req.setAttribute("org.makumba.wasException", "yes");
    }

    /**
     * Tests if there was an exception during the request
     * 
     * @param req
     *            the http request corresponding to the current access
     * @return <code>true</code> if there was an exception, <code>false</code> otherwise
     */
    public boolean wasException(HttpServletRequest req) {
        return "yes".equals(req.getAttribute("org.makumba.wasException"));
    }

}
