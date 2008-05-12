package org.makumba.forms.responder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
import org.makumba.CompositeValidationException;
import org.makumba.InvalidValueException;
import org.makumba.LogicException;
import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.commons.attributes.RequestAttributes;
import org.makumba.controller.http.ControllerFilter;
import org.makumba.providers.TransactionProvider;
import org.makumba.providers.TransactionProviderInterface;

/**
 * This factory handles the creation, caching and retrieval of Responder objects.
 * 
 * @author Manuel Gay
 * @version $Id: ResponderFactory.java,v 1.1 12.10.2007 13:17:45 Manuel Exp $
 */
public class ResponderFactory {

    private ResponderCacheManager cacheManager = ResponderCacheManager.getInstance();

    private static ResponderFactory instance = null;

    public static ResponderFactory getInstance() {
        if (instance == null) {
            instance = new ResponderFactory();
            instance.init();
        }
        return instance;
    }

    /**
     * Initalises the factory
     */
    private void init() {
        cacheManager.setFactory(this);
    }

    /**
     * Reads all responder codes from a request (all code_suffix values of __mak__responder__).
     * 
     * @param req
     *            the request in which we currently are
     * @return the enumeration of responder codes
     */
    public Iterator<String> getResponderCodes(HttpServletRequest req) {
        TreeSet<String> set = new TreeSet<String>(bySuffix);
        
        Object o = RequestAttributes.getParameters(req).getParameter(Responder.responderName);
        if (o != null) {
            if (o instanceof String)
                set.add((String)o);
            else
                set.addAll((Vector) o);
        }
        
         
        return set.iterator();
    }
    
    /**
     * Returns the responders in the page in a topological order
     * @param req the request corresponding to the current page
     * @return an Iterator iterating over a sorted array of responder codes
     */
    public Iterator<String> getOrderedResponderCodes(HttpServletRequest req) {
        
        // let's fetch the List containing the order of the forms in the page
        // for this we need to fetch a root form responder
        
        Iterator<String> responderCodes = getResponderCodes(req);
        //Map<MultipleKey, String> formKeyToResponderCode = new HashMap<MultipleKey, String>();
        String[] order = null;
        
        while(responderCodes.hasNext()) {
            String responderCode = responderCodes.next();
            if(responderCode == null) {
                continue;
            }
            Responder responder = getResponder(responderCode);
            if(responder.getFormOrder() != null) {
                order = responder.getFormOrder();
            } 
        }
        
        /* now we can order the responders
        List<String> orderedResponderCodes = new LinkedList<String>();
        
        if(order != null ) {
            
            for(int i = 0; i < order.length; i++) {
                if(order[i] != null)
                    orderedResponderCodes.add(formKeyToResponderCode.get(order[i]));
            }
        }
        */
        if(order != null)
            return Arrays.asList(order).iterator();
        else return new ArrayList<String>().iterator();
        
    }
    
    public void printOrderedResponders(HttpServletRequest req) {
        System.out.println("\nresponders ordered:");
        printResponderIterator(getOrderedResponderCodes(req));
        System.out.println("\nresponders normal:");
        printResponderIterator(getResponderCodes(req));
    }

    private void printResponderIterator(Iterator<String> order) {
        if(order == null)
            return;
        while(order.hasNext()) {
            String code = order.next();
            System.out.println("** responder code: "+code);
            if(code == null) break;
            Responder r = getResponder(code);
            System.out.println("** responder form name: "+r.getFormName());
            System.out.println("** responder form key:  "+r.responderKey());
        }
    }

    /**
     * Simple comparator to be able to sort by suffix
     */
    private Comparator<Object> bySuffix = new Comparator<Object>() {
        public int compare(Object o1, Object o2) {
            return suffix((String) o1).compareTo(suffix((String) o2));
        }

        public boolean equals(Object o) {
            return false;
        }
    };

    /**
     * Given a responder code, extracts suffix and parentSuffix
     * 
     * @param responderCode
     *            the responder code
     * @return a String[] containing the suffix as first element and the parentSuffix as second element FIXME maybe this
     *         goes just 2 levels, so forms in forms in forms aren't working?
     */
    public String[] getSuffixes(String responderCode) {
        String suffix = "";
        String parentSuffix = null;
        int n = responderCode.indexOf(Responder.suffixSeparator);
        if (n != -1) {
            suffix = responderCode.substring(n);
            parentSuffix = "";
            n = suffix.indexOf(Responder.suffixSeparator, 1);
            if (n != -1) {
                parentSuffix = suffix.substring(n);
                suffix = suffix.substring(0, n);
            }
        }
        return new String[] { suffix, parentSuffix };
    }

    /**
     * Given a responder code, extracts the suffix
     * 
     * @param code
     *            the responder code
     * @return the responder suffix, ZERO if none found
     * 
     * FIXME maybe this goes just 2 levels, so forms in forms in forms
     * aren't working?
     */
    private Integer suffix(String code) {
        int n = code.indexOf(Responder.suffixSeparator);
        if (n == -1)
            return ZERO;
        code = code.substring(n + 1);
        n = code.indexOf(Responder.suffixSeparator);
        if (n != -1)
            code = code.substring(0, n);
        return new Integer(Integer.parseInt(code));
    }

    static Integer ZERO = new Integer(0);

    public void setResponderWorkingDir(HttpServletRequest request) {
        cacheManager.setResponderWorkingDir(request);
    }

    /**
     * Returns a responder based on its code
     * 
     * @param code
     *            the code that identifies one form in one page
     * @return the {@link Responder} corresponding to the code, fetched from the cache
     */
    public Responder getResponder(String code) {
        String suffix = getSuffixes(code)[0];
        String parentSuffix = getSuffixes(code)[1];
        if(suffix != "")
            code = code.substring(0, code.indexOf(suffix));
        return cacheManager.getResponder(code, suffix, parentSuffix);
    }

    /**
     * Returns the first responder object found fitting the request. It can be used to retrieve information about the
     * form which is valid for all nested forms, and is used e.g. in {@link ControllerFilter} to find out the value of
     * {@link #getReloadFormOnError()}.
     * 
     * @param req
     *            the current request
     * @return the first responder fitting the request.
     */
    public Responder getFirstResponder(ServletRequest req) {
        Iterator responderCodes = getResponderCodes((HttpServletRequest) req);
        if (responderCodes.hasNext()) {
            String code = (String) responderCodes.next();
            String suffix = "";
            String parentSuffix = null;
            int n = code.indexOf(Responder.suffixSeparator);
            if (n != -1) {
                suffix = code.substring(n);
                parentSuffix = "";
                n = suffix.indexOf(Responder.suffixSeparator, 1);
                if (n != -1) {
                    parentSuffix = suffix.substring(n);
                    suffix = suffix.substring(0, n);
                }
            }
            return getResponder(code);
        } else {
            return null;
        }
    }

    /**
     * For all the (nested) forms of a page, gives all the errors which have not been assigned to a specific field.
     * 
     * @param e
     *            the {@link CompositeValidationException} holding the errors
     * @param req
     *            the request corresponding to the current page
     * @return an ArrayList containing all the unassigned exceptions
     */
    public ArrayList<InvalidValueException> getUnassignedExceptions(CompositeValidationException e, HttpServletRequest req) {
        ArrayList<InvalidValueException> unassignedExceptions = e.getExceptions();
        for (Iterator<String> responderCodes = getResponderCodes(req); responderCodes.hasNext();) {
            String responderCode = responderCodes.next();
            String[] suffixes = getSuffixes(responderCode);
            getResponder(responderCode).getUnassignedExceptions(e, unassignedExceptions, suffixes[0]);
        }
        return unassignedExceptions;
    }

    /**
     * Creates a new empty FormResponder, to be used at form computation time.
     * 
     * @return a new empty {@link FormResponder}
     */
    public FormResponder createResponder() {
        FormResponder fr = new FormResponder();
        fr.setFactory(this);
        return fr;
    }

    /**
     * Based on its instance, returns the key of the responder
     * 
     * @param responder
     *            the Responder object of which we want to get the key
     * @return the unique key identifiying a specific instance of a Responder
     */
    public int getResponderIdentity(Responder responder) {
        return ((Responder) ResponderCacheManager.cache.getResource(responder)).identity;
    }

    static public final String RESPONSE_STRING_NAME = "makumba.response";

    public static final String resultNamePrefix = "org.makumba.controller.resultOf_";

    /**
     * Should compute the {@link Response} based on all the responders of one page, but for now just computes an
     * Exception
     * 
     * FIXME this code is not taking into account multiple forms: it iterates through all the responders of a
     * page, but directly treats the exception of the first form responder, which means that errors in the nested forms
     * are ignored. this should be fixed, in doing something like this:
     * - iterate through all the forms, extract the form hierarchy and start processing forms in order of appearance
     * - for each form responder, store the message, errors, request and response (containing modified attributes) into
     *   a Response object
     * - generate a CompositeResponse object that holds all the errors, messages etc in the right order (or just pass
     *   an ArrayList of Response objects)
     * - the controller should then treat the responses and exceptions starting by the inner forms (otherwise errors get ignored)
     * 
     * @param req
     *            the {@link HttpServletRequest} corresponding to the current page
     * @param resp
     *            the {@link HttpServletRequest} corresponding to the current page
     * @return a response object holding all necessary information for the {@link ControllerFilter}
     */
    public Exception getResponse(HttpServletRequest req, HttpServletResponse resp) {
        
        setResponderWorkingDir(req);

        if (req.getAttribute(RESPONSE_STRING_NAME) != null)
            return null;
        req.setAttribute(RESPONSE_STRING_NAME, "");
        String message = "";
        
        //printOrderedResponders(req);

        // store the results from each responder, needed for nested new/add forms wanting to refer to newly created objects
        Hashtable<String, Object> responderResults = new  Hashtable<String, Object>();
        
        // we go over all the responders of this page (hold in the request)
        for (Iterator<String> responderCodes = getOrderedResponderCodes(req); responderCodes.hasNext();) {

            // first we need to retrieve the responder from the cache
            String code = responderCodes.next();
            String suffix = getSuffixes(code)[0];
            String parentSuffix = getSuffixes(code)[1];
            Responder formResponder = getResponder(code);

            try {
                checkMultipleSubmission(req, formResponder);
                // respond, depending on the operation (new, add, edit, delete)
                Object result = formResponder.op.respondTo(req, formResponder, suffix, parentSuffix);
                if (formResponder instanceof FormResponder) {
                    // FIXME: what to do if responder is not a form responder? pull up the result attribute field to
                    // responder?
                    if(result!=null)
                        responderResults.put(((FormResponder) formResponder).resultAttribute, result);
                }
                // display the response message and set attributes
                message = "<font color=green>" + formResponder.message + "</font>";
                if (result != null) {
                    req.setAttribute(formResponder.resultAttribute, result);
                    req.setAttribute(resultNamePrefix + suffix, result);
                }
                req.setAttribute("makumba.successfulResponse", "yes");

            } catch (AttributeNotFoundException anfe) {
                // attribute not found is a programmer error and is reported
                throw new RuntimeWrappedException(anfe);
            } catch (CompositeValidationException e) {
                req.setAttribute(formResponder.resultAttribute, Pointer.Null);
                req.setAttribute(resultNamePrefix + suffix, Pointer.Null);
                // we do nothing, cause we will treat that from the ControllerFilter.doFilter
                return e;
            } catch (LogicException e) {
                java.util.logging.Logger.getLogger("org.makumba." + "logic.error").log(Level.INFO, "error", e);
                message = Responder.errorMessage(e);
                req.setAttribute(formResponder.resultAttribute, Pointer.Null);
                req.setAttribute(resultNamePrefix + suffix, Pointer.Null);
            } catch (Throwable t) {
                // all included error types should be considered here
                throw new RuntimeWrappedException(t);
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
        String reqFormSession = (String) RequestAttributes.getParameters(req).getParameter(Responder.formSessionName);
        if (fr.multipleSubmitErrorMsg != null && !fr.multipleSubmitErrorMsg.equals("") && reqFormSession != null) {
            Transaction db = null;
            try {
                db = TransactionProvider.getInstance().getConnectionTo(RequestAttributes.getAttributes(req).getRequestDatabase());

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

}
