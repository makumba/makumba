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

package org.makumba.controller.jsp;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;


public class LogoutTag extends TagSupport
{
  String attr=null;

  public void setActor(String a){ attr=a; }
  
  public int doStartTag() throws JspException 
  {
    if (attr == null) {
        // should not happen. Actor is a required field.        

    } else if (attr.equals("*")) {
        pageContext.getSession().invalidate();

    } else if (attr.indexOf("*") != -1) {
        List removableAttributes = new ArrayList();

        // It's illegal (ConcurrentModificationException) to removeAttribute() while enumating
        for (Enumeration e = pageContext.getAttributeNamesInScope(PageContext.SESSION_SCOPE); e.hasMoreElements(); ) {
            String name = (String) e.nextElement();
            if (match(name, attr)) { 
                removableAttributes.add(name); 
            }
        }
        for (Iterator it = removableAttributes.iterator(); it.hasNext(); ) { 
            pageContext.removeAttribute ( (String) it.next(), PageContext.SESSION_SCOPE);
        }

    } else if (pageContext.getAttribute(attr, PageContext.SESSION_SCOPE)!=null) {
        pageContext.removeAttribute(attr, PageContext.SESSION_SCOPE);
    }
    return EVAL_BODY_INCLUDE;
  }




 /**
  * Tests if a String s matches to a pattern. The pattern's only wildcard character is '*'.
  * '*' matches with any number of characters.
  */
  private boolean match(String s, String pattern) {

    /* [IMPROVE] may want to move to a more general location (package org.makumba.util ?) */

    StringTokenizer st = new StringTokenizer (pattern, "*");
    String token;
    int pos = 0;   // keeps track where we are (advancing) in s

    // special cases: special patterns
    if (pattern.length() == 0) return false;       // "" 
    if (!st.hasMoreTokens()) return true;          // "*****" 

    // special case: pattern starts NOT with wildcard
    if (!pattern.startsWith("*")) {
        token = st.nextToken();
        if (!s.startsWith(token)) return false;
        pos += token.length();
    }

    // run through the pattern, match all tokens (between the wildcards)
    while (st.hasMoreTokens() ) {
        token = st.nextToken();
        int p = s.substring(pos).indexOf(token);     // p = distance within substring
        if (p == -1) return false;
        pos += p + token.length();
    }

    // if at the end of s, or pattern ends with wildcard, the match is successful.
    return ( pos == s.length() || pattern.endsWith("*") );
  }


}
