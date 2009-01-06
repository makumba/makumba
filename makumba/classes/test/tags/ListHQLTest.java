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
 * Tests the HQL list engine.
 * @author Johannes Peeters
 * @author Manuel Gay
 * @version $Id: ListOQLTest.java,v 1.1 25.09.2007 15:58:58 Manuel Exp $
 */
public class ListHQLTest extends MakumbaJspTestCase {

    private boolean record = false;

    private String output;

    static Suite setup;

    private static final class Suite extends MakumbaTestSetup {
        private Suite(Test arg0) {
            super(arg0, "hql");
        }

    }

    public static Test suite() {
        setup = new Suite(new TestSuite(ListHQLTest.class));
        return setup;
    }

    public void beginTomcat(Request request) {
        WebConversation wc = new WebConversation();
        WebRequest     req = new GetMethodWebRequest(System.getProperty("cactus.contextURL"));
        try {
            wc.getResponse( req );
        } catch (MalformedURLException e) {
        } catch (IOException e) {
            setup.tearDown();
            System.err.println("\n\n\n\n\nYou should run tomcat first! Use mak-tomcat to do that.\n\n");
            System.exit(1);
        } catch (SAXException e) {
        }
    }
    public void testTomcat(){}















    public void testHibernateMakObjectTag() throws ServletException, IOException {
        pageContext.include("list-hql/testHibernateMakObjectTag.jsp");
    }
    public void endHibernateMakObjectTag(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));

    }

    public void testHibernateMakListTag() throws ServletException, IOException {
        pageContext.include("list-hql/testHibernateMakListTag.jsp");
    }

    public void endHibernateMakListTag(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testHibernateMakValueChar() throws ServletException, IOException {
        pageContext.include("list-hql/testHibernateMakValueChar.jsp");
    }

    public void endHibernateMakValueChar(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testHibernateMakValueDate() throws ServletException, IOException {
        pageContext.include("list-hql/testHibernateMakValueDate.jsp");
    }

    public void endHibernateMakValueDate(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testHibernateMakValueInt() throws ServletException, IOException {
        pageContext.include("list-hql/testHibernateMakValueInt.jsp");
    }

    public void endHibernateMakValueInt(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testHibernateMakValueDouble() throws ServletException, IOException {
        pageContext.include("list-hql/testHibernateMakValueDouble.jsp");
    }
    public void endHibernateMakValueDouble(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));

    }

    public void testHibernateMakValueText() throws ServletException, IOException {
        pageContext.include("list-hql/testHibernateMakValueText.jsp");
    }
    public void endHibernateMakValueText(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));

    }

    public void testHibernateMakValueSet() throws ServletException, IOException {
        pageContext.include("list-hql/testHibernateMakValueSet.jsp");
    }
    public void endHibernateMakValueSet(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));

    }

    public void testHibernateMakValueTS_create() throws ServletException, IOException {
        pageContext.include("list-hql/testHibernateMakValueTS_create.jsp");
    }
    public void endHibernateMakValueTS_create(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));

    }

    public void testHibernateMakValueTS_modify() throws ServletException, IOException {
        pageContext.include("list-hql/testHibernateMakValueTS_modify.jsp");
    }
    public void endHibernateMakValueTS_modify(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));

    }

    public void testHibernateMakIf() throws ServletException, IOException, SAXException {
        pageContext.include("list-hql/testHibernateMakIfTag.jsp");
    }

    public void endHibernateMakIf(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));

    }

}
