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
//  $Id: ControllerFilter.java 2380 2008-05-18 12:35:59Z rosso_nero $
//  $Name$
/////////////////////////////////////

package org.makumba.devel;

import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.makumba.commons.ControllerHandler;
import org.makumba.commons.ServletObjects;
import org.makumba.providers.Configuration;
import org.makumba.providers.DeveloperTool;

/**
 * Handle access to the data tools.
 * 
 * @author Rudolf Mayer
 * @version $Id: DataToolsControllerHandler.java,v 1.1 Sep 3, 2008 9:13:16 PM rudi Exp $
 */
public class DataToolsControllerHandler extends ControllerHandler {

    @Override
    public boolean beforeFilter(ServletRequest req, ServletResponse res, FilterConfig conf,
            ServletObjects httpServletObjects) throws Exception {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = getPath(request);
        if (path.startsWith(Configuration.getToolLocation(DeveloperTool.DATA_QUERY))) {
            new DataQueryServlet().doGet(request, response);
            return false;
        } else if (path.startsWith(Configuration.getToolLocation(DeveloperTool.DATA_LISTER))) {
            new DataTypeListerServlet().doGet(request, response);
            return false;
        } else if (path.startsWith(Configuration.getToolLocation(DeveloperTool.OBJECT_VIEWER))) {
            new DataObjectViewerServlet().doGet(request, response);
            return false;
        } else if (path.startsWith(Configuration.getToolLocation(DeveloperTool.OBJECT_ID_CONVERTER))) {
            new DataPointerValueConverter().doGet(request, response);
            return false;
        } else if (path.startsWith(Configuration.getToolLocation(DeveloperTool.REFERENCE_CHECKER))) {
            new ReferenceChecker().doGet(request, response);
            return false;
        } else if (path.startsWith(Configuration.getToolLocation(DeveloperTool.ERRORLOG_VIEWER))) {
            new ErrorLogViewerServlet().doGet(request, response);
            return false;
        } else {
            return true;
        }
    }
}
