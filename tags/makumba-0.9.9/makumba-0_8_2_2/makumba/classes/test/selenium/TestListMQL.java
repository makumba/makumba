package test.selenium;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.thoughtworks.selenium.*;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.servlet.ServletException;

import org.apache.cactus.Request;
import org.xml.sax.SAXException;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.MakumbaTestSetup;

public class TestListMQL extends SeleneseTestCase {

    static Suite setup;
    
    private static final class Suite extends MakumbaTestSetup {
        private Suite(Test arg0) {
            super(arg0, "oql");
        }
    }
    
    public static Test suite() {
        setup = new Suite(new TestSuite(TestListMQL.class));
        return setup;
    }
    
    public void setUp() throws Exception {
        setUp("http://localhost:8080/", "*firefox");
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
        selenium.open("/tests/list-oql/testMakObjectTag.jsp");
        verifyTrue(selenium.isTextPresent("name:john"));
        verifyTrue(selenium.isTextPresent("weight:85.7"));

    }
}
