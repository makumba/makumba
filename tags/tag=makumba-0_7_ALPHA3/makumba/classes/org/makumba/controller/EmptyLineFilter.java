package org.makumba.controller;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Implements a filter that deletes empty lines in HTML, i.e. can be used to beautify the resulting output of a JSP
 * page. The filter does respect &lt;pre&gt; tags, but will ignore CSS styles of type <i>white-space: pre</i>.
 * 
 * @author Rudolf Mayer
 * @version $Id: EmptyLineFilter.java,v 1.1 Sep 19, 2007 12:25:54 AM rudi Exp $
 */
public class EmptyLineFilter implements Filter {

    public void init(FilterConfig filterConfig) {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        // we make a wrapper, and then continue filtering
        EmptyLineFilterWrapper emptyLineFilterWrapper = new EmptyLineFilterWrapper(response);
        chain.doFilter(request, emptyLineFilterWrapper);
        // after the other filters are finished, we close the stream
        emptyLineFilterWrapper.getOutputStream().close();
    }

    public void destroy() {
    }

    public void deleteEmptyLines(Reader reader, Writer writer) throws IOException {
        BufferedReader in = new BufferedReader(reader);
        String line = null;
        boolean inPreTag = false;
        while ((line = in.readLine()) != null) {
            String lineRest = line;
            int indexPreTagOpen = -1;
            if (inPreTag && line.indexOf("</pre") != -1) {
                inPreTag = false;
                lineRest = lineRest.substring(lineRest.indexOf(">", line.indexOf("</pre")));
            }
            while ((indexPreTagOpen = lineRest.indexOf("<pre")) != -1) {
                inPreTag = true;
                int indexPreTagClosing = lineRest.indexOf(">", indexPreTagOpen);
                lineRest = lineRest.substring(indexPreTagClosing + 1);
                int indexEndTagPre = lineRest.indexOf("</pre");
                if (indexEndTagPre != -1) {
                    inPreTag = false;
                    lineRest = lineRest.substring(lineRest.indexOf(">", indexEndTagPre));
                }
            }

            if (inPreTag || !line.trim().equals("")) {
                writer.write(line + "\n");
            }
        }
    }

    public String deleteEmptyLines(Reader reader) throws IOException {
        StringBuffer result = new StringBuffer();
        BufferedReader in = new BufferedReader(reader);
        String line = null;
        boolean inPreTag = false;
        while ((line = in.readLine()) != null) {
            String lineRest = line;
            int indexPreTagOpen = -1;
            if (inPreTag && line.indexOf("</pre") != -1) {
                inPreTag = false;
                lineRest = lineRest.substring(lineRest.indexOf(">", line.indexOf("</pre")));
            }
            while ((indexPreTagOpen = lineRest.indexOf("<pre")) != -1) {
                inPreTag = true;
                int indexPreTagClosing = lineRest.indexOf(">", indexPreTagOpen);
                lineRest = lineRest.substring(indexPreTagClosing + 1);
                int indexEndTagPre = lineRest.indexOf("</pre");
                if (indexEndTagPre != -1) {
                    inPreTag = false;
                    lineRest = lineRest.substring(lineRest.indexOf(">", indexEndTagPre));
                }
            }

            if (inPreTag || !line.trim().equals("")) {
                result.append(line).append("\n");
            }
        }
        return result.toString();
    }

    class EmptyLineFilterStream extends ServletOutputStream {

        private OutputStream intStream;

        private ByteArrayOutputStream baStream;

        private boolean closed = false;

        public EmptyLineFilterStream(OutputStream outStream) {
            intStream = outStream;
            baStream = new ByteArrayOutputStream();
        }

        public void write(int i) throws java.io.IOException {
            baStream.write(i);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            baStream.write(b, off, len);
        }

        public void close() throws java.io.IOException {
            if (!closed) {
                processStream();
                intStream.close();
                closed = true;
            }
        }

        public void flush() throws java.io.IOException {
            if (baStream.size() != 0) {
                if (!closed) {
                    processStream(); // need to synchronize the flush!
                    baStream = new ByteArrayOutputStream();
                }
            }
        }

        public void processStream() throws java.io.IOException {
            intStream.write(replaceContent(baStream.toByteArray()));
            intStream.flush();
        }

        public byte[] replaceContent(byte[] inBytes) {
            System.out.println("replace content: ");
            StringReader reader = new StringReader(new String(inBytes));
            try {
                return deleteEmptyLines(reader).getBytes();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
        }

    }

    class EmptyLineFilterWrapper extends HttpServletResponseWrapper {
        private PrintWriter tpWriter;

        private EmptyLineFilterStream tpStream;

        public EmptyLineFilterWrapper(ServletResponse inResp) throws java.io.IOException {
            super((HttpServletResponse) inResp);
            tpStream = new EmptyLineFilterStream(inResp.getOutputStream());
            tpWriter = new EmptyLineFilterPrintWriter(tpStream);
        }

        public ServletOutputStream getOutputStream() throws java.io.IOException {
            return tpStream;
        }

        public PrintWriter getWriter() throws java.io.IOException {
            return tpWriter;
        }
    }

    class EmptyLineFilterPrintWriter extends PrintWriter {

        private OutputStream outStream;

        public EmptyLineFilterPrintWriter(OutputStream out) {
            super(out);
            this.outStream = out;
        }

        public void write(char[] buf, int off, int len) {
            // we modify the call and send the content directly to the output stream
            try {
                outStream.write(new String(buf).getBytes(), off, len);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) throws IOException {
        String text = "<html>\n" + "\n\n" + "<pre>\n\n</pre>\n"
                + "<pre> some stuff </pre> bla <pre>\nstuff\nnext line stuff\n</pre>\n\n\n\n\n hhh";
        StringReader r = new StringReader(text);
        StringWriter w = new StringWriter();
        new EmptyLineFilter().deleteEmptyLines(r, w);
        System.out.println("*** Original ***");
        System.out.print(text);
        System.out.println("-----------------");
        System.out.println("*** Filtered ***");
        System.out.print(w.toString());
        System.out.println("-----------------");
    }
}
