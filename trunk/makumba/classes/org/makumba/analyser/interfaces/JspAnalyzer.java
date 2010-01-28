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

package org.makumba.analyser.interfaces;

import javax.el.Expression;

import org.dom4j.Element;
import org.makumba.analyser.ELData;
import org.makumba.analyser.TagData;

/**
 * The interface of a JSP analyzer.
 * 
 * @author
 * @version $Id$
 */
public interface JspAnalyzer {
    /**
     * Makes a status holder, which is passed to all other methods
     * 
     * @param initStatus
     *            an initial status to be passed to the JspAnalyzer. for example, the pageContext for an example-based
     *            analyzer
     */
    Object makeStatusHolder(Object initStatus);

    /**
     * Start of a body tag
     * 
     * @param td
     *            the TagData holding the parsed data
     * @param status
     *            the status of the parsing
     * @see #endTag(TagData, Object)
     */
    void startTag(TagData td, Object status);

    /**
     * End of a body tag, like </...>
     * 
     * @param td
     *            the TagData holdking the parsed data
     * @param status
     *            the status of the parsing
     */
    void endTag(TagData td, Object status);

    /**
     * A simple tag, like <... />
     * 
     * @param td
     *            the TagData holdking the parsed data
     * @param status
     *            the status of the parsing
     */
    void simpleTag(TagData td, Object status);

    /**
     * A system tag, like <%@ ...%>
     * 
     * @param td
     *            the TagData holding the parsed data
     * @param status
     *            the status of the parsing
     */
    void systemTag(TagData td, Object status);

    /**
     * A EL expression (see {@link Expression})
     * 
     * @param td
     * @param status
     *            the status of the parsing
     */
    void elExpression(ELData ed, Object status);

    /**
     * The end of the page
     * 
     * @param status
     *            the status of the parsing
     * @return The result of the analysis
     */
    Object endPage(Object status);
}