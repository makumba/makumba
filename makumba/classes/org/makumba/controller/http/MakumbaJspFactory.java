//////////////////////////////
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
/////////////////////////////////////
package org.makumba.controller.http;

import java.util.Stack;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspEngineInfo;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;

/**
 * A JSP factory that wraps the default factory from the servlet container.<br>
 * This is needed for Makumba to find out when a page begins and ends, and to store its pageContext.
 * 
 * @author cristi
 * @version $Id: MakumbaJspFactory.java,v 1.1 May 5, 2010 10:51:00 PM cristi Exp $
 */
public class MakumbaJspFactory extends JspFactory {

    // TODO: not sure if the ThreadLocal should be here, or whether there's a more fitting place
    private static ThreadLocal<Stack<PageContext>> pageContextStack = new ThreadLocal<Stack<PageContext>>();

    // state pattern, we stay in the initial state until we find the container factory
    // this will happen at first access but we make sure that concurrent initial accesses don't collide
    // further accesses will use the noop state which does nothing
    static public Runnable checker = new Runnable() {
        public synchronized void run() {

            JspFactory fact = JspFactory.getDefaultFactory();
            if (fact != null) {
                if (!(fact.getClass().getName().endsWith("MakumbaJspFactory"))) {
                    JspFactory.setDefaultFactory(new MakumbaJspFactory(fact));
                } else
                    throw new IllegalStateException("Cannot use JspFactory from old classloader!");

                checker = noop;
            }
        }
    };

    static Runnable noop = new Runnable() {
        public void run() {
        }
    };

    /**
     * Reset the factory to the original value We do this at webapp undeploy, otherwise the next webapp will use an
     * instance of this class loaded with the previous classloader.
     */
    static void reset() {
        JspFactory current = JspFactory.getDefaultFactory();
        if (!(current instanceof MakumbaJspFactory))
            return;
        pageContextStack = null;
        JspFactory.setDefaultFactory(((MakumbaJspFactory) current).getDecorated());
    }

    JspFactory fact = null;

    public JspFactory getDecorated() {
        return fact;
    }

    public String toString() {
        return "mak factory decorating " + fact;
    }

    public MakumbaJspFactory(JspFactory fact) {
        this.fact = fact;
    }

    @Override
    public JspEngineInfo getEngineInfo() {
        return fact.getEngineInfo();
    }

    @Override
    public PageContext getPageContext(Servlet servlet, ServletRequest request, ServletResponse response,
            String errorPageURL, boolean needsSession, int buffer, boolean autoflush) {
        // System.out.println(servlet);

        // we hang the pageContext in a threadLocal stack
        PageContext pageContext = fact.getPageContext(servlet, request, response, errorPageURL, needsSession, buffer,
            autoflush);
        if(pageContextStack.get()==null)
            pageContextStack.set(new Stack<PageContext>());
        pageContextStack.get().push(pageContext);

        // and also trigger page analysis
        // this also tells us when a page starts or is included

        return pageContext;
    }

    @Override
    public void releasePageContext(PageContext pc) {
        fact.releasePageContext(pc);

        // this tells us when a page finishes and if it was included, we will go back to the including page

        // here we pop the pageContext from the thread local stack
        pageContextStack.get().pop(); // TODO: maybe check if the result of pop() and pc is the same?

        // TODO: activate the runtime state of the makumba tags from the including page
    }

    @Override
    public JspApplicationContext getJspApplicationContext(ServletContext arg0) {
        return fact.getJspApplicationContext(arg0);
    }

    public static PageContext getPageContext() {
        return pageContextStack.get().peek();
    }
}