package org.makumba.devel;
import javax.servlet.http.*;

/** This class offers a way for web applications (like Parade) 
 * to communicate to the makumba webapp.
 * For now, the only sensible thing done is to set the logging root
 */
public class SystemServlet extends HttpServlet
{
  public void service(HttpServletRequest req, HttpServletResponse resp)
  {
    String s=(String)req.getAttribute("org.makumba.systemServlet.command");
    if("setLoggingRoot".equals(s))
      {
	org.makumba.MakumbaSystem.setLoggingRoot((String)req.getAttribute("org.makumba.systemServlet.param1"));
      }
  }
}
