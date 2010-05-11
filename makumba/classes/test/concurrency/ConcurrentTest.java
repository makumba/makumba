package test.concurrency;

import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.CountDownLatch;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import test.MakumbaTestSetup;

public class ConcurrentTest extends TestCase {

    private static MakumbaTestSetup setup;
    
    private static final int THREADS = 200;
    
    private static final String CONCURRENT_TEST_URL = "http://localhost:8080/tests/concurrent/concurrentListTest.jsp";
    
    private static final String EXPECTED_TEST_RESULT_FRAGMENT = "speaks: English English French French German German Italian Italian Spanish Spanish";
    
    public static Test suite() {
        return setup = new MakumbaTestSetup(new TestSuite(ConcurrentTest.class), "oql");
    }
    
    public void testListConcurrent() throws Exception {
        
        checkListPageAvailable();
        
        final CountDownLatch startGate = new CountDownLatch(1);
        final CountDownLatch endGate = new CountDownLatch(THREADS);         

        for (int i = 0; i < THREADS; i++) {
            final int index = i;
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        // each thread waits for the latch to be released before firing
                        startGate.await();                      
                        try {
                            long start = System.currentTimeMillis();
                            HttpClient httpClient = new HttpClient();
                            GetMethod getTestPage = new GetMethod(CONCURRENT_TEST_URL);
                            httpClient.executeMethod(getTestPage);
                            String result = getTestPage.getResponseBodyAsString();
                            //System.out.println(result);
                            assertTrue(result.indexOf(EXPECTED_TEST_RESULT_FRAGMENT) > -1);
                            System.out.println("LIST TIME for:"+index +"="+(System.currentTimeMillis()- start)+" ms");
                        } finally {
                            // count down the end gate, when no threads are left the endGate will fire
                            endGate.countDown();
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            thread.start();
        }
        
        long start = System.currentTimeMillis();
        // fire all threads
        startGate.countDown();
        
        // the main thread waits for all threads to have ran before continuing
        endGate.await();
        System.out.println("TOTAL TIME:"+(System.currentTimeMillis()- start)+" ms");
    }

    private void checkListPageAvailable() throws IOException, HttpException {
        HttpClient httpClient = new HttpClient();
        GetMethod getTestPage = new GetMethod(CONCURRENT_TEST_URL);
        int status = -1;
        try {
            status = httpClient.executeMethod(getTestPage);
        } catch(ConnectException c) {
            setup.tearDown();
            System.err.println("\n\n\n\n\nYou should run tomcat first! Use tomcat-mak to do that.\n\n");
            System.exit(1);
        }
        assertEquals("Test page did not run correctly", HttpStatus.SC_OK, status);
    }
    
}
