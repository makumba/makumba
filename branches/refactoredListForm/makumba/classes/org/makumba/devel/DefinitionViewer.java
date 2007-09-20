package org.makumba.devel;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringReader;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.makumba.DataDefinition;
import org.makumba.MakumbaError;
import org.makumba.ValidationDefinition;

/**
 * Provides basic functionality for a file providing some kind of definition, as e.g. the {@link DataDefinition} via the
 * {@link mddViewer}, or the {@link ValidationDefinitionViewer} showing {@link ValidationDefinition}.
 * 
 * @author Rudolf Mayer
 * @version $Id: DataViewer.java,v 1.1 Sep 17, 2007 2:53:30 AM rudi Exp $
 */
public abstract class DefinitionViewer extends LineViewer {

    protected MakumbaError err;

    protected DefinitionViewer(boolean printLineNumbers, HttpServletRequest req, HttpServlet servlet) {
        super(printLineNumbers, req, servlet);
        setSearchLevels(false, false, false, true);
        virtualPath = req.getPathInfo();
        contextPath = req.getContextPath();
        if (virtualPath == null)
            virtualPath = "/";
    }

    public void footer(PrintWriter pw) throws IOException {
        if (err != null)
            pw.println("<hr><a name=\"errors\"></a><pre>" + err.getMessage() + "</pre>");
        super.footer(pw);
    }

    public String getLineTag(String s) {
        String ln = s.trim();
        int eq;
        if (!ln.startsWith("#") && !ln.startsWith("!") && !ln.startsWith("=") && (eq = ln.indexOf('=')) != -1) {
            return ln.substring(0, eq).trim();
        }
        return null;
    }

    public void printLine(PrintWriter w, String s, String toPrint) throws IOException {
        if (err != null) {
            // we go thru the error text, if we find this particular line, we display its error message
            // this is a hack, it should rather go thru the multiple exceptions
            LineNumberReader lr = new LineNumberReader(new StringReader(err.getMessage()));
            String e = null;
            String before = null;
            while (true) {
                before = e;
                e = lr.readLine();
                if (e == null)
                    break;
                if (e.length() > 0 && e.equals(s)) {
                    w.print("<span style=\"background-color: pink;\">");
                    super.printLine(w, s, e);
                    w.print("</span>\t<span style=\"color:red;\">" + lr.readLine() + " "
                            + before.substring(before.indexOf(':') + 1) + "</span>\r\n");
                    return;
                }
            }
        }
        super.printLine(w, s, toPrint);
    }

}