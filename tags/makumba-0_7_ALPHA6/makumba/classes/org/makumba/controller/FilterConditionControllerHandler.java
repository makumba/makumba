package org.makumba.controller;

import java.net.URL;

import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.makumba.commons.ControllerHandler;
import org.makumba.commons.ServletObjects;

public class FilterConditionControllerHandler extends ControllerHandler {

    @Override
    public boolean beforeFilter(ServletRequest request, ServletResponse response, FilterConfig conf, ServletObjects httpServletObjects) throws Exception {
        HttpServletRequest req=(HttpServletRequest)request;
        String uri = req.getRequestURI();

        // accesses to the source viewer are not filtered
        if (uri.startsWith("/dataDefinitions") || uri.startsWith("/logic") || uri.startsWith("/classes")) {
            return false;
        }
        String file = null;
        try {
            file = new URL(req.getRequestURL().toString()).getFile();
        } catch (java.net.MalformedURLException e) {
        } // can't be
 
        // JSP and HTML are always filtered
        if (file.endsWith(".jsp") || file.endsWith(".html")) {
            return true;
        }

        // JSPX is never filtered
        if (file.endsWith(".jspx")) {
            return false;
        }

        // we compute the file that corresponds to the indicated path
        java.io.File f = new java.io.File(conf.getServletContext().getRealPath(uri));

        // if it's a directory, there will most probably be a redirection, we filter anyway
        if (f.isDirectory()) {
            return true;
        }

        // if the file does not exist on disk, it means that it's produced dynamically, so we filter
        // it it exists, it's probably an image or a CSS, we don't filter
        return !f.exists();
    }


}
