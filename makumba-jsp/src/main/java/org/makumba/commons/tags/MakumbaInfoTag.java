// /////////////////////////////
//Makumba, Makumba tag library
//Copyright (C) 2000-2003 http://www.makumba.org
//
//This library is free software; you can redistribute it and/or
//modify it under the terms of the GNU Lesser General Public
//License as published by the Free Software Foundation; either
//version 2.1 of the License, or (at your option) any later version.
//
//This library is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//Lesser General Public License for more details.
//
//You should have received a copy of the GNU Lesser General Public
//License along with this library; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//-------------
//$Id$
//$Name$
/////////////////////////////////////

package org.makumba.commons.tags;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;
import org.makumba.MakumbaSystem;
import org.makumba.Text;
import org.makumba.commons.Base64;
import org.makumba.commons.ReadableFormatter;
import org.makumba.db.makumba.MakumbaTransactionProvider;
import org.makumba.providers.TransactionProvider;

/**
 * Implementation of the mak:info tag, which displays information about the application server environment, the current
 * session, etc.
 * 
 * @author Rudolf Mayer
 * @version $Id$
 */
public class MakumbaInfoTag extends TagSupport {
    private static final long serialVersionUID = 1L;

    private int line = 0;

    String applicationProperties;

    Properties sysprops = System.getProperties();

    /**
     * @param applicationProperties
     *            The applicationProperties to set.
     */
    public void setApplicationProperties(String applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Override
    public int doStartTag() throws JspException {
        try {
            Properties projectProperties = new Properties();
            try {
                if (applicationProperties != null) {
                    projectProperties.load(new FileInputStream(applicationProperties));
                }
            } catch (IOException e) {
                System.err.println("IGNORED " + e);
            }

            JspWriter out = pageContext.getOut();
            out.println("<style type=\"text/css\"> h1,h2,h3,h4 {color: blue; margin-bottom:0px}</style>");
            out.println("<h1>Makumba System Information (&lt;mak:info /&gt;)</h1>");

            final String startupProp = "startupTime";

            final DateFormat df = new SimpleDateFormat("dd MMM yyyy HH:mm");
            if (System.getProperty(startupProp) == null) {
                System.setProperty(startupProp, df.format(MakumbaSystem.loadingTime));
            }

            Runtime rt = java.lang.Runtime.getRuntime();

            long maxHeap = rt.maxMemory();
            String initialHeap = System.getProperty("tomcat.jvm.initial_memory");
            if (initialHeap == null) { // not in system props? try in application properties
                initialHeap = projectProperties.getProperty("tomcat.jvm.initial_memory");
                if (initialHeap == null) {
                    initialHeap = "N/A";
                }
            }

            String username = System.getProperty("tomcat.manager.user");
            if (username == null) { // not in system props? try in application properties
                username = projectProperties.getProperty("tomcat.manager.user");
            }

            String password = System.getProperty("tomcat.manager.pass");
            if (password == null) { // not in system props? try in application properties
                password = projectProperties.getProperty("tomcat.manager.pass");
            }

            String hotspot = System.getProperty("tomcat.jvm.hotspot");
            if (hotspot == null) { // not in system props? try in application properties
                hotspot = projectProperties.getProperty("tomcat.jvm.hotspot");
                if (hotspot == null) {
                    hotspot = "N/A";
                }
            }

            String tomcatLocation = System.getProperty("tomcat.manager.location");
            if (tomcatLocation == null) { // not in system props? try in application properties
                tomcatLocation = projectProperties.getProperty("tomcat.manager.location");
            }

            HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
            HttpSession session = request.getSession(true);
            int port = request.getServerPort();

            String tomcatPort = System.getProperty("tomcat.manager.port");
            if (tomcatPort == null) { // not in system props? try in application properties
                tomcatPort = projectProperties.getProperty("tomcat.manager.port");
            }
            if (StringUtils.isBlank(tomcatPort)) {
                tomcatPort = String.valueOf(port);
            }

            // if the manager application is installed, and the .\">Unknown</span>
            String activeSessions = null;
            String activeSessionsTitle = null;

            String serverInfo = session.getServletContext().getServerInfo();

            if (serverInfo.indexOf("Tomcat") != -1) { // we have a tomcat
                if (username != null && password != null) { // we have a user and password
                    String tomcatLoc = tomcatLocation != null ? tomcatLocation : "";
                    String managerLocation = "http://localhost:" + tomcatPort + (tomcatLoc.startsWith("/") ? "" : "/")
                            + tomcatLoc + (tomcatLoc.endsWith("/") ? "" : "/") + "manager/list";
                    try { // --> connect
                        HttpURLConnection uc = (HttpURLConnection) new URL(managerLocation).openConnection();
                        uc.setRequestProperty("connection", "close");
                        uc.setRequestProperty("Authorization", "Basic "
                                + Base64.encode((username + ":" + password).getBytes()));
                        uc.setUseCaches(false);
                        uc.connect();
                        if (uc.getResponseCode() != 200) {
                            throw new RuntimeException(uc.getResponseMessage());
                        }
                        if (uc.getContentLength() == 0) {
                            throw new RuntimeException("zero content received");
                        }
                        StringWriter sw = new StringWriter();
                        InputStreamReader ir = new InputStreamReader(uc.getInputStream());
                        char[] buf = new char[1024];
                        int n;
                        while ((n = ir.read(buf)) != -1) {
                            sw.write(buf, 0, n);
                        }
                        String list = sw.toString();
                        String marker = request.getContextPath();
                        if (marker.length() == 0) {
                            marker = "/";
                        }
                        marker = marker + ":running:";
                        int found = list.indexOf(marker) + marker.length();
                        if (found == -1) {
                            throw new RuntimeException("context not found");
                        }
                        activeSessions = list.substring(found, list.indexOf(":", found + 1));
                    } catch (Throwable t) {
                        out.println(" <p>could connect to " + managerLocation + ": " + t.getMessage() + " </p>");
                    }
                } else {
                    activeSessionsTitle = "Could not authenticate: system properties or custom properties "
                            + applicationProperties + " must specify 'tomcat.manager.user' and 'tomcat.manager.pass'!";
                }
            } else {
                activeSessionsTitle = "This feature is currentely only supported for Apache Tomcat!";
            }

            if (activeSessions == null) {
                activeSessions = "<span title=\"" + activeSessionsTitle + "\">Unknown</span>";
            }

            out.println("<table border=\"0\" bgcolor=\"white\" cellspacing=\"3\" cellpadding=\"3\" >");
            out.println("  <tr>");
            out.println("    <td valign=\"top\" nowrap=\"nowrap\"><font size=\"+1\"><b>Client:</td>");

            out.println("    <td>");
            out.println("      <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");

            printSystemInfoRow(out, "Browser", request.getHeader("User-Agent"));
            printSystemInfoRow(out, "Host (IP)", request.getRemoteHost() + "(" + request.getRemoteAddr() + ")");

            out.println("      </table>");
            out.println("    </td>");
            out.println("  </tr>");
            out.println("  <tr>");
            out.println("    <td valign=\"top\"><font size=\"+1\"><b>Server:</b></font> JVM mode: <b>" + hotspot);
            out.println("    <td>");
            out.println("      <table border=\"0\" cellspacing=\"3\" cellpadding=\"3\">");

            String serverUpSince = null;
            try {
                serverUpSince = ReadableFormatter.readableAge(new Date().getTime()
                        - df.parse(System.getProperty(startupProp)).getTime());
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            String serverURL = request.getScheme() + "://" + request.getServerName() + ":" + port;

            line = 0;
            printSystemInfoRow(out, "Initial heap (-Xms)", initialHeap);
            printSystemInfoRow(out, "Max heap (-Xmx)", ReadableFormatter.readableBytes(maxHeap));
            printSystemInfoRow(out, "Current heap size", ReadableFormatter.readableBytes(rt.totalMemory()));
            printSystemInfoRow(out, "Heap in use", ReadableFormatter.readableBytes(rt.totalMemory() - rt.freeMemory()));
            printSystemInfoRow(out, "Free heap", ReadableFormatter.readableBytes(rt.freeMemory()));
            printSystemInfoRow(out, "Server is up since", System.getProperty(startupProp) + "</b>(" + serverUpSince
                    + " ago)");
            printSystemInfoRow(out, "Last application (re)load", df.format(MakumbaSystem.loadingTime) + "("
                    + ReadableFormatter.readableAge(new Date().getTime() - MakumbaSystem.loadingTime.getTime())
                    + " ago)");
            printSystemInfoRow(out, "Server time", df.format(new Date()));
            printSystemInfoRow(out, "Active sessions", activeSessions);
            printSystemInfoRow(out, "Server protocol, name and port", "<a href=\"" + serverURL + "\">" + serverURL
                    + "</a>");
            printSystemInfoRow(out, "Server software", pageContext.getServletContext().getServerInfo());
            out.println("        <tr bgcolor=\"#" + (line++ % 2 == 0 ? "eeeeee" : "ffffff") + "\">");
            out.print("          <td colspan=\"2\">");
            if (StringUtils.equals(request.getParameter("garbageCollection"), "true")) {
                System.gc();
                out.print("Performed garbage collection. <a href=\"?\">Run without</a>");
            } else {
                out.print("<a href=\"?garbageCollection=true\">Request garbage collection</a>");
            }
            out.println("          </td>");
            out.println("        </tr>");
            out.println("      </table>");
            out.println("    </td>");
            out.println("  </tr>");
            out.println("</table>");

            if (applicationProperties != null) {
                out.println("<h2>Application specific properties <span style=\"font-size:smaller\">("
                        + applicationProperties + ")</span> </h2>");
                try {
                    projectProperties.load(new FileInputStream(applicationProperties));
                    printProperties(out, projectProperties);
                } catch (IOException io) {
                    out.println("<p style=\"color: red;\">Could not find application specific properties file <i>'"
                            + applicationProperties + "'</i> in the current directory '"
                            + new File("").getAbsolutePath() + "'</p>");
                }
            }

            out.println();
            out.println("<h2><a href=\"/makumba-docs/\"> Makumba</a></h2>");
            out.println("<table border=\"0\" cellspacing=\"3\" cellpadding=\"3\">");
            out.println("  <tr bgcolor=\"#cccccc\"> <th>Property</th> <th>Value</th> </tr>");

            String dbname = TransactionProvider.getInstance().getDefaultDataSourceName();

            line = 0;
            printMakumbaPropertyRow(out, "<a href=\"/makumba-docs/CHANGELOG.txt\">version</a>",
                MakumbaSystem.getVersion());
            printMakumbaPropertyRow(out, "Default datasource name", dbname);
            printMakumbaPropertyRow(out, "DBSV", MakumbaTransactionProvider.getDatabaseProperty(dbname, "dbsv"));
            printMakumbaPropertyRow(out, "Size of the connection pool", MakumbaTransactionProvider.getDatabaseProperty(
                dbname, "resource_pool_size"));
            printMakumbaPropertyRow(out, "Number of busy connections", MakumbaTransactionProvider.getDatabaseProperty(
                dbname, "jdbc_connections"));
            printMakumbaPropertyRow(out, "Unused connections in the pool",
                MakumbaTransactionProvider.getDatabaseProperty(dbname, "idle_connections"));
            printMakumbaPropertyRow(out, "SQL engine and version", MakumbaTransactionProvider.getDatabaseProperty(
                dbname, "sql_engine.name"));
            printMakumbaPropertyRow(out, "JDBC driver and version", MakumbaTransactionProvider.getDatabaseProperty(
                dbname, "jdbc_driver.name")
                    + " " + MakumbaTransactionProvider.getDatabaseProperty(dbname, "jdbc_driver.version"));

            out.println("</table>");

            out.println("<h3>Makumba caches: </h3>");
            out.println("<table border=\"0\" cellspacing=\"5\" cellpadding=\"3\">");
            out.println("  <tr> <th>Name</th> <th>size</th> <th>hits</th> <th>misses</th> </tr>");

            line = 0;
            Map<String, int[]> m = MakumbaSystem.getCacheInfo();
            TreeSet<String> treeSet = new TreeSet<String>(m.keySet());

            for (String nm : treeSet) {
                Object o = m.get(nm);

                out.println("  <tr bgcolor=\"#" + (line++ % 2 == 0 ? "eeeeee" : "ffffff") + "\">");
                out.println("    <td>" + nm + "</td>");

                if (o instanceof int[]) {
                    int[] intArray = (int[]) o;
                    for (int element : intArray) {
                        out.println("    <td align=right><code>" + element + "</code></td>");
                    }
                } else {
                    out.println("    <td align=right><code>" + o + "</code></td>");
                }
                out.println("  </tr>");
            }

            out.println("</table>");

            out.println("<h2>User Session</h2>");
            out.print("Created: " + ReadableFormatter.readableAge(new Date().getTime() - session.getCreationTime())
                    + " ago ");
            out.println("(" + session.getCreationTime() + ")<br/>");
            out.print("Last Accessed: "
                    + ReadableFormatter.readableAge(new Date().getTime() - session.getLastAccessedTime()) + " ago ");
            out.println("(" + session.getLastAccessedTime() + ")<br>");
            out.print("Max inactive interval:" + ReadableFormatter.readableAge(session.getMaxInactiveInterval() * 1000));
            out.println("(" + session.getMaxInactiveInterval() + ")<br>");

            @SuppressWarnings("unchecked")
            Enumeration<String> attribs = session.getAttributeNames();
            out.println("<table border=\"0\" cellspacing=\"3\" cellpadding=\"3\">");
            out.println("  <tr bgcolor=\"#cccccc\"> <th>Attribute</th> <th>Value</th> <th>Class</th> </tr>");

            line = 0;
            while (attribs.hasMoreElements()) {
                String key = attribs.nextElement();

                out.println("  <tr bgcolor=\"#" + (line++ % 2 == 0 ? "eeeeee" : "ffffff") + "\">");
                out.println("    <td valign=\"top\">" + key + ":</td>");
                Object o = session.getAttribute(key);
                if (o instanceof Text) {
                    o = ((Text) o).toShortString(100);
                }
                out.println("    <td><pre>" + o + "</pre></td>");
                out.println("    <td>" + session.getAttribute(key).getClass().getName() + "</td>");
                out.println("  </tr>");
            }
            out.println("</table>");

            out.println("<h2>Java Virtual Machine properties</h2>");

            printProperties(out, sysprops);
        } catch (java.io.IOException e) {
            throw new JspException(e.getMessage());
        }
        return EVAL_BODY_INCLUDE;
    }

    private void printProperties(JspWriter out, Properties props) throws IOException {
        @SuppressWarnings("unchecked")
        Enumeration<String> enprop = (Enumeration<String>) props.propertyNames();

        out.println("<table border=\"0\" cellspacing=\"3\" cellpadding=\"3\">");
        out.println("  <tr bgcolor=\"#cccccc\"> <th>Property</th> <th>Value</th> </tr>");

        line = 0;
        while (enprop.hasMoreElements()) {
            String key = enprop.nextElement();
            out.println("  <tr bgcolor=\"#" + (line++ % 2 == 0 ? "eeeeee" : "ffffff") + "\">");
            out.println("    <td valign=\"top\">" + key + "</td>");
            out.print("    <td><pre>");
            if (key != null) {
                if (key.equals("tomcat.manager.pass")) { // we don't want to display the password to everyone
                    out.print("*******");
                } else if (key.endsWith("path")) {
                    out.print(props.getProperty(key).replace(sysprops.getProperty("path.separator").charAt(0), '\n'));
                } else if (props.getProperty(key).startsWith("http://")) {
                    out.print("<a href=" + props.getProperty(key) + ">" + props.getProperty(key) + "</a>");
                } else {
                    out.print(props.getProperty(key));
                }
            }
            out.println("</pre></td>");
            out.println("  </tr>");
        }

        out.println("</table>");
    }

    public void printMakumbaPropertyRow(JspWriter out, String key, String value) throws IOException {
        StringBuffer sb = new StringBuffer();
        sb.append("  <tr bgcolor=\"#" + (line++ % 2 == 0 ? "eeeeee" : "ffffff") + "\">");
        sb.append("    <td>" + key + "</td>");
        sb.append("    <td><code>" + value + "</code></td>");
        sb.append("  </tr>");
        out.println(sb);
    }

    public void printSystemInfoRow(JspWriter out, String key, String value) throws IOException {
        StringBuffer sb = new StringBuffer();
        sb.append("        <tr bgcolor=\"#" + (line++ % 2 == 0 ? "eeeeee" : "ffffff") + "\">");
        sb.append("          <td>" + key + ":&nbsp;&nbsp;&nbsp;</td>");
        sb.append("          <td>" + value + "</td>");
        sb.append("        </tr>");
        out.println(sb);
    }
}
