package org.makumba.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.makumba.CompositeValidationException;
import org.makumba.InvalidValueException;
import org.makumba.commons.ControllerHandler;
import org.makumba.commons.ServletObjects;
import org.makumba.forms.responder.ResponderFactory;
import org.makumba.forms.responder.ResponseControllerHandler;
import org.makumba.list.tags.SectionTag;

import com.google.gson.Gson;

/**
 * ControllerHandler that handles AJAX-related data writing<br/>
 * FIXME does not seem to work for multiple forms
 * 
 * @author Manuel Gay
 * @version $Id: ResponseModifierControllerHandler.java,v 1.1 Dec 25, 2009 10:05:55 PM manu Exp $
 */
public class AJAXDataControllerHandler extends ControllerHandler {

    final Logger logger = java.util.logging.Logger.getLogger("org.makumba.controller");

    Gson gson = new Gson();

    @Override
    public boolean beforeFilter(ServletRequest request, ServletResponse response, FilterConfig conf,
            ServletObjects httpServletObjects) throws Exception {

        return true;
    }

    @Override
    public void afterFilter(ServletRequest request, ServletResponse response, FilterConfig conf) {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        handleEvent(req, resp);
        handleFormPostback(req, resp);
    }

    private void handleEvent(HttpServletRequest req, HttpServletResponse response) {
        String event = req.getParameter(SectionTag.MAKUMBA_EVENT);

        if (event != null) {

            response.reset();
            response.setContentType("application/json");

            // fetch data from request context
            Map<String, String> data = new HashMap<String, String>();
            Enumeration<String> keys = req.getAttributeNames();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                if (key.startsWith(SectionTag.MAKUMBA_EVENT + "###" + event)) {
                    data.putAll((Map<String, String>) req.getAttribute(key));
                }
            }

            try {
                response.getWriter().append(gson.toJson(data));
                response.getWriter().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleFormPostback(HttpServletRequest req, HttpServletResponse response) {
        String partialPostback = (String) req.getAttribute(ResponseControllerHandler.MAKUMBA_FORM_PARTIAL_POSTBACK_EVENT);
        if (partialPostback != null) {
            logger.fine("partial form postback");

            CompositeValidationException v = (CompositeValidationException) req.getAttribute(ResponseControllerHandler.MAKUMBA_FORM_VALIDATION_ERRORS);
            // String message = (String) req.getAttribute(ResponderFactory.RESPONSE_STRING_NAME);
            String formattedMessage = (String) req.getAttribute(ResponderFactory.RESPONSE_FORMATTED_STRING_NAME);
            String formName = (String) req.getAttribute(ResponseControllerHandler.MAKUMBA_FORM_ID);

            try {

                if (v != null) {
                    // we need to respond to the client and give it all the information needed to display the errors in
                    // the form
                    // - the composite validation exception
                    // - the form message
                    // for the first one we do something a bit hackish, i.e. we use the parameters of the serialized
                    // forms to get all the inputs

                    HashMap<String, Object> o = new HashMap<String, Object>();
                    HashMap<Object, ArrayList<String>> fieldErrors = new HashMap<Object, ArrayList<String>>();
                    Enumeration<String> params = req.getParameterNames();
                    while (params.hasMoreElements()) {
                        String param = params.nextElement();
                        if (v.getExceptions(param) != null) {
                            Collection<InvalidValueException> paramFieldErrors = v.getExceptions(param);
                            ArrayList<String> errors = new ArrayList<String>();
                            // store the message with the input ID as key
                            // TODO not sure if this works with multiple forms
                            fieldErrors.put(param + formName, errors);
                            for (InvalidValueException ive : paramFieldErrors) {
                                errors.add(ive.getShortMessage());
                            }
                        }
                    }

                    o.put("fieldErrors", fieldErrors);
                    // TODO we might want to pass something more elaborate than the message, e.g. a collection of errors
                    o.put("message", formattedMessage);
                    response.reset();
                    response.setContentType("application/json");
                    logger.fine("writing error information: " + gson.toJson(o));
                    response.getWriter().print(gson.toJson(o));
                    response.getWriter().flush();
                } else {
                    // respond by giving the name of the event
                    HashMap<String, String> o = new HashMap<String, String>();
                    o.put("event", partialPostback);
                    o.put("message", formattedMessage);
                    // FIXME this does not seem to work for certain form submissions such as multiple forms, the
                    // response seems to have been
                    // partly written already before we could do anything
                    response.reset();
                    response.setContentType("application/json");
                    logger.fine("writing event: " + gson.toJson(o));
                    response.getWriter().print(gson.toJson(o));
                    response.getWriter().flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
