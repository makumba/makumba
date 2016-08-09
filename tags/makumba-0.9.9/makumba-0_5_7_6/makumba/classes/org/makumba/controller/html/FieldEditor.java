package org.makumba.controller.html;
import org.makumba.view.*;
import javax.servlet.*;
import java.util.*;

public class FieldEditor extends FieldFormatter
{
  static String[] params={"type"};
  static String[][] paramValues={{"hidden"}};
  public String[] getAcceptedParams(){ return params; }
  public String[][] getAcceptedValue(){ return paramValues; }

  static final String suffixName="org.makumba.editorSuffix";

  public static String getSuffix(Dictionary formatParams) { return (String)formatParams.get(suffixName); }
  public static void setSuffix(Dictionary formatParams, String suffix) { formatParams.put(suffixName, suffix); }

  public void checkParam(String name, String val)
  {
    if(name.equals("type") && val.equals("hidden"))
      return;
    super.checkParam(name, val);
  }

  public String format(Object o, Dictionary formatParams)
  {
    String s=(String)formatParams.get("type");
    if(s!=null && s.equals("hidden"))
      return formatHidden(o, formatParams);
    return formatShow(o, formatParams);
  }

  public String formatShow(Object o, Dictionary formatParams)
  {
    // this will call formatNull and formatNotNull which should be redefined
    // or, the entire formatShow should be redefined
    return super.format(o, formatParams);
  }

  public String formatHidden(Object o, Dictionary formatParams)
  {
    return "<input type=\"hidden\" name=\""+getInputName(formatParams)+"\" value=\""+formatHiddenValue(o, formatParams)+"\">";
  }

  public String formatHiddenValue(Object o, Dictionary formatParams)
  {
    if(o==null || o.equals(getNull()))
      return super.formatNull(formatParams);
    return super.formatNotNull(o, formatParams);
  }

  public void onStartup(RecordEditor re){}

  public String getInputName(Dictionary formatParams){ return getInputName(getSuffix(formatParams)); }

  public String getInputName(String suffix){ return getExpr()+suffix; }

  public Object readFrom(org.makumba.controller.http.HttpParameters p, String suffix) 
  {
    return p.getParameter(getInputName(suffix));
  }

  protected Integer toInt(Object o)
  {
    String s=(""+o).trim();
    if(s.length()==0)
      return null;
    try{ 
      return new Integer(Integer.parseInt(s));
    }catch(NumberFormatException e) { throw new InvalidValueException(this, "invalid integer: "+o); }
  }

}
