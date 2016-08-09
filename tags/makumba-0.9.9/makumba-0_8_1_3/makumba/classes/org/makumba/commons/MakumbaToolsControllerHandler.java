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
            w.println("<h4>Makumbat Tools</h4>");
            w.println("<table border=\"1\" _width=\"100%\">");
            w.println("  <tr>");
            w.println("    <th>Name</th>");
            w.println("    <th>Description</th>");
            w.println("    <th>Config file key</th>");
            w.println("    <th>Location</th>");
            w.println("  </tr>");
            // w.println("  <tr> <td colspan=\"5\"> <h5>Makumba Tools</h5> </td> </tr>");
            writeDescr(w, "Download", "Download of file-type data", Configuration.KEY_MAKUMBA_DOWNLOAD,
                Configuration.getMakumbaDownloadLocation(), request.getContextPath());
            writeDescr(w, "Resources",
                "Resources (javaScript, images,...) needed for calendar editor, live-validation, ...",
                Configuration.KEY_MAKUMBA_RESOURCES, Configuration.getMakumbaResourcesLocation(),
                request.getContextPath());
            writeDescr(w, "Uniqueness", "AJAX uniqueness check", Configuration.KEY_MAKUMBA_UNIQUENESS_VALIDATOR,
                Configuration.getMakumbaUniqueLocation(), request.getContextPath());
            writeDescr(w, "Value Editor", "Tool for edit-in-place", Configuration.KEY_MAKUMBA_VALUE_EDITOR,
                Configuration.getMakumbaValueEditorLocation(), request.getContextPath());
            // w.println("  <tr> <td colspan=\"5\"> <h3>Makumba Developer Tools</h3> </td> </tr>");
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
            writeDescr(w, "Reference Checker", "Check for broken references and show status of foreign key creation",
                Configuration.KEY_REFERENCE_CHECKER, Configuration.getReferenceCheckerLocation(),
                request.getContextPath());
            w.println("</table>");

            w.println("<h4>Controller settings</h4>");
            w.println("<table border=\"1\" _width=\"100%\">");
            w.println("  <tr>");
            w.println("    <th>Name</th>");
            w.println("    <th>Description</th>");
            w.println("    <th>Config file key</th>");
            w.println("    <th>Value</th>");
            w.println("  </tr>");
            writeDescr(w, "Transaction Provider", "", Configuration.KEY_DEFAULT_TRANSACTION_PROVIDER, "");
            writeDescr(w, "Form reaload", "Wether forms shall be reloaded on validation errors",
                Configuration.KEY_RELOAD_FORM_ON_ERROR, Configuration.getReloadFormOnErrorDefault());
            writeDescr(w, "Clientside validation",
                "Wether client-side validation is enabled, and if it is live or on form submission",
                Configuration.KEY_CLIENT_SIDE_VALIDATION, Configuration.getClientSideValidationDefault());
            w.println("</table>");

            w.println("<h4>Input style settings</h4>");

            DevelUtils.writePageEnd(w);
            return false;
        } else {
            return true;
        }
    }

    private void writeDescr(PrintWriter w, final String name, final String desc, final String key, Object value) {
        w.println("  <tr>");
        w.println("    <td>" + name + "</td> <td>" + desc + "</td> <td>" + key + "</td> <td>" + value + "</td>");
        w.println("  </tr>");
    }

    private void writeDescr(PrintWriter w, final String name, final String desc, final String key, String loc,
            String contextPath) {
        w.println("  <tr>");
        w.println("    <td>" + name + "</td> <td>" + desc + "</td> <td>" + key + "</td> <td> <a href=\"" + contextPath
                + loc + "\">" + contextPath + loc + "</a> </td>");
        w.println("  </tr>");
    }

}
