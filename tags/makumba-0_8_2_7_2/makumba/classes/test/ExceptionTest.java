package test;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.servlet.ServletException;

import org.apache.cactus.Request;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;
import test.util.MakumbaJspTestCase;

/**
 * Tests for the way makumba handles expcetions
 * 
 * FIXME this appears to be broken, cactus does not proceed the page but instead interrupts execution
 * 
 * @author Manuel Gay
 * @version $Id: ExceptionTest.java,v 1.1 May 11, 2008 3:25:44 AM manu Exp $
 */
public class ExceptionTest extends MakumbaJspTestCase {

    static Suite setup;

    private String output;
    private boolean record = false;

    private static final class Suite extends TestSetup {
        private Suite(Test arg0) {
            super(arg0);
        }
    }

    public static Test suite() {
        setup = new Suite(new TestSuite(ExceptionTest.class));
        return setup;
    }

    public void beginTomcat(Request request) {
        WebConversation wc = new WebConversation();
        WebRequest req = new GetMethodWebRequest(System.getProperty("cactus.contextURL"));
        try {
            WebResponse resp = wc.getResponse(req);
        } catch (MalformedURLException e) {
        } catch (IOException e) {
            System.err.println("\n\n\n\n\nYou should run tomcat first! Use mak-tomcat to do that.\n\n");
            System.exit(1);
        } catch (SAXException e) {
        }
    }

    public void testTomcat() {
    }
    
    public void testArrayIndexException() throws ServletException, IOException {
        pageContext.include("exceptions/testArrayIndexException.jsp");        
    }
    public void endArrayIndexException(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }
    
    public void testCompilationError() throws ServletException, IOException {
        pageContext.include("exceptions/testCompilationError.jsp");        
    }
    public void endCompilationError(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }
    
    public void testDataDefinitionNotFoundException() throws ServletException, IOException {
        pageContext.include("exceptions/testDataDefinitionNotFoundException.jsp");        
    }
    public void endDataDefinitionNotFoundException(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testNoSuchFieldException() throws ServletException, IOException {
        pageContext.include("exceptions/testNoSuchFieldException.jsp");        
    }
    public void endNoSuchFieldException(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }
    
    public void testNullPointerException() throws ServletException, IOException {
        pageContext.include("exceptions/testNullPointerException.jsp");        
    }
    public void endNullPointerException(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }
    
    public void testNumberFormatException() throws ServletException, IOException {
        pageContext.include("exceptions/testNumberFormatException.jsp");        
    }
    public void endNumberFormatException(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }
    
}