package test;

import java.io.File;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * Simple runner for Jetty, that runs the test webapp
 * 
 * @author Manuel Gay
 * @version $Id: Jetty.java,v 1.1 Jan 2, 2010 1:15:03 PM manu Exp $
 */
public class Jetty {

    public static void main(String[] args) {
        Jetty j = new Jetty();
        try {
            j.start();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void start() throws Exception {
        Server server = new Server(8080);

        WebAppContext wac = new WebAppContext();
        wac.setContextPath("/tests");

        String webappPath = new File("webapps/tests/").getAbsolutePath();
        wac.setWar(webappPath); // this is path to .war OR TO expanded, existing webapp; WILL FIND web.xml and parse it
        server.setHandler(wac);
        server.setStopAtShutdown(true);

        server.start();

    }

}
