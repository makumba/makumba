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
import java.util.ArrayList;
import java.util.Arrays;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.apache.cactus.JspTestCase;

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
public class MakumbaJspTestCase extends JspTestCase {

    private static final class Suite extends TestSetup {
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
        String testName = new Throwable().fillInStackTrace().getStackTrace()[1].getMethodName();

        // based on the method name, we retrieve the file used as comparison basis

        File f = new File("classes/test/expected/" + testName + ".txt");

        if (!f.exists())
            throw new Exception("Couldn't find the comparison file in classes/test/expected/" + testName
                    + ".txt - create it first using the fetchValidTestResult(String result) method!");

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
                testOk = strFile.equals(strStr);
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
                System.out.println("**** '>' marks lines added in the test result, '<' lines in the expected result *****");
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
        String testName = new Throwable().fillInStackTrace().getStackTrace()[1].getMethodName();

        File f = new File("classes/test/expected/" + testName + ".txt");
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
}
