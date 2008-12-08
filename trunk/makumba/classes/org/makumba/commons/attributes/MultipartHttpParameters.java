//////////////////////////////////
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
//  $Id: MultipartHttpParameters.java 1726 2007-10-02 09:11:59Z manuel_gay $
//  $Name$
/////////////////////////////////////

package org.makumba.commons.attributes;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.makumba.LogicException;
import org.makumba.Text;
import org.makumba.commons.RuntimeWrappedException;

import eu.medsea.util.MimeUtil;

/**
 * Parses the input stream of a http request as a multipart/form-data. Stores uploaded files as org.makumba.Text. Normal
 * http parameters are stored as Text.toString (simple) or Vectors (multiple) data inside the request:
 * <ul>
 * <li>1st line: boundary + CR+LF</li>
 * <li>headers & values + CR+LF (e.g. filename="file.doc" Content-Type: application/octec-stream)</li>
 * <li>CR+LF (Konqueror 3.2.1 sends CR CR)</li>
 * <li>content (related to the headers just read)</li>
 * <li>CR+LF - boundary CR+LF - headers... and so forth ...</li>
 * <li>and after the last boundary you will have '--' with CR+LF</li>
 * </ul>
 * 
 * @author Cristian Bogdan
 * @author Andreas Pesenhofer
 * @author Rudolf Mayer
 * @version $Id: MultipartHttpParameters.java 1726 2007-10-02 09:11:59Z manuel_gay $
 */
public class MultipartHttpParameters extends HttpParameters {
    Hashtable<String, Object> parameters = new Hashtable<String, Object>();

    void computeAtStart() {
    }

    public boolean knownAtStart(String s) {
        return parameters.get(s) != null;
    }

    // TODO: we should make our own FileItemFactory that writes the content directly to a Text object
    // so we don't have to copy the Text content from item.getInputStream()
    // as it is now, the content is cached twice, once by commons.fileupload, and once by Text.
    // The longer the content, the bigger the performance penalty.
    // We just need to implement DiskFileItem.getOuputStream() returning an OutputStream that writes to the Text. 
    // since Text requires an InputStream, we could use InputStream-OutputStream conversion using java.io.PipedInputStream
    // see http://ostermiller.org/convert_java_outputstream_inputstream.html method 2
    static DiskFileItemFactory factory = new DiskFileItemFactory();
    
    public MultipartHttpParameters(HttpServletRequest req) {
        super(req);

        java.util.logging.Logger.getLogger("org.makumba." + "fileUpload").fine(
            "\n\n---- code with apache.commons.fileupload  ------\n");

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);

        // Parse the request
        List items = null;
        try {
            items = upload.parseRequest(request);
        } catch (FileUploadException e1) {
            throw new RuntimeWrappedException(e1);
        }

        // Process the uploaded items
        Iterator iter = items.iterator();
        while (iter.hasNext()) {
            DiskFileItem item = (DiskFileItem) iter.next();

            if (item.isFormField()) { // Process a regular form field
                String name = item.getFieldName();
                String value = item.getString();
                addParameter(name, value);

            } else { // Process a file upload
                String name = item.getFieldName();
                String fileName = item.getName();

                // Internet Explorer provides the entire path to the uploaded file and not just the base file name, we
                // remove the path information
                if (fileName != null) {
                    fileName = FilenameUtils.getName(fileName);
                }

                // mime-type detection
                String mimeType = MimeUtil.getMimeType(item.getStoreLocation()); // first try content analysis
                String browserContentType = item.getContentType();
                if (mimeType == MimeUtil.UNKNOWN_MIME_TYPE) { // if the content type analysis does not work
                    mimeType = browserContentType; // get the type from the browser
                    if (StringUtils.isBlank(mimeType)) { // if that is empty
                        mimeType = MimeUtil.getMimeType(item.getName()); // guess from the file name
                    }
                }

                // If we have an image content type, determine image width and height
                if (MimeUtil.getMajorComponent(mimeType).equals("image")) {
                    try {
                        // using image readers is faster than reading the image itself
                        Iterator<ImageReader> iterator = ImageIO.getImageReadersByMIMEType(mimeType);
                        if (iterator == null) {
                            throw new RuntimeWrappedException(new LogicException(
                                    "Could not read image information, unknown content-type '" + mimeType
                                            + "' provided."));
                        }
                        java.util.logging.Logger.getLogger("org.makumba." + "fileUpload").severe(
                            "Could not read image information, unknown content-type '" + mimeType
                                    + "' provided.\nAttribute name: '" + name + "'\n" + "Page: "
                                    + request.getRequestURI());                        
                        ImageReader reader = iterator.next();
                        ImageInputStream iis = ImageIO.createImageInputStream(item.getInputStream());
                        reader.setInput(iis, false);
                        parameters.put(name + "_imageWidth", reader.getWidth(0));
                        parameters.put(name + "_imageHeight", reader.getHeight(0));
                        reader.dispose();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // ---- read the content and set parameters
                Text contentToSave;
                try {
                    contentToSave = new Text(item.getInputStream());
                } catch (IOException e) {
                    throw new RuntimeWrappedException(e);
                }
                int contentSize = contentToSave.length();

                // FIXME: what to do if content type is null? not set, or set to an empty String / String constant?
                parameters.put(name + "_contentType", mimeType);

                parameters.put(name + "_filename", fileName);
                parameters.put(name + "_contentLength", contentSize);
                parameters.put(name, contentToSave);

                java.util.logging.Logger.getLogger("org.makumba." + "fileUpload").fine(
                    "Parameters set: contentType=" + mimeType + ", fileName=" + fileName + ", contentSize="
                            + contentSize);

            }
        }
    }// end of the method MultipartHttpParameters

    void addParameter(String name, String value) {
        Object o = parameters.get(name);
        if (o != null)
            if (o instanceof Vector)
                ((Vector) o).addElement(value);
            else {
                Vector<Object> v = new Vector<Object>();
                v.addElement(o);
                v.addElement(value);
                parameters.put(name, v);
            }
        else
            parameters.put(name, value);
    }

    /**
     * Composes what is read from the multipart with what is in the query string. The assumption is that the multipart
     * cannot change during execution, while the query string may change due to e.g. forwards
     * 
     * @param s
     *            the query string
     * @return An Object holding the parameters
     */
    public Object getParameter(String s) {
        return compose(parameters.get(s), super.getParameter(s));
    }

    /**
     * TODO this should not be here but in a util class Composes two objects, if both are vectors, unites them
     * 
     * @param a1
     *            the first object
     * @param a2
     *            the second object
     * @return a composed object
     */
    static Object compose(Object a1, Object a2) {
        if (a1 == null)
            return a2;
        if (a2 == null)
            return a1;

        if (a1 instanceof Vector)
            if (a2 instanceof Vector) {
                for (Enumeration e = ((Vector) a2).elements(); e.hasMoreElements();)
                    ((Vector) a1).addElement(e.nextElement());
                return a1;
            } else {
                ((Vector) a1).addElement(a2);
                return a1;
            }
        else if (a2 instanceof Vector) {
            ((Vector) a2).addElement(a1);
            return a2;
        } else {
            Vector<Object> v = new Vector<Object>();
            v.addElement(a1);
            v.addElement(a2);
            return v;
        }
    }
}