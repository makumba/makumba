package org.makumba.test.component;

import java.io.IOException;

import javax.servlet.ServletException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.makumba.test.util.MakumbaJspTestCase;
import org.makumba.test.util.MakumbaTestSetup;

import com.meterware.httpunit.WebResponse;

/**
 * Tests for the way makumba handles expcetions FIXME this appears to be broken, cactus does not proceed the page but
 * instead interrupts execution
 * 
 * @author Manuel Bernhardt <manuel@makumba.org>
 * @version $Id: ExceptionTest.java,v 1.1 May 11, 2008 3:25:44 AM manu Exp $
 */
public class ExceptionTest extends MakumbaJspTestCase {

    static Suite setup;

    @Override
    protected String getJspDir() {
        return "exceptions";
    }

    @Override
    protected MakumbaTestSetup getSetup() {
        return setup;
    }

    private static final class Suite extends MakumbaTestSetup {
        private Suite(Test arg0) {
            super(arg0, "oql");
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
        compareToFileWithTestName(response, false);
    }

    public void testCompilationError() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endCompilationError(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testDataDefinitionNotFoundException() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endDataDefinitionNotFoundException(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testNoSuchFieldException() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endNoSuchFieldException(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testNullPointerException() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endNullPointerException(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testNumberFormatException() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endNumberFormatException(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }
}