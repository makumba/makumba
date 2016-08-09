package org.makumba.view.jsptaglib;
import org.makumba.view.*;

import org.makumba.*;
import javax.servlet.jsp.*;
import org.makumba.abstr.*;

public class AddTag extends FormTagBase 
{
  public boolean canComputeTypeFromEnclosingQuery() 
  { return true; }

  public FieldDefinition computeTypeFromEnclosingQuery(QueryStrategy qs, String fieldName) 
  {
    if(qs.knewProjectionAtStart(enclosingLabel)==-1)
      return null;
    DataDefinition dd= ((FieldInfo)qs.query.getLabelType(enclosingLabel).getFieldDefinition(field)).getPointedType();
    return deriveType(dd, fieldName);
  }

  public FormResponder makeResponder() { return new AddResponder(); }

  String field;

  public void setField(String s) { field=s; }

  public String getEditedType()
  {
    if(responder.pointerType==null) // not set yet
      return null;
    return responder.pointerType+"->"+field; 
  }
}

class AddResponder extends FormResponder
{
  public Object respondTo(PageContext pc) throws LogicException
  {
    return Logic.doAdd(controller, type, getHttpBasePointer(pc), getHttpData(pc), makeAttributes(pc), database);
  }
}


