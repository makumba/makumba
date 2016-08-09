package org.makumba.view;
import org.makumba.abstr.*;
import java.util.*;

public class FieldFormatter extends FieldHandler
{
  static String[] params={};
  static String[][] paramValues={};
  public String[] getAcceptedParams(){ return params; }
  public String[][] getAcceptedValue(){ return paramValues; }

  static Object dummy=new Object();

  String expr;

  public String getExpr() 
  { 
    if(expr!=null)
      return expr;
    return getName();
  }

  public void initExpr(String s) { expr=s; }

  Hashtable validParams= new Hashtable(13);

  public FieldFormatter()
  {
    for(int i=0; i<getAcceptedParams().length; i++)
      {
	Hashtable h=new Hashtable(13);
	if(getAcceptedValue()[i]!=null)
	  for(int j=0; j<getAcceptedValue()[i].length; j++)
	    h.put(getAcceptedValue()[i][j], dummy);
	validParams.put(getAcceptedParams()[i], h);
      }
  }

  public void checkParams(Dictionary formatParams)
  {
    for(Enumeration e=formatParams.keys(); e.hasMoreElements(); )
      {
	String s=(String)e.nextElement();
	checkParam(s, ((String)formatParams.get(s)).toLowerCase());
      }
  }

  public void checkParam(String name, String val)
  {
    if(name.startsWith("org.makumba"))
      return;
    Hashtable h=(Hashtable)validParams.get(name);
    if(h==null)
      throw new InvalidValueException(this, "invalid format parameter \'"+name+"\'");
    if(h.size()==0)
      return;
    if(h.get(val)==null)
      throw new InvalidValueException(this, "invalid value for format parameter \'"+name+"\': <"+val+">");
  }

  public String format(Object o, Dictionary formatParams)
  {
    if(o==null || o.equals(getNull()))
      return formatNull(formatParams);
    return formatNotNull(o, formatParams);
  }

  public String formatNull(Dictionary formatParams) { return ""; }
  public String formatNotNull(Object o, Dictionary formatParams) {return o.toString(); }

  public int getIntParam(Dictionary formatParams, String name)
  {
    String s=(String)formatParams.get(name);
    if(s==null)
      return -1;
    try{
      return Integer.parseInt(s);
    }catch(NumberFormatException e) { throw new InvalidValueException(this, "invalid integer for "+name+": "+s); }
  }

  public String getIntParamString(Dictionary formatParams, String name)
  {
    int n=getIntParam(formatParams, name);
    if(n==-1)
      return "";
    return name+"=\""+n+"\" ";
  }
}
