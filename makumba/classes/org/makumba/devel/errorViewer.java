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

package org.makumba.devel;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.makumba.MakumbaSystem;
import org.makumba.view.jsptaglib.TomcatJsp;

/**
 * the error viewer. To be used from TagExceptionServlet.
 * 
 * @version $Id$
 * @author Stefan Baebler
 * @author Rudolf Mayer
 *  
 */
public class errorViewer extends LineViewer {
    private String hiddenBody;

    public errorViewer(HttpServletRequest request, HttpServlet servlet, String title, String body, String hiddenBody)
            throws IOException {
        super(false, request, servlet);
        realPath = servlet.getServletConfig().getServletContext().getRealPath("/");

        jspClasspath = TomcatJsp.getContextCompiledJSPDir(servlet.getServletContext());
        super.title = title;
        this.hiddenBody = hiddenBody;
        reader = new StringReader(body);
    }

    public String parseLine(String s) {
        Class javaClass;
        String jspPage;
        String jspClass;

        StringBuffer source = new StringBuffer(s);
        StringBuffer result = new StringBuffer();

        StringTokenizer tokenizer = getLineTokenizer(s);
        while (tokenizer.hasMoreElements()) {
            String token = tokenizer.nextToken();

            int indexOf = source.indexOf(token);
            int indexAfter = indexOf + token.length();

            result.append(source.substring(0, indexOf));

            if (token.indexOf(".") != -1) {
                Integer lineNumber = null;

                //FIXME should not depend directly on RecordParser
                if (searchMDD && org.makumba.providers.datadefinition.makumba.RecordParser.findDataDefinition(token, "mdd") != null
                        || org.makumba.providers.datadefinition.makumba.RecordParser.findDataDefinition(token, "idd") != null) {
                    result.append(formatMDDLink(token));
                } else if (searchJavaClasses && (javaClass = findClassSimple(token)) != null) {
                    String substring = source.substring(indexAfter);
                    lineNumber = findLineNumber(substring);
                    result.append(formatClassLink(javaClass.getName(), token, lineNumber));
                } else if ((javaClass = findClass(token)) != null) {
                    result.append(formatClassLink(javaClass, null, token));
                } else if (searchJSPPages && (jspPage = findPage(token)) != null) {
                    lineNumber = findLineNumber(source.toString());
                    result.append(formatJSPLink(jspPage, token, lineNumber));
                } else if (searchCompiledJSPClasses && (jspClass = findCompiledJSP(token)) != null) {
                    lineNumber = findLineNumber(source.toString());
                    result.append(formatClassLink(jspClass, token, lineNumber));
                } else {
                    result.append(token);
                }
            } else {
                result.append(token);
            }
            source.delete(0, indexOf + token.length());
        }
        return result.append(source).toString();
    }

    private Integer findLineNumber(String s) {
        String beginToken = ":";
        Integer lineNr = null;
        int indexNumberBegin = s.indexOf(beginToken);
        
        if (indexNumberBegin == -1) { // try if we have a line number after a '('
            indexNumberBegin = s.indexOf("(");
        }
        
        if (indexNumberBegin != -1) {
            indexNumberBegin = indexNumberBegin + 1;
            int indexNumberEnd = indexNumberBegin;
            while (s.length() > indexNumberEnd && Character.isDigit(s.charAt(indexNumberEnd))) {
                indexNumberEnd++;
            }
            String lineNumberText = s.substring(indexNumberBegin, indexNumberEnd).trim();
            if (lineNumberText.length() > 0) {
                try {
                    lineNr = Integer.valueOf(lineNumberText);
                } catch (NumberFormatException e) {
                    MakumbaSystem.getMakumbaLogger("devel").warning("Error in error viewer: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return lineNr;
    }

    /**
     * @param token
     * @return
     */
    public Class findClassSimple(String token) {
        int index = token.lastIndexOf('.');
        String className = token.substring(0, index);
        return super.findClassSimple(className);
    }

    public void footer(PrintWriter pw) throws IOException {
        if (hiddenBody != null)
            pw.println("<!--\n" + hiddenBody + "\n-->");
        super.footer(pw);
    }

}

