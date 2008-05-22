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
//  $Id: TomcatJsp.java 1482 2007-09-02 23:05:26Z rosso_nero $
//  $Name$
/////////////////////////////////////

package org.makumba.analyser.engine;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;


//
// import javax.servlet.http.HttpServlet;
// import javax.servlet.http.HttpServletResponse;
// import javax.servlet.ServletConfig;
// import javax.servlet.ServletException;
// import java.io.IOException;

/**
 * This class is tomcat-specific because the available APIs do not expose this functionality. Given a servlet request,
 * it allows to determine the URI of the currently executing JSP. This is needed for static page analysis. Adaptations
 * will need to be made for other servlet containers. With simple modifications, the class can be made to act as a JSP
 * servlet decorator.
 * 
 * @author Cristian Bogdan
 * @version $Id: TomcatJsp.java 1482 2007-09-02 23:05:26Z rosso_nero $
 */
public class TomcatJsp /* extends HttpServlet */{

    /**
     * Computes the URI to the current JSP
     * 
     * @param request the Http request associated with the JSP
     * @return A String containing the URI to the JSP
     */
    public static String getJspURI(HttpServletRequest request) {
        String includeUri = (String) request.getAttribute("javax.servlet.include.servlet_path");

        String jspUri;

        if (includeUri == null) {
            jspUri = request.getServletPath();
        } else {
            jspUri = includeUri;
        }
        String jspFile = (String) request.getAttribute("org.apache.catalina.jsp_file");
        if (jspFile != null) {
            jspUri = jspFile;
        }
        return jspUri;
    }

    /**
     * Computes the path to the directory in which the compiled JSP is
     * 
     * @param context the ServletContext of the running tomcat
     * @return A String containing the path to the directory where JSPs are being compiled
     */
    public static String getContextCompiledJSPDir(ServletContext context) {
        return String.valueOf(context.getAttribute("javax.servlet.context.tempdir"));
    }

    public static String getJspCompilerPackage() {
        return "org.apache.jasper";
    }

    
    //
    // when uncommenting the line below, add jasper-compiler.jar to the compilation classpath
    // 
    // example configuration:
    // 
    // <servlet> <servlet-name>jspMak</servlet-name>
    // <servlet-class>org.makumba.view.jsptaglib.JspServlet</servlet-class>
    // <init-param> <param-name>logVerbosityLevel</param-name> <param-value>WARNING</param-value> </init-param>
    // 
    // <init-param> <param-name>jspCompilerPlugin</param-name>
    // <param-value>org.apache.jasper.compiler.JikesJavaCompiler</param-value> </init-param>
    // 
    // <load-on-startup>3</load-on-startup> </servlet>
    // 
    // 
    // 
    // <!-- The mapping for the JSP servlet --> <servlet-mapping> <servlet-name>jspMak</servlet-name>
    // <url-pattern>*.jsp</url-pattern> </servlet-mapping>
    // 
    // 
    // HttpServlet decorated = new org.apache.jasper.servlet.JspServlet();
    // 
    // public void init(ServletConfig config) throws ServletException { decorated.init(config); }
    // 
    // public void service (HttpServletRequest request, HttpServletResponse response) throws ServletException,
    // IOException { System.out.println("jsp hook: "+getJspPath(request)); decorated.service(request, response); }
    // public void destroy() { decorated.destroy(); }
    //
}
