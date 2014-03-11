package org.makumba.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.makumba.analyser.AnalysableElement;
import org.makumba.analyser.PageCache;
import org.makumba.commons.MakumbaJspAnalyzer;
import org.makumba.forms.validation.LiveValidationProvider;
import org.makumba.list.tags.SectionTag;
import org.makumba.providers.Configuration;
import org.makumba.providers.MakumbaServlet;

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

    private HttpServletRequest request;

    private String cssResources = "";

    private String javaScriptResources = "";

    private static final Logger logger = Logger.getLogger("org.makumba.controller.response");

    public MakumbaResponseWrapper(HttpServletResponse response, HttpServletRequest request) {
        super(response);
        this.request = request;
    }

    public void outputOff() throws IOException {
        if (writerState == noWriter) {
            return;
        }

        // hopefully all header detection happens now, because there won't be any other
        getWriter().flush();
        writerState = noWriter;
    }

    public void outputOn() throws IOException {
        if (writerState != noWriter) {
            return;
        }
        getWriter().flush();
        writerState = usualWriter;
    }

    abstract class ResponseWriterState {
        public abstract void write(char[] buf, int off, int len);

        public abstract void write(String s, int off, int len);

        public void warnOnClose() {

        }
    }

    final ResponseWriterState usualWriter = new ResponseWriterState() {
        @Override
        public void write(char[] buf, int off, int len) {
            originalWriter.write(buf, off, len);
        }

        @Override
        public void write(String s, int off, int len) {
            originalWriter.write(s, off, len);
        }
    };

    final ResponseWriterState noWriter = new ResponseWriterState() {
        @Override
        public void write(char[] buf, int off, int len) {
            // System.out.print(new String(buf, off, len));
        }

        @Override
        public void write(String s, int off, int len) {
            // System.out.print(new String(s.toCharArray(), off, len));
        }
    };

    final ResponseWriterState beforeHeader = new ResponseWriterState() {
        @Override
        public void write(char[] buf, int off, int len) {
            // convert to string since in this state this is needed to look for <head> and </head>
            write(new String(buf, off, len), 0, len);
        }

        @Override
        public void write(String s, int off, int len) {
            int off1 = checkOpen(s, off, len);
            len = len + off - off1;
            int off2 = checkClosed(s, off1, len);
            originalWriter.write(s, off2, len + off1 - off2);
        }

        @Override
        public void warnOnClose() {
            logger.info("Did not find open head -> did not add scripts!!");
        }
    };

    final ResponseWriterState afterHead = new ResponseWriterState() {

        @Override
        public void write(char[] buf, int off, int len) {
            // convert to string since in this state this is needed to look for </head>
            write(new String(buf, off, len), 0, len);
        }

        @Override
        public void write(String s, int off, int len) {
            int off2 = checkClosed(s, off, len);
            originalWriter.write(s, off2, len + off - off2);
        }

        @Override
        public void warnOnClose() {
            logger.info("Found opening head tag, but no closing -> did not add javascripts!!");
        }

    };

    ResponseWriterState writerState;

    private static final String BEGIN_AUTOMATICALLY_ADDED_BY_MAKUMBA = "  <!-- BEGIN: automatically added by Makumba -->\n";

    private static final String END_AUTOMATICALLY_ADDED_BY_MAKUMBA = "  <!-- END: automatically added by Makumba -->\n";

    int checkOpen(String s, int off, int len) {
        int indexOfHeadOpen = s.indexOf("<head>");
        if (indexOfHeadOpen != -1 && indexOfHeadOpen > off) {
            originalWriter.write(s, off, indexOfHeadOpen - off);

            originalWriter.write("<head>\n" + BEGIN_AUTOMATICALLY_ADDED_BY_MAKUMBA + cssResources
                    + END_AUTOMATICALLY_ADDED_BY_MAKUMBA);
            logger.finer(request.getRequestURI() + ", found opening head tag, added style sheet.");
            writerState = afterHead;
            return indexOfHeadOpen + 6;
        }
        return off;
    }

    int checkClosed(String s, int off, int len) {
        int indexOfHeadClosed = s.indexOf("</head>");
        if (indexOfHeadClosed != -1 && indexOfHeadClosed > off) {
            originalWriter.write(s, off, indexOfHeadClosed - off);
            originalWriter.write("\n" + BEGIN_AUTOMATICALLY_ADDED_BY_MAKUMBA + javaScriptResources
                    + END_AUTOMATICALLY_ADDED_BY_MAKUMBA + "</head>");
            logger.finer(request.getRequestURI() + ", found closing head tag, added scripts.");
            writerState = usualWriter;
            return indexOfHeadClosed + 7;
        }
        return off;
    }

    @Override
    public PrintWriter getWriter() throws IOException {

        if (makumbaWriter == null) {
            originalWriter = super.getWriter();
            makumbaWriter = new MakumbaPrintWriter(originalWriter);

            // we do the header modifications only for .jsp files
            // and only if we are not doing something related to makumba tools
            if ((request.getRequestURI().endsWith(".jsp") || request.getRequestURI().endsWith(
                ".jsp;jsessionid=" + request.getSession().getId()))
                    && !request.getRequestURI().startsWith(
                        request.getContextPath() + Configuration.getMakumbaToolsLocation())
                    && request.getAttribute(javax.servlet.jsp.PageContext.EXCEPTION) == null && writerState == null) {
                writerState = beforeHeader;
                initResourceReplacements();
            } else {
                writerState = usualWriter;
            }
        }
        return makumbaWriter;

    }

    // this may be called already before getWriter()
    public void stopHeaderRewrite() {
        writerState = usualWriter;
    }

    /** Process the requested resources, and split them into CSS and JavaScript resources */
    public void initResourceReplacements() {
        // TODO: maybe do some clever checking which resources are actually already in the header, and skip those...
        PageCache pageCache = AnalysableElement.getPageCache(request,
            request.getSession().getServletContext().getRealPath("/"), MakumbaJspAnalyzer.getInstance());
        if (pageCache != null) {
            // workaround to http://trac.makumba.org/ticket/1277: writing all potentially required resources
            LinkedHashSet<String> resources = Configuration.getRequiredResources();
            if (resources != null) {
                for (Object object : resources) {
                    String resource = (String) object;
                    if (resource.endsWith(".js")) {
                        javaScriptResources += "  <script type=\"text/javascript\" src=\"" + request.getContextPath()
                                + Configuration.getServletLocation(MakumbaServlet.RESOURCES) + "/javaScript/"
                                + resource + "\"></script>\n";
                    } else if (resource.endsWith(".css")) {
                        cssResources += "  <link rel=\"StyleSheet\" type=\"text/css\" media=\"all\" href=\""
                                + request.getContextPath() + Configuration.getServletLocation(MakumbaServlet.RESOURCES)
                                + "/css/" + resource + "\"/>\n";
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
            writerState.write(buf, off, len);
        }

        @Override
        public void write(String s, int off, int len) {
            writerState.write(s, off, len);
        }

        @Override
        public void write(String s) {
            write(s, 0, s.length());
        }

        @Override
        public void close() {
            super.close();
            // print a warning if we are still in a writer that searches for some tag...
            writerState.warnOnClose();
        }

    }

}
