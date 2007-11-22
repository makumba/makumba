package test.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;

import junit.extensions.TestSetup;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.makumba.commons.tags.MakumbaVersionTag;

import test.util.MakumbaJspTestCase;

public class MakumbaCommonTagsTest extends MakumbaJspTestCase {
    
    static Suite setup;
    
    private static final class Suite extends TestSetup {
        private Suite(Test arg0) {
            super(arg0);
        }
    }
    
    public static Test suite() {
        setup = new Suite(new TestSuite(MakumbaCommonTagsTest.class));
        return setup;
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

}
