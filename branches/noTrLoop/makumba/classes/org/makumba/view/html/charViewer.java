package org.makumba.view.html;
import org.makumba.*;
import java.util.Dictionary;
import org.makumba.util.HtmlUtils;

public class charViewer extends FieldViewer
{
  static String[] params= { "urlEncode", "html" };
  static String[][] paramValues= { {"true", "false"}, { "true", "false", "auto" }};

  public String[] getAcceptedParams(){ return params; }
  public String[][] getAcceptedValue(){ return paramValues; }

  public String formatNotNull(Object o, Dictionary formatParams) 
  {
    String txt=o.toString();
    String ht= (String)formatParams.get("html");
    if(ht!=null && (ht.equals("true") || ht.equals("auto") && HtmlUtils.detectHtml(txt)))
      return txt;
    return HtmlUtils.string2html(txt);

  }
}
