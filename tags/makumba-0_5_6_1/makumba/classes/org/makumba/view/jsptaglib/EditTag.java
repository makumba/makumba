package org.makumba.view.jsptaglib;

public class EditTag extends FormTagBase 
{
  // for input tags:
  public String getDefaultExpr(String fieldName) 
  { return baseObject+"."+fieldName; }
}
