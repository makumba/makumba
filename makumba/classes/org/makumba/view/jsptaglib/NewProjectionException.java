package org.makumba.view.jsptaglib;

public class NewProjectionException 
    extends org.makumba.controller.http.AllowedException
{
    public NewProjectionException(String vname, String s)
    { super("dummy exception due to self-reload of "+s); }

    // we avoid log pollution
    public void printStackTrace(java.io.PrintWriter s){ s.print(getMessage());}
}
