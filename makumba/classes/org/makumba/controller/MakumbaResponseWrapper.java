package org.makumba.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.collections.set.ListOrderedSet;
import org.makumba.analyser.AnalysableElement;
import org.makumba.analyser.PageCache;
import org.makumba.commons.MakumbaJspAnalyzer;
import org.makumba.commons.MakumbaResourceServlet;
import org.makumba.forms.tags.FormTagBase;
import org.makumba.forms.validation.LiveValidationProvider;
import org.makumba.list.tags.SectionTag;
import org.makumba.providers.Configuration;

/**
 * This class provides a wrapper around a {@link HttpServletResponse}, and modifies on the fly some of the output to be
 * written. In detail, these modifications are:
 * <ul>
 * <li>At the begin of the &lt;head&gt; tag, a the Makumba CSS style sheet is injected.</li>
 * <li>At the end of the &lt;head&gt;, the required Javascript libraries are included.</li>
 * </ul>
 * These modifications, especially inclusion of the Javascripts are needed for certain functionality of the Makumba Tag
 * Library, such as {@link LiveValidationProvider} and AJAX features (e.g. {@link SectionTag})<br/>
 * Note that the modifications are only applied on the {@link PrintWriter} provided by the {@link #getWriter()} method,
 * but not on the {@link OutputStream}.
 * 
 * @author Rudolf Mayer
 * @version $Id: MakumbaResponseWrapper.java,v 1.1 31 Dec 2009 17:15:55 rudi Exp $
 */
public class MakumbaResponseWrapper extends HttpServletResponseWrapper {
    private PrintWriter originalWriter;

    private PrintWriter makumbaWriter;

    private boolean headOpenPassed = false;

    private boolean headClosedPassed = false;

    private String makumbaStyleSheet;

    private HttpServletRequest request;

    private String cssResources = "";

    private String javaScriptResources = "";

    private static final Logger logger = Logger.getLogger("org.makumba.controller.response");

    public MakumbaResponseWrapper(HttpServletResponse response, HttpServletRequest request) {
        super(response);
        this.request = request;
        makumbaStyleSheet = "<link rel=\"StyleSheet\" type=\"text/css\" media=\"all\" href=\""
                + request.getContextPath() + Configuration.getMakumbaResourcesLocation() + "/"
                + MakumbaResourceServlet.RESOURCE_PATH_CSS + "makumba.css\"/>";
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        // we do the header modifications only for .jsp files
        if (request.getRequestURI().endsWith(".jsp") && request.getAttribute(javax.servlet.jsp.PageContext.EXCEPTION) == null) {
            if (makumbaWriter == null) {
                originalWriter = super.getWriter();
                makumbaWriter = new MakumbaPrintWriter(originalWriter);
                initResourceReplacements();
            }
            return makumbaWriter;
        } else {
            return super.getWriter();
        }
    }

    /** Process the requested resources, and split them into CSS and JavaScript resources */
    public void initResourceReplacements() {
        // TODO: maybe do some clever checking which resources are actually already in the header, and skip those...
        PageCache pageCache = AnalysableElement.getPageCache(request, request.getSession().getServletContext().getRealPath(
            "/"), MakumbaJspAnalyzer.getInstance());
        if (pageCache != null) {
            ListOrderedSet resources = pageCache.retrieveSetValues(FormTagBase.NEEDED_RESOURCES);
            if (resources != null) {
                for (Object object : resources) {
                    String resource = (String) object;
                    if (resource.endsWith(".js")) {
                        javaScriptResources += "  <script type=\"text/javascript\" src=\"" + request.getContextPath()
                                + Configuration.getMakumbaResourcesLocation() + "/javaScript/" + resource
                                + "\"></script>\n";
                    } else if (resource.endsWith(".css")) {
                        cssResources += "\n  <link rel=\"StyleSheet\" type=\"text/css\" media=\"all\" href=\""
                                + request.getContextPath() + Configuration.getMakumbaResourcesLocation()
                                + "/css/makumbaDevelStyles.css\"/>";
                    }
                }
            }
        }
    }

    /**
     * This class wraps over an existing (JSP Servlet Container) {@link PrintWriter}, and modifies the output to link
     * Makumba-required resources within the &lt;head&gt; tag.<br>
     * Note: This class overwrites all methods of {@link PrintWriter} that are writing char or {@link String}, with
     * basically identical code as {@link PrintWriter}; this is a precaution, as it is not known how the actual
     * implementation of {@link PrintWriter} provided by the servlet container is handling the different calls to
     * write/print/println.
     */
    class MakumbaPrintWriter extends PrintWriter {

        public MakumbaPrintWriter(Writer originalWriter) {
            super(originalWriter);
        }

        @Override
        public void print(String s) {
            write(s);
        }

        @Override
        public void println(String s) {
            write(s);
            println();
        }

        @Override
        public PrintWriter printf(String format, Object... args) {
            // TODO: investigate this more!
            return format(format, args);
        }

        @Override
        public void print(Object obj) {
            write(String.valueOf(obj));
        }

        @Override
        public void println(Object obj) {
            write(String.valueOf(obj));
            println();
        }

        @Override
        public void print(char c) {
            write(c);
        }

        @Override
        public void println(char c) {
            write(c);
            println();
        }

        @Override
        public void print(char[] c) {
            write(c);
        }

        @Override
        public void println(char[] c) {
            write(c);
            println();
        }

        @Override
        public void write(char[] buf) {
            write(buf, 0, buf.length);
        }

        @Override
        public void write(char[] buf, int off, int len) {
            // this method duplicates the code of {@link #write(String, int, int)} instead of using that methods, to
            // avoid converting from char[] to String
            if (!headClosedPassed) {
                String s = null;
                if (!headOpenPassed) {
                    s = new String(buf, off, len);
                    int indexOfHeadOpen = s.indexOf("<head>");
                    if (indexOfHeadOpen != -1 && indexOfHeadOpen > off) {
                        headOpenPassed = true;
                        s = injectStyleSheets(s);
                        buf = s.toCharArray();
                        len = s.length();
                        logger.finer(request.getRequestURI() + ", found opening head tag, added style sheet.");
                    }
                }
                if (headOpenPassed) {
                    if (s == null) {
                        s = new String(buf, off, len);
                    }
                    int indexOfHeadClosed = s.indexOf("</head>");
                    if (indexOfHeadClosed != -1 && indexOfHeadClosed > off) {
                        headClosedPassed = true;
                        s = injectJavaScriptsResources(s);
                        buf = s.toCharArray();
                        len = s.length();
                        logger.finer(request.getRequestURI() + ", found closing head tag, added scripts.");
                    }
                }
            }
            super.write(buf, off, len);
        }

        @Override
        public void write(String s) {
            write(s, 0, s.length());
        }

        @Override
        public void write(String s, int off, int len) {
            if (!headClosedPassed) {
                if (!headOpenPassed) {
                    int indexOfHeadOpen = s.indexOf("<head>");
                    if (indexOfHeadOpen != -1 && indexOfHeadOpen > off) {
                        headOpenPassed = true;
                        s = injectStyleSheets(s);
                        len = s.length();
                        logger.finer(request.getRequestURI() + ", found opening head tag, added style sheet.");
                    }
                }
                if (headOpenPassed) {
                    int indexOfHeadClosed = s.indexOf("</head>");
                    if (indexOfHeadClosed != -1 && indexOfHeadClosed > off) {
                        headClosedPassed = true;
                        s = injectJavaScriptsResources(s);
                        len = s.length();
                        logger.finer(request.getRequestURI() + ", found closing head tag, added scripts.");
                    }
                }
            }
            super.write(s, off, len);
        }

        private String injectJavaScriptsResources(String s) {
            // we add the JavaScripts just before the </head>
            return s.replace("</head>", javaScriptResources + "</head>");
        }

        private String injectStyleSheets(String s) {
            // we add the CSS stylesheet right after the <head>
            return s.replace("<head>", "<head>\n  " + makumbaStyleSheet + cssResources);
        }

        @Override
        public void close() {
            super.close();
            // print a warning if we found the opening of the head, but not the closing
            if (headOpenPassed && !headClosedPassed) {
                logger.info("Found opening head tag, but no closing -> did not add scripts!!");
            }
        }

    }

}
