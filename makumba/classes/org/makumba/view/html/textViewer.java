package org.makumba.view.html;
import org.makumba.*;
import java.util.Dictionary;
import org.makumba.util.HtmlUtils;

public class textViewer extends FieldViewer
{
  static String[] params= { "lineSeparator", "longLineLength", "html"};
  static String[][] paramValues= { null,  null, {"true", "false" , "auto"}};

  public String[] getAcceptedParams(){ return params; }
  public String[][] getAcceptedValue(){ return paramValues; }

  static int screenLength=30;

  public String formatNotNull(Object o, Dictionary formatParams) 
  {
    String txt=o.toString();
    String ht= (String)formatParams.get("html");
    if(ht!=null && (ht.equals("true") || ht.equals("auto") && HtmlUtils.detectHtml(txt)))
      return txt;

    String startSeparator="<p>";
    String endSeparator="</p>";
    String s= (String)formatParams.get("lineSeparator");
    if(s!=null){
      startSeparator=s;
      endSeparator="";
    }

    int n= getIntParam(formatParams, "longLineLength");
    if(n==-1)
      n=screenLength;
    
    if( HtmlUtils.maxLineLength(txt) > n)
      // special text formatting
      return HtmlUtils.text2html(txt, startSeparator, endSeparator);
    else
      // else: text preformatted
      return "<pre>"+HtmlUtils.string2html(txt)+"</pre>";
  }
  
  
}
