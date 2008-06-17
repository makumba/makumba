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

package org.makumba.commons.attributes;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

import org.makumba.AttributeNotFoundException;
import org.makumba.Attributes;
import org.makumba.LogicException;

public class PageAttributes implements Attributes {
    public static PageAttributes getAttributes(PageContext pc) {
        if (pc.getAttribute(RequestAttributes.ATTRIBUTES_NAME) == null)
            pc.setAttribute(RequestAttributes.ATTRIBUTES_NAME, new PageAttributes(pc));
        return (PageAttributes) pc.getAttribute(RequestAttributes.ATTRIBUTES_NAME);
    }

    PageContext pageContext;

    PageAttributes(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    static public void setAttribute(PageContext pc, String var, Object o) {
        if (o != null) {
            pc.setAttribute(var, o);
            pc.removeAttribute(var + "_null");
        } else {
            pc.removeAttribute(var);
            pc.setAttribute(var + "_null", "null");
        }
    }

    /**
     * @see org.makumba.Attributes#setAttribute(java.lang.String, java.lang.Object)
     */
    public Object setAttribute(String s, Object o) throws LogicException {
        return RequestAttributes.getAttributes((HttpServletRequest) pageContext.getRequest()).setAttribute(s, o);
    }

    /**
     * @see org.makumba.Attributes#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String s) throws LogicException {
        RequestAttributes.getAttributes((HttpServletRequest) pageContext.getRequest()).removeAttribute(s);
    }

    /**
     * @see org.makumba.Attributes#hasAttribute(java.lang.String)
     */
    public boolean hasAttribute(String s) {
        try {
            return (RequestAttributes.getAttributes((HttpServletRequest) pageContext.getRequest()).hasAttribute(s) || checkPageForAttribute(s) != RequestAttributes.notFound);
        } catch (LogicException e) {
            return false;
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String[] scopeNames = { "Session", "Application", "Request", "Page" };
        int[] scopes = { PageContext.SESSION_SCOPE, PageContext.APPLICATION_SCOPE, PageContext.REQUEST_SCOPE,
                PageContext.PAGE_SCOPE };
        String s = "";
        for (int i = 0; i < scopes.length; i++) {
            s += scopeNames[i] + ": {";
            Enumeration enumeration = pageContext.getAttributeNamesInScope(scopes[i]);
            while (enumeration.hasMoreElements()) {
                String name = (String) enumeration.nextElement();
                s += name + "=";
                Object value = pageContext.getAttribute(name, scopes[i]);
                if (value instanceof PageAttributes) { // don't print if type is from this class --> avoid endless loop
                    s += value.getClass();
                } else {
                    s += value;
                }
                if (enumeration.hasMoreElements()) {
                    s += ", ";
                }
            }
            s += "}\n";
        }
        return s;
    }

    /**
     * @see org.makumba.Attributes#getAttribute(java.lang.String)
     */
    public Object getAttribute(String s) throws LogicException {
        RequestAttributes reqAttrs = RequestAttributes.getAttributes((HttpServletRequest) pageContext.getRequest());

        Object o = reqAttrs.checkSessionForAttribute(s);
        if (o != RequestAttributes.notFound)
            return o;

        o = reqAttrs.checkServletLoginForAttribute(s);
        if (o != RequestAttributes.notFound)
            return o;

        o = checkPageForAttribute(s);
        if (o != RequestAttributes.notFound)
            return o;

        o = reqAttrs.checkLogicForAttribute(s);
        if (o != RequestAttributes.notFound)
            return o;

        o = reqAttrs.checkParameterForAttribute(s);
        if (o != RequestAttributes.notFound)
            return o;

        throw new AttributeNotFoundException(s, false);

    }

    public Object checkPageForAttribute(String s) {
        String snull = s + "_null";

        Object value = pageContext.getAttribute(s);
        if (value != null)
            return value;
        if (pageContext.getAttribute(snull) != null)
            return null;
        return RequestAttributes.notFound;
    }
}
