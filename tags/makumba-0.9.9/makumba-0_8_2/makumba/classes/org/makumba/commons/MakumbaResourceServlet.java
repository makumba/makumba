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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.makumba.devel.DevelUtils;
import org.makumba.devel.SourceViewControllerHandler;
import org.makumba.forms.html.KruseCalendarEditor;
import org.makumba.forms.validation.LiveValidationProvider;
import org.makumba.providers.Configuration;

/**
 * This servlet provides resources needed by makumba, e.g. JavaScript for the date editor {@link KruseCalendarEditor}
 * and live validation {@link LiveValidationProvider}.
 * 
 * @author Rudolf Mayer
 * @version $Id$
 */
public class MakumbaResourceServlet extends HttpServlet {
    private static final String resourceDirectory = "makumbaResources";

    private static final long serialVersionUID = 1L;

    public static final String RESOURCE_PATH_JAVASCRIPT = "javaScript/";

    public static final String RESOURCE_PATH_CSS = "css/";

    public static final String RESOURCE_PATH_IMAGES = "image/";

    public static final SimpleDateFormat dfLastModified = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String servletPath = req.getContextPath() + Configuration.getMakumbaResourcesLocation();
        String requestURI = req.getRequestURI();
        String resource = requestURI.substring(requestURI.indexOf(servletPath) + servletPath.length());
        URL url = ClassResource.get(resourceDirectory + resource);
        try {
            File file = null;
            JarEntry jarEntry = null;
            JarFile jarFile = null;
            if (!url.toExternalForm().startsWith("jar:")) {
                file = new File(url.toURI());
            } else {
                jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
                String[] jarURL = url.toExternalForm().split("!");
                jarEntry = jarFile.getJarEntry(jarURL[1].substring(1));
            }
            // check if we have a jar file, or a directory directory
            if (file != null && file.isDirectory()) {
                // do a directory viewing
                PrintWriter writer = resp.getWriter();
                resp.setContentType("text/html");
                DevelUtils.writePageBegin(writer);
                DevelUtils.writeTitleAndHeaderEnd(writer, "Makumba resources");
                if (DevelUtils.redirected(req, resp, resource)) {
                    return;
                }
                String relativeDirectory = file.getName();
                if (file.getAbsolutePath().indexOf(resourceDirectory) != -1) {
                    relativeDirectory = file.getAbsolutePath().substring(
                        file.getAbsolutePath().indexOf(resourceDirectory));
                }
                SourceViewControllerHandler.printDirlistingHeader(writer, file.getCanonicalPath(), relativeDirectory,
                    req.getContextPath(), null);

                if (!(relativeDirectory.equals(resourceDirectory))) {
                    writer.println("<b><a href=\"../\">../</a></b> (up one level)");
                }

                // process and display directories
                SourceViewControllerHandler.processDirectory(writer, file, null);

                // process and display files
                String[] list = file.list();
                Arrays.sort(list);
                for (int i = 0; i < list.length; i++) {
                    String s = list[i];
                    File f = new File(file.getAbsolutePath() + File.separator + s);
                    if (f.isFile()) {
                        writer.println("<b><a href=\"" + s + "\">" + s + "</a></b>");
                    }
                }
                writer.println("</pre>");
                DevelUtils.writePageEnd(writer);
                resp.setHeader("Last-Modified", dfLastModified.format(new Date()));
                return;
            } else if (jarEntry != null && jarEntry.isDirectory()) {
                Enumeration<JarEntry> entries = jarFile.entries();
                ArrayList<String> files = new ArrayList<String>();
                ArrayList<String> directories = new ArrayList<String>();
                PrintWriter writer = resp.getWriter();
                resp.setContentType("text/html");
                DevelUtils.writePageBegin(writer);
                DevelUtils.writeTitleAndHeaderEnd(writer, "Makumba resources");
                if (DevelUtils.redirected(req, resp, resource)) {
                    return;
                }
                String relativeDirectory = jarEntry.getName();
                SourceViewControllerHandler.printDirlistingHeader(writer, url.toExternalForm(), relativeDirectory,
                    req.getContextPath(), null);

                if (!relativeDirectory.equals(resourceDirectory) && !relativeDirectory.equals(resourceDirectory + "/")) {
                    writer.println("<b><a href=\"../\">../</a></b> (up one level)");
                }

                while (entries.hasMoreElements()) {
                    JarEntry entry = (JarEntry) entries.nextElement();
                    if (entry.getName().startsWith(relativeDirectory)) {
                        String s = entry.getName().substring(relativeDirectory.length());
                        while (s.length() > 0 && s.startsWith("/")) {
                            s = s.substring(1);
                        }
                        if (s.indexOf("/") == -1) { // we have an entry
                            if (s.length() > 0) {
                                files.add(s);
                            }
                        } else if (s.indexOf("/") == s.lastIndexOf("/") && s.endsWith("/")) { // we have a 1-level dir
                            if (s.endsWith("/")) { // remove trailing /
                                s = s.substring(0, s.length() - 1);
                            }
                            if (s.length() > 0) {
                                directories.add(s);
                            }
                        }
                    }
                }

                // display directories
                for (String string : directories) {
                    writer.println("<b><a href=\"" + string + "/\">" + string + "/</a></b>");
                }

                // display files
                for (String string : files) {
                    writer.println("<b><a href=\"" + string + "\">" + string + "</a></b>");
                }

                writer.println("</pre>");
                DevelUtils.writePageEnd(writer);
                resp.setHeader("Last-Modified", dfLastModified.format(new Date()));
                return;
            } else { // file or file in jar entry

                if (jarEntry != null && jarEntry.getSize() == 0) {
                    // for some reason, sometimes an entry w/o the leading / is not recognised as directory
                    // check whether there is 0 file-size
                    if (DevelUtils.redirected(req, resp, resource)) {
                        return;
                    }
                }

                final Date lastModified;
                if (url.toExternalForm().startsWith("jar:")) { // for jar files, read from the jar
                    JarFile jf = ((JarURLConnection) url.openConnection()).getJarFile();
                    String[] jarURL = url.toExternalForm().split("!");
                    lastModified = new Date(jf.getJarEntry(jarURL[1].substring(1)).getTime());
                } else {
                    lastModified = new Date(new File(url.toURI()).lastModified());
                }
                resp.setHeader("Last-Modified", dfLastModified.format(lastModified));
                resp.setContentType(getContentType(url));
                Object cachedResource = NamedResources.getStaticCache(makumbaResources).getResource(resource);
                ServletOutputStream outputStream = resp.getOutputStream();
                if (isBinary(url)) {
                    for (int i = 0; i < ((byte[]) cachedResource).length; i++) {
                        outputStream.write(((byte[]) cachedResource)[i]);
                    }
                } else {
                    if (cachedResource.toString().contains(Configuration.PLACEHOLDER_CONTEXT_PATH)) {
                        // exchange placeholders with dynamic values
                        outputStream.print(cachedResource.toString().replaceAll(Configuration.PLACEHOLDER_CONTEXT_PATH, req.getContextPath()));
                    } else if(cachedResource.toString().contains(Configuration.PLACEHOLDER_UNIQUENESS_SERVLET_PATH)) {
                        String uniquenessPath = req.getContextPath() + Configuration.getMakumbaUniqueLocation();
                        outputStream.print(cachedResource.toString().replaceAll(Configuration.PLACEHOLDER_UNIQUENESS_SERVLET_PATH, uniquenessPath));
                    } else {
                        outputStream.print(cachedResource.toString());
                    }
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    
    private void printLineBreaks(String s, OutputStream o) throws IOException {
        byte[] bytes = s.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            if(bytes[i] != '\n') {
                o.write(bytes[i]);
            } else {
                o.write('\n');
                o.write(bytes[i]);
            }
        }
    }

    public static void writeResources(StringBuffer sb, String contextPath, Iterable<Object> resources) {
        for (Object object : resources) {
            String rsc = contextPath + Configuration.getMakumbaResourcesLocation() + "/";
            String o = (String) object;
            if(o.endsWith(".js")) {
                writeScriptsInHeader(sb, contextPath, o);
            } else if(o.endsWith(".css")) {
                writeStylesInHeader(sb, contextPath, o);
            }
    
        }
    }

    public static void writeStylesInHeader(StringBuffer sb, String contextPath, String styleSheet) {
        String filerefName = "fileref" + styleSheet.replace(".", "").replace("-", "_");
        sb.append("<script type=\"text/javascript\">\n");
        sb.append("var "+filerefName+" = document.createElement('link');\n");
        sb.append(filerefName+".setAttribute(\"media\",\"all\");\n");
        sb.append(filerefName+".setAttribute(\"type\",\"text/css\");\n");
        sb.append(filerefName+".setAttribute(\"href\", '"+contextPath + Configuration.getMakumbaResourcesLocation() + "/" + RESOURCE_PATH_CSS + styleSheet + "');\n");
        sb.append(filerefName+".setAttribute(\"rel\",\"StyleSheet\");\n");
        sb.append("if (typeof "+ filerefName +" != \"undefined\")\n");
        sb.append("document.getElementsByTagName(\"head\")[0].appendChild("+filerefName+");\n");
        sb.append("</script>\n");
        
    }

    public static String getContentType(URL url) {
        for (int i = 0; i < imageContentTypes.length; i++) {
            if (url.getFile().endsWith("." + imageContentTypes[i])) {
                return "image / " + imageContentTypes[i];
            }
        }
        if(url.getFile().endsWith(".css")) {
            return "text/css";
        }
        return "text/html";
    }

    public static boolean isImageType(URL url) {
        for (int i = 0; i < imageContentTypes.length; i++) {
            if (url.getFile().endsWith("." + imageContentTypes[i])) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBinary(URL url) {
        // TODO: this should be capable of detecting other types. A solution would be to check for "not text type"
        return isImageType(url);
    }
    
    public static void writeScriptsInHeader(StringBuffer sb, String contextPath, String script) {
        // we write the scripts in the header using JS and DOM rewriting
        String filerefName = "fileref" + script.replace(".", "").replace("-", "_");
        sb.append("<script type=\"text/javascript\">\n");
        sb.append("var "+filerefName+" = document.createElement('script');\n");
        sb.append(filerefName+".setAttribute(\"type\",\"text/javascript\");\n");
        sb.append(filerefName+".setAttribute(\"src\", '"+contextPath + Configuration.getMakumbaResourcesLocation() + "/" + RESOURCE_PATH_JAVASCRIPT + script + "');\n");
        sb.append("if (typeof "+ filerefName +" != \"undefined\")\n");
        sb.append("document.getElementsByTagName(\"head\")[0].appendChild("+filerefName+");\n");
        sb.append("</script>\n");
        
    }
    

    public static int makumbaResources = NamedResources.makeStaticCache("Makumba resources",
        new NamedResourceFactory() {
            private static final long serialVersionUID = 1L;

            public Object getHashObject(Object o) {
                return ClassResource.get(resourceDirectory + o);
            }

            public Object makeResource(Object o, Object hashName) throws Throwable {
                if (hashName == null) {
                    return null;
                }
                StringBuffer sb = new StringBuffer();
                URL url = (URL) hashName;
                InputStream stream;

                if (url.toExternalForm().startsWith("jar:")) { // for jar files, open the entry
                    JarFile jf = ((JarURLConnection) url.openConnection()).getJarFile();
                    String[] jarURL = url.toExternalForm().split("!");
                    JarEntry je = jf.getJarEntry(jarURL[1].substring(1));
                    stream = jf.getInputStream(je);
                } else { // for files, simply open a stream
                    stream = url.openStream();
                }
                if (isBinary(url)) {
                    ArrayList<Byte> bytesList = new ArrayList<Byte>();
                    byte[] b = new byte[16];
                    int readBytes = -1;
                    while ((readBytes = stream.read(b)) != -1) {
                        for (int i = 0; i < readBytes; i++) {
                            bytesList.add(b[i]);
                        }
                    }
                    byte[] bytes = new byte[bytesList.size()];
                    for (int i = 0; i < bytes.length; i++) {
                        bytes[i] = bytesList.get(i);
                    }
                    return bytes;
                } else {
                    BufferedReader bis = new BufferedReader(new InputStreamReader(stream));
                    byte b;
                    while ((b = (byte) bis.read()) != -1) {
                        sb.append(String.valueOf((char) b));
                    }
                    return sb.toString();
                }
            }
        }, true);

    public static final String[] imageContentTypes = { "jpg", "jpeg", "png", "gif", "pjpeg", "tiff", "bmp", "x-emf",
            "x-wmf", "x-xbitmap" };

}
