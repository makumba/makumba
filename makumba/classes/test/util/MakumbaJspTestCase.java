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
import java.net.MalformedURLException;
import java.util.ArrayList;

import javax.servlet.ServletException;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.apache.cactus.JspTestCase;
import org.apache.cactus.Request;
import org.makumba.MakumbaError;
import org.xml.sax.SAXException;

import test.MakumbaTestSetup;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.sun.jdi.InvalidStackFrameException;

import bmsi.util.Diff;
import bmsi.util.DiffPrint;

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
    protected boolean compareTest(String result) throws Exception {

        boolean testOk = true;

        // first we retrieve the name of the method which calls us
        String testName = getTestMethod();

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
     * @param record
     *            TODO
     */
    protected void fetchValidTestResult(String output, boolean record) {

        if (!record)
            return;

        // first we retrieve the name of the method which calls us
        String testName = getTestMethod();

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
     * @return the test method name
     */
    private String getTestMethod() {
        StackTraceElement[] stackTrace = new Throwable().fillInStackTrace().getStackTrace();
        for(int n=0; ; n++)
            if(stackTrace[n].getMethodName().startsWith("begin") ||
                    stackTrace[n].getMethodName().startsWith("test") ||
                    stackTrace[n].getMethodName().startsWith("end") 
                    )
                return stackTrace[n].getMethodName();
    }
    
    /**
     * Compare the given WebResponse to the file that has the name of the current test
     * @param response the WebResponse to compare
     * @throws Exception
     */
    protected void compareToFileWithTestName(WebResponse response) throws Exception {
        String output= null;
        try {
            output = response.getText();
            fetchValidTestResult(output, getRecordingMode());
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    /**
     * Include the JSP from the JSP dir of this suite that has the name of the currently running test
     * @throws ServletException
     * @throws IOException
     */
    protected void includeJspWithTestName() throws ServletException, IOException{
        pageContext.include(getJspNameBasedOnTestMethod());
    }

    /**
     * Retrieve the page with the test name from the suite JSP dir,
     * compare the content to the comparison file with the test name, 
     * and return the first form in the page.
     * @return the first WebForm in the response page
     */
    protected WebForm getFormInJspWithTestName() throws MalformedURLException, IOException, SAXException, Exception {
        return getFormInJsp("/"+getJspNameBasedOnTestMethod());
    }

    /**
     * Retrieve the page with the test name from the suite JSP dir,
     * optionally compare the content to the comparison file with the test name, 
     * and return the first form in the page.
     * @param check whether to compare the content with the comparison file with test name 
     * @return the first WebForm in the response page
     */
    protected WebForm getFormInJspWithTestName(boolean check) throws MalformedURLException, IOException, SAXException, Exception {
        return getFormInJsp("/"+getJspNameBasedOnTestMethod(), check);
    }

    /**
     * Retrieve the indicated page,
     * compare the content to the comparison file with the test name, 
     * and return the first form in the page.
     * @param page the page to retrieve
     * @return the first WebForm in the response page
     */
    protected WebForm getFormInJsp(String page) throws MalformedURLException, IOException, SAXException, Exception {
        return getFormInJsp(page, true);
    }
    
    /**
     * Retrieve the indicated page,
     * optionally compare the content to the comparison file with the test name, 
     * and return the first form in the page.
     * @param page the page to retrieve
     * @param check whether to compare the content with the comparison file with test name 
     * @return the first WebForm in the response page
     */
    protected WebForm getFormInJsp(String page, boolean check) throws MalformedURLException, IOException, SAXException, Exception {
        WebResponse resp = getJspResponse(page, check);

        if (resp.getForms().length == 0) {
            fail("Form expected but not present. Page:\n" + resp.getText());
        }
        // we get the first form in the jsp
        WebForm form = resp.getForms()[0];
        return form;
    }
    
    /**
     * Retrieve the indicated page,
     * optionally compare the content to the comparison file with the test name.
     * @param page the page to retrieve
     * @param check whether to compare the content with the comparison file with test name 
     * @return the WebResponse from the page access
     */
    protected WebResponse getJspResponse(String page, boolean check) throws MalformedURLException, IOException,
            SAXException, Exception {
        WebConversation wc = new WebConversation();
        WebResponse resp = wc.getResponse(System.getProperty("cactus.contextURL")+ page);

        // first, compare that the form generated is ok
        if(check)
            compareToFileWithTestName(resp);
        return resp;
    }
 

    private String getJspNameBasedOnTestMethod() {
        return getJspDir()+"/"+getTestMethod()+".jsp";
    }

    /**
     * Common method for classes that want to test tomcat. They only need to declare an empty testTomcat() method
     * @param request
     */
    public void beginTomcat(Request request) {
        WebConversation wc = new WebConversation();
        WebRequest req = new GetMethodWebRequest(System.getProperty("cactus.contextURL"));
        try {
            wc.getResponse(req);
        } catch (MalformedURLException e) {
        } catch (IOException e) {
            getSetup().tearDown();
            System.err.println("\n\n\n\n\nYou should run tomcat first! Use mak-tomcat to do that.\n\n");
            System.exit(1);
        } catch (SAXException e) {
        }
    }
    
    /**
     * the setup object of the suite, to tear down if tocmat doesn't run
     * @return the setup object
     */
    protected abstract MakumbaTestSetup getSetup();

    /**
     * The JSP dir of this suite
     * @return a dir name relative to the root
     */
    protected abstract String getJspDir();

    /**
     * Recording mode of the suite, for generating comparison files.
     * @return true if the suite should record its responses, false otherwise.
     */
    protected abstract boolean getRecordingMode();
}
