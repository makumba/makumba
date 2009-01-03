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
//  $Id: SourceViewControllerHandler.java 3224 2008-10-05 22:32:17Z rosso_nero $
//  $Name$
/////////////////////////////////////

package org.makumba.commons;

import java.io.PrintWriter;

import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.makumba.db.makumba.UniquenessServlet;
import org.makumba.devel.DevelUtils;
import org.makumba.devel.relations.RelationCrawlerTool;
import org.makumba.forms.responder.ValueEditor;
import org.makumba.list.MakumbaDownloadServlet;
import org.makumba.providers.Configuration;

/**
 * Handle access to makumba tools, like the {@link UniquenessServlet}.
 * 
 * @author Rudolf Mayer
 * @version $Id: MakumbaToolsControllerHandler.java,v 1.1 Sep 4, 2008 1:33:31 AM rudi Exp $
 */
public class MakumbaToolsControllerHandler extends ControllerHandler {
    public boolean beforeFilter(ServletRequest req, ServletResponse res, FilterConfig conf,
            ServletObjects httpServletObjects) throws Exception {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI().replace(request.getContextPath(), "");
        if (path.startsWith(Configuration.getMakumbaUniqueLocation())) {
            new UniquenessServlet().doGet(request, response);
            return false;
        } else if (path.startsWith(Configuration.getMakumbaAutoCompleteLocation())) {
            new AutoCompleteServlet().doGet(request, response);
            return false;
        } else if (path.startsWith(Configuration.getMakumbaResourcesLocation())) {
            new MakumbaResourceServlet().doGet(request, response);
            return false;
        } else if (path.startsWith(Configuration.getMakumbaDownloadLocation())) {
            new MakumbaDownloadServlet().doGet(request, response);
            return false;
        } else if (path.startsWith(Configuration.getMakumbaValueEditorLocation())) {
            new ValueEditor().doPost(request, response);
            return false;
        } else if (path.startsWith(Configuration.getMakumbaRelationCrawlerLocation())) {
            new RelationCrawlerTool().doPost(request, response);
            return false;
        } else if (path.startsWith(Configuration.getMakumbaCacheCleanerLocation())) {
            // reload the cache
            java.util.logging.Logger.getLogger("org.makumba.system").info(
                "Cleaning makumba caches, triggered from Makumba Tools");
            NamedResources.cleanCaches();
            DevelUtils.printResponseMessage(res, "Makumba Cache Cleaner", "<br/><br/>Cleaned Makumba caches.");
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
            DevelUtils.printPageHeader(w, "Makumba Configuration");
            w.println("</table>");
            w.println("<h3>Welcome to the Makumba Configuration page!</h3>");
            w.println("<p>This page gives you a short overview on the configuration of this Makumba installation and basic information on the tools available.</p>");

            writeSectionHeader(w, "Location", "Makumba Tools");
            writeDescr(w, "Download", "Download of file-type data", Configuration.KEY_MAKUMBA_DOWNLOAD,
                Configuration.getMakumbaDownloadLocation(), request.getContextPath());
            writeDescr(w, "Resources",
                "Resources (javaScript, images,...) needed for calendar editor, live-validation, ...",
                Configuration.KEY_MAKUMBA_RESOURCES, Configuration.getMakumbaResourcesLocation(),
                request.getContextPath());
            writeDescr(w, "Uniqueness", "AJAX uniqueness check", Configuration.KEY_MAKUMBA_UNIQUENESS_VALIDATOR,
                Configuration.getMakumbaUniqueLocation(), request.getContextPath());
            writeDescr(w, "Autocomplete", "AJAX autcomplete", Configuration.KEY_MAKUMBA_AUTOCOMPLETE,
                Configuration.getMakumbaAutoCompleteLocation(), request.getContextPath());
            writeDescr(w, "Value Editor", "Tool for edit-in-place", Configuration.KEY_MAKUMBA_VALUE_EDITOR,
                Configuration.getMakumbaValueEditorLocation(), request.getContextPath());
            writeDescr(w, "DataDefinition viewer", "View data definitions", Configuration.KEY_MDD_VIEWER,
                Configuration.getMddViewerLocation(), request.getContextPath());
            writeDescr(w, "Java Viewer", "View Java Business Logics", Configuration.KEY_JAVA_VIEWER,
                Configuration.getJavaViewerLocation(), request.getContextPath());
            writeDescr(w, "Logic Discover", "View Business Logics associated with a certain page",
                Configuration.KEY_LOGIC_DISCOVERY, Configuration.getLogicDiscoveryViewerLocation(),
                request.getContextPath());
            writeDescr(w, "Data lister", "List data from a certain type", Configuration.KEY_DATA_LISTER,
                Configuration.getDataListerLocation(), request.getContextPath());
            writeDescr(w, "Object viewer", "View a specific object", Configuration.KEY_DATA_OBJECT_VIEWER,
                Configuration.getDataViewerLocation(), request.getContextPath());
            writeDescr(w, "Data query", "Free-form OQL queries", Configuration.KEY_DATA_QUERY_TOOL,
                Configuration.getDataQueryLocation(), request.getContextPath());
            writeDescr(w, "Pointer value converter", "Convert pointer values between internal/external/DB form",
                Configuration.KEY_OBJECT_ID_CONVERTER, Configuration.getObjectIdConverterLocation(),
                request.getContextPath());
            writeDescr(w, "Code generator", "Generate forms & lists from data definitions",
                Configuration.KEY_CODE_GENERATOR, Configuration.getCodeGeneratorLocation(), request.getContextPath());
            writeDescr(w, "Reference Checker",
                "Checks creation the status of foreign and unique keys and displays broken references",
                Configuration.KEY_REFERENCE_CHECKER, Configuration.getReferenceCheckerLocation(),
                request.getContextPath());
            writeDescr(w, "Relation Crawler", "Runs a detection of file relations", Configuration.KEY_RELATION_CRAWLER,
                Configuration.getMakumbaRelationCrawlerLocation(), request.getContextPath());
            writeDescr(w, "Makumba Cache Cleaner",
                "Cleans all internal Makumba caches, like queries, data-definitions.<br/>"
                        + "Useful during development, to avoid having to restart the servlet container.",
                Configuration.KEY_MAKUMBA_CACHE_CLEANER, Configuration.getMakumbaCacheCleanerLocation(),
                request.getContextPath());
            w.println("</table>");

            writeSectionHeader(w, "Value", "Controller settings");
            writeDescr(w, "Database layer", "", Configuration.KEY_DEFAULT_DATABASE_LAYER, "The default database layer to use for Transactions (Makumba or Hibernate)");
            writeDescr(w, "Form reload", "Whether forms shall be reloaded on validation errors",
                Configuration.KEY_RELOAD_FORM_ON_ERROR, Configuration.getReloadFormOnErrorDefault());
            writeDescr(w, "Clientside validation",
                "Whether client-side validation is enabled, and if it is live or on form submission",
                Configuration.KEY_CLIENT_SIDE_VALIDATION, Configuration.getClientSideValidationDefault());
            w.println("</table>");

            writeSectionHeader(w, "Value", "Input style settings");
            writeDescr(w, "Calendar editor",
                "Whether the calendar-editor will be displayed by default for mak:input on date types",
                Configuration.KEY_CALENDAR_EDITOR, Configuration.getCalendarEditorDefault());
            writeDescr(w, "Calendar editor link", "The default link created for the calendar editor",
                Configuration.KEY_CALENDAR_EDITOR_LINK,
                StringEscapeUtils.escapeHtml(Configuration.getDefaultCalendarEditorLink(request.getContextPath())));
            w.println("</table>");

            writeSectionHeader(w, "Value", "Other settings");
            writeDescr(w, "Repository URL",
                "The URL prefix to compose links from the source code viewers to the source code repository",
                Configuration.KEY_REPOSITORY_URL, Configuration.getRepositoryURL());
            writeDescr(w, "Repository Link Text", "The text displayed on the repository URL link",
                Configuration.KEY_REPOSITORY_LINK_TEXT, Configuration.getRepositoryLinkText());
            w.println("</table>");

            DevelUtils.writePageEnd(w);
            w.close();
            return false;
        } else {
            return true;
        }
    }

    private void writeSectionHeader(PrintWriter w, String columnName, String sectionName) {
        w.println("<h4>" + sectionName + "</h4>");
        w.println("<table border=\"1\" _width=\"100%\">");
        w.println("  <tr>");
        w.println("    <th>Name</th>");
        w.println("    <th>Description</th>");
        w.println("    <th>Config file key</th>");
        w.println("    <th>" + columnName + "</th>");
        w.println("  </tr>");
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
