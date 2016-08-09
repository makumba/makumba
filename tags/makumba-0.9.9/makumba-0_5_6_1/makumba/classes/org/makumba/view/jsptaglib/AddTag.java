package org.makumba.view.jsptaglib;
import org.makumba.view.*;

import org.makumba.*;
import javax.servlet.jsp.*;
import org.makumba.abstr.*;

public class AddTag extends FormTagBase 
{
  public void setField(String s) { responder.setAddField(field=s);}

  // for input tags:
  String field;

  public boolean canComputeTypeFromEnclosingQuery() 
  { return true; }

  public FieldDefinition computeTypeFromEnclosingQuery(QueryStrategy qs, String fieldName) 
  {
    if(qs.knewProjectionAtStart(baseObject)==-1)
      return null;
    DataDefinition dd= ((FieldInfo)qs.query.getLabelType(baseObject).getFieldDefinition(field)).getPointedType();
    return deriveType(dd, fieldName);
  }

}


