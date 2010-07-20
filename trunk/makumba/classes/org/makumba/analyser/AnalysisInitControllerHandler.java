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

package org.makumba.analyser;

import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.makumba.commons.ControllerHandler;
import org.makumba.commons.ServletObjects;

/**
 * This {@link ControllerHandler} ensures that the analysis is initialised correctly at each access
 * 
 * @author Manuel Gay
 * @version $Id$
 */
public class AnalysisInitControllerHandler extends ControllerHandler {

    private static final String HAD_ERROR = "org.makumba.analysisInit.hadError";

    @Override
    public boolean beforeFilter(ServletRequest request, ServletResponse response, FilterConfig conf,
            ServletObjects httpServletObjects) {

        AnalysableElement.initializeThread(((HttpServletRequest) request).getSession(), getAnalysisStateKey(request));
        return true;
    }

    @Override
    public boolean onError(ServletRequest request, ServletResponse response, Throwable e, FilterConfig conf) {
        request.setAttribute(HAD_ERROR, new Boolean(true));
        return true;
    }

    @Override
    public void finalize(ServletRequest request, ServletResponse response) {
        if (!response.isCommitted()) {

            HttpSession session = ((HttpServletRequest) request).getSession();
            String key = getAnalysisStateKey(request);
            if (request.getAttribute(HAD_ERROR) != null && (Boolean) request.getAttribute(HAD_ERROR)) {
                // keep the state of the previous analysis so we can display errors even when reloading the page
                AnalysableElement.keepAnalysisState(session, key);
            } else {
                // first remove the state object from the session
                if (session.getServletContext().getAttribute(key) != null) {
                    session.getServletContext().removeAttribute(key);
                }
                // then initialize the thread, it won't reload the state this time
                AnalysableElement.initializeThread(session, key);

                // finally discard the parsing data
                AnalysableElement.discardJSPParsingData();
            }
        }
    }

    private String getAnalysisStateKey(ServletRequest request) {
        return AnalysableElement.ANALYSIS_STATE + ((HttpServletRequest) request).getRequestURI()
                + ((HttpServletRequest) request).getSession().getId();
    }
}
