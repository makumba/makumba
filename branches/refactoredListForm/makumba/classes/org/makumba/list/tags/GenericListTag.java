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
//  $Id: MakumbaTag.java 1546 2007-09-14 20:34:45Z manuel_gay $
//  $Name$
/////////////////////////////////////

package org.makumba.list.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.makumba.MakumbaSystem;
import org.makumba.analyser.AnalysableTag;
import org.makumba.commons.GenericMakumbaTag;
import org.makumba.list.ListFormDataProvider;
import org.makumba.util.MultipleKey;
import org.makumba.view.jsptaglib.MakumbaJspException;

/**
 * This class provides utility methods for all makumba tags, such as
 * <ul>
 * <li>exception handling</li>
 * <li>storage of formatting parameters</li>
 * <li>database name setting/getting</li>
 * <li>cleanup</li>
 * </ul>
 * It extends {@link org.makumba.analyser.AnalysableTag} which enables JSP analysis support. FIXME form classes extend
 * this one because they need to compute the base pointers and dummy queries. this dependency needs to be removed.
 * 
 * @author Cristian Bogdan
 * @author Manuel Gay
 * @version $Id: MakumbaTag.java 1546 2007-09-14 20:34:45Z manuel_gay $
 */
public abstract class GenericListTag extends GenericMakumbaTag {

    public static final String VALUE_COMPUTERS = "org.makumba.valueComputers";

    public static final String QUERY = "org.makumba.query";

    public static final String DB_ATTR = "org.makumba.database";
    
    protected ListFormDataProvider fdp = new ListFormDataProvider();

    @Override
    public int doEndTag() throws JspException {
        try {
            return super.doEndTag();
        } finally {
            if (findAncestorWithClass(this, GenericListTag.class) == null)
                pageContext.removeAttribute(DB_ATTR);
            
        }
    }
    
    /**
     * Adds a key to the parentList, verifies if the tag has a parent.
     * 
     * @param o
     *            The key to be added
     */
    public void addToParentListKey(Object o) {
        AnalysableTag parentList = QueryTag.getParentList(this);
        if (parentList == null)
            throw new org.makumba.ProgrammerError(
                    "VALUE tags, INPUT, FORM or OPTION tags that compute a value should always be enclosed in a LIST or OBJECT tag");
        tagKey = new MultipleKey(parentList.getTagKey(), o);
    }

    /**
     * Obtains the makumba database; this can be more complex (accept arguments, etc)
     * 
     * @return A String containing the name of the database
     */
    public String getDatabaseName() {
        return getDatabaseName(pageContext);
    }

    /**
     * Obtains the makumba database; this can be more complex (accept arguments, etc)
     * 
     * @param pc
     *            The PageContext object of this page
     * @return A String containing the name of the database
     */
    public static String getDatabaseName(PageContext pc) {
        String db = (String) pc.getAttribute(DB_ATTR);
        if (db == null)
            return MakumbaSystem.getDefaultDatabaseName();
        return db;
    }

    /**
     * Checks if this is not the root tag and throws an exception containing the name of the argument not allowed in
     * non-root tags.
     * 
     * @param s
     *            The name of the argument
     * @throws JspException
     */
    protected void onlyRootArgument(String s) throws JspException {
        if (findAncestorWithClass(this, GenericListTag.class) != null)
            treatException(new MakumbaJspException(this, "the " + s
                    + " argument cannot be set for non-root makumba tags"));
    }

    /**
     * Sets the database argument
     * 
     * @param db
     *            The database argument
     * @throws JspException
     */
    public void setDb(String db) throws JspException {
        onlyRootArgument("db");
        if (pageContext != null)
            pageContext.setAttribute(DB_ATTR, db);
    }
}
