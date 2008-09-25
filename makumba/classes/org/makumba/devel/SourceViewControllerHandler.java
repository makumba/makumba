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
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.makumba.commons.ControllerHandler;
import org.makumba.commons.ServletObjects;
import org.makumba.providers.Configuration;

/**
 * invoke the necessary SourceViewer, depending on the type of the source the architecture should change, and be
 * organized in filters. example:
 * <ul>
 * <li>jspx and jsps: JSP syntax colouring | Java linking | MDD linking | line numbering | header</li>
 * <li>java: Java syntax colouring | Java linking | MDD linking | line numbering | header</li>
 * <li>mdd: syntax_colouring | MDD linking | line numbering | header</li>
 * <li>jspxp: JSP syntax colouring | line numbering | makumba reduction | java linking | mdd linking | header</li>
 * </ul>
 * It's not difficult to get the current architecture to work like that. This will be slower but the big advantage is
 * that the Java and JSP syntax colouring (and maybe Java linking) can be outsourced.
 */
public class SourceViewControllerHandler extends ControllerHandler {
    private static final long serialVersionUID = 1L;

    public static final String PARAM_REPOSITORY_URL = "repositoryURL";

    public static final String PARAM_REPOSITORY_LINK_TEXT = "repositoryLinkText";

    @Override
    public boolean beforeFilter(ServletRequest request, ServletResponse response, FilterConfig conf,
            ServletObjects httpServletObjects) throws Exception {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI().replace(req.getContextPath(), "");
        SourceViewer sw = null;
        if (path.startsWith(Configuration.getMddViewerLocation())) {
            sw = new mddViewer(req);
        } else if (path.startsWith(Configuration.getJavaViewerLocation())) {
            sw = new javaViewer(req);
        } else if (path.startsWith(Configuration.getLogicDiscoveryViewerLocation())) {
            sw = new logicViewer(req);
        } else if (path.startsWith(Configuration.getCodeGeneratorLocation())) {
            sw = new GeneratedCodeViewer(req);
        } else if (path.endsWith(".jspx") || path.endsWith(".jsps") || path.endsWith(".jspxp")) {
            sw = new jspViewer(req);
        } else {
            return true;
        }

        PrintWriter w = res.getWriter();
        String servletPath = req.getServletPath();
        File dir = sw.getDirectory();
        if (dir == null) {
            res.setContentType("text/html");

            try {
                sw.parseText(w);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {// try to handle anyway
            if (DevelUtils.redirected(req, res, servletPath)) {
                return false;
            }
            if (sw instanceof GeneratedCodeViewer && req.getPathInfo().endsWith("/")) {
                // redirect browsing of directories to mdd Viewer
                res.sendRedirect(req.getContextPath() + "/dataDefinitions" + req.getPathInfo());
                return false;
            }

            // make a directory listing
            String relativeDirectory = dir.getName();
            if (dir.getAbsolutePath().indexOf("classes/") != -1) { // class viewer
                relativeDirectory = dir.getAbsolutePath().substring(dir.getAbsolutePath().indexOf("classes"));
            } else if (dir.getAbsolutePath().indexOf("dataDefinitions/") != -1) { // MDD viewer
                relativeDirectory = dir.getAbsolutePath().substring(dir.getAbsolutePath().indexOf("dataDefinitions"));
            }
            res.setContentType("text/html");
            printDirlistingHeader(w, dir.getCanonicalPath(), relativeDirectory);

            if (!(relativeDirectory.equals("classes") || relativeDirectory.equals("classes/dataDefinitions"))) {
                w.println("<b><a href=\"../\">../</a></b> (up one level)");
            }

            if (sw instanceof javaViewer) {
                // process and display directories
                processDirectory(w, dir, ".java");

                // process and display files
                String[] list = dir.list(new SuffixFileFilter(".java"));
                Arrays.sort(list);
                for (int i = 0; i < list.length; i++) {
                    String s = list[i];
                    w.println("<b><a href=\"" + s + "\">" + s + "</a></b>");
                }
            } else if (sw instanceof mddViewer) {
                // process and display directories
                processDirectory(w, dir, "dd");

                // process and display files
                String[] list = dir.list(new SuffixFileFilter(new String[] { ".idd", ".mdd" }));
                Arrays.sort(list);
                for (int i = 0; i < list.length; i++) {
                    String s = DevelUtils.getVirtualPath(req, Configuration.getMddViewerLocation()) + list[i];
                    s = s.substring(1, s.lastIndexOf(".")).replace('/', '.');
                    String addr = req.getContextPath() + Configuration.getMddViewerLocation() + "/" + s;
                    w.println("<a href=\"" + addr + "\">" + s + "</a>");
                }
            } else {
                java.util.logging.Logger.getLogger("org.makumba." + "devel").warning(
                    "Don't know how to handle viewer: " + sw + "(" + sw.getClass() + ")");
            }
            w.println("</pre>");
            DevelUtils.printDeveloperSupportFooter(w);
            w.println("</body></html>");
        }
        return false;
    }

    public static void printDirlistingHeader(PrintWriter w, String dir, String relativeDirectory) throws IOException {
        w.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
        w.println("<html><head><title>" + relativeDirectory + "</title>");
        w.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" >");

        w.println("</head><body bgcolor=white><table width=\"100%\" bgcolor=\"lightblue\"><tr><td rowspan=\"2\">");
        w.print("<font size=\"+2\"><a href=\".\"><font color=\"darkblue\">" + relativeDirectory + "</font></a></font>");
        w.print("<font size=\"-1\"><br>" + dir + "</font>");
        w.print("</td>");

        w.print("</tr></table>\n<pre style=\"margin-top:0\">");
    }

    public static void processDirectory(PrintWriter w, File dir, String extension) {
        File[] list = dir.listFiles(new DirectoriesExcludingRepositoriesFilter());
        Arrays.sort(list);
        for (int i = 0; i < list.length; i++) {
            if (extension == null || containsFilesWithExtension(list[i], extension)) {
                w.println("<b><a href=\"" + list[i].getName() + "/\">" + list[i].getName() + "/</a></b>");
            }
        }
    }

    static boolean containsFilesWithExtension(File dir, String... extension) {
        File[] files = dir.listFiles((FileFilter) new SuffixFileFilter(extension));
        // we first process only files, to decrease the amount of sub-directories we search
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                return true;
            }
        }
        files = dir.listFiles(new DirectoriesExcludingRepositoriesFilter());
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                if (containsFilesWithExtension(files[i], extension)) { // if not a dir with classes, continue the search
                    return true;
                }
            }
        }
        return false;
    }

    public static final class DirectoriesExcludingRepositoriesFilter implements FileFilter {
        public boolean accept(File f) {
            return f.isDirectory() && (!f.getName().equals("CVS") && !f.getName().equals(".svn"));
        }
    }

}
