package org.makumba.devel;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.makumba.ForeignKeyError;
import org.makumba.LogicException;
import org.makumba.LogicInvocationError;
import org.makumba.MakumbaError;
import org.makumba.OQLParseError;
import org.makumba.Transaction;
import org.makumba.analyser.AnalysableElement;
import org.makumba.analyser.ELData;
import org.makumba.analyser.ElementData;
import org.makumba.analyser.TagData;
import org.makumba.analyser.engine.JspParseData;
import org.makumba.commons.DbConnectionProvider;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.commons.attributes.RequestAttributes;
import org.makumba.providers.Configuration;

/**
 * The class that performs exception handling. Receives errors in any makumba page and treats them meant to be friendly
 * developer, so he'll see helpful stuff during development and not stupid stacktraces (which are shown only in case of
 * unknown exceptions) also indented for intercepting lack of authorization and show login pages. Most of the code was
 * copied from the TagExceptionServlet.<br>
 * FIXME the exception hierarchy needs to be reviewed.<br>
 * 
 * @author Cristian Bogdan
 * @author Stefan Baebler
 * @author Rudolf Mayer
 * @author Filip Kis
 * @version $Id: ControllerFilter.java 1785 2007-10-12 14:41:55Z manuel_gay $ *
 */

public class ErrorFormatter {

    static Object errors[][] = { { org.makumba.OQLParseError.class, "query" },
            { org.makumba.DataDefinitionNotFoundError.class, "data definition not found" },
            { org.makumba.DataDefinitionParseError.class, "data definition parse" },
            { org.makumba.ValidationDefinitionParseError.class, "validation definition parse" },
            { org.makumba.DBError.class, "database" }, { org.makumba.ConfigurationError.class, "configuration" },
            { org.makumba.ProgrammerError.class, "programmer" },
            { org.makumba.list.tags.MakumbaJspException.class, "page" },
            { org.makumba.AttributeNotFoundException.class, "attribute not set" },
            { org.makumba.UnauthorizedException.class, "authorization" },
            { org.makumba.InvalidValueException.class, "invalid value" },
            { org.makumba.InvalidFieldTypeException.class, "invalid field type" },
            { org.makumba.NoSuchFieldException.class, "no such field" },
            { org.makumba.NoSuchLabelException.class, "no such label" },
            { org.makumba.LogicException.class, "business logic" },
            { org.makumba.list.tags.MakumbaELException.class, "expression language"} };

    static final Class<?>[] knownJSPruntimeErrors = { ArrayIndexOutOfBoundsException.class,
            NumberFormatException.class, ClassCastException.class, javax.el.ELException.class };

    protected ServletContext servletContext;

    private String title = "";

    protected boolean printeHeaderFooter;

    public String getTitle() {
        return this.title;
    }

    public ErrorFormatter() {
    }

    public ErrorFormatter(HttpServletRequest req, ServletContext servletContext, PrintWriter wr,
            boolean printHeaderFooter) throws IOException, ServletException {

        this.printeHeaderFooter = printHeaderFooter;
        this.servletContext = servletContext;

        Throwable t = (Throwable) req.getAttribute(javax.servlet.jsp.PageContext.EXCEPTION);
        Throwable t1 = null;

        Throwable original = t;

        // sometimes tomcat wraps the exception in a JasperException (which extends ServletException) and then again in
        // a ServletException
        if (t.getClass().getSuperclass().isAssignableFrom(ServletException.class)
                && ((ServletException) t).getRootCause() != null
                && ((ServletException) t).getRootCause().getClass().isAssignableFrom(ServletException.class)
                && t.getMessage().startsWith("Exception in JSP:")) {
            t = ((ServletException) ((ServletException) t).getRootCause()).getRootCause();
        }

        while (true) {
            if (t instanceof LogicException) {
                t1 = ((LogicException) t).getReason();
            } else if (t instanceof MakumbaError && !(t instanceof OQLParseError)) {
                t1 = ((MakumbaError) t).getCause();
            } else if (t instanceof LogicInvocationError) {
                t1 = ((LogicInvocationError) t).getReason();
            } else if (t instanceof RuntimeWrappedException) {
                t1 = ((RuntimeWrappedException) t).getCause();
            } else if (t instanceof ServletException
                    && ((ServletException) t).getRootCause() instanceof NullPointerException) {
                // handle null pointer exception seperate, as otherwise tomcat 5.0.x reports only "null" as message, and
                // no stacktrace.
                ServletException e = (ServletException) t;
                t1 = e.getRootCause();
                t1.setStackTrace(e.getRootCause().getStackTrace());
            } else if (t instanceof ServletException) {
                t1 = ((ServletException) t).getRootCause();
            } else {
                break;
            }
            if (t1 == null) {
                break;
            }
            t = t1;
        }
        logError(t, req);

        if (t.getClass().getName().startsWith(org.makumba.analyser.engine.TomcatJsp.getJspCompilerPackage())) {
            boolean jspSpecificError = treatJspException(original, t, wr, req, this.servletContext, printHeaderFooter,
                title);

            if (!jspSpecificError) {
                // FIXME the code below of trying to get more info about the exception is not really accurate
                // ==> not every JSP exception is a compilation error!! i.e. many runtime exceptions are wrapped into
                // JSP exceptions
                // ==> as a quick fix, we treat those as unknown errors
                if (isRuntimeJspErrors((ServletException) t)) {
                    treatJspRuntimeException(original, (ServletException) t, wr, req, this.servletContext,
                        printHeaderFooter);
                    return;
                } else {
                    Throwable rootCause = ((ServletException) t).getRootCause();
                    // FIXME this is most probably tomcat-specific
                    /*
                     * after JSP compilation, the actual JSP problem is in getRootCause, yet the exception is also very
                     * informative (includes code context). So we need to show both.
                     */
                    if (rootCause != null) {
                        t = rootCause;
                        if (t != null && original.getMessage() != null && !original.getMessage().equals(t.getMessage())) {
                            try {
                                // make new throwable, but keep original class just in case
                                // need to use reflection - no methods to clone or set message available in Throwable
                                String message = t.getMessage() + "\n\n" + original.getMessage();
                                t1 = (Throwable) t.getClass().getConstructor(String.class).newInstance(message);
                                t1.setStackTrace(t.getStackTrace());
                                t = t1;
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    title = "JSP Compilation error";
                    for (Object[] element : errors) {
                        if ((((Class<?>) element[0])).isInstance(t) || t1 != null
                                && (((Class<?>) element[0])).isInstance(t = t1)) {
                            title = "Makumba " + element[1] + " error";
                        }
                    }
                    knownError(title, t, original, req, wr);
                }

            }
            return;
        } else if (original.getClass().getName().startsWith(
            org.makumba.analyser.engine.TomcatJsp.getJspCompilerPackage())) {
            // FIXME this is most probably tomcat-specific
            // FIXME optimise this duplicate code
            /*
             * after JSP compilation, the actual JSP problem is in getRootCause, yet the exception is also very
             * informative (includes code context). So we need to show both.
             */
            if (t != null && original.getMessage() != null && !original.getMessage().equals(t.getMessage())) {
                try {
                    // make new throwable, but keep original class to preserve as much as possible
                    // need to use reflection - no methods to clone or set message available in Throwable class.
                    String message = t.getMessage() + "\n\n" + original.getMessage();
                    t1 = (Throwable) t.getClass().getConstructor(String.class).newInstance(message);
                    t1.setStackTrace(t.getStackTrace());
                } catch (Throwable e) {
                    // if we do not find a standard constructor, make a new throwable
                    t1 = new Throwable(t.getMessage() + "\n\n" + original.getMessage());
                    t1.setStackTrace(t.getStackTrace());
                }
                t = t1;
            }
            
            // FIXME this is duplicated code from above to get this working with Jetty.
            // the problem is that tomcat has a specific way of wrapping exceptions, which jetty does not because it direclty uses Jasper
            if(isRuntimeJspErrors((ServletException)original)) {
                treatJspRuntimeException(original, t, wr, req, this.servletContext,
                    printHeaderFooter);
                return;

            }
            
        } else if(isRuntimeJspErrors(t)) {
            // Jetty throws Runtime Exceptions in a different way thant Tomcat
            treatJspRuntimeException(original, t, wr, req, this.servletContext, printHeaderFooter);
            return;
        }
        

        for (Object[] element : errors) {
            if ((((Class<?>) element[0])).isInstance(t) || t1 != null && (((Class<?>) element[0])).isInstance(t = t1)) {
                title = "Makumba " + element[1] + " error";
                knownError(title, t, original, req, wr);
                return;
            }
        }
        unknownError(original, t, wr, req);
    }

    private boolean isRuntimeJspErrors(Throwable e) {
        
        if(e instanceof ServletException) {
            ServletException t1 = (ServletException) e;
            if (t1.getRootCause() != null) {
                for (Class<?> element : knownJSPruntimeErrors) {
                    if (t1.getRootCause().getClass().isAssignableFrom(element)) {
                        return true;
                    }
                }
            }

        } else {
            for (Class<?> element : knownJSPruntimeErrors) {
                if (e.getClass().isAssignableFrom(element)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * Stores the error details to the database (ErrorLog.mdd)
     * 
     * @param t
     *            the exception
     * @param req
     *            the http request corresponding to the access
     */

    public void logError(Throwable t, HttpServletRequest req) {
        
        //we only log if this is configured
        if(Configuration.getErrorLog()) {
        
            try{
            // we re-use the transaction provider of the request to do our logging
            DbConnectionProvider dbc = (DbConnectionProvider) req.getAttribute(RequestAttributes.PROVIDER_ATTRIBUTE);
            Transaction tr = dbc.getTransactionProvider().getConnectionTo(
                dbc.getTransactionProvider().getDefaultDataSourceName());
    
            try {
                Dictionary<String, Object> d = new Hashtable<String, Object>();
    
                // TODO: read and store the source of the submited page
                // d.put("page", "");
                if (t != null && t.getMessage() != null) {
                    d.put("exception", t.getMessage());
                }
                d.put("executionDate", new Date());
                d.put("url", req.getRequestURL().toString());
                if (req.getAttribute("makumba.parameters") != null) {
                    d.put("makumbaParameters", req.getAttribute("makumba.parameters").toString());
                }
                if (req.getAttribute(RequestAttributes.ATTRIBUTES_NAME) != null) {
                    d.put("makumbaAttributes", req.getAttribute(RequestAttributes.ATTRIBUTES_NAME).toString());
                }
                if (req.getAttribute("makumba.controller") != null) {
                    d.put("makumbaController", req.getAttribute("makumba.controller").toString());
                }
    
                tr.insert("org.makumba.controller.ErrorLog", d);
            } finally {
                    tr.close();
            }
            }catch (Throwable t1) {
                java.util.logging.Logger.getLogger("org.makumba.errorFormatter").log(
                    java.util.logging.Level.SEVERE,
                    "Could not log exception to the db, exception to log was", t);
                java.util.logging.Logger.getLogger("org.makumba.errorFormatter").log(
                    java.util.logging.Level.SEVERE,
                    "Could not log exception to the db, database logging exception was", t1);
            }
        }
    }

    /**
     * Displays a knows error in the case of an error originating from a tag
     * 
     * @param title
     *            title describing the error
     * @param t
     *            the exception
     * @param original
     *            the original error exception, not treated
     * @param req
     *            the http request corresponding to the access
     * @param wr
     *            the PrintWriter on which the error is printed
     */
    void knownError(String title, Throwable t, Throwable original, HttpServletRequest req, PrintWriter wr) {
        String trcOrig = trace(t);
        String trc = shortTrace(trcOrig);
        String body = t.getMessage();
        String hiddenBody = null;

        // we check whether this exception is a logic exception thrown at controller, or is a foreign key error
        if (((t instanceof LogicException && ((LogicException) t).isControllerOriginated()) || t instanceof ForeignKeyError)
                && findNonMakumbaRootCause(t) != -1) {
            // TODO: // maybe this should not be just for logic exception and foreign key error, but for everything in general?
            int i = findNonMakumbaRootCause(t);
            body = "Exception occured at " + t.getStackTrace()[i].getClassName() + "."
                    + t.getStackTrace()[i].getMethodName() + ":" + t.getStackTrace()[i].getLineNumber() + "\n\n" + body;
        } else {
            
            // TODO we could improve the error message for specific errors, e.g. if a MakumbaQueryError occurs during analysis of a mak:value
            // or a EL value expr, we could also display the query tag in order to help the developer
            
            body = formatElementData(req) + body;
        }

        if (original instanceof LogicInvocationError || trcOrig.indexOf("at org.makumba.abstr.Logic") != -1) {
            body = body + "\n\n" + trc;
        } else {
            hiddenBody = trc;
        }
        try {
            SourceViewer sw = new errorViewer(req, servletContext, title, body, hiddenBody, printeHeaderFooter);
            sw.parseText(wr);
        } catch (IOException e) {
            e.printStackTrace();
            throw new org.makumba.commons.RuntimeWrappedException(e);
        }
    }

    private int findNonMakumbaRootCause(Throwable t) {
        boolean foundRootCause = false;
        int i = 0;
        while (!foundRootCause && i < t.getStackTrace().length) {
            if (t.getStackTrace()[i].getClassName().indexOf("org.makumba") == -1) {
                foundRootCause = true;
            } else {
                i++;
            }
        }
        if (foundRootCause) {
            return i;
        } else {
            return -1;
        }
    }

    /**
     * Displays information about the element (tag or EL expression) in which the error occurs in a nice way
     * 
     * @param req
     *            the http request corresponding to the current access
     * @return The element error, nicely displayed
     */
    String formatElementData(HttpServletRequest req) {
        
        String explanation = new String();
        ElementData data = null;
        
        // try to figure out where we are
        // first try analysis, then running, then body tag
        
        // analysis
        data = AnalysableElement.getAnalyzedElementData();
        if(data != null) {
            if(data instanceof TagData) {
                explanation = "During analysis of the following tag (and possibly tags around or inside it):";
            } else if(data instanceof ELData) {
                explanation = "During analysis of the following EL expression (and possibly tags around it):";
            }
        } else {
            // runtime
            data = AnalysableElement.getRunningElementData();
            if(data != null) {
                explanation = "During running of:";
            } else {
                // body tag - fetch the data of the first surrounding tag
                data = AnalysableElement.getCurrentBodyTagData();
                if(data != null) {
                    explanation = "While executing inside this body tag, but most probably *not* due to the tag:";
                }
            }
        }
        
        if (data == null) {
            String filePath = req.getRequestURL().toString();
            try {
                String serverName = req.getLocalName() + ":" + req.getLocalPort() + req.getContextPath();
                if (filePath.indexOf(serverName) != -1) {
                    filePath = filePath.substring(filePath.indexOf(serverName) + serverName.length());
                }
            } catch (Exception e) { // if some error occurs during the string parsing
                filePath = req.getRequestURL().toString(); // --> we just present the whole request URL
            }
            return explanation = "While executing page " + filePath + "\n\n";
        }
        
        StringBuffer sb = new StringBuffer();
        try {
            JspParseData.tagDataLine(data, sb);
        } catch (Throwable t) {
            // ignore source code retrieving bugs
            t.printStackTrace();
        }
        String tagLine = sb.toString();
        String filePath;
        try {
            filePath = "/"
                    + data.getSourceSyntaxPoints().getFile().getAbsolutePath().substring(
                        req.getSession().getServletContext().getRealPath("/").length());
        } catch (Exception e) { // we might not have a servlet context available
            filePath = data.getSourceSyntaxPoints().getFile().getAbsolutePath();
        }
        return explanation + filePath + ":" + data.getStartLine() + ":" + data.getStartColumn() + ":"
                + data.getEndLine() + ":" + data.getEndColumn() + "\n" + tagLine + "\n\n";

    }

    /**
     * Prints the stacktrace
     * 
     * @param t
     *            the exception
     * @return A String holding the stacktrace
     */
    String trace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    /**
     * Filters out a short part of the stacktrace
     * 
     * @param s
     *            the stacktrace to be filtered
     * @return A short version of the stacktrace, meaning only the beginning
     */
    String shortTrace(String s) {
        int i = s.indexOf("at org.makumba.controller.Logic");
        if (i != -1) {
            s = s.substring(0, i);
            i = s.indexOf("at sun.reflect");
            if (i != -1) {
                s = s.substring(0, i);
            }
        } else {
            i = s.indexOf("at javax.servlet.http.HttpServlet.service(HttpServlet.java");
            if (i != -1) {
                s = s.substring(0, i);
            } else {
                i = s.indexOf("at org.makumba.controller.http.ControllerFilter.doFilter(ControllerFilter.java");
                if (i != -1) {
                    s = s.substring(0, i);
                } else {
                    java.util.logging.Logger.getLogger("org.makumba.devel").severe(
                        "servlet or filter call not found in stacktrace");
                }
            }

        }
        return s;
    }

    /**
     * Displays an unknown error
     * 
     * @param original
     *            the original error exception, not treated
     * @param t
     *            the exception
     * @param wr
     *            the PrintWriter on which the error is printed
     * @param req
     *            the http request corresponding to the access
     * @throws IOException
     * @throws ServletException
     */
    void unknownError(Throwable original, Throwable t, PrintWriter wr, HttpServletRequest req) throws IOException,
            ServletException {
        Throwable traced = t;
        title = "";
        String body = "";
        if (original instanceof LogicInvocationError) {
            title = "Error in business logic code";
        } else if (traced instanceof NullPointerException) {
            title = "Null pointer exception";
            body = "Please report to the developers.\n\n";
        } else if (trace(traced).indexOf("org.makumba") != -1) {
            title = "Internal Makumba error";
            body = "Please report to the developers.\n";
            if (t instanceof ServletException) {
                traced = ((ServletException) t).getRootCause();
                // maybe there's no root cause...
                if (traced == null) {
                    traced = t;
                }
            }
        } else {
            title = "Error in JSP Java scriplet or servlet container";
        }

        if (traced instanceof java.sql.SQLException) {
            title = "SQL " + title;
            body = "The problem is related to SQL:\n" + "    SQLstate: "
                    + ((java.sql.SQLException) traced).getSQLState() + "\n" + "  ErrorCode: "
                    + ((java.sql.SQLException) traced).getErrorCode() + "\n" + "     Message: " + traced.getMessage()
                    + "\n\n" + "Refer to your SQL server\'s documentation for error explanation.\n"
                    + "Please check the configuration of your webapp and SQL server.\n" + body;
        }
        // if (!(traced instanceof NullPointerException)) {
        body = formatElementData(req) + body + shortTrace(trace(traced));

        try {
            SourceViewer sw = new errorViewer(req, servletContext, title, body, trace(traced), printeHeaderFooter);
            sw.parseText(wr);
        } catch (IOException e) {
            e.printStackTrace();
            throw new org.makumba.commons.RuntimeWrappedException(e);
        }

    }

    /**
     * Returns a string describing the error that occurred.
     * 
     * @param req
     * @return the described error
     */
    public String getErrorMessage(HttpServletRequest req) {
        Throwable t = (Throwable) req.getAttribute(javax.servlet.jsp.PageContext.EXCEPTION);
        Throwable t1 = null;
        Throwable original = t;

        while (true) {
            if (t instanceof LogicException) {
                t1 = ((LogicException) t).getReason();
            } else if (t instanceof MakumbaError && !(t instanceof OQLParseError)) {
                t1 = ((MakumbaError) t).getCause();
            } else if (t instanceof LogicInvocationError) {
                t1 = ((LogicInvocationError) t).getReason();
            } else if (t instanceof RuntimeWrappedException) {
                t1 = ((RuntimeWrappedException) t).getCause();
            } else if (t instanceof ServletException
                    && ((ServletException) t).getRootCause() instanceof NullPointerException) {
                // handle null pointer exception seperate, as otherwise tomcat 5.0.x reports only "null" as message, and
                // no stacktrace.
                ServletException e = (ServletException) t;
                t1 = e.getRootCause();
                t1.setStackTrace(e.getRootCause().getStackTrace());
            } else {
                break;
            }
            if (t1 == null) {
                break;
            }
            t = t1;
        }

        logError(t, req);

        if (t.getClass().getName().startsWith(org.makumba.analyser.engine.TomcatJsp.getJspCompilerPackage())) {
            return "JSP compilation error:\n" + formatElementData(req) + t.getMessage();
        }
        for (Object[] element : errors) {
            if ((((Class<?>) element[0])).isInstance(t) || t1 != null && (((Class<?>) element[0])).isInstance(t = t1)) {
                return "Makumba " + element[1] + " error:\n" + formatElementData(req) + t.getMessage();
            }
        }
        return unknownErrorMessage(original, t, req);
    }

    /**
     * Returns a string describing an unknown error. TODO: this code is more or less a copy of unknownError() --> could
     * be optimised
     * 
     * @param original
     * @param t
     * @return
     */
    String unknownErrorMessage(Throwable original, Throwable t, HttpServletRequest req) {
        System.out.println("unknown message:");
        Throwable traced = t;
        title = "";
        String body = "";
        if (original instanceof LogicInvocationError) {
            title = "Error in business logic code";
        } else if (trace(traced).indexOf("org.makumba") != -1) {
            title = "Internal Makumba error";
            body = "Please report to the developers.\n\n";
            if (t instanceof ServletException) {
                traced = ((ServletException) t).getRootCause();
                // maybe there's no root cause...
                if (traced == null) {
                    traced = t;
                }
            }
        } else {
            title = "Error in JSP Java scriplet or servlet container";
        }
        if (traced instanceof java.sql.SQLException) {
            title = "SQL " + title;
            body = "The problem is related to SQL:\n" + "   SQLstate: "
                    + ((java.sql.SQLException) traced).getSQLState() + "\n" + "  ErrorCode: "
                    + ((java.sql.SQLException) traced).getErrorCode() + "\n" + "    Message: " + traced.getMessage()
                    + "\n\n" + "Refer to your SQL server\'s documentation for error explanation.\n"
                    + "Please check the configuration of your webapp and SQL server.\n" + body;
        }
        return title + ":\n" + formatElementData(req) + body + shortTrace(trace(traced), 10);
    }

    /**
     * Cuts down a stack trace to the given number of lines.
     * 
     * @param s
     *            a stacktrace as string.
     * @param lineNumbers
     *            the number of lines to be displayed.
     * @return the cut down stack trace.
     */
    String shortTrace(String s, int lineNumbers) {
        String[] parts = s.split("\n", lineNumbers + 1);
        String result = "";
        for (int i = 0; i < parts.length && i < lineNumbers; i++) {
            result += parts[i] + "\n";
        }
        String[] allParts = s.split("\n");
        if (allParts.length > lineNumbers + 1) {
            result += "-- Rest of stacktrace cut --\n";
        }
        return result;
    }

    public static String[] jspReservedWords = { "application", "config", "out", "page", "request", "response",
            "pageContext" };

    public static ArrayList<String> jspReservedWordList = new ArrayList<String>(Arrays.asList(jspReservedWords));

    boolean treatJspRuntimeException(Throwable original, Throwable t, PrintWriter wr, HttpServletRequest req,
            ServletContext servletContext, boolean printHeaderFooter) {

        
        
        Throwable rootCause = null;
        
        if(t instanceof ServletException) {
            rootCause = ((ServletException)t).getRootCause();
        } else {
            rootCause = t;
        }

        String title = rootCause.getClass().getSimpleName();
        String message = rootCause.getMessage();
        String body = "A " + rootCause.getClass().getName()
                + " occured (most likely because of a programming error in the JSP):\n\n" + message;

        if (t != null && original.getStackTrace() != null && !java.util.Arrays.equals(original.getStackTrace(), t.getStackTrace())) {
            body += "\n\n" + trace(rootCause);
        }

        String hiddenBody = trace(rootCause);

        try {
            SourceViewer sw = new errorViewer(req, servletContext, title, body, hiddenBody, printHeaderFooter);
            sw.parseText(wr);
        } catch (IOException e) {
            e.printStackTrace();
            throw new org.makumba.commons.RuntimeWrappedException(e);
        }
        return true;

    }

    boolean treatJspException(Throwable original, Throwable t, PrintWriter wr, HttpServletRequest req,
            ServletContext servletContext, boolean printHeaderFooter, String title) {
        if (t.getMessage() != null && t.getMessage().indexOf("Duplicate local variable") != -1) {
            String message = t.getMessage();
            String[] split = message.split("\n");
            String variableName = null;
            String errorLine = null;
            for (String element : split) {
                if (element.startsWith("An error occurred at line:")) {
                    errorLine = element;
                } else if (element.startsWith("Duplicate local variable")) {
                    variableName = element.substring("Duplicate local variable".length()).trim();
                }
            }
            if (variableName != null && jspReservedWordList.contains(variableName)) {
                String body = errorLine + "\n\n";
                body += "'" + variableName + "' is a reserverd keyword in the JSP standard!\n";
                body += "Do not use it as name for your Java variables, or as <mak:value expr=\"...\" var=\""
                        + variableName + "\" /> resp. <mak:value expr=\"...\" printVar=\"" + variableName + "\" />";
                String hiddenBody = t.getMessage();
                title = "Programmer Error - usage of reserved Tomcat keyword";
                try {
                    SourceViewer sw = new errorViewer(req, servletContext, title, body, hiddenBody, printHeaderFooter);
                    sw.parseText(wr);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new org.makumba.commons.RuntimeWrappedException(e);
                }
                return true;
            }
        }
        return false;
    }

}
