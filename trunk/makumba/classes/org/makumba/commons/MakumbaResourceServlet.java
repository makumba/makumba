package org.makumba.commons;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.makumba.devel.SourceViewServlet;
import org.makumba.forms.html.KruseCalendarEditor;
import org.makumba.forms.validation.LiveValidationProvider;

/**
 * This servlet provides resources needed by makumba, e.g. JavaScript for the date editor {@link KruseCalendarEditor}
 * and live validation {@link LiveValidationProvider}.
 * 
 * @author Rudolf Mayer
 * @version $Id: MakumbaResourceServlet.java,v 1.1 Sep 22, 2007 2:02:17 AM rudi Exp $
 */
public class MakumbaResourceServlet extends HttpServlet {
    public static final String resourceDirectory = "makumbaResources";

    private static final long serialVersionUID = 1L;

    public static final String RESOURCE_PATH_JAVASCRIPT = "javaScript/";

    public static final String RESOURCE_PATH_CSS = "css/";

    public static final String RESOURCE_PATH_IMAGES = "image/";

    public static final SimpleDateFormat dfLastModified = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String servletPath = req.getServletPath();
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
                if (SourceViewServlet.redirected(req, resp, servletPath)) {
                    return;
                }
                String relativeDirectory = file.getName();
                if (file.getAbsolutePath().indexOf(resourceDirectory) != -1) {
                    relativeDirectory = file.getAbsolutePath().substring(
                        file.getAbsolutePath().indexOf(resourceDirectory));
                }
                SourceViewServlet.printDirlistingHeader(writer, file.getCanonicalPath(), relativeDirectory);

                if (!(relativeDirectory.equals(resourceDirectory))) {
                    writer.println("<b><a href=\"../\">../</a></b> (up one level)");
                }

                // process and display directories
                SourceViewServlet.processDirectory(writer, file, null);

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
                if (SourceViewServlet.redirected(req, resp, servletPath)) {
                    return;
                }
                String relativeDirectory = jarEntry.getName();
                SourceViewServlet.printDirlistingHeader(writer, url.toExternalForm(), relativeDirectory);

                if (!relativeDirectory.equals(resourceDirectory) && !relativeDirectory.equals(resourceDirectory + "/")) {
                    writer.println("<b><a href=\"../\">../</a></b> (up one level)");
                }

                while (entries.hasMoreElements()) {
                    JarEntry entry = (JarEntry) entries.nextElement();
                    if (entry.getName().startsWith(relativeDirectory)) {
                        System.out.println("entry name: " + entry.getName());
                        String s = entry.getName().substring(relativeDirectory.length());
                        while (s.length() > 0 && s.startsWith("/")) {
                            s = s.substring(1);
                        }
                        System.out.println(s);
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
                    outputStream.print(cachedResource.toString());
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static String getContentType(URL url) {
        for (int i = 0; i < imageContentTypes.length; i++) {
            if (url.getFile().endsWith("." + imageContentTypes[i])) {
                return "image / " + imageContentTypes[i];
            }
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
                    System.out.println("\n\n\nopened jar url: " + je.getName());
                } else { // for files, simply open a stream
                    stream = url.openStream();
                }
                if (isBinary(url)) {
                    ArrayList<Byte> bytesList = new ArrayList<Byte>();
                    InputStreamReader bis = new InputStreamReader(stream);
                    byte b;
                    while ((b = (byte) bis.read()) != -1) {
                        bytesList.add(b);
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
                    return sb;
                }
            }
        }, true);

    public static final String[] imageContentTypes = { "jpg", "jpeg", "png", "gif", "pjpeg", "tiff", "bmp", "x-emf",
            "x-wmf", "x-xbitmap" };

}
