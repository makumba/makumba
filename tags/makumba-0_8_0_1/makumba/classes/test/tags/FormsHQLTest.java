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
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

/**
 * @author Rudolf Mayer
 * @author Manuel Gay
 * @version $Id$
 */
public class FormsHQLTest extends MakumbaJspTestCase {

    private boolean record = false;

    static Suite setup;

    private String output;

    private static final class Suite extends MakumbaTestSetup {

        private Suite(Test arg0) {
            super(arg0, "oql");
        }
    }

    public static Test suite() {
        setup = new Suite(new TestSuite(FormsHQLTest.class));
        return setup;
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

    public void testHibernateMakNewForm() throws ServletException, IOException {
        pageContext.include("forms-hql/testHibernateMakNewForm.jsp");
    }

    public void endHibernateMakNewForm(WebResponse response) throws Exception {
        try {
            output = response.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));

    }

    public void beginHibernateMakAddForm(Request request) throws MalformedURLException, IOException, SAXException {
        WebConversation wc = new WebConversation();
        WebResponse resp = wc.getResponse(System.getProperty("cactus.contextURL")
                + "/forms-hql/beginHibernateMakAddForm.jsp");

        // we get the first form in the jsp
        if (resp.getForms().length == 0) {
            fail("forms expected\n" + resp.getText());
        }
        WebForm form = resp.getForms()[0];
        // set the input field "email" to "bartolomeus@rogue.be"
        form.setParameter("email", "bartolomeus@rogue.be");
        // submit the form
        form.submit();
    }

    public void testHibernateMakAddForm() throws ServletException, IOException {
        pageContext.include("forms-hql/testHibernateMakAddForm.jsp");
    }

    public void endHibernateMakAddForm(WebResponse response) throws Exception {
        try {
            output = response.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }

        assertTrue(compareTest(output));
    }

    public void testHibernateMakEditForm() throws ServletException, IOException {
        pageContext.include("forms-hql/testHibernateMakEditForm.jsp");
    }

    public void endHibernateMakEditForm(WebResponse response) throws Exception {
        try {
            output = response.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }

        assertTrue(compareTest(output));

    }

    public void testHibernateMakForm() throws ServletException, IOException, SAXException {
        pageContext.include("forms-hql/testHibernateMakForm.jsp");
    }

    public void endHibernateMakForm(WebResponse response) throws Exception {
        try {
            output = response.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }

        assertTrue(compareTest(output));

    }
}
