package org.makumba.view.jsptaglib;
import org.makumba.view.*;
import org.makumba.*;
import javax.servlet.jsp.*;
import org.makumba.abstr.Logic;
import java.io.*;

public class DeleteTag extends EditTag
{
  public FormResponder makeResponder() { return new DeleteResponder(); }

  // no input tags allowed!!

  public void writeFormPreamble(JspWriter pw) throws JspException, IOException
  {
    String sep="?";
    if( ((String)action).indexOf('?')>=0) { sep="&"; }
    pw.print("<a href=\""+action+sep+FormResponder.basePointerName+"="+getBasePointer()+"&"+FormResponder.responderName+"="+responder.getIdentity(getEditedType())+"\">");
  }

  public void writeFormPostamble(JspWriter pw) throws JspException, IOException
  {
    pw.print("</a>");
  }

  //  public Object getKeyDifference(){ return ""+super.getKeyDifference()+"DELETE"; }
}

class DeleteResponder extends FormResponder
{
  public Object respondTo(PageContext pc) throws LogicException
  {
    return Logic.doDelete(controller, type, getHttpBasePointer(pc), makeAttributes(pc), database);
  }


}

