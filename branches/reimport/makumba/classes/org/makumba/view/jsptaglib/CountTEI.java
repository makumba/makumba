package org.makumba.view.jsptaglib;
import java.util.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;


public class CountTEI extends TagExtraInfo 
{
  public VariableInfo[] getVariableInfo(TagData data) {
    Vector v= new Vector();
    
    String var= data.getAttributeString("countVar");
    if(var!=null)
      v.addElement(new VariableInfo(var, "java.lang.Integer", true, VariableInfo.AT_BEGIN));

    var= data.getAttributeString("maxCountVar");
    if(var!=null)
      v.addElement(new VariableInfo(var, "java.lang.Integer", true, VariableInfo.AT_BEGIN));
    
    return vector2VarInfo(v);
  }

  public static VariableInfo[] vector2VarInfo(Vector v)
  {
    if(v.size()==0)
      return null;
    VariableInfo vi[] = new VariableInfo[v.size()];
    for(int i=0; i<v.size(); i++)
      vi[i]=(VariableInfo)v.elementAt(i);
    return vi;
  }
}
