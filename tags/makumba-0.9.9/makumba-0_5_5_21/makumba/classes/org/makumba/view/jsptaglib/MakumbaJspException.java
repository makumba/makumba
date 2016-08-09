package org.makumba.view.jsptaglib;

/** An exception thrown due to makumba-specific reasons during tag execution */
public class MakumbaJspException extends javax.servlet.jsp.JspException
{
  Exception e;

  public MakumbaJspException(Exception e){this.e=e; }
  public MakumbaJspException(TagStrategy t, String s){this(new RuntimeException(s+"\nin tag: "+t.toString())); }

  public String getMessage(){ return e.getMessage(); }
}
