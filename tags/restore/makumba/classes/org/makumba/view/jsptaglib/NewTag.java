package org.makumba.view.jsptaglib;
import org.makumba.view.*;
import org.makumba.*;
import javax.servlet.jsp.*;
import org.makumba.abstr.*;

public class NewTag extends FormTagBase
{
  public FormResponder makeResponder() { return new NewResponder(); }

  RecordInfo type;

  public FieldDefinition getDefaultType(String fieldName) 
  {
    return deriveType(type, fieldName);
  }

  public void setType(String s) { type=RecordInfo.getRecordInfo(s); }

  public String getEditedType(){ return type.getName(); }
}

class NewResponder extends FormResponder
{
  public Object respondTo(PageContext pc) throws LogicException
  {
    return Logic.doNew(controller, type, getHttpData(pc), makeAttributes(pc), database);
  }
}
