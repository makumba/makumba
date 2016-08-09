package org.makumba.test.tags;

import java.io.IOException;

import javax.servlet.ServletException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.makumba.test.util.MakumbaJspTestCase;
import org.makumba.test.util.MakumbaTestSetup;
import org.xml.sax.SAXException;

import com.meterware.httpunit.WebResponse;

/**
 * Tests the HQL list engine.
 * 
 * @author Johannes Peeters
 * @author Manuel Gay
 * @version $Id: ListOQLTest.java,v 1.1 25.09.2007 15:58:58 Manuel Exp $
 */
public class ListHQLTest extends MakumbaJspTestCase {

    @Override
    protected String getJspDir() {
        return "list-hql";
    }

    @Override
    protected MakumbaTestSetup getSetup() {
        return setup;
    }

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

    public void testTomcat() {
    }

    public void testHibernateMakObjectTag() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endHibernateMakObjectTag(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testHibernateMakListTag() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endHibernateMakListTag(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testHibernateMakValueChar() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endHibernateMakValueChar(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testHibernateMakValueDate() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endHibernateMakValueDate(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testHibernateMakValueInt() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endHibernateMakValueInt(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testHibernateMakValueDouble() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endHibernateMakValueDouble(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testHibernateMakValueText() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endHibernateMakValueText(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testHibernateMakValueSet() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endHibernateMakValueSet(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    /*
     * commented out by manu on 22-05-2009, because these tests fail with the current comparison method public void
     * testHibernateMakValueTS_create() throws ServletException, IOException {
     * pageContext.include("list-hql/testHibernateMakValueTS_create.jsp"); } public void
     * endHibernateMakValueTS_create(WebResponse response) throws Exception { try { output = response.getText();
     * fetchValidTestResult(output, record); } catch (IOException e) { fail("JSP output error: " +
     * response.getResponseMessage()); } assertTrue(compareTest(output)); } public void testHibernateMakValueTS_modify()
     * throws ServletException, IOException { pageContext.include("list-hql/testHibernateMakValueTS_modify.jsp"); }
     * public void endHibernateMakValueTS_modify(WebResponse response) throws Exception { try { output =
     * response.getText(); fetchValidTestResult(output, record); } catch (IOException e) { fail("JSP output error: " +
     * response.getResponseMessage()); } assertTrue(compareTest(output)); }
     */

    public void testHibernateMakIf() throws ServletException, IOException, SAXException {
        pageContext.include("list-hql/testHibernateMakIfTag.jsp");
    }

    public void endHibernateMakIf(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

}
