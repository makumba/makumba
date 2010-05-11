package test;

import java.io.IOException;

import org.makumba.ProgrammerError;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;

import junit.framework.Test;

public class MakumbaWebTestSetup extends MakumbaTestSetup {

    public MakumbaWebTestSetup(Test test, String transactionProviderType) {
        super(test, transactionProviderType);
    }
    
    public void setUp(){
        WebConversation wc = new WebConversation();
        WebRequest req = new GetMethodWebRequest(System.getProperty("cactus.contextURL"));
        try {
            wc.getResponse(req);
        } catch (IOException e) {
            String tomcatMsg= "\n\n\n\n\nYou should run tomcat first! Use mak-tomcat to do that.\n\n";
            System.out.println(tomcatMsg);
            throw new ProgrammerError(tomcatMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.setUp();
    }

}
