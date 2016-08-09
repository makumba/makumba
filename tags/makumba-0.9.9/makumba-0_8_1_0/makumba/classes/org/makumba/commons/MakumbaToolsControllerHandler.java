package org.makumba.commons;

import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.makumba.db.makumba.UniquenessServlet;
import org.makumba.forms.responder.ValueEditor;
import org.makumba.list.MakumbaDownloadServlet;
import org.makumba.providers.Configuration;

/**
 * Handle access to makumba tools, like the {@link UniquenessServlet}.
 * 
 * @author Rudolf Mayer
 * @version $Id: MakumbaToolsControllerHandler.java,v 1.1 Sep 4, 2008 1:33:31 AM rudi Exp $
 */
public class MakumbaToolsControllerHandler extends ControllerHandler {
    public boolean beforeFilter(ServletRequest req, ServletResponse res, FilterConfig conf,
            ServletObjects httpServletObjects) throws Exception {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI().replace(request.getContextPath(), "");
        if (path.startsWith(Configuration.getMakumbaUniqueLocation())) {
            new UniquenessServlet().doGet(request, response);
            return false;
        } else if (path.startsWith(Configuration.getMakumbaResourcesLocation())) {
            new MakumbaResourceServlet().doGet(request, response);
            return false;
        } else if (path.startsWith(Configuration.getMakumbaDownloadLocation())) {
            new MakumbaDownloadServlet().doGet(request, response);
            return false;
        } else if (path.startsWith(Configuration.getMakumbaValueEditorLocation())) {
            new ValueEditor().doPost(request, response);
            return false;
        } else {
            return true;
        }
    }

}
