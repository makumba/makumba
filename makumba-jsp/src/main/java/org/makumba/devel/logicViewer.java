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

import java.io.PrintWriter;
import java.io.StringReader;

import javax.servlet.http.HttpServletRequest;

import org.makumba.commons.tags.MakumbaJspConfiguration;
import org.makumba.controller.Logic;

/** The java logic finder visualizer. It shows how the logic is searched for and which logic is finally used. */
public class logicViewer extends LineViewer {
    public logicViewer(HttpServletRequest req) throws Exception {
        super(false, req);
        viewerName = "Logic Viewer";
        virtualPath = DevelUtils.getVirtualPath(req,
            MakumbaJspConfiguration.getToolLocation(DeveloperTool.LOGIC_DISCOVERY));
        contextPath = req.getContextPath();
        Logic.getLogic(virtualPath);
        reader = new StringReader(Logic.getSearchMessage(virtualPath));
        title = "Logic for " + virtualPath;
    }

    @Override
    public void navigation(PrintWriter w) {
        DevelUtils.printNavigationButton(w, "page", contextPath + virtualPath + "x", "", 0);
        DevelUtils.writeDevelUtilLinks(w, DeveloperTool.LOGIC_DISCOVERY.getKey(), contextPath);
    }

}