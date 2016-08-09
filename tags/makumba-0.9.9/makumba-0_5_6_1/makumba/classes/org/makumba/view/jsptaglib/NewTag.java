package org.makumba.view.jsptaglib;
import org.makumba.abstr.*;
import org.makumba.FieldDefinition;

public class NewTag extends FormTagBase
{
  public void setType(String s) { responder.setNewType(type=RecordInfo.getRecordInfo(s)); }

  // for input tags:
  RecordInfo type;

  public FieldDefinition getDefaultType(String fieldName) 
  {
    return deriveType(type, fieldName);
  }
}

