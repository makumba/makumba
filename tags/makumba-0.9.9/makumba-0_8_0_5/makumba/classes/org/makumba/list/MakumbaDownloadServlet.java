// /////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003 http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//  -------------
//  $Id: RecordEditor.java 2886 2008-07-20 23:29:53Z rosso_nero $
//  $Name$
/////////////////////////////////////

package org.makumba.list;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.makumba.LogicException;
import org.makumba.MakumbaSystem;
import org.makumba.Pointer;
import org.makumba.Text;
import org.makumba.Transaction;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.providers.TransactionProvider;

/**
 * A Servlet to download attachments, resp. display them inline.
 * 
 * @author Rudolf Mayer
 * @version $Id: MakumbaDownloadServlet.java,v 1.1 Jul 23, 2008 10:21:16 PM rudi Exp $
 */
public class MakumbaDownloadServlet extends HttpServlet {
    private static final String QUERY_WHERE = " f WHERE f=$1";

    private static final String QUERY_SELECT = "SELECT f.content as content, f.contentType as contentType, f.contentLength as contentLength, f.name as name FROM ";

    private static final long serialVersionUID = 1L;

    private static final String[] INLINE_CONTENT_TYPES = { "text/plain", "text/html", "image/jpeg", "image/jpg",
            "image/pjpeg", "image/gif", "image/png" };

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        Transaction t = null;
        try {
            t = TransactionProvider.getInstance().getConnectionTo(MakumbaSystem.getDefaultDatabaseName());
            ServletOutputStream out = response.getOutputStream();
            String ptr = req.getParameter("value");
            String type = req.getParameter("type");
            Vector<Dictionary<String, Object>> v = t.executeQuery(QUERY_SELECT + type + QUERY_WHERE, new Pointer(type,
                    ptr));
            if (v.size() == 1) {
                Text content = (Text) v.firstElement().get("content");
                String contentType = (String) v.firstElement().get("contentType");
                Integer contentLength = (Integer) v.firstElement().get("contentLength");
                String name = (String) v.firstElement().get("name");
                if (isInlineContentType(contentType)) {
                    response.setHeader("Content-Disposition", "inline;");
                } else {
                    response.setHeader("Content-Disposition", "attachment; filename=\"" + name + "\"");
                }
                response.setContentType(contentType);
                response.setContentLength(((Integer) contentLength).intValue());

                content.writeTo(out);

            } else {
                throw new RuntimeWrappedException(new LogicException("Error retrieving file of type '" + type
                        + "', id '" + ptr + "', found " + v.size() + " matching results."));
            }
        } finally {
            if (t != null) {
                t.close();
            }
        }
    }

    /**
     * Determines whether the given content type is of "inline" disposition type, i.e. whether it should be displayed
     * directly by the browser.<br>
     * FIXME: find a library that does this better.
     */
    public static boolean isInlineContentType(String contentType) {
        for (int i = 0; i < INLINE_CONTENT_TYPES.length; i++) {
            if (contentType.equalsIgnoreCase(INLINE_CONTENT_TYPES[i])) {
                return true;
            }
        }
        return false;
    }

}
