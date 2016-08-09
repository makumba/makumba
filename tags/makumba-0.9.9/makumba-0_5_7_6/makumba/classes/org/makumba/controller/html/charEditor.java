package org.makumba.controller.html;
import java.util.*;
import org.makumba.view.*;
import org.makumba.util.HtmlUtils;

public class charEditor extends FieldEditor
{
  static String[] params= { "type", "size", "maxlength" };
  static String[][] paramValues= { {"text", "password"}, null, null };
  public String[] getAcceptedParams(){ return params; }
  public String[][] getAcceptedValue(){ return paramValues; }

  public String getParams(Dictionary formatParams){ 
    String ret=getIntParamString(formatParams, "size");
    int n=getIntParam(formatParams, "maxlength");
    if(n> getWidth())
      throw new InvalidValueException(this, "invalid too big for maxlength "+n); 
    if(n==-1)
      n=getWidth();
    ret+="maxlength=\""+n+"\" ";
    return ret;
  }

  public String formatNull(Dictionary formatParams) 
  { return "<input name=\""+getInputName(formatParams)+"\" type=\""+getInputType(formatParams)+"\" value=\"\" "+getParams(formatParams)+" >"; }

  public String formatNotNull(Object o, Dictionary formatParams) 
  { return "<input name=\""+getInputName(formatParams)+"\" type=\""+getInputType(formatParams)+"\" value=\""+getLiteral(o, formatParams)+"\" "+getParams(formatParams)+" >"; }


  public String getLiteral(Object o, Dictionary formatParams) 
  {return HtmlUtils.string2html(o.toString()); }

  public String getInputType(Dictionary formatParams)
  {
    String s=(String)formatParams.get("type");
    if(s== null)
      s="text";
    return s;
  }
}
