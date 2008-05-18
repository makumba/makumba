package org.makumba.forms.responder;

import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

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
    public boolean beforeFilter(ServletRequest req, ServletResponse resp, FilterConfig conf, ServletObjects httpServletObjects) throws Exception {

        Exception e = factory.getResponse((HttpServletRequest) req, (HttpServletResponse) resp);

        if (e instanceof CompositeValidationException) {
            CompositeValidationException v = (CompositeValidationException) e;
            req.setAttribute(MAKUMBA_FORM_VALIDATION_ERRORS, v);
            req.setAttribute(MAKUMBA_FORM_RELOAD, "true");

            String message;

            // Check if we shall reload the form page
            Responder firstResponder = factory.getFirstResponder(req);
            java.util.logging.Logger.getLogger("org.makumba." + "controller").fine(
                "Caught a CompositeValidationException, reloading form page: " + firstResponder.getReloadFormOnError());

            if (firstResponder.getReloadFormOnError()) {

                final String root = conf.getInitParameter(req.getServerName());

                httpServletObjects.setRequest(getFormReloadRequest(req));
                httpServletObjects.setResponse(getFormReloadResponse(resp, root));

                java.util.logging.Logger.getLogger("org.makumba." + "controller").fine(
                    "CompositeValidationException: annotating form: " + firstResponder.getShowFormAnnotated());

                if (firstResponder.getShowFormAnnotated()) {
                    java.util.logging.Logger.getLogger("org.makumba." + "controller").finer(
                        "Processing CompositeValidationException for annotation:\n" + v.toString());
                    // if the form shall be annotated, we need to filter which exceptions can be assigned to
                    // fields, and which not
                    ArrayList unassignedExceptions = factory.getUnassignedExceptions(v, (HttpServletRequest) req);
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

        }
        return true;
    }

    /**
     * Makes a response wrapper, using an anonymous inner class
     * 
     * @param resp
     *            the original response
     * @param root
     *            the server hostname
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
     * 
     * @param req
     *            the original request
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
             * We override this method so we can serve the previous request URI. This doesn't have any effect on the URI
             * displayed in the browser, but it makes submitting the form twice possible...
             */
            public String getRequestURI() {
                return getRequest().getParameter(Responder.originatingPageName);
            }
        };
        return req;
    }
}
