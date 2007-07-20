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

package org.makumba.view.jsptaglib;

import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.makumba.MakumbaSystem;
import org.makumba.util.Base64;
import org.makumba.util.ReadableFormatter;

/**
 * mak:info tag
 * @author 
 * @version $Id$
 *
 */
public class MakumbaInfoTag extends TagSupport {
    public int doStartTag() throws JspException {
        try {
            pageContext.getOut()
                    .print("<style type=\"text/css\"> h1,h2,h3,h4 {color: blue; margin-bottom:0px}</style>");
            final String startupProp = "startupTime";

            DateFormat uptime = new SimpleDateFormat("dd MMM yyyy HH:mm");
            if (System.getProperty(startupProp) == null)
                System.setProperty(startupProp, uptime.format(MakumbaSystem.loadingTime));
            // TODO detect initial memory and hotspot property

            int ln;
            Runtime rt = java.lang.Runtime.getRuntime();

            long maxHeap = rt.maxMemory();
            // String initialHeap = System.getProperty("tomcat.jvm.initial_memory");
            String username = System.getProperty("tomcat.manager.user");
            String password = System.getProperty("tomcat.manager.pass");
            String port = System.getProperty("http.port");
            String hotspot = System.getProperty("tomcat.jvm.hotspot");
            String activeSessions = "?";

            // pageContext.getOut().print(maxHeap+":"+initialHeap+":"+username+":"+password+":"+port+":");

            HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
            HttpSession session = request.getSession(true);

            try {
                HttpURLConnection uc = (HttpURLConnection) (new URL("http://localhost:" + port + "/manager/list"))
                        .openConnection();
                uc.setRequestProperty("connection", "close");
                uc
                        .setRequestProperty("Authorization", "Basic "
                                + Base64.encode((username + ":" + password).getBytes()));
                uc.setUseCaches(false);
                uc.connect();
                if (uc.getResponseCode() != 200)
                    throw new RuntimeException(uc.getResponseMessage());
                if (uc.getContentLength() == 0)
                    throw new RuntimeException("content zero");
                StringWriter sw = new StringWriter();
                InputStreamReader ir = new InputStreamReader(uc.getInputStream());
                char[] buf = new char[1024];
                int n;
                while ((n = ir.read(buf)) != -1)
                    sw.write(buf, 0, n);
                String list = sw.toString();
                String marker = request.getContextPath();
                if (marker.length() == 0)
                    marker = "/";
                marker = marker + ":running:";
                int found = list.indexOf(marker) + marker.length();
                if (found == -1)
                    throw new RuntimeException("context not found");
                activeSessions = list.substring(found, list.indexOf(":", found + 1));
            } catch (Throwable t) {
                pageContext.getOut().print(" <p>could connect to /manager/list </p>");
            }

            pageContext
                    .getOut()
                    .print(
                            "<h1>System Information</h1><table border=0 bgcolor=\"white\"><tr><td valign=top><font size=+1><b>Client: <td>Browser:");
            pageContext.getOut().print(request.getHeader("User-Agent"));
            pageContext.getOut().print("<br>Host (IP):");
            pageContext.getOut().print(request.getRemoteHost());
            pageContext.getOut().print("(");
            pageContext.getOut().print(request.getRemoteAddr());
            pageContext.getOut().print(")<br><tr><td valign=top><font size=+1><b>Server:</b></font> JVM mode: <b>-");
            pageContext.getOut().print("n/a");// hotspot);
            pageContext
                    .getOut()
                    .print(
                            "<table border=\"0\" cellspacing=0 cellpadding=0><tr><td>Initial heap (-Xms):&nbsp;<td align=right>");
            pageContext.getOut().print("n/a");// initialHeap);
            pageContext.getOut().print("B</td></tr><tr bgcolor=\"#eeeeee\"><td>Max heap (-Xmx):&nbsp;<td align=right>");
            pageContext.getOut().print(ReadableFormatter.readableBytes(maxHeap));
            ;
            pageContext.getOut().print("</td></tr><tr><td>Current heap size:&nbsp;<td align=right>");
            pageContext.getOut().print(ReadableFormatter.readableBytes(rt.totalMemory()));
            pageContext.getOut().print("</td></tr><tr bgcolor=\"#eeeeee\"><td>Heap in use:&nbsp;<td align=right>");
            pageContext.getOut().print(ReadableFormatter.readableBytes(rt.totalMemory() - rt.freeMemory()));
            pageContext.getOut().print("</td></tr><tr><td>Free heap:&nbsp;<td align=right>");
            pageContext.getOut().print(ReadableFormatter.readableBytes(rt.freeMemory()));
            pageContext.getOut().print("</td></tr></table><td>Server is up since: <b>");
            pageContext.getOut().print(System.getProperty(startupProp));
            pageContext.getOut().print("</b>(");
            // DateFormat df=new SimpleDateFormat();
            // try{
            pageContext.getOut().print(
                    ReadableFormatter.readableAge((new Date()).getTime()
                            - new Date(System.getProperty(startupProp)).getTime()));
            // }catch(ParseException pe){
            // TODO treat this parse exception
            // }
            pageContext.getOut().print(" ago)<br>Last application (re)load:");
            pageContext.getOut().print(uptime.format(MakumbaSystem.loadingTime));
            pageContext.getOut().print("(");
            pageContext.getOut().print(
                    ReadableFormatter.readableAge((new Date()).getTime() - MakumbaSystem.loadingTime.getTime()));
            pageContext.getOut().print(" ago)<br>Server time:");
            pageContext.getOut().print(uptime.format(new Date()));
            pageContext.getOut().print("<br>Active sessions: <b>");
            pageContext.getOut().print(activeSessions);
            pageContext.getOut().print("</b><br>");

            if (port.equals("80")) {
                port = "";
            } else {
                port = ":" + port;
            }

            pageContext.getOut().print("Server protocol, name and port: <a href=\"");
            pageContext.getOut().print(request.getScheme());
            pageContext.getOut().print("://");
            pageContext.getOut().print(request.getServerName());
            pageContext.getOut().print(port);
            pageContext.getOut().print("\">");
            pageContext.getOut().print(request.getScheme());
            pageContext.getOut().print("://");
            pageContext.getOut().print(request.getServerName());
            pageContext.getOut().print(port);
            pageContext.getOut().print("</a><br>");
            pageContext.getOut().print("Server software:");
            pageContext.getOut().print(pageContext.getServletContext().getServerInfo());
            pageContext
                    .getOut()
                    .print(
                            "<br></table><p><h2><a href=\"/makumba-docs/\"> Makumba</a>: </h2><table border=\"0\" cellspacing=0 cellpadding=0><tr bgcolor=\"#cccccc\"><Th>Property<th>Value");

            int line = 0;
            String dbname = MakumbaSystem.getDefaultDatabaseName();

            pageContext.getOut().print("<tr bgcolor=\"#");
            if (line % 2 == 0) {
                pageContext.getOut().print("eeeeee");
            } else {
                pageContext.getOut().print("ffffff");
            }

            pageContext.getOut().print("\"><td><a href=\"/makumba-docs/CHANGELOG.txt\">version</a> &nbsp;<td><code>");
            pageContext.getOut().print(MakumbaSystem.getVersion());
            pageContext.getOut().print("</code>");
            line++;
            pageContext.getOut().print("<tr bgcolor=\"#");

            if (line % 2 == 0) {
                pageContext.getOut().print("eeeeee");
            } else {
                pageContext.getOut().print("ffffff");
            }
            pageContext.getOut().print("\"><td>Default database name &nbsp;<td><code>");
            pageContext.getOut().print(dbname);
            pageContext.getOut().print("</code>");
            line++;
            pageContext.getOut().print("<tr bgcolor=\"#");

            if (line % 2 == 0) {
                pageContext.getOut().print("eeeeee");
            } else {
                pageContext.getOut().print("ffffff");
            }

            pageContext.getOut().print("\"><td>DBSV &nbsp;<td><code>");
            pageContext.getOut().print(MakumbaSystem.getDatabaseProperty(dbname, "dbsv"));

            pageContext.getOut().print("</code>");
            line++;
            pageContext.getOut().print("<tr bgcolor=\"#");
            if (line % 2 == 0) {
                pageContext.getOut().print("eeeeee");
            } else {
                pageContext.getOut().print("ffffff");
            }
            pageContext.getOut().print("\"><td>Number of connections open &nbsp;<td><code>");
            pageContext.getOut().print(MakumbaSystem.getDatabaseProperty(dbname, "jdbc_connections"));
            pageContext.getOut().print("</code>");
            line++;
            pageContext.getOut().print("<tr bgcolor=\"#");
            if (line % 2 == 0) {
                pageContext.getOut().print("eeeeee");
            } else {
                pageContext.getOut().print("ffffff");
            }
            pageContext.getOut().print("\"><td>SQL engine and version &nbsp;<td><code>");
            pageContext.getOut().print(
                    MakumbaSystem.getDatabaseProperty(dbname, "sql_engine.name") + " "
                            + MakumbaSystem.getDatabaseProperty(dbname, "sql_engine.version"));
            pageContext.getOut().print("</code>");
            line++;
            pageContext.getOut().print("<tr bgcolor=\"#");
            if (line % 2 == 0) {
                pageContext.getOut().print("eeeeee");
            } else {
                pageContext.getOut().print("ffffff");
            }
            pageContext.getOut().print("\"><td>JDBC driver and version &nbsp;<td><code>");
            pageContext.getOut().print(
                    MakumbaSystem.getDatabaseProperty(dbname, "jdbc_driver.name") + " "
                            + MakumbaSystem.getDatabaseProperty(dbname, "jdbc_driver.version"));
            pageContext
                    .getOut()
                    .print(
                            "</code></table><h3>Makumba caches: </h3><table border=\"0\" cellspacing=5 cellpadding=0><tr><th> Name <th>size<th>hits<th>misses</th>");
            line = 0;
            Map m = MakumbaSystem.getCacheInfo();
            for (Iterator i = new TreeSet(m.keySet()).iterator(); i.hasNext();) {
                String nm = (String) i.next();

                pageContext.getOut().print("<tr bgcolor=\"#");
                if (line % 2 == 0) {
                    pageContext.getOut().print("eeeeee");
                } else {
                    pageContext.getOut().print("ffffff");
                }
                pageContext.getOut().print("\"><td>");
                pageContext.getOut().print(nm);

                Object o = m.get(nm);
                if (o instanceof int[]) {

                    pageContext.getOut().print("<td align=right><code>");
                    pageContext.getOut().print(((int[]) o)[0]);
                    pageContext.getOut().print("</code><td align=right><code>");
                    pageContext.getOut().print(((int[]) o)[1]);
                    pageContext.getOut().print("</code><td align=right><code>");
                    pageContext.getOut().print(((int[]) o)[2]);
                    pageContext.getOut().print("</code>");
                } else {
                    pageContext.getOut().print("<td align=right><code>");
                    pageContext.getOut().print(o);
                    pageContext.getOut().print("</code>");
                }
                line++;
            }

            pageContext.getOut().print("</table><h2>User Session</h2>Created:");
            pageContext.getOut().print(
                    ReadableFormatter.readableAge((new Date()).getTime() - session.getCreationTime()));
            pageContext.getOut().print(" ago (");
            pageContext.getOut().print(session.getCreationTime());
            pageContext.getOut().print(")<br>Last Accessed:");
            pageContext.getOut().print(
                    ReadableFormatter.readableAge((new Date()).getTime() - session.getLastAccessedTime()));
            pageContext.getOut().print(" ago (");
            pageContext.getOut().print(session.getLastAccessedTime());
            pageContext.getOut().print(")<br>Max inactive interval:");
            pageContext.getOut().print(ReadableFormatter.readableAge(session.getMaxInactiveInterval() * 1000));
            pageContext.getOut().print("(");
            pageContext.getOut().print(session.getMaxInactiveInterval());
            pageContext.getOut().print(")<br>");

            Enumeration attribs = session.getAttributeNames();
            pageContext
                    .getOut()
                    .print(
                            "<table border=\"0\" cellspacing=0 cellpadding=0><tr bgcolor=\"#cccccc\"><Th>Attribute<th>Value<th>Class");

            line = 0;
            while (attribs.hasMoreElements()) {
                String key = (String) attribs.nextElement();
                line++;

                pageContext.getOut().print("<tr bgcolor=\"#");
                if (line % 2 == 0) {
                    pageContext.getOut().print("eeeeee");
                } else {
                    pageContext.getOut().print("ffffff");
                }
                pageContext.getOut().print("\"><td valign=\"top\">");
                pageContext.getOut().print(key);
                pageContext.getOut().print(":&nbsp;<td><pre>");
                pageContext.getOut().print(session.getAttribute(key));
                pageContext.getOut().print("<td>&nbsp;");
                pageContext.getOut().print(session.getAttribute(key).getClass().getName());
                pageContext.getOut().print("</tr>");
            }
            pageContext.getOut().print("</table><h2>Java Virtual Machine properties</h2>");

            Properties sysprops = System.getProperties();
            Enumeration enprop = sysprops.propertyNames();
            String str = "";

            pageContext.getOut().print(
                    "<table border=\"0\" cellspacing=0 cellpadding=0><tr bgcolor=\"#cccccc\"><Th>Property<th>Value");

            line = 0;
            while (enprop.hasMoreElements()) {
                String key = (String) enprop.nextElement();
                line++;

                pageContext.getOut().print("<tr bgcolor=\"#");
                if (line % 2 == 0) {
                    pageContext.getOut().print("eeeeee");
                } else {
                    pageContext.getOut().print("ffffff");
                }
                pageContext.getOut().print("\"><td valign=\"top\">");
                pageContext.getOut().print(key);
                pageContext.getOut().print(":&nbsp;<td><pre>");
                pageContext.getOut().print(
                        key.endsWith("path") ? sysprops.getProperty(key).replace(
                                sysprops.getProperty("path.separator").charAt(0), '\n') : (((String) sysprops
                                .getProperty(key)).startsWith("http://")) ? "<a href=" + sysprops.getProperty(key)
                                + ">" + sysprops.getProperty(key) + "</a>" : sysprops.getProperty(key));
                pageContext.getOut().print("</tr>");
            }

            pageContext.getOut().print("</table>");
        } catch (java.io.IOException e) {
            throw new JspException(e.getMessage());
        }
        return EVAL_BODY_INCLUDE;
    }
}
