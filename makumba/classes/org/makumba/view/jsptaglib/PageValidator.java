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

package org.makumba.view.jsptaglib;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Stack;

import javax.servlet.jsp.tagext.TagLibraryValidator;
import javax.servlet.jsp.tagext.ValidationMessage;

/** this class is not in use. to use it, copy servlet_context/WEB-INF/makumba.tld to servlet_context/WEB-INF/makumba.tld.1.1 and servlet_context/WEB-INF/makumba.tld.1.2 to servlet_context/WEB-INF/makumba.tld. make sure that makumba.tld declares within <taglib ...> :
	<validator>
		<validator-class>org.makumba.view.jsptaglib.PageValidator</validator-class>
	</validator>
	*/
 
public class PageValidator extends TagLibraryValidator
{
  static ThreadLocal pageStack= new ThreadLocal();

  public ValidationMessage[] validate(java.lang.String prefix,
				      java.lang.String uri,
				      javax.servlet.jsp.tagext.PageData page)
  {
    StringBuffer sb= new StringBuffer();
    String s;
    BufferedReader r= new BufferedReader(new InputStreamReader(page.getInputStream()));
    try{
      while((s=r.readLine())!=null)
	sb.append(s).append("\n");
    }catch(IOException e){System.err.println(e);}
    String pg= sb.toString();
    int n= pg.indexOf("mak:");
    if(n!=-1)
      System.out.println("######## pushing"+ pg.substring(n-1, n+40));
    Stack st= (Stack)pageStack.get();
    if(st==null)
      {st=new Stack(); pageStack.set(st);}
    st.push(pg);
    return null;
  }
}
