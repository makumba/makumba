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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * invoke the necessary SourceViewer, depending on the type of the source the architecture should change, and be
 * organized in filters. example:
 * <ul>
 * <li>jspx and jsps: JSP syntax colouring | Java linking | MDD linking | line numbering | header</li>
 * <li>java: Java syntax colouring | Java linking | MDD linking | line numbering | header</li>
 * <li>mdd: syntax_colouring | MDD linking | line numbering | header</li>
 * <li>jspxp: JSP syntax colouring | line numbering | makumba reduction | java linking | mdd linking | header</li>
 * </ul>
 * It's not difficult to get the current architecture to work like that This will be slower but the big advantage is
 * that the Java and JSP syntax colouring (and maybe Java linking) can be outsourced.
 */
public class SourceViewServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        PrintWriter w = res.getWriter();

        SourceViewer sw = null;
        String servletPath = req.getServletPath();
        try {
            if (servletPath.equals("/dataDefinitions")) {
                sw = new mddViewer(req, this);
            } else if (servletPath.endsWith(".jspx") || servletPath.endsWith(".jsps") || servletPath.endsWith(".jspxp")) {
                sw = new jspViewer(req, this);
            } else if (servletPath.equals("/classes")) {
                sw = new javaViewer(req, this);
            } else if (servletPath.equals("/logic")) {
                sw = new logicViewer(req, this);
            } else if (servletPath.equals("/codeGenerator")) {
                sw = new GeneratedCodeViewer(req, this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            res.sendError(404, e.toString());
            return;
        }
        if (sw != null) // we have a known handler
        {
            File dir = sw.getDirectory();
            if (dir == null) {
                res.setContentType("text/html");

                try {
                    sw.parseText(w);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else // try to handle anyway
            {
                if (redirected(req, res, servletPath)) {
                    return;
                }
                if (sw instanceof GeneratedCodeViewer && req.getPathInfo().endsWith("/")) { // redirect browsing of
                                                                                            // directories to mdd Viewer
                    res.sendRedirect(req.getContextPath() + "/dataDefinitions" + req.getPathInfo());
                    return;
                }

                // make a directory listing
                String relativeDirectory = dir.getName();
                if (dir.getAbsolutePath().indexOf("classes/") != -1) { // class viewer
                    relativeDirectory = dir.getAbsolutePath().substring(dir.getAbsolutePath().indexOf("classes"));
                } else if (dir.getAbsolutePath().indexOf("dataDefinitions/") != -1) { // MDD viewer
                    relativeDirectory = dir.getAbsolutePath().substring(
                        dir.getAbsolutePath().indexOf("dataDefinitions"));
                }
                res.setContentType("text/html");
                printDirlistingHeader(w, dir, relativeDirectory);

                if (!(relativeDirectory.equals("classes") || relativeDirectory.equals("classes/dataDefinitions"))) {
                    w.println("<b><a href=\"../\">../</a></b> (up one level)");
                }

                if (sw instanceof javaViewer) {
                    // process and display directories
                    processDirectory(w, dir, ".java");

                    // process and display files
                    String[] list = dir.list();
                    Arrays.sort(list);
                    for (int i = 0; i < list.length; i++) {
                        String s = list[i];
                        File f = new File(dir.getAbsolutePath() + File.separator + s);
                        if (f.isFile() && f.getName().endsWith(".java")) {
                            w.println("<b><a href=\"" + s + "\">" + s + "</a></b>");
                        }
                    }
                } else if (sw instanceof mddViewer) {
                    // process and display directories
                    processDirectory(w, dir, "dd");

                    // process and display files
                    String[] list = dir.list();
                    Arrays.sort(list);
                    for (int i = 0; i < list.length; i++) {
                        String s = list[i];
                        if (s.indexOf(".") != -1 && s.endsWith("dd")) {
                            String dd = req.getPathInfo() + s;
                            dd = dd.substring(1, dd.lastIndexOf(".")).replace('/', '.');
                            String addr = req.getContextPath() + "/dataDefinitions/" + dd;
                            w.println("<a href=\"" + addr + "\">" + s + "</a>");
                        }
                    }
                } else {
                    java.util.logging.Logger.getLogger("org.makumba." + "devel").warning(
                        "Don't know how to handle viewer: " + sw + "(" + sw.getClass() + ")");
                }
                w.println("</pre>");
                DevelUtils.printDeveloperSupportFooter(w);
                w.println("</body></html>");
            }
        } else {
            res.setContentType("text/html");
            w.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
            w.println("<html><head><title>Error in source viewer</title><body>");
            w.println("Error in source viewer servlet '" + getClass().getName() + "' - unknown source type: "
                    + servletPath);
            DevelUtils.printDeveloperSupportFooter(w);
            w.println("</body></html>");
        }
    }

    public static boolean redirected(HttpServletRequest req, HttpServletResponse res, String servletPath)
            throws IOException {
        if (req.getPathInfo() == null) {
            if (servletPath.startsWith("/"))
                servletPath = servletPath.substring(1);
            res.sendRedirect(servletPath + "/");
            return true;
        }
        if (!req.getPathInfo().endsWith("/")) {
            res.sendRedirect(servletPath + req.getPathInfo() + "/");
            return true;
        }
        return false;
    }

    public static void printDirlistingHeader(PrintWriter w, File dir, String relativeDirectory) throws IOException {
        w.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
        w.println("<html><head><title>" + relativeDirectory + "</title>");
        w.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" >");

        w.println("</head><body bgcolor=white><table width=\"100%\" bgcolor=\"lightblue\"><tr><td rowspan=\"2\">");
        w.print("<font size=\"+2\"><a href=\".\"><font color=\"darkblue\">" + relativeDirectory + "</font></a></font>");
        w.print("<font size=\"-1\"><br>" + dir.getCanonicalPath() + "</font>");
        w.print("</td>");

        w.print("</tr></table>\n<pre style=\"margin-top:0\">");
    }

    public static void processDirectory(PrintWriter w, File dir, String extension) {
        String[] list = dir.list();
        Arrays.sort(list);
        for (int i = 0; i < list.length; i++) {
            String s = list[i];
            File f = new File(dir.getAbsolutePath() + File.separator + s);
            if (f.isDirectory() && !f.getName().equals("CVS") && !f.getName().equals(".svn")) {
                if (extension == null || containsFilesWithExtension(f, extension)) {
                    w.println("<b><a href=\"" + s + "/\">" + s + "/</a></b>");
                }
            }
        }
    }

    static boolean containsFilesWithExtension(File dir, String extension) {
        String[] list = dir.list();
        // we first process only files, to decrease the amount of sub-directories we search
        for (int i = 0; i < list.length; i++) {
            File f = new File(dir.getAbsolutePath() + File.separator + list[i]);
            if (f.isFile() && f.getName().endsWith(extension)) {
                return true;
            }
        }
        for (int i = 0; i < list.length; i++) {
            File f = new File(dir.getAbsolutePath() + File.separator + list[i]);
            if (f.isDirectory()) {
                if (containsFilesWithExtension(f, extension)) { // if not a dir with classes, we continue the search
                    return true;
                }
            }
        }
        return false;
    }
}
