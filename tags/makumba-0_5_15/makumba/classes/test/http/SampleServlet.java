package test.http;

import javax.servlet.http.*;

public class SampleServlet extends HttpServlet
{
    public void saveToSession(HttpServletRequest request)
    {
        String testparam = request.getParameter("testparam");
        request.getSession().setAttribute("testAttribute", testparam);
    }
}