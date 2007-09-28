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
//  $Id$
//  $Name$
/////////////////////////////////////

package org.makumba.controller.http;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.makumba.Text;

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
 * @version $Id$
 */
public class MultipartHttpParameters extends HttpParameters {
    Hashtable parameters = new Hashtable();

    void computeAtStart() {
    }

    public boolean knownAtStart(String s) {
        return parameters.get(s) != null;
    }

    public MultipartHttpParameters(HttpServletRequest req) {
        super(req);

        java.util.logging.Logger.getLogger("org.makumba." + "fileUpload").fine("\n\n---- code with apache.commons.fileupload  ------\n");

        // Create a factory for disk-based file items
        DiskFileItemFactory factory = new DiskFileItemFactory();

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);

        // Parse the request
        List items = null;
        try {
            items = upload.parseRequest(request);
        } catch (FileUploadException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        // Process the uploaded items
        Iterator iter = items.iterator();
        while (iter.hasNext()) {
            FileItem item = (FileItem) iter.next();

            if (item.isFormField()) {
                // Process a regular form field
                if (item.isFormField()) {
                    String name = item.getFieldName();
                    String value = item.getString();
                    addParameter(name, value);
                }

            } else {
                // Process a file upload
                if (!item.isFormField()) {
                    Text contentToSave;
                    int contentSize;

                    String name = item.getFieldName();
                    String fileName = item.getName();
                    String type = item.getContentType();
                    // boolean isInMemory = item.isInMemory();

                    // ---- read the content and set parameters
                    contentToSave = new Text(item.get());
                    contentSize = contentToSave.length();

                    parameters.put(name + "_contentType", type);
                    parameters.put(name + "_filename", fileName);
                    parameters.put(name + "_contentLength", new Integer(contentSize));
                    parameters.put(name, contentToSave);

                    java.util.logging.Logger.getLogger("org.makumba." + "fileUpload").fine(
                        "Parameters set: contentType=" + type + ", fileName=" + fileName + ", contentSize="
                                + contentSize);
                }

            }
        }
    }// end of the method MultipartHttpParameters

    void addParameter(String name, String value) {
        Object o = parameters.get(name);
        if (o != null)
            if (o instanceof Vector)
                ((Vector) o).addElement(value);
            else {
                Vector v = new Vector();
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
     * @param s the query string
     * @return An Object holding the parameters
     */
    public Object getParameter(String s) {
        return compose(parameters.get(s), super.getParameter(s));
    }

    /**
     * TODO this should not be here but in a util class Composes two objects, if both are vectors, unites them
     * 
     * @param a1 the first object
     * @param a2 the second object
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
            Vector v = new Vector();
            v.addElement(a1);
            v.addElement(a2);
            return v;
        }
    }
}