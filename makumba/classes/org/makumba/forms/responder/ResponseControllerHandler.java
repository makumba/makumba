package org.makumba.forms.responder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

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

    final Logger logger = java.util.logging.Logger.getLogger("org.makumba.controller");

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
            logger.fine("Caught a CompositeValidationException, reloading form page: "
                    + responder.getReloadFormOnError());

            if (responder.getReloadFormOnError()) {

                final String root = conf.getInitParameter(req.getServerName());

                httpServletObjects.setRequest(getFormReloadRequest(req, responder));
                httpServletObjects.setResponse(getFormReloadResponse(resp, root, responder));

                logger.fine("CompositeValidationException: annotating form: " + responder.getShowFormAnnotated());

                if (responder.getShowFormAnnotated()) {
                    logger.finer("Processing CompositeValidationException for annotation:\n" + v.toString());
                    // if the form shall be annotated, we need to filter which exceptions can be assigned to
                    // fields, and which not
                    ArrayList<InvalidValueException> unassignedExceptions = factory.getUnassignedExceptions(v,
                        (HttpServletRequest) req);
                    logger.finer("Exceptions not assigned:\n" + StringUtils.toString(unassignedExceptions));

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

            // FIXME
            // commented out for the moment, as changing the action page has some severe implications and side-effects
            // if page parameter names thus get accidentally defined twice
            // see FormResponder too
            //
            // } else if (e == null && responder != null) { // if there was no error, and we have a form responder
            // // check if we have set to reload the form, we go to the original action page
            // final HttpServletRequest httpServletRequest = (HttpServletRequest) req;
            // final String absoluteAction = StringUtils.getAbsolutePath(httpServletRequest.getRequestURI(),
            // responder.getAction());
            // final boolean shallReload = shallReload(responder.getReloadFormOnError(), responder.getAction(),
            // absoluteAction, responder.getOriginatingPageName());
            // logger.info("Form submission ok, operation: " + responder.operation + ", reloadForm: "
            // + responder.getReloadFormOnError() + ", will reload: " + shallReload);
            // logger.info("Originating page: '" + responder.getOriginatingPageName() + "', action page: '"
            // + responder.getAction() + "' (absolute: " + absoluteAction + "), equal: "
            // + responder.getOriginatingPageName().equals(absoluteAction));
            //
            // if (shallReload) {
            // // store the response attributes in the session, to be able to retrieve it later in RequestAttributes
            // HttpSession session = httpServletRequest.getSession();
            //
            // final String suffix = "_" + absoluteAction;
            // for (String attr : ResponderFactory.RESPONSE_ATTRIBUTE_NAMES) {
            // session.setAttribute(attr + suffix, req.getAttribute(attr));
            // logger.info("Setting '" + attr + suffix + "' value: '" + req.getAttribute(attr) + "'.");
            // }
            //
            // // redirecting
            // logger.info("Sending redirect from '" + httpServletRequest.getRequestURI() + "' to '"
            // + responder.getAction() + "'.");
            // ((HttpServletResponse) resp).sendRedirect(responder.getAction());
            // }
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

    public static boolean shallReload(boolean reloadFormOnError, String action, String absoluteAction,
            String originatingPageName) {
        return reloadFormOnError && org.apache.commons.lang.StringUtils.isNotBlank(action)
                && !originatingPageName.startsWith(absoluteAction);
    }

    public static void main(String[] args) throws IOException {
        System.out.println(StringUtils.getAbsolutePath("/test/forms-oql/abc.", "../action.jsp"));
        System.out.println(StringUtils.getAbsolutePath("/test/forms-oql/", "../action.jsp"));
        System.out.println(StringUtils.getAbsolutePath("/test/forms-oql/", "../forms-oql/action.jsp"));
        System.out.println(StringUtils.getAbsolutePath("/", "../forms-oql/action.jsp"));
        System.out.println(StringUtils.getAbsolutePath("", "../action.jsp"));
    }
}
