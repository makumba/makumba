package org.makumba.devel;

import java.io.IOException;
import java.io.PrintWriter;

public class DevelUtils {

    /** Write the page footer to the given writer. */
    public static void printDeveloperSupportFooter(PrintWriter printWriter) throws IOException {
        printWriter.println("<hr><font size=\"-1\"><a href=\"http://www.makumba.org\">Makumba</a> developer support, version: "
                + org.makumba.MakumbaSystem.getVersion() + "</font>");
    }

}
