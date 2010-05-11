package test.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.JspTestCase;
import org.apache.cactus.Request;
import org.xml.sax.SAXException;

import test.MakumbaWebTestSetup;

import bmsi.util.Diff;
import bmsi.util.DiffPrint;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebResponse;

/**
 * Utility class which enables it to quickly write tests based on the execution of a JSP. Since we know the expected
 * result, we can fetch this result and store it into a file, and then compare the next executions of the test against
 * this file.
 * 
 * @author Manuel Gay
 * @version $Id: MakumbaJSPTest.java,v 1.1 25.09.2007 16:08:26 Manuel Exp $
 */
public abstract class MakumbaJspTestCase extends JspTestCase {

    private static final String EXPECTED_RESULT_EXTENSION = ".html";

    public static final class Suite extends TestSetup {
        private Suite(Test arg0) {
            super(arg0);
        }
    }

    /**
     * Compares a test output to its stored (expected) result. The method detects automatically the name of the test
     * based on the invoking method, so all there's to do is to pass it the new result.<br>
     * TODO this should be much more verbose
     * 
     * @param result
     *            the new result, from the currently running test
     * @return <code>true</code> if this worked out, <code>false</code> otherwise.
     * @throws FileNotFoundException
     *             in case the comparison basis file is not found, this indicates it
     */
    protected boolean compareTest(String result, String testName) throws Exception {

        boolean testOk = true;

        // based on the method name, we retrieve the file used as comparison basis
        File f = getExpectedResult(testName);

        if (!f.exists())
            throw new Exception("Couldn't find the comparison file in classes/test/expected/" + testName
                    + EXPECTED_RESULT_EXTENSION
                    + " - create it first using the fetchValidTestResult(String result) method!");

        String fileIntoString = "";
        BufferedReader fileIn = null;
        BufferedReader stringIn = null;
        ArrayList<String> expectedResult = new ArrayList<String>();
        ArrayList<String> realResult = new ArrayList<String>();

        try {
            fileIn = new BufferedReader(new FileReader(f));
            stringIn = new BufferedReader(new StringReader(result));
            String strFile = "";
            String strStr = "";

            while ((strFile = fileIn.readLine()) != null) {
                fileIntoString += strFile + "\n";
                strStr = stringIn.readLine();
                if (!strFile.equals(strStr)) {
                    // important! set this only to false in case we are different DO NOT set it to true if it is equal,
                    // otherwise we potentially forget about previously differing lines
                    testOk = false;
                }
                expectedResult.add(strFile);
                if (strStr != null) { // we need to check if the expected line is not null
                    realResult.add(strStr);
                }
            }
            while ((strStr = stringIn.readLine()) != null) { // read possible rest of result page
                realResult.add(strStr);
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                fileIn.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (!testOk) {
            System.out.println("\n************************ Test " + testName + " failed! ************************");

            try {
                StringWriter stringWriter = new StringWriter();
                String[] a = (String[]) expectedResult.toArray(new String[expectedResult.size()]);
                String[] b = (String[]) realResult.toArray(new String[realResult.size()]);
                // System.out.println(Arrays.toString(a));
                // System.out.println(Arrays.toString(b));
                Diff d = new Diff(a, b);
                Diff.change script = d.diff_2(false);
                DiffPrint.NormalPrint p = new DiffPrint.NormalPrint(a, b);
                p.setOutput(stringWriter);
                p.print_script(script);
                System.out.println("**** '>' marks lines added in the test result, '<' lines in the expected result (in file "
                        + "classes/test/expected/" + testName + EXPECTED_RESULT_EXTENSION + ") *****");
                System.out.println(stringWriter.toString());
            } catch (Exception e) { // if there is an error in the Diff calculation, we fall back to the old display
                System.out.println("======================== Expected ========================");
                System.out.println(fileIntoString);
                System.out.println("======================== Actual ========================");
                System.out.println(result);
            }
        }

        return testOk;
    }

    private File getExpectedResult(String testName) {
        File f = new File("classes/test/expected/" + testName + EXPECTED_RESULT_EXTENSION);
        return f;
    }

    /**
     * Method that helps to fetch the result of a test, on the first run. Just pass it the expected result, it will
     * store it automatically. Don't forget to remove it after the first time!
     * 
     * @param output
     *            the result (HTML code) of the page that was ran correctly.
     * @param testName
     * @param record
     *            TODO
     */
    protected void fetchValidTestResult(String output, String testName, boolean record) {

        if (!record)
            return;

        // based on the method name, we retrieve the file used as comparison basis
        File f = getExpectedResult(testName);
        if (!f.exists())
            try {
                f.createNewFile();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(f));
            out.write(output);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Examine the stacktrace and find the test method
     * 
     * @return the test method name
     */
    private String getTestMethod() {
        StackTraceElement[] stackTrace = new Throwable().fillInStackTrace().getStackTrace();
        for (int n = 0;; n++)
            if (stackTrace[n].getMethodName().startsWith("begin") || stackTrace[n].getMethodName().startsWith("test")
                    || stackTrace[n].getMethodName().startsWith("end"))
                return stackTrace[n].getMethodName();
    }

    /**
     * Compare the given WebResponse to the file that has the name of the current test
     * 
     * @param response
     *            the WebResponse to compare
     * @throws Exception
     */
    protected void compareToFileWithTestName(WebResponse response) throws Exception {
        compareToFile(response, getTestMethod());
    }

    protected void compareToFile(WebResponse response, String testName) throws Exception {
        String output = null;
        try {
            output = response.getText();
            fetchValidTestResult(output, testName, getRecordingMode());
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output, testName));
    }

    /**
     * Include the JSP from the JSP dir of this suite that has the name of the currently running test
     * 
     * @throws ServletException
     * @throws IOException
     */
    protected void includeJspWithTestName() throws ServletException, IOException {
        pageContext.include(getJspNameBasedOnTestMethod());
    }

    /**
     * Retrieve the page with the test name from the suite JSP dir, compare the content to the comparison file with the
     * test name, and return the first form in the page.
     * 
     * @return the first WebForm in the response page
     */
    protected WebForm getFormInJspWithTestName() throws MalformedURLException, IOException, SAXException, Exception {
        return getFormInJsp("/" + getJspNameBasedOnTestMethod());
    }

    /**
     * Retrieve the page with the test name from the suite JSP dir, optionally compare the content to the comparison
     * file with the test name, and return the first form in the page.
     * 
     * @param check
     *            whether to compare the content with the comparison file with test name
     * @return the first WebForm in the response page
     */
    protected WebForm getFormInJspWithTestName(boolean check) throws MalformedURLException, IOException, SAXException,
            Exception {
        return getFormInJsp("/" + getJspNameBasedOnTestMethod(), check);
    }

    /**
     * Retrieve the indicated page, compare the content to the comparison file with the test name, and return the first
     * form in the page.
     * 
     * @param page
     *            the page to retrieve
     * @return the first WebForm in the response page
     */
    protected WebForm getFormInJsp(String page) throws MalformedURLException, IOException, SAXException, Exception {
        return getFormInJsp(page, true);
    }

    /**
     * Retrieve the indicated page, optionally compare the content to the comparison file with the test name, and return
     * the first form in the page.
     * 
     * @param page
     *            the page to retrieve
     * @param check
     *            whether to compare the content with the comparison file with test name
     * @return the first WebForm in the response page
     */
    protected WebForm getFormInJsp(String page, boolean check) throws MalformedURLException, IOException, SAXException,
            Exception {
        WebResponse resp = getJspResponse(page, check);

        if (resp.getForms().length == 0) {
            fail("Form expected but not present. Page:\n" + resp.getText());
        }
        // we get the first form in the jsp
        WebForm form = resp.getForms()[0];
        return form;
    }

    /**
     * Retrieve the indicated page, optionally compare the content to the comparison file with the test name.
     * 
     * @param page
     *            the page to retrieve
     * @param check
     *            whether to compare the content with the comparison file with test name
     * @return the WebResponse from the page access
     */
    protected WebResponse getJspResponse(String page, boolean check) throws MalformedURLException, IOException,
            SAXException, Exception {
        WebConversation wc = new WebConversation();
        WebResponse resp = wc.getResponse(System.getProperty("cactus.contextURL") + page);

        // first, compare that the form generated is ok
        if (check)
            compareToFileWithTestName(resp);
        return resp;
    }

    private String getJspNameBasedOnTestMethod() {
        return getJspDir() + "/" + getTestMethod() + ".jsp";
    }

    /**
     * The JSP dir of this suite
     * 
     * @return a dir name relative to the root
     */
    public String getJspDir() {
        return jspDir;
    }

    /**
     * Recording mode of the suite, for generating comparison files.
     * 
     * @return true if the suite should record its responses, false otherwise.
     */
    public boolean getRecordingMode() {
        return recording;
    }

    /**
     * recording mode
     */
    protected boolean recording = false;

    /**
     * JSP dir of the suite
     */
    protected String jspDir;

    /**
     * for dynamically built suites, the names and order of tests
     */
    protected String[] tests;

    /**
     * for dynamically built suites, the tests whose included jsp has a different name
     */
    protected Map<String, String> differentNameJspsMap = new HashMap<String, String>();

    protected Map<String, String> differentNameJspsReverseMap = new HashMap<String, String>();

    /**
     * some tests will compare content against the response of a form submission
     */
    protected WebResponse submissionResponse;

    protected void differentNameJsps(String name, String jsp) {
        differentNameJspsMap.put(name, jsp);
        differentNameJspsReverseMap.put(jsp, name);
    }

    Set<String> disabledTests = new HashSet<String>();

    protected void disableTest(String test) {
        disabledTests.add(test);
    }

    public static class MakumbaJspTestCaseDecorator extends MakumbaJspTestCase {
        MakumbaJspTestCase decorated;

        public MakumbaJspTestCaseDecorator(MakumbaJspTestCase decorated) {
            this.decorated = decorated;
        }

        @Override
        public String getJspDir() {
            return decorated.getJspDir();
        }

        @Override
        public boolean getRecordingMode() {
            return decorated.getRecordingMode();
        }

    }

    public static final class JspTest extends MakumbaJspTestCaseDecorator {
        String test;

        /**
         * only invokved at server side therefore the server-side object is a bit dumb because it wraps nobody
         */
        public JspTest() {
            super(null);
        }

        /** decorate a test class to run one of its tests */
        public JspTest(MakumbaJspTestCase decorated, String testName) {
            super(decorated);
            this.test = testName;
        }

        /**
         * see http://jakarta.apache.org/cactus/how_it_works.html for the begin, end etc, methods
         */
        /** invoked by cactus at test begin, on client */
        public void begin(Request request) throws Exception {
            decorated.submissionResponse = null;
            // we run the beginXXX method if we find one
            Method m = null;
            try {
                m = decorated.getClass().getMethod(test.replace("test", "begin"), Request.class);
            } catch (Exception e) {
            }
            if (m != null)
                m.invoke(decorated, request);

            // now we set a header, to tell the server side what to include
            // some tests have a jsp page with a different name
            String jspPage = decorated.differentNameJspsMap.get(test);
            if (jspPage == null)
                // but most have the page with the same name
                jspPage = "/" + decorated.getJspDir() + "/" + test + ".jsp";

            // some tests include no jsp
            if (decorated.submissionResponse == null)
                ((org.apache.cactus.WebRequest) request).addHeader("mak-test-page", jspPage);
        }

        /** invoked by cactus on server */
        public void runBareServer() throws Exception {
            // if we have a jsp, we include it
            String page = (String) request.getHeader("mak-test-page");
            if (page != null)
                pageContext.include(page);
        }

        /** invoked by cactus at the end, on client */
        public void end(WebResponse response) throws Exception {
            // if the submisionResponse is set, we use it instead of the response
            if (decorated.submissionResponse != null)
                response = decorated.submissionResponse;
            // compare the response to the result file with the same name
            compareToFile(response, test.replace("test", "end"));
            decorated.submissionResponse = null;
        }

        /**
         * test name
         */
        public String getName() {
            return test;
        }

    }

    /**
     * Make a dynamic test suite, from a prototype object. This allows testTestName and endTestName to be missing. The
     * test names are found in the tests[] array. Each test includes a JSP with (in principle) the same name, and
     * compares to a comparison file. If a beginTestName method exists, it will be executed. If testTestName tests are
     * present in the prototype class, they are honored _before_ all other tests.
     * 
     * @param prototype
     *            the object whose tests[] array will be used to make the suite
     * @param queryLang
     *            query language used in the test
     * @return the test suite
     */
    public static Test makeJspSuite(MakumbaJspTestCase prototype, String queryLang) {
        TestSuite ts = new TestSuite(prototype.getClass().getName());
        ts.addTest(new TestSuite(prototype.getClass()));
        for (String test : prototype.tests) {
            ts.addTest(new JspTest(prototype, test));
        }
        return new MakumbaWebTestSetup(ts, queryLang);
    }

    /**
     * Make a test suite using the standard, reflection-based mechanism
     * 
     * @param claz
     *            the class where the tests are extracted from
     * @param queryLang
     *            the query language used in the test
     * @return the test suite
     */
    public static Test makeSuite(Class<?> claz, String queryLang) {
        return new MakumbaWebTestSetup(new TestSuite(claz), queryLang);
    }

    /**
     * Make a dynamic test suite, from a prototype object by looking into its JSP folder. This allows testTestName and
     * endTestName to be missing. The test names are found from the test*.jsp files in the JSP folder of the prototype.
     * Each test includes a JSP with (in principle) the same name, and compares to a comparison file. If a beginTestName
     * method exists, it will be executed.
     * 
     * @param prototype
     *            the object whose tests[] array will be used to make the suite
     * @param queryLang
     *            query language used in the test
     * @return the test suite
     */
    public static Test makeJspDirSuite(MakumbaJspTestCase prototype, String queryLang) {
        TestSuite ts = new TestSuite(prototype.getClass().getName());
        // ts.addTest(new TestSuite(prototype.getClass()));
        File dir = new File("webapps/tests/" + prototype.getJspDir());

        for (String test : dir.list()) {
            if (test.startsWith("test") && test.endsWith(".jsp")) {
                String testName = test.replace(".jsp", "");
                if (prototype.disabledTests.contains(testName))
                    continue;
                String test1 = prototype.differentNameJspsReverseMap.get(prototype.getJspDir() + "/" + test);
                if (test1 != null)
                    ts.addTest(new JspTest(prototype, test1));
                else
                    ts.addTest(new JspTest(prototype, testName));
            }
        }
        return new MakumbaWebTestSetup(ts, queryLang);
    }

}
