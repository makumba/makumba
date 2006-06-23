package test.http;

import junit.framework.*;
import org.apache.cactus.ServletTestCase;
import org.apache.cactus.WebRequest;

public class TestSampleServlet extends ServletTestCase
{

    public TestSampleServlet(String theName)
    {
        super(theName);
    }

    public static Test suite()
    {
        return new TestSuite(TestSampleServlet.class);
    }

    public void beginSaveToSessionOK(WebRequest webRequest)
    {
        webRequest.addParameter("testparam", "it works!");
    }

    public void testSaveToSessionOK()
    {
        SampleServlet servlet = new SampleServlet();
        servlet.saveToSession(request);
        Assert.assertEquals("it works!", session.getAttribute("testAttribute"));
    }
}