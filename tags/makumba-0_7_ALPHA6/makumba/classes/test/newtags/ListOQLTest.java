package test.newtags;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.servlet.ServletException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.Request;
import org.xml.sax.SAXException;

import test.MakumbaTestSetup;
import test.util.MakumbaJspTestCase;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

/**
 * Tests the OQL list engine.
 *
 * @author Johannes Peeters
 * @author Manuel Gay
 * @version $Id: ListOQLTest.java,v 1.1 25.09.2007 15:58:58 Manuel Exp $
 */
public class ListOQLTest extends MakumbaJspTestCase {

    private boolean record = false;

    static Suite setup;

        private String output;

        private static final class Suite extends MakumbaTestSetup {

            private Suite(Test arg0) {
                super(arg0, "oql");
        }
    }

    public static Test suite() {
        setup = new Suite(new TestSuite(ListOQLTest.class));
        return setup;
    }

    public void beginTomcat(Request request) {
        WebConversation wc = new WebConversation();
        WebRequest req = new GetMethodWebRequest(System.getProperty("cactus.contextURL"));
        try {
            WebResponse resp = wc.getResponse(req);
        } catch (MalformedURLException e) {
        } catch (IOException e) {
            setup.tearDown();
            System.err.println("\n\n\n\n\nYou should run tomcat first! Use mak-tomcat to do that.\n\n");
            System.exit(1);
        } catch (SAXException e) {
        }
    }

    public void testTomcat() {
    }

    public void testListObjectTag() throws ServletException, IOException {
        pageContext.include("list-oql/testMakObjectTag.jsp");
    }

    public void endListObjectTag(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testListListTag() throws ServletException, IOException {
        pageContext.include("list-oql/testMakListTag.jsp");
    }

    public void endListListTag(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }

        assertTrue(compareTest(output));
    }

    public void testListListCount() throws ServletException, IOException {
        pageContext.include("list-oql/testMakListCount.jsp");
    }

    public void endListListCount(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }

        assertTrue(compareTest(output));
    }

    public void testListValueChar() throws ServletException, IOException {
        pageContext.include("list-oql/testMakValueChar.jsp");
    }

    public void endListValueChar(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));

    }

    public void testListValueDate() throws ServletException, IOException {
        pageContext.include("list-oql/testMakValueDate.jsp");
    }

    public void endListValueDate(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testListValueInt() throws ServletException, IOException {
        pageContext.include("list-oql/testMakValueInt.jsp");
    }

    public void endListValueInt(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testListValueDouble() throws ServletException, IOException {
        pageContext.include("list-oql/testMakValueDouble.jsp");
    }

    public void endListValueDouble(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testListValueText() throws ServletException, IOException {
        pageContext.include("list-oql/testMakValueText.jsp");
    }

    public void endListValueText(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testListValueSet() throws ServletException, IOException {
        pageContext.include("list-oql/testMakValueSet.jsp");
    }

    public void endListValueSet(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));

    }

    public void testListValueTS_create() throws ServletException, IOException {
        pageContext.include("list-oql/testMakValueTS_create.jsp");
    }

    public void endListValueTS_create(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testListValueTS_modify() throws ServletException, IOException {
        pageContext.include("list-oql/testMakValueTS_modify.jsp");
    }

    public void endListValueTS_modify(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

}
