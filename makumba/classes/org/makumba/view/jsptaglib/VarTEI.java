package org.makumba.view.jsptaglib;
import java.util.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;


public class VarTEI extends TagExtraInfo 
{
  public VariableInfo[] getVariableInfo(TagData data) {
    Vector v= new Vector();
    
    String var= data.getAttributeString("var");
    if(var!=null)
      v.addElement(new VariableInfo(var, "java.lang.Object", true, VariableInfo.AT_BEGIN));

    var= data.getAttributeString("printVar");
    if(var!=null)
      v.addElement(new VariableInfo(var, "java.lang.String", true, VariableInfo.AT_BEGIN));
    
    return CountTEI.vector2VarInfo(v);
  }    

}
