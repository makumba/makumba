package org.makumba.devel;
import java.io.*;
import javax.servlet.http.*;
import org.makumba.controller.Logic;

/** the java viewer. It should be a filter from another (mb third-party) viewer that links known .java and .mdd sources. See SourceViewServlet for the filter architecture */
public class logicViewer extends LineViewer
{
  public logicViewer(HttpServletRequest req, HttpServlet sv) throws Exception
  {
    super(false);
    virtualPath=req.getPathInfo();
    contextPath=req.getContextPath();
    Logic.getLogic(virtualPath);
    reader=new StringReader(Logic.getSearchMessage(virtualPath));
  }
}
