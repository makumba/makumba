///////////////////////////////
//Makumba, Makumba tag library
//Copyright (C) 2000-2003  http://www.makumba.org
//
//This library is free software; you can redistribute it and/or
//modify it under the terms of the GNU Lesser General Public
//License as published by the Free Software Foundation; either
//version 2.1 of the License, or (at your option) any later version.
//
//This library is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//Lesser General Public License for more details.
//
//You should have received a copy of the GNU Lesser General Public
//License along with this library; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//-------------
//$Id$
//$Name$
/////////////////////////////////////

package org.makumba.devel;

import org.makumba.analyser.ELData;
import org.makumba.analyser.TagData;
import org.makumba.analyser.engine.JspParseData;
import org.makumba.analyser.interfaces.JspAnalyzer;

/**
 * @version $ID $
 * @author Cristian Bogdan
 *  
 */
public class JspxJspAnalyzer implements JspAnalyzer {

	private static final class SingletonHolder implements org.makumba.commons.SingletonHolder {
		static JspAnalyzer singleton = new JspxJspAnalyzer();
		
		public void release() {
            singleton = null;
        }

        public SingletonHolder() {
            org.makumba.commons.SingletonReleaser.register(this);
        }
	}

	private JspxJspAnalyzer() {}

	public static JspAnalyzer getInstance() {
		return SingletonHolder.singleton;
	}

    /**
     * make a status holder, which is passed to all other methods
     * 
     * @param initStatus
     *            an initial status to be passed to the JspAnalyzer. for example, the pageContext for an example-based analyzer
     */
    public Object makeStatusHolder(Object initStatus) {
        return null;
    }

    /**
     * start a body tag
     * 
     * @see #endTag(JspParseData.TagData, Object)
     */
    public void startTag(TagData td, Object status) {
    }

    /** the end of a body tag, like </...> */
    public void endTag(TagData td, Object status) {
    }

    /** a simple tag, like <... /> */
    public void simpleTag(TagData td, Object status) {
    }

    /** a system tag, like <%@ ...%> */
    public void systemTag(TagData td, Object status) {
    }
    
    /** an EL expression, either ${...} or #{...} **/
    public void elExpression(ELData ed, Object status) {
    }

    /**
     * the end of the page
     * 
     * @return the result of the analysis
     */
    public Object endPage(Object status) {
        return null;
    }

}