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
 * Tests the HQL list engine.
 * @author Johannes Peeters
 * @author Manuel Gay
 * @version $Id: ListOQLTest.java,v 1.1 25.09.2007 15:58:58 Manuel Exp $
 */
public class ListHQLTest extends MakumbaJspTestCase {

    private boolean record = false;

    static Suite setup;

    private String output;

    private static final class Suite extends MakumbaTestSetup {
        private Suite(Test arg0) {
            super(arg0, "oql");
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
            WebResponse   resp = wc.getResponse( req );
        } catch (MalformedURLException e) {
        } catch (IOException e) {
            setup.tearDown();
            System.err.println("\n\n\n\n\nYou should run tomcat first! Use mak-tomcat to do that.\n\n");
            System.exit(1);
        } catch (SAXException e) {
        }
    }
    public void testTomcat(){}















    public void testHibernateListObjectTag() throws ServletException, IOException {
        pageContext.include("list-hql/testHibernateMakObjectTag.jsp");
    }
    public void endHibernateListObjectTag(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));

    }

    public void testHibernateListListTag() throws ServletException, IOException {
        pageContext.include("list-hql/testHibernateMakListTag.jsp");
    }

    public void endHibernateListListTag(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testHibernateListValueChar() throws ServletException, IOException {
        pageContext.include("list-hql/testHibernateMakValueChar.jsp");
    }

    public void endHibernateListValueChar(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testHibernateListValueDate() throws ServletException, IOException {
        pageContext.include("list-hql/testHibernateMakValueDate.jsp");
    }

    public void endHibernateListValueDate(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testHibernateListValueInt() throws ServletException, IOException {
        pageContext.include("list-hql/testHibernateMakValueInt.jsp");
    }

    public void endHibernateListValueInt(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testHibernateListValueDouble() throws ServletException, IOException {
        pageContext.include("list-hql/testHibernateMakValueDouble.jsp");
    }
    public void endHibernateListValueDouble(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));

    }

    public void testHibernateListValueText() throws ServletException, IOException {
        pageContext.include("list-hql/testHibernateMakValueText.jsp");
    }
    public void endHibernateListValueText(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));

    }

    public void testHibernateListValueSet() throws ServletException, IOException {
        pageContext.include("list-hql/testHibernateMakValueSet.jsp");
    }
    public void endHibernateListValueSet(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));

    }

    public void testHibernateListValueTS_create() throws ServletException, IOException {
        pageContext.include("list-hql/testHibernateMakValueTS_create.jsp");
    }
    public void endHibernateListValueTS_create(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));

    }

    public void testHibernateListValueTS_modify() throws ServletException, IOException {
        pageContext.include("list-hql/testHibernateMakValueTS_modify.jsp");
    }
    public void endHibernateListValueTS_modify(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));

    }

    public void testHibernateListIf() throws ServletException, IOException, SAXException {
        pageContext.include("list-hql/testHibernateMakIfTag.jsp");
    }

    public void endHibernateListIf(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));

    }

}
