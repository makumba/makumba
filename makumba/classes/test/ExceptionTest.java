package test;

import java.io.IOException;

import javax.servlet.ServletException;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.util.MakumbaJspTestCase;

import com.meterware.httpunit.WebResponse;

/**
 * Tests for the way makumba handles expcetions FIXME this appears to be broken, cactus does not proceed the page but
 * instead interrupts execution
 * 
 * @author Manuel Gay
 * @version $Id: ExceptionTest.java,v 1.1 May 11, 2008 3:25:44 AM manu Exp $
 */
public class ExceptionTest extends MakumbaJspTestCase {

    @Override
    protected String getJspDir() {
        return "exceptions";
    }

    @Override
    protected boolean getRecordingMode() {
        return false;
    }

    @Override
    protected MakumbaTestSetup getSetup() {
        return setup;
    }

    static Suite setup;

    private static final class Suite extends MakumbaTestSetup {
        private Suite(Test arg0) {
            super(arg0, null);
        }
    }

    public static Test suite() {
        setup = new Suite(new TestSuite(ExceptionTest.class));
        return setup;
    }

    public void testTomcat() {
    }

    public void testArrayIndexException() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endArrayIndexException(WebResponse response) throws Exception {
        compareToFileWithTestName(response);
    }

    public void testCompilationError() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endCompilationError(WebResponse response) throws Exception {
        compareToFileWithTestName(response);
    }

    public void testDataDefinitionNotFoundException() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endDataDefinitionNotFoundException(WebResponse response) throws Exception {
        compareToFileWithTestName(response);
    }

    public void testNoSuchFieldException() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endNoSuchFieldException(WebResponse response) throws Exception {
        compareToFileWithTestName(response);
    }

    public void testNullPointerException() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endNullPointerException(WebResponse response) throws Exception {
        compareToFileWithTestName(response);
    }

    public void testNumberFormatException() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endNumberFormatException(WebResponse response) throws Exception {
        compareToFileWithTestName(response);
    }
}