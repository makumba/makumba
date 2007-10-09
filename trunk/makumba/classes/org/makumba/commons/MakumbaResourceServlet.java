package org.makumba.commons;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.servlet.ServletException;
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

    public static final SimpleDateFormat dfLastModified = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();
        String servletPath = req.getServletPath();
        String requestURI = req.getRequestURI();
        String resource = requestURI.substring(requestURI.indexOf(servletPath) + servletPath.length());
        URL url = ClassResource.get(resourceDirectory + resource);
        try {
            File file = new File(url.toURI());
            if (file.isDirectory()) {
                resp.setContentType("text/html");
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
                DevelUtils.printDeveloperSupportFooter(writer);
                writer.println("</body></html>");
                return;
            }
            resp.setHeader("Last-Modified", dfLastModified.format(new Date(file.lastModified())));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        resp.setContentType(getContentType(url));
        writer.print(NamedResources.getStaticCache(makumbaResources).getResource(resource));
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
                BufferedReader bis = new BufferedReader(new InputStreamReader(url.openStream()));
                byte c;
                while ((c = (byte) bis.read()) != -1) {
                    sb.append(String.valueOf((char) c));
                }
                return sb;
            }
        }, true);

    public static final String[] imageContentTypes = { "jpg", "jpeg", "png", "gif", "pjpeg", "tiff", "bmp", "x-emf",
            "x-wmf", "x-xbitmap" };

}
