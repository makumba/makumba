package org.makumba.test.util;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import junit.framework.Test;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.makumba.MakumbaError;
import org.makumba.ProgrammerError;

import java.io.IOException;

public class MakumbaWebTestSetup extends MakumbaTestSetup {

    public MakumbaWebTestSetup(Test test, String transactionProviderType) {
        super(test, transactionProviderType);
    }

    @Override
    public void setUp() {
        WebConversation wc = new WebConversation();
        WebRequest req = new GetMethodWebRequest(System.getProperty("cactus.contextURL") + "/testMakInfo.jsp");
        try {
            wc.getResponse(req);
        } catch (IOException e) {
            String tomcatMsg = "\n\n\n\n\nYou should run tomcat first! Use mak-tomcat to do that.\n\n";
            System.out.println(tomcatMsg);
            throw new ProgrammerError(tomcatMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.setUp();

        // init tests on the server side, i.e. clean static caches and populate database state (primary key values, so
        // this works with auto-increment)
        // make sure you update that servlet if new types appear!
        HttpMethod getMethod = new GetMethod(System.getProperty("cactus.contextURL") + "/testInit");
        HttpClient c = new HttpClient();
        int code = 0;
        try {
            code = c.executeMethod(getMethod);
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (code != HttpStatus.SC_OK) {
            throw new MakumbaError("Could not initialise tests on the server-side, status code of servlet is " + code);
        }

    }

}
