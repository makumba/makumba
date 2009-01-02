package org.makumba.forms.responder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

import org.makumba.CompositeValidationException;
import org.makumba.InvalidValueException;
import org.makumba.commons.ControllerHandler;
import org.makumba.commons.ServletObjects;
import org.makumba.commons.StringUtils;

public class ResponseControllerHandler extends ControllerHandler {

    public static final String MAKUMBA_FORM_VALIDATION_ERRORS = "__makumba__formValidationErrors__";

    public static final String MAKUMBA_FORM_RELOAD = "__makumba__formReload__";

    private ResponderFactory factory = ResponderFactory.getInstance();

    @Override
    public boolean beforeFilter(ServletRequest req, ServletResponse resp, FilterConfig conf,
            ServletObjects httpServletObjects) throws Exception {

        Exception e = factory.getResponse((HttpServletRequest) req, (HttpServletResponse) resp);
        FormResponder responder = (FormResponder) factory.getFirstResponder(req);

        if (e instanceof CompositeValidationException) {
            CompositeValidationException v = (CompositeValidationException) e;
            req.setAttribute(MAKUMBA_FORM_VALIDATION_ERRORS, v);
            req.setAttribute(MAKUMBA_FORM_RELOAD, "true");

            String message;

            // Check if we shall reload the form page
            java.util.logging.Logger.getLogger("org.makumba.controller").fine(
                "Caught a CompositeValidationException, reloading form page: " + responder.getReloadFormOnError());

            if (responder.getReloadFormOnError()) {

                final String root = conf.getInitParameter(req.getServerName());

                httpServletObjects.setRequest(getFormReloadRequest(req, responder));
                httpServletObjects.setResponse(getFormReloadResponse(resp, root, responder));

                java.util.logging.Logger.getLogger("org.makumba.controller").fine(
                    "CompositeValidationException: annotating form: " + responder.getShowFormAnnotated());

                if (responder.getShowFormAnnotated()) {
                    java.util.logging.Logger.getLogger("org.makumba.controller").finer(
                        "Processing CompositeValidationException for annotation:\n" + v.toString());
                    // if the form shall be annotated, we need to filter which exceptions can be assigned to
                    // fields, and which not
                    ArrayList<InvalidValueException> unassignedExceptions = factory.getUnassignedExceptions(v,
                        (HttpServletRequest) req);
                    java.util.logging.Logger.getLogger("org.makumba.controller").finer(
                        "Exceptions not assigned:\n" + StringUtils.toString(unassignedExceptions));

                    // the messages left unassigned will be shown as the form response
                    message = "";
                    for (InvalidValueException invalidValueException : unassignedExceptions) {
                        InvalidValueException invEx = invalidValueException;
                        message += invEx.getMessage() + "<br>";
                    }
                } else {
                    message = v.toString();
                }

            } else { // we do not change the target page
                message = v.toString();
            }
            req.setAttribute(ResponderFactory.RESPONSE_STRING_NAME, message);
            req.setAttribute(ResponderFactory.RESPONSE_FORMATTED_STRING_NAME, Responder.errorMessageFormatter(message));

        }
        // if there was no error, and we have set to reload the form, we go to the original action page
        else if (e == null && responder != null && responder.getReloadFormOnError()) {
            // store the response in the session, to be able to retrieve it later in ResponseTag
            final HttpServletRequest httpServletRequest = (HttpServletRequest) req;
            HttpSession session = httpServletRequest.getSession();

            String action = getAbsolutePath(httpServletRequest.getRequestURI(), responder.getAction());

            final String suffix = "_" + action;
            session.setAttribute(ResponderFactory.RESPONSE_STRING_NAME + suffix, responder.message);
            session.setAttribute(ResponderFactory.RESPONSE_FORMATTED_STRING_NAME + suffix,
                Responder.successFulMessageFormatter(responder.message));

            // redirecting
            java.util.logging.Logger.getLogger("org.makumba.controller").info(
                "Sending redirect from '" + httpServletRequest.getRequestURI() + "' to '" + responder.getAction()
                        + "'.");
            ((HttpServletResponse) resp).sendRedirect(responder.getAction());
        }
        return true;
    }

    private static String getAbsolutePath(String requestURI, String action) throws IOException {
        if (!action.startsWith("/")) {
            // if we have a relative URL, compute the absolute URL of the action page, as we'll get that in ResponseTag
            if (requestURI.lastIndexOf("/") != -1) {
                requestURI = requestURI.substring(0, requestURI.lastIndexOf("/") + 1);
            }
            action = requestURI + action;
        }
        return new File("/", action).getCanonicalPath();
    }

    /**
     * Makes a response wrapper, using an anonymous inner class
     * 
     * @param resp
     *            the original response
     * @param root
     *            the server hostname
     * @param responder
     *            the form responder
     * @return a wrapped ServletResponse redirecting to the original page
     */
    private ServletResponse getFormReloadResponse(ServletResponse resp, final String root, Responder responder) {
        resp = new HttpServletResponseWrapper((HttpServletResponse) resp) {
            @Override
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
     * 
     * @param req
     *            the original request
     * @param responder
     *            the form responder
     * @return a wrapped ServletRequest containing information about the originally submitted page
     */
    private ServletRequest getFormReloadRequest(ServletRequest req, final Responder responder) {
        // 
        req = new HttpServletRequestWrapper((HttpServletRequest) req) {

            @Override
            public String getServletPath() {
                HttpServletRequest httpServletRequest = ((HttpServletRequest) getRequest());
                String originatingPage = responder.getOriginatingPageName();
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
             * We override this method so we can serve the previous request URI. This doesn't have any effect on the URI
             * displayed in the browser, but it makes submitting the form twice possible...
             */
            @Override
            public String getRequestURI() {
                String originatingPage = responder.getOriginatingPageName();
                return originatingPage;
            }
        };
        return req;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(getAbsolutePath("/test/forms-oql/abc.", "../action.jsp"));
        System.out.println(getAbsolutePath("/test/forms-oql/", "../action.jsp"));
        System.out.println(getAbsolutePath("/test/forms-oql/", "../forms-oql/action.jsp"));
        System.out.println(getAbsolutePath("/", "../forms-oql/action.jsp"));
        System.out.println(getAbsolutePath("", "../action.jsp"));
    }
}
