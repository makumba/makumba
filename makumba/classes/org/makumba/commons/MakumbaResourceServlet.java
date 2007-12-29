package org.makumba.commons;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

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
            File file = new File(url.toURI());
            if (file.isDirectory()) {
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
                SourceViewServlet.printDirlistingHeader(writer, file, relativeDirectory);

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
            } else {
                resp.setHeader("Last-Modified", dfLastModified.format(new Date(file.lastModified())));
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
                // FIXME: we need to handle image types, if we decide we want this
                InputStream stream = url.openStream();
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
