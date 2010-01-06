///////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003  http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//  -------------
//  $Id$
//  $Name$
/////////////////////////////////////

package test.tags;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.Request;
import org.apache.commons.collections.CollectionUtils;
import org.makumba.commons.NamedResources;
import org.makumba.forms.responder.ResponderFactory;
import org.xml.sax.SAXException;

import test.MakumbaTestData;
import test.MakumbaTestSetup;
import test.util.MakumbaJspTestCase;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HTMLElement;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

/**
 * @author Johannes Peeters
 * @author Manuel Gay
 * @version $Id$
 */
public class FormsOQLTest extends MakumbaJspTestCase {

    private static final String namePersonIndivSurname = "Makumbian";

    private boolean record = false;

    static Suite setup;

    private String output;

    private WebResponse submissionResponse;

    private static final class Suite extends MakumbaTestSetup {

        public Suite(Test test) {
            super(test, "oql");
        }

    }

    public static Test suite() {
        setup = new Suite(new TestSuite(FormsOQLTest.class));
        return setup;
    }

    public void testDbReset(){
        System.err.println("cleaning caches");
        NamedResources.cleanStaticCache("Databases open");
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

    public void testMakNewForm() throws ServletException, IOException {
        pageContext.include("forms-oql/testMakNewForm.jsp");
    }

    public void endMakNewForm(WebResponse response) throws Exception {
        try {
            output = response.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void beginMakAddForm(Request request) throws Exception {
        WebConversation wc = new WebConversation();
        WebResponse resp = wc.getResponse(System.getProperty("cactus.contextURL") + "/forms-oql/beginMakAddForm.jsp");

        // first, compare that the form generated is ok
        try {
            output = resp.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + resp.getResponseMessage());
        }
        assertTrue(compareTest(output));

        // we get the first form in the jsp
        WebForm form = resp.getForms()[0];
        // set the input field "email" to "bartolomeus@rogue.be"
        form.setParameter("email", "bartolomeus@rogue.be");
        // submit the form
        form.submit();
    }

    public void testMakAddForm() throws ServletException, IOException {
        pageContext.include("forms-oql/testMakAddForm.jsp");
    }

    public void endMakAddForm(WebResponse response) throws Exception {
        try {
            output = response.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testMakEditForm() throws ServletException, IOException {
        pageContext.include("forms-oql/testMakEditForm.jsp");
    }

    public void endMakEditForm(WebResponse response) throws Exception {
        try {
            output = response.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testMakForm() throws ServletException, IOException, SAXException {
        pageContext.include("forms-oql/testMakForm.jsp");
    }

    public void endMakForm(WebResponse response) throws Exception {
        try {
            output = response.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testBug946() throws ServletException, IOException, SAXException {
        pageContext.include("forms-oql/testBug946.jsp");
    }

    public void endBug946(WebResponse response) throws Exception {
        try {
            output = response.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }
    
    public void testBug1115() throws ServletException, IOException, SAXException {
        pageContext.include("forms-oql/testBug1115.jsp");
    }

    public void endBug1115(WebResponse response) throws Exception {
        try {
            output = response.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }
    
        

    public void testFormRepeatedForms() throws ServletException, IOException, SAXException {
        pageContext.include("forms-oql/testMakRepeatedForms.jsp");
    }

    public void endFormRepeatedForms(WebResponse response) throws Exception {
        try {
            output = response.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }

        assertTrue(compareTest(output));
    }

    public void testFormNestedForms() throws ServletException, IOException, SAXException {
        pageContext.include("forms-oql/testMakNestedForms.jsp");
    }

    public void endFormNestedForms(WebResponse response) throws Exception {
        try {
            output = response.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testFormMakNewFile() throws ServletException, IOException, SAXException {
        pageContext.include("forms-oql/testMakNewFormFile.jsp");
    }

    public void endFormMakNewFile(WebResponse response) throws Exception {
        try {
            output = response.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testFormMakNewBinary() throws ServletException, IOException, SAXException {
        pageContext.include("forms-oql/testMakNewFormBinary.jsp");
    }

    public void endFormMakNewBinary(WebResponse response) throws Exception {
        try {
            output = response.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void beginMakAddToNewForm(Request request) throws Exception {
        WebConversation wc = new WebConversation();
        WebResponse resp = wc.getResponse(System.getProperty("cactus.contextURL")
                + "/forms-oql/beginMakAddToNewForm.jsp");

        // first, compare that the form generated is ok
        try {
            output = resp.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + resp.getResponseMessage());
        }
        assertTrue(compareTest(output));

        // we get the first form in the jsp
        WebForm form = resp.getForms()[0];
        // set the inputs in the add-to-new form
        form.setParameter("indiv.name", MakumbaTestData.namePersonIndivName_AddToNew);
        form.setParameter("description_1", "addToNewDescription");
        form.setParameter("email_1", "addToNew@makumba.org");
        // submit the form
        form.submit();
    }

    public void testMakAddToNewForm() throws ServletException, IOException {
        pageContext.include("forms-oql/testMakAddToNewForm.jsp");
    }

    public void endMakAddToNewForm(WebResponse response) throws Exception {
        try {
            output = response.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void beginMakSearchForm(Request request) throws Exception {
        WebConversation wc = new WebConversation();
        WebResponse resp = wc.getResponse(System.getProperty("cactus.contextURL") + "/forms-oql/testMakSearchForm.jsp");

        // first, compare that the form generated is ok
        try {
            output = resp.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + resp.getResponseMessage());
        }
        assertTrue(compareTest(output));

        // we get the first form in the jsp
        WebForm form = resp.getForms()[0];
        // set the inputs in the add-to-new form
        form.setParameter("indiv.name", "a");

        // TODO: read HTTP unit documents carefully.
        // not sure if that is the most elegant / intended solution
        // but, we want to save this specific form submission for later evaluation
        // cause the WebResponse passed in endMakSearchForm is not from this submission
        // we could also do the comparison here, though, and leave the endMakSearchForm method empty
        submissionResponse = form.submit();
    }

    public void testMakSearchForm() throws ServletException, IOException {
        // we need to have this method, even if it is empty; otherwise, the test is not run
    }

    public void endMakSearchForm(WebResponse response) throws Exception {
        try {
            output = submissionResponse.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void beginMakSearchForm2(Request request) throws Exception {
        WebConversation wc = new WebConversation();
        WebResponse resp = wc.getResponse(System.getProperty("cactus.contextURL") + "/forms-oql/testMakSearchForm2.jsp");

        // first, compare that the form generated is ok
        try {
            output = resp.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + resp.getResponseMessage());
        }
        assertTrue(compareTest(output));

        // we get the first form in the jsp
        WebForm form = resp.getForms()[0];
        // set the inputs in the add-to-new form
        form.setParameter("indiv.name", "a");

        // TODO: read HTTP unit documents carefully.
        // not sure if that is the most elegant / intended solution
        // but, we want to save this specific form submission for later evaluation
        // cause the WebResponse passed in endMakSearchForm is not from this submission
        // we could also do the comparison here, though, and leave the endMakSearchForm method empty
        submissionResponse = form.submit();
    }

    public void testMakSearchForm2() throws ServletException, IOException {
        // we need to have this method, even if it is empty; otherwise, the test is not run
    }

    public void endMakSearchForm2(WebResponse response) throws Exception {
        try {
            output = submissionResponse.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void beginMakSearchForm3(Request request) throws Exception {
        WebConversation wc = new WebConversation();
        WebResponse resp = wc.getResponse(System.getProperty("cactus.contextURL") + "/forms-oql/testMakSearchForm3.jsp");

        // first, compare that the form generated is ok
        try {
            output = resp.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + resp.getResponseMessage());
        }
        assertTrue(compareTest(output));

        // we get the first form in the jsp
        WebForm form = resp.getForms()[0];

        // TODO: read HTTP unit documents carefully.
        // not sure if that is the most elegant / intended solution
        // but, we want to save this specific form submission for later evaluation
        // cause the WebResponse passed in endMakSearchForm is not from this submission
        // we could also do the comparison here, though, and leave the endMakSearchForm method empty
        submissionResponse = form.submit();
    }

    public void testMakSearchForm3() throws ServletException, IOException {
        // we need to have this method, even if it is empty; otherwise, the test is not run
    }

    public void endMakSearchForm3(WebResponse response) throws Exception {
        try {
            output = submissionResponse.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testMakSearchFormDefaultMatchMode() throws ServletException, IOException, SAXException {
        pageContext.include("forms-oql/testMakSearchFormDefaultMatchMode.jsp");
    }

    public void endMakSearchFormDefaultMatchMode(WebResponse response) throws Exception {
        try {
            output = response.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void beginFormAnnotation(Request request) throws Exception {
        WebConversation wc = new WebConversation();
        WebResponse resp = wc.getResponse(System.getProperty("cactus.contextURL")
                + "/forms-oql/beginFormAnnotation.jsp");

        // first, compare that the form generated is ok
        try {
            output = resp.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + resp.getResponseMessage());
        }
        assertTrue(compareTest(output));

        // we get the first form in the jsp
        WebForm form = resp.getForms()[0];
        // set the inputs in the add-to-new form
        form.setParameter("indiv.name", "name");
        form.setParameter("indiv.surname", "surname");
        form.setParameter("age", "invalidInt");
        form.setParameter("weight", "invalidReal");
        form.setParameter("email", "invalidEmail");
        final Date first = new Date(90, 0, 1);
        form.setParameter("firstSex_0", String.valueOf(first.getDate()));
        form.setParameter("firstSex_1", String.valueOf(first.getMonth()));
        form.setParameter("firstSex_2", String.valueOf(first.getYear() + 1900));
        form.setParameter("birthdate_0", String.valueOf(MakumbaTestData.birthdateJohn.getDate()));
        form.setParameter("birthdate_1", String.valueOf(MakumbaTestData.birthdateJohn.getMonth()));
        form.setParameter("birthdate_2", String.valueOf(MakumbaTestData.birthdateJohn.getYear() + 1800));
        form.setParameter("uniqDate_0", String.valueOf(MakumbaTestData.birthdateJohn.getDate()));
        form.setParameter("uniqDate_1", String.valueOf(MakumbaTestData.birthdateJohn.getMonth()));
        form.setParameter("uniqDate_2", String.valueOf(MakumbaTestData.birthdateJohn.getYear() + 1900));
        form.setParameter("hobbies", " ");
        form.setParameter("uniqInt", MakumbaTestData.uniqInt.toString());
        form.setParameter("uniqChar", MakumbaTestData.uniqChar);

        // TODO: read HTTP unit documents carefully.
        // not sure if that is the most elegant / intended solution
        // but, we want to save this specific form submission for later evaluation
        // cause the WebResponse passed in endMakSearchForm is not from this submission
        // we could also do the comparison here, though, and leave the endMakSearchForm method empty
        submissionResponse = form.submit();
    }

    public void testFormAnnotation() throws ServletException, IOException {
        // we need to have this method, even if it is empty; otherwise, the test is not run
    }

    public void endFormAnnotation(WebResponse response) throws Exception {
        try {
            output = submissionResponse.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void beginFormResponderOrder(Request request) throws Exception {
        WebConversation wc = new WebConversation();
        WebResponse resp = wc.getResponse(System.getProperty("cactus.contextURL")
                + "/forms-oql/beginMakNestedNewForms.jsp");

        // first, compare that the form generated is ok
        try {
            output = resp.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + resp.getResponseMessage());
        }
        assertTrue(compareTest(output));

        // read all the inputs with responder codes, store them in an array
        HTMLElement[] responderElements = resp.getElementsWithAttribute("name", "__makumba__responder__");
        String[] responderCodesString = new String[responderElements.length];
        for (int i = 0; i < responderElements.length; i++) {
            responderCodesString[i] = responderElements[i].getAttribute("value");

        }

        // we will have subsequently a new instance of responderFactory (the one used until now is in tomcat-mak)
        // thus, we need to prepare the responder working dir
        // we don't have an HTTPServletRequest at hand, so we have to do this manually / partly hardcoded
        String contextPath = "tests";
        String tempDir = new File(getClass().getResource("/").toURI()).getParent() + "/tomcat/work/Catalina/localhost/"
                + contextPath;
        ResponderFactory responderFactory = ResponderFactory.getInstance();
        responderFactory.setResponderWorkingDir(tempDir, contextPath);

        // we need the codes as iterator; we could do an iterator ourselves, but let's do it as if we got them from the
        // attributes, i.e. as vector
        List<String> list = Arrays.asList(responderCodesString);
        Vector<String> v = new Vector<String>();
        v.addAll(list);
        Iterator<String> responderCodes = responderFactory.getResponderCodes(v);

        Iterator<String> orderedResponderCodes = responderFactory.getOrderedResponderCodes(list.iterator());

        // debug info
        // System.out.println("Responder codes read from form inputs: " + Arrays.toString(responderCodesString));

        ArrayList<String> responderCodesAsList = new ArrayList<String>();
        CollectionUtils.addAll(responderCodesAsList, responderCodes);
        // System.out.println("Responder codes as passed through responderFactory.getResponderCodes(..): "
        // + ArrayUtils.toString(responderCodesAsList));

        ArrayList<String> orderedResponderCodesAsList = new ArrayList<String>();
        CollectionUtils.addAll(orderedResponderCodesAsList, orderedResponderCodes);
        // System.out.println("Ordered responder codes:" + ArrayUtils.toString(orderedResponderCodesAsList));

        // TODO
        // - define an Iterator / something else with the expected responder codes
        // - define an Iterator / something else with the expected ordered responder codes
        // - compare them
    }

    public void testFormResponderOrder() throws ServletException, IOException {
        // we need to have this method, even if it is empty; otherwise, the test is not run
    }

    public void endFormResponderOrder(WebResponse response) throws Exception {
    }

    public void testClientSideValidationMultipleForms() throws ServletException, IOException, SAXException {
        pageContext.include("forms-oql/testClientSideValidationMultipleForms.jsp");
    }

    public void endClientSideValidationMultipleForms(WebResponse response) throws Exception {
        try {
            output = response.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testCalendarEditor() throws ServletException, IOException, SAXException {
        pageContext.include("forms-oql/testCalendarEditor.jsp");
    }

    public void endCalendarEditor(WebResponse response) throws Exception {
        try {
            output = response.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void beginMakNestedNewFormsSimple(Request request) throws Exception {
        WebConversation wc = new WebConversation();
        WebResponse resp = wc.getResponse(System.getProperty("cactus.contextURL")
                + "/forms-oql/beginMakNestedNewFormsSimple.jsp");

        // first, compare that the form generated is ok
        try {
            output = resp.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + resp.getResponseMessage());
        }
        assertTrue(compareTest(output));

        // we get the first form in the jsp
        WebForm form = resp.getForms()[0];
        // set the inputs in the add-to-new form
        form.setParameter("indiv.name", MakumbaTestData.namePersonIndivName_FirstBrother);
        form.setParameter("indiv.surname", "Person");
        form.setParameter("indiv.name_1", MakumbaTestData.namePersonIndivName_SecondBrother);
        form.setParameter("indiv.surname_1", "Person");

        // TODO: read HTTP unit documents carefully.
        // not sure if that is the most elegant / intended solution
        // but, we want to save this specific form submission for later evaluation
        // cause the WebResponse passed in endMakSearchForm is not from this submission
        // we could also do the comparison here, though, and leave the endMakSearchForm method empty
        submissionResponse = form.submit();
    }

    public void testMakNestedNewFormsSimple() throws ServletException, IOException {
        // we need to have this method, even if it is empty; otherwise, the test is not run
    }

    public void endMakNestedNewFormsSimple(WebResponse response) throws Exception {
        try {
            output = submissionResponse.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void beginMakNestedNewAndEditFormsSimple(Request request) throws Exception {
        WebConversation wc = new WebConversation();
        WebResponse resp = wc.getResponse(System.getProperty("cactus.contextURL")
                + "/forms-oql/beginMakNestedNewAndEditFormsSimple.jsp");

        // first, compare that the form generated is ok
        try {
            output = resp.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + resp.getResponseMessage());
        }
        assertTrue(compareTest(output));

        // we get the first form in the jsp
        WebForm form = resp.getForms()[0];
        // set the inputs in the add-to-new form
        form.setParameter("indiv.name", MakumbaTestData.namePersonIndivName_StepBrother);
        form.setParameter("indiv.surname", namePersonIndivSurname);

        // TODO: read HTTP unit documents carefully.
        // not sure if that is the most elegant / intended solution
        // but, we want to save this specific form submission for later evaluation
        // cause the WebResponse passed in endMakSearchForm is not from this submission
        // we could also do the comparison here, though, and leave the endMakSearchForm method empty
        submissionResponse = form.submit();
    }

    public void testMakNestedNewAndEditFormsSimple() throws ServletException, IOException {
        // we need to have this method, even if it is empty; otherwise, the test is not run
    }

    public void endMakNestedNewAndEditFormsSimple(WebResponse response) throws Exception {
        try {
            output = submissionResponse.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void beginLogin(Request request) throws MalformedURLException, IOException, SAXException {
        WebConversation wc = new WebConversation();
        WebResponse resp = wc.getResponse(System.getProperty("cactus.contextURL") + "/login/loginTest.jsp");

        // we get the first form in the jsp
        if (resp.getForms().length == 0) {
            fail("Loging test fails: login page not created correctly, no form present. Page:\n" + resp.getText());
        }
        WebForm form = resp.getForms()[0];
        // we try to login
        form.setParameter("username", "manu");
        form.setParameter("password", "secret");
        // submit the form
        form.submit();
    }

    public void testLogin() throws ServletException, IOException {
        pageContext.include("login/loginTest.jsp");
    }

    public void endLogin(WebResponse response) throws Exception {
        try {
            output = response.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

}
