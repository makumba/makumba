package org.makumba.view.jsptaglib;
import java.util.*;

public class textEditor extends FieldEditor
{
  static String[] params= { "type", "rows", "cols" };
  static String[][] paramValues= { {"textarea", "file" }, null, null };
  public String[] getAcceptedParams(){ return params; }
  public String[][] getAcceptedValue(){ return paramValues; }

  public String getParams(Dictionary formatParams){ 
    return getIntParamString(formatParams, "rows") + getIntParamString(formatParams, "cols");
  }

  public String formatNull(Dictionary formatParams) 
  { 
    if(isTextArea(formatParams))
      return "<TEXTAREA name=\""+getInputName()+"\" "+getParams(formatParams)+" ></TEXTAREA>"; 
    else
      return fileInput();
  }

  public String formatNotNull(Object o, Dictionary formatParams) 
  { 
    if(isTextArea(formatParams))
      return "<TEXTAREA name=\""+getInputName()+"\" "+getParams(formatParams)+" >"+HtmlUtils.string2html(o.toString())+"</TEXTAREA>"; 
    else
      return fileInput();
  }
  
  String fileInput()
  {
    return "<INPUT name=\""+getInputName()+"\" type=\"file\">"; 
  }

  boolean isTextArea(Dictionary formatParams)
  {
    String s=(String)formatParams.get("type");
    if(s== null)
      return true;
    return s.equals("textarea");
  }
}
