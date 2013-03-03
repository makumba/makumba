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

package org.makumba.commons;

import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.makumba.db.makumba.UniquenessServlet;
import org.makumba.devel.DevelUtils;
import org.makumba.devel.relations.RelationCrawlerTool;
import org.makumba.forms.responder.ValueEditor;
import org.makumba.list.MakumbaDownloadServlet;
import org.makumba.providers.Configuration;
import org.makumba.providers.DeveloperTool;
import org.makumba.providers.MakumbaServlet;

/**
 * Handle access to makumba tools, like the {@link UniquenessServlet}.
 * 
 * @author Rudolf Mayer
 * @version $Id$
 */
public class MakumbaToolsControllerHandler extends ControllerHandler {
    @Override
    public boolean beforeFilter(ServletRequest req, ServletResponse res, FilterConfig conf,
            ServletObjects httpServletObjects) throws Exception {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = getPath(request);
        if (path.startsWith(Configuration.getServletLocation(MakumbaServlet.UNIQUENESS))) {
            new UniquenessServlet().doGet(request, response);
            return false;
        } else if (path.startsWith(Configuration.getServletLocation(MakumbaServlet.AUTOCOMPLETE))) {
            new AutoCompleteServlet().doGet(request, response);
            return false;
        } else if (path.startsWith(Configuration.getServletLocation(MakumbaServlet.RESOURCES))) {
            new MakumbaResourceServlet().doGet(request, response);
            return false;
        } else if (path.startsWith(Configuration.getServletLocation(MakumbaServlet.DOWNLOAD))) {
            new MakumbaDownloadServlet().doGet(request, response);
            return false;
        } else if (path.startsWith(Configuration.getServletLocation(MakumbaServlet.VALUE_EDITOR))) {
            new ValueEditor().doPost(request, response);
            return false;
        } else if (path.startsWith(Configuration.getServletLocation(MakumbaServlet.RELATION_CRAWLER))) {
            new RelationCrawlerTool().doPost(request, response);
            return false;
        } else if (path.startsWith(Configuration.getToolLocation(DeveloperTool.CACHE_CLEANER))) {
            // check if there is a specific cache
            String cacheName = request.getParameter("cacheName");
            ArrayList<String> cacheNames = NamedResources.getActiveCacheNames();
            cacheNames.add("all");
            if (StringUtils.isBlank(cacheName) || !cacheNames.contains(cacheName)) {
                // print a list of all caches to clean
                PrintWriter w = res.getWriter();
                res.setContentType("text/html");
                DevelUtils.writePageBegin(w);
                DevelUtils.writeStylesAndScripts(w,((HttpServletRequest) req).getContextPath());
                DevelUtils.writeTitleAndHeaderEnd(w, "Makumba Cache Cleaner");
                DevelUtils.printNavigationBegin(w, "Makumba Cache Cleaner");
                DevelUtils.printNavigationEnd(w);
                w.println("<h3>Select the cache to clean</h3>");
                for (String string : cacheNames) {
                    w.println("<a href=\"?cacheName=" + string + "\">" + string + "</a><br/>");
                }
                DevelUtils.writePageEnd(w);
                w.close();
            } else if (cacheName.equals("all")) {
                // reload the complete cache
                java.util.logging.Logger.getLogger("org.makumba.system").info(
                    "Cleaning makumba caches, triggered from Makumba Tools");
                NamedResources.cleanupStaticCaches();
                DevelUtils.printResponseMessage(res, "Makumba Cache Cleaner", "<br/><br/>Cleaned Makumba caches.");
            } else {
                // check if the cache type exists
                NamedResources.cleanStaticCache(cacheName);
                DevelUtils.printResponseMessage(res, "Makumba Cache Cleaner", "<br/><br/>Cleaned Makumba cache '"
                        + cacheName + "'.");
            }

            return false;
        } else if (path.startsWith(Configuration.getMakumbaToolsLocation())) {
            // redirect if we have a unknown path
            if (!path.equals(Configuration.getMakumbaToolsLocation() + "/")) {
                response.sendRedirect(request.getContextPath() + Configuration.getMakumbaToolsLocation() + "/");
                return false;
            }
            // a page showing all makumba tools, and their location
            PrintWriter w = res.getWriter();
            res.setContentType("text/html");
            DevelUtils.writePageBegin(w);
            DevelUtils.writeStyles(w, request.getContextPath());
            DevelUtils.writeTitleAndHeaderEnd(w, "Makumba Configuration");
            DevelUtils.printNavigationBegin(w, "Makumba Configuration");
            DevelUtils.printNavigationEnd(w);

            w.println("  <h3>Welcome to the Makumba Configuration page!</h3>");
            w.println("  <p>This page gives you a short overview on the configuration of this Makumba installation and basic information on the tools available.</p>");

            writeSectionHeader(w, "Location", "Makumba Tools");
            for (DeveloperTool t : DeveloperTool.values()) {
                writeDescr(w, t.getName(), t.getDescription(), t.getKey(), Configuration.getToolLocation(t),
                    request.getContextPath());
            }
            w.println("</table>");

            writeSectionHeader(w, "Location", "Makumba servlets");

            for (MakumbaServlet s : MakumbaServlet.values()) {
                writeDescr(w, s.getName(), s.getDescription(), s.getKey(), Configuration.getServletLocation(s),
                    request.getContextPath());
            }

            w.println("  </tbody>");
            w.println("</table>");

            writeSectionHeader(w, "Value", "Controller settings");
            writeDescr(w, "Form reload", "Whether forms shall be reloaded on validation errors",
                Configuration.KEY_RELOAD_FORM_ON_ERROR, Configuration.getReloadFormOnErrorDefault());
            writeDescr(w, "Clientside validation",
                "Whether client-side validation is enabled, and if it is live or on form submission",
                Configuration.KEY_CLIENT_SIDE_VALIDATION, Configuration.getClientSideValidationDefault());
            w.println("  </tbody>");
            w.println("</table>");

            writeSectionHeader(w, "Value", "Input style settings");
            writeDescr(w, "Calendar editor",
                "Whether the calendar-editor will be displayed by default for mak:input on date types",
                Configuration.KEY_CALENDAR_EDITOR, Configuration.getCalendarEditorDefault());
            writeDescr(w, "Calendar editor link", "The default link created for the calendar editor",
                Configuration.KEY_CALENDAR_EDITOR_LINK,
                StringEscapeUtils.escapeHtml(Configuration.getDefaultCalendarEditorLink(request.getContextPath())));
            w.println("  </tbody>");
            w.println("</table>");

            writeSectionHeader(w, "Value", "Other settings");
            writeDescr(w, "Database layer",
                "The default database layer to use for Transactions (Makumba or Hibernate)",
                Configuration.KEY_DEFAULT_DATABASE_LAYER, Configuration.getDefaultDatabaseLayer());
            writeDescr(w, "Query Function Inliner provider", "The module inlining MDD functions",
                Configuration.KEY_QUERYFUNCTIONINLINER, Configuration.getQueryInliner());
            writeDescr(w, "Repository URL",
                "The URL prefix to compose links from the source code viewers to the source code repository",
                Configuration.KEY_REPOSITORY_URL, Configuration.getRepositoryURL());
            writeDescr(w, "Repository Link Text", "The text displayed on the repository URL link",
                Configuration.KEY_REPOSITORY_LINK_TEXT, Configuration.getRepositoryLinkText());
            w.println("  </tbody>");
            w.println("</table>");

            w.println("</div>");
            DevelUtils.writePageEnd(w);
            w.close();
            return false;
        } else {
            return true;
        }
    }

    private void writeSectionHeader(PrintWriter w, String columnName, String sectionName) {
        w.println("<h4>" + sectionName + "</h4>");
        w.println("<table class=\"table table-bordered table-condensed\">");
        w.println("  <thead>");
        w.println("    <tr>");
        w.println("      <th>Name</th>");
        w.println("      <th>Description</th>");
        w.println("      <th>Config file key</th>");
        w.println("      <th>" + columnName + "</th>");
        w.println("    </tr>");
        w.println("  </thead>");
        w.println("  </tbody>");
    }

    private void writeDescr(PrintWriter w, final String name, final String desc, final String key, Object value) {
        w.println("  <tr>");
        w.println("    <td>" + name + "</td> <td>" + desc + "</td> <td>" + key + "</td> <td>" + value + "</td>");
        w.println("  </tr>");
    }

    private void writeDescr(PrintWriter w, final String name, final String desc, final String key, String loc,
            String contextPath) {
        w.println("  <tr>");
        String link = loc.equals(Configuration.PROPERTY_NOT_SET) ? "-- disabled --" : "<a href=\"" + contextPath + loc
                + "\">" + contextPath + loc + "</a>";
        w.println("    <td>" + name + "</td> <td>" + desc + "</td> <td>" + key + "</td> <td> " + link + " </td>");
        w.println("  </tr>");
    }
}
