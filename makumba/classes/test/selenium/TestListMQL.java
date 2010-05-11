package test.selenium;

import java.io.IOException;

import javax.servlet.ServletException;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.MakumbaTestSetup;

import com.thoughtworks.selenium.SeleneseTestCase;

public class TestListMQL extends SeleneseTestCase {
    
    public static Test suite() {
        return new MakumbaTestSetup(new TestSuite(TestListMQL.class), "oql");
    }
    
    public void setUp() throws Exception {
        setUp("http://localhost:8080/", "*firefox");
    }
    
    public void testMakObjectTag() throws ServletException, IOException {
        selenium.open("/tests/list-oql/testMakObjectTag.jsp");
        verifyTrue(selenium.isTextPresent("name:john"));
        verifyTrue(selenium.isTextPresent("weight:85.7"));

    }
}
