package org.makumba.devel;
import java.io.*;
import javax.servlet.http.*;

/** the java viewer. It should be a filter from another (mb third-party) viewer that links known .java and .mdd sources. See SourceViewServlet for the filter architecture */
public class javaViewer extends LineViewer
{
  public javaViewer(HttpServletRequest req, HttpServlet sv) throws Exception
  {
    super(true);
    contextPath=req.getContextPath();
    virtualPath=req.getPathInfo().substring(1);
    readFromURL(org.makumba.util.ClassResource.get(virtualPath.replace('.', '/')+".java"));
  }
}
