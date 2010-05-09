package test.tags;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;

import junit.framework.Assert;
import junit.framework.Test;

import org.apache.cactus.Request;
import org.makumba.commons.tags.MakumbaVersionTag;

import test.util.MakumbaJspTestCase;

import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebResponse;

public class MakumbaCommonTagsTest extends MakumbaJspTestCase {
    {
        recording = false;
        jspDir = "login";
    }

    public static Test suite() {
        return makeSuite(MakumbaCommonTagsTest.class, "oql");
    }

    
    public void testVersionTag() throws JspException, IOException {
        MakumbaVersionTag versionTag = new MakumbaVersionTag();
        versionTag.setPageContext(pageContext);
        versionTag.doStartTag();
        session.setAttribute("version", "0.0");
        Assert.assertEquals(1, versionTag.doStartTag());
        BodyContent bodyContent = pageContext.pushBody();
        bodyContent.println("Makumbaaaaaaaaaaa");
        bodyContent.print("Version 0");
        versionTag.doAfterBody();
        versionTag.doEndTag();
        pageContext.popBody();
    }

    public void beginLogin(Request request) throws Exception {
        WebForm form = getFormInJsp("/login/testLogin.jsp", false);
        // we try to login
        form.setParameter("username","manu");
        form.setParameter("password", "secret");
        // submit the form
        form.submit();
    }
    public void testLogin() throws ServletException, IOException {
        includeJspWithTestName();
    }
    public void endLogin(WebResponse response) throws Exception {
        compareToFileWithTestName(response);
    }

    /*
    // TODO this test isn't actually testing the chooser logic, it just helps triggering it
    public void beginHibernateTransactionProviderChooserLogic(Request request) throws MalformedURLException, IOException, SAXException {
        WebConversation wc = new WebConversation();
        WebResponse   resp = wc.getResponse( System.getProperty("cactus.contextURL") + "/transactionProviderChooser/beginHibernateTransactionProviderChooser.jsp");

        // we get the first form in the jsp
        if(resp.getForms().length==0) {
            fail("forms expected\n"+resp.getText());
        }
        WebForm form = resp.getForms()[0];
        // set the input field "email" to "bartolomeus@rogue.be"
        form.setParameter("email","bartolomeus@rogue.be");
        // submit the form
        form.submit();
    }

    public void testHibernateTransactionProviderChooserLogic() throws ServletException, IOException {
        pageContext.include("transactionProviderChooser/testHibernateTransactionProviderChooser.jsp");
    }
    public void endHibernateTransactionProviderChooserLogic(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }

        assertTrue(compareTest(output));
    }
    */
}