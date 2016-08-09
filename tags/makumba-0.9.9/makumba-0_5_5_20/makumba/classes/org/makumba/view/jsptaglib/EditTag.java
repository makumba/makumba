package org.makumba.view.jsptaglib;
import org.makumba.view.*;
import org.makumba.*;
import javax.servlet.jsp.*;
import org.makumba.abstr.Logic;

public class EditTag extends FormTagBase 
{
  public String getDefaultExpr(String fieldName) 
  { return enclosingLabel+"."+fieldName; }

  public FormResponder makeResponder() { return new EditResponder(); }
}

class EditResponder extends FormResponder
{
  public Object respondTo(PageContext pc) throws LogicException
  {
    return Logic.doEdit(controller, type, getHttpBasePointer(pc), 
			getHttpData(pc), makeAttributes(pc), database);
  }
}
