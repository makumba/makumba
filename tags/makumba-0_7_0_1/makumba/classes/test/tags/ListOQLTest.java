package test.tags;

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
            wc.getResponse(req);
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

    public void testMakObjectTag() throws ServletException, IOException {
        pageContext.include("list-oql/testMakObjectTag.jsp");
    }

    public void endMakObjectTag(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testMakListTag() throws ServletException, IOException {
        pageContext.include("list-oql/testMakListTag.jsp");
    }

    public void endMakListTag(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }

        assertTrue(compareTest(output));
    }

    public void testMakListCount() throws ServletException, IOException {
        pageContext.include("list-oql/testMakListCount.jsp");
    }

    public void endMakListCount(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }

        assertTrue(compareTest(output));
    }

    public void testMakListMaxResults() throws ServletException, IOException {
        pageContext.include("list-oql/testMakListMaxResults.jsp");
    }

    public void endMakListMaxResults(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }

        assertTrue(compareTest(output));
    }

    public void testMakValueChar() throws ServletException, IOException {
        pageContext.include("list-oql/testMakValueChar.jsp");
    }

    public void endMakValueChar(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));

    }

    public void testMakValueDate() throws ServletException, IOException {
        pageContext.include("list-oql/testMakValueDate.jsp");
    }

    public void endMakValueDate(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testMakValueInt() throws ServletException, IOException {
        pageContext.include("list-oql/testMakValueInt.jsp");
    }

    public void endMakValueInt(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testMakValueDouble() throws ServletException, IOException {
        pageContext.include("list-oql/testMakValueDouble.jsp");
    }

    public void endMakValueDouble(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testMakValueText() throws ServletException, IOException {
        pageContext.include("list-oql/testMakValueText.jsp");
    }

    public void endMakValueText(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testMakValueSet() throws ServletException, IOException {
        pageContext.include("list-oql/testMakValueSet.jsp");
    }

    public void endMakValueSet(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));

    }

    public void testMakValueTS_create() throws ServletException, IOException {
        pageContext.include("list-oql/testMakValueTS_create.jsp");
    }

    public void endMakValueTS_create(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testMakValueTS_modify() throws ServletException, IOException {
        pageContext.include("list-oql/testMakValueTS_modify.jsp");
    }

    public void endMakValueTS_modify(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testMakPagination() throws ServletException, IOException {
        pageContext.include("list-oql/testMakPaginationTag.jsp");
    }

    public void endMakPagination(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testMDDFunctions() throws ServletException, IOException {
        pageContext.include("list-oql/testMDDFunctions.jsp");
    }

    public void endMDDFunctions(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, true);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }
}
