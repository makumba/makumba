///////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003  http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//  -------------
//  $Id$
//  $Name$
/////////////////////////////////////

package org.makumba.devel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.makumba.MakumbaSystem;

/**
 * a viewer that shows everything per line
 * 
 * 
 * @version $Id$
 * @author Stefan Baebler
 * @author Rudolf Mayer
 *  
 */
public class LineViewer implements SourceViewer {
    private static final Pattern patternUrl = Pattern.compile("[http:|/|\\w]+\\.\\w+[\\.\\w]*[/|\\w]*");

    protected static final String PARAM_HIDE_LINES = "hideLines";

    protected ServletContext servletContext;

    protected String realPath;

    protected String virtualPath;

    protected String contextPath;

    protected Reader reader;

    protected boolean printLineNumbers;

    private File dir;

    protected String title;

    protected boolean searchJSPPages = true;

    protected boolean searchCompiledJSPClasses = true;

    protected boolean searchJavaClasses = true;

    protected boolean searchMDD = true;

    //  default for old .jspx - change this to "s" when .jsps gets adopted (bug 677)
    protected String jspSourceViewExtension = "x";

    protected String jspClasspath;

    protected HttpServletRequest request;

    protected HttpServlet servlet;

    protected String servletPath;

    protected String logicPath;
    
    protected String codeBackgroundStyle = "";
    
    protected boolean hideLineNumbers = false;
    
    //  TODO: temporarily, to be determined by java Anlyzer
    private String[] importedPackages = new String[] { "", "java.lang." };

    /** if this resource is actually a directory, returns not null */
    public File getDirectory() {
        if (dir != null && dir.isDirectory())
            return dir;
        return null;
    }

    public Reader getReader() {
        return reader;
    }

    void readFromURL(java.net.URL u) throws IOException {
        if (u == null)
            throw new FileNotFoundException(virtualPath);
        realPath = u.getFile();
        try {
            dir = new File(realPath);
            if (!dir.isDirectory())
                reader = new InputStreamReader(new FileInputStream(dir));
        } catch (FileNotFoundException fnfe) {
            realPath = null;
            reader = new InputStreamReader((InputStream) u.getContent());
        }
    }

    public LineViewer(boolean printLineNumbers, HttpServletRequest request, HttpServlet servlet) {
        this.request = request;
        this.servlet = servlet;
        this.printLineNumbers = printLineNumbers;
        servletContext = servlet.getServletContext();
        contextPath = request.getContextPath();
        hideLineNumbers = request.getParameter(PARAM_HIDE_LINES) != null && request.getParameter(PARAM_HIDE_LINES).equals("true");
    }

    /**
     * parse the text and write the output
     */
    public void parseText(PrintWriter writer) throws IOException {
        GregorianCalendar begin = new GregorianCalendar();
        printPageBegin(writer);

        // we go line by line as an MDD references cannot span over newlines
        // as a bonus, we print the line numbers as well.
        LineNumberReader lr = new LineNumberReader(reader);
        String s = null;
        while ((s = lr.readLine()) != null) {
            if (printLineNumbers && !hideLineNumbers) {
                int n = lr.getLineNumber();
                writer.print("<a name=\"" + n + "\" href=\"#" + n + "\" class=\"lineNo\">" + n + ":\t</a>");
            }
            printLine(writer, s, parseLine(htmlEscape(s)));
        }
        printPageEnd(writer);
        reader.close();
        double timeTaken = new Date(new GregorianCalendar().getTimeInMillis() - begin.getTimeInMillis()).getTime();
        MakumbaSystem.getMakumbaLogger("org.makumba.devel.sourceViewer").fine(
                "Sourcecode viewer took :" + (timeTaken / 1000.0) + " seconds");
    }

    /**
     * @param writer
     * @throws IOException
     */
    public void printPageEnd(PrintWriter writer) throws IOException {
        writer.println("\n</pre>");
        footer(writer);
        writer.println("\n</body></html>");
    }

    /**
     * Write the beginning of the page to the given writer.
     */
    public void printPageBegin(PrintWriter writer) throws IOException {
        writer.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
        writer.println("<html>");
        writer.println("<head>");
        writer.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" >");
        if (realPath != null && virtualPath != null)
            title = virtualPath + "";
        else if (title == null || title != null && title.equals(""))
            title = "";
        writer.println("<title>" + title + "</title>");
        if (printLineNumbers && !hideLineNumbers) {
            writer.println("<style type=\"text/css\">");
            writer.println("A.lineNo {color:navy; background-color:lightblue; text-decoration:none; cursor:default;}");
            writer.println("pre.code {margin-top:0; " + codeBackgroundStyle + "}");
            writer.println("\n</style>");
        }
        writer.println("</head>");
        writer.println("<body bgcolor=white>");
        writer.println("<table width=\"100%\" bgcolor=\"lightblue\">");
        writer.println("<tr>");
        writer.println("<td rowspan=\"2\">");

        if (title != null && !title.equals("") && !title.equals(virtualPath))
            writer.print("<font size=\"+2\"><font color=\"darkblue\">" + title + "</font></font>");
        else if (virtualPath != null)
            writer.print("<font size=\"+2\"><a href=\"" + virtualPath + "\"><font color=\"darkblue\">" + virtualPath
                    + "</font></a></font>");

        if (realPath != null) {
            writer.println("<font size=\"-1\"><br>" + new File(realPath).getCanonicalPath() + "</font>");
        }
        printPageBeginAdditional(writer);

        if (printLineNumbers) {
            String urlParams = "";
            Enumeration e = request.getParameterNames();
            while (e.hasMoreElements()) {
                String key =  (String) e.nextElement();
                Object value = request.getParameter(key);
                if (!key.equals(PARAM_HIDE_LINES)) {
                    if (!urlParams.equals("")) {
                        urlParams += "&";
                    }
                    urlParams += key + "=" + value;
                }
            }
            
            if (!urlParams.equals("")) {
                urlParams += "&";
            }
            urlParams += PARAM_HIDE_LINES + "=" + !hideLineNumbers;
            if (!urlParams.equals("")) {
                urlParams = "?" + urlParams;
            }            
            String link = request.getRequestURI() + urlParams;
         
            writer.print("<div style=\"font-size: smaller; vertical-align: bottom;\"><a href=\"" + link + "\">");
            if (hideLineNumbers) {
                writer.print("Show");
            } else {
                writer.print("Hide");
            }
            writer.println(" line numbers</a></div>");
        }
        writer.println("</td>");

        intro(writer);
        writer.println("</tr>");
        writer.println("</table>");
        writer.print("<pre class=\"code\">");
    }

    /**
     * Write the page header to the given writer.
     */
    public void intro(PrintWriter printWriter) throws IOException {
    }

    public void printPageBeginAdditional(PrintWriter printWriter) throws IOException {

    }

    /** Write the page footer to the given writer. */
    public void footer(PrintWriter printWriter) throws IOException {
        printWriter.println("<hr><font size=\"-1\"><a href=\"http://www.makumba.org\">Makumba</a> developer support, version: "
                + org.makumba.MakumbaSystem.getVersion() + "</font>");
    }

    public void printLine(PrintWriter printWriter, String s, String toPrint) throws IOException {
        String t = getLineTag(s);
        if (t != null)
            printWriter.print("<a name=\"" + t + "\"></a>");
        printWriter.print(toPrint);

        // not sure of this fix...was "<br>"
        printWriter.print("\n");
    }

    public String getLineTag(String s) {
        return null;
    }

    /**
     * Sets the amount of links to other files the viewer is trying to find. changing some of these parameters can significantely speed up the viewing
     * process.
     * 
     * @param searchJSPPages
     *            whether to search for .jsp files.
     * @param searchCompiledJSPClasses
     *            wheter to search for compiled jsp files, i.e. files with the extension _jsp.java
     * @param searchJavaClasses
     *            wheter to search for java source files.
     * @param searchMDD
     *            whether to search for Makumba Data Definitions, .mdd files (and Inlcuded Data Defitions, .idd).
     */
    public void setSearchLevels(boolean searchJSPPages, boolean searchCompiledJSPClasses, boolean searchJavaClasses,
            boolean searchMDD) {
        this.searchJSPPages = searchJSPPages;
        this.searchCompiledJSPClasses = searchCompiledJSPClasses;
        this.searchJavaClasses = searchJavaClasses;
        this.searchMDD = searchMDD;
    }

    /**
     * Processes one line of code, and adds links for
     * <ul>
     * <li>MDDs</li>
     * <li>JSP pages</li>
     * <li>Java Classes</li>
     * <li>from JSP pages generated Java classes</li>
     * </ul>
     * 
     * Subclasses that want to provide any additional formatting (syntax highlighting, etc) should extend this method, apply their formatting and
     * before/afterwards call this method.
     * 
     * This method is rather time-consuming, and subclasses interested in providing links just to a part of the above should use the
     * <code>setSearchLevels</code> method to specify for what types of files are searched for.
     * 
     * @param s
     *            the unformatted code line.
     * @return The formatted code line.
     */
    public String parseLine(String s) {
        Class javaClass;
        String jdkClass;
        String jspPage;
        String jspClass;

        StringBuffer source = new StringBuffer(s);
        StringBuffer result = new StringBuffer();

        Matcher matcher = patternUrl.matcher(s);
        while (matcher.find()) {
            String token = matcher.group();

            int indexOf = source.indexOf(token);
            int indexAfter = indexOf + token.length();

            result.append(source.substring(0, indexOf));

            if (token.indexOf("www.makumba.org") != -1) {
                result.append(formatMakumbaLink(token));
            } else if (token.indexOf("java.sun.com") != -1) {
                result.append(formatSunLink(token));
            } else if (searchMDD && org.makumba.abstr.RecordParser.findDataDefinition(token, "mdd") != null
                    || org.makumba.abstr.RecordParser.findDataDefinition(token, "idd") != null) {
                result.append(formatMDDLink(token));
            } else if (searchJavaClasses && (javaClass = findClass(token)) != null) {
                result.append(formatClassLink(javaClass.getName(), token, null));
            } else if (searchJavaClasses && (jdkClass = findJDKClass(token)) != null) {
                result.append(jdkClass);
            } else if (searchJSPPages && (jspPage = findPage(token)) != null) {
                result.append(formatJSPLink(jspPage, token, null));
            } else if (searchCompiledJSPClasses && (jspClass = findCompiledJSP(token)) != null) {
                result.append(formatClassLink(jspClass, token, null));
            } else {
                result.append(token);
            }
            source.delete(0, indexAfter);
        }
        return result.append(source).toString();
    }

    /**
     * @param jspPage
     * @param result
     * @param token
     */
    public String formatJSPLink(String jspPage, String token, Integer lineNumber) {
        StringBuffer result = new StringBuffer();
        result.append("<a href=\"" + jspPage);
        if (jspPage.endsWith("jsp")) {
            result.append(jspSourceViewExtension);
        }
        if (lineNumber != null) {
            result.append("#" + lineNumber);
        }
        result.append("\">").append(token).append("</a>");
        return result.toString();
    }

    /**
     * @param className
     * @param token
     * @return
     */
    public String formatClassLink(String className, String token, Integer lineNumber) {
        if (lineNumber != null) {
            return "<a href=\"" + contextPath + "/classes/" + className + "#" + lineNumber + "\">" + token + "</a>";
        } else {
            return "<a href=\"" + contextPath + "/classes/" + className + "\">" + token + "</a>";

        }
    }

    /**
     * @param token
     * @return
     */
    public String formatMDDLink(String token) {
        return "<a href=\"" + contextPath + "/dataDefinitions/" + token + "\">" + token + "</a>";
    }

    /**
     * @param result
     * @param token
     */
    public String formatMakumbaLink(String token) {
        return "<a href=\"http://www.makumba.org\" target=\"_blank\">" + token + "</a>";
    }

    /**
     * @param token
     * @return
     */
    public String formatSunLink(String token) {
        if (token.indexOf("java.sun.com/jstl/") != -1 || token.indexOf("http://java.sun.com/jsp/jstl/") != -1) {
            return "<a href=\"http://java.sun.com/products/jsp/jstl/1.1/docs/tlddocs/\" target=\"_blank\">" + token
                    + "</a>";
        } else {
            return "<a href=\"http://java.sun.com\" target=\"_blank\">" + token + "</a>";
        }
    }

    /**
     * @param s
     * @return
     */
    public StringTokenizer getLineTokenizer(String s) {
        return new StringTokenizer(s, "\"\' (){}[]<>,;-?#:", true);
    }

    /**
     * Finds a JSP page with the given name.
     * 
     * @param s
     *            The page to search for
     * @return The page found, <code>null</code> otherwise
     */
    public String findPage(String s) {
        if (s.startsWith("/")) { //absolute reference to file
            File file = new File(servletContext.getRealPath(s));
            if (file.exists()) {
                return contextPath + s;
            } else {
                file = new File(s);
                if (file.exists()) {// full path name?
                    return s;
                }
            }
        }
        if (s.startsWith("/")) { //absolute reference to file, take two. rather a dirty hack
            // needed e.g. for files like /usr/local/cvsroot/karamba/public_html/general/survey/user/viewStatistics.jsp
            s = s.substring(s.lastIndexOf("/") + 1);
        }
        if (!s.startsWith("/") && realPath != null) { //relative reference
            File file = new File(realPath.substring(0, realPath.lastIndexOf(File.separatorChar)) + File.separatorChar
                    + s.replace('/', File.separatorChar));
            if (file.exists()) {
                return s;
            }
        }
        return null;
    }

    /**
     * Searches for Java Classes with the given name
     * 
     * @param s
     *            The class name to search for
     * @return The class, if found, <code>null</code> otherwise.
     */
    public Class findClass(String s) {
        Class c = null;
        try {
            c = Class.forName(s);
        } catch (Throwable t) {
            return null;
        }
        if (org.makumba.util.ClassResource.get(c.getName().replace('.', '/') + ".java") != null)
            return c;
        return null;
    }

    public String findCompiledJSP(String s) {
        if (jspClasspath != null && s.indexOf("_jsp") != -1) {
            try {
                String newClassName = s.substring(0, s.indexOf("_jsp") + 4);
                String filePath = jspClasspath + "/" + newClassName.replace('.', '/') + ".java";
                File file = new File(filePath);
                if (file.exists()) {
                    return newClassName;
                } else {
                    return null;
                }
            } catch (Throwable t) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Escapes a string to HTML-conform format.
     * 
     * @param s
     *            The string to escape
     * @return The given, with &amp;, &lt; and &gt; escaped.
     */
    public String htmlEscape(String s) {
        //TODO: use internal java class?!

        // we NEED to have this replacement first, otherwise we will replace the '&' from the &lt; and &gt;
        s = s.replaceAll("&", "&amp;");

        s = s.replaceAll("<", "&lt;");
        s = s.replaceAll(">", "&gt;");
        return s;
    }

    /**
     * @param token
     * @return
     */
    public String findJDKClass(String token) {
        Class c = null;
        for (int i = 0; i < importedPackages.length; i++) {
            try {
                c = Class.forName(importedPackages[i] + token);
                break;
            } catch (Throwable throwable) {
                // we just continue with the next package
            }
        }
        if (c != null && c.getName().startsWith("java")) {
            return "<a href=\"http://java.sun.com/j2se/1.4.2/docs/api/" + c.getName().replaceAll("\\.", "/")
                    + ".html\">" + c.getName() + "</a>";
        } else if (c != null && c.getName().startsWith("org.makumba")) {
            return "<a href=\"http://www.makumba.org/api/" + c.getName().replaceAll("\\.", "/") + ".html\">"
                    + c.getName() + "</a>";
        } else {

            String className = token.substring(0, token.lastIndexOf("."));
            String methodName = token.substring(token.lastIndexOf(".") + 1);
            try {
                for (int i = 0; i < importedPackages.length; i++) {
                    c = Class.forName(importedPackages[i] + className);
                    break;
                }
            } catch (Throwable throwable) {
                // we just continue with the next package
            }
            if (c != null && c.getName().startsWith("java")) {
                return "<a href=\"http://java.sun.com/j2se/1.4.2/docs/api/" + c.getName().replaceAll("\\.", "/")
                        + ".html#" + methodName + "()\">" + c.getName() + "." + methodName + "()" + "</a>";
            } else {
                return null;
            }
        }
    }

    public static void main(String[] args) {

        String p1 = "http://www.makumba.org/presentation";
        String p2 = "mak:object from=\"general.survey.Survey survey\" where=\"survey=$survey\">";
        String p3 = "/layout/header.jsp?title=Statistics for ";
        String p4 = "c:set var=\"viewStatsPage\" value=\"viewStatistics.jsp?survey=";

        String[] patterns = new String[] { p1, p2, p3, p4 };
        // patterns=new String[] {w1,w2};
        System.out.println("pattern: " + patternUrl.pattern());
        for (int i = 0; i < patterns.length; i++) {
            System.out.println("\n!!!trying\n---" + patterns[i] + " ---");
            Matcher m = patternUrl.matcher(patterns[i]);

            while (m.find()) {
                System.out.print(m.group() + " - ");
            }
            System.out.println();
        }

    }
}

