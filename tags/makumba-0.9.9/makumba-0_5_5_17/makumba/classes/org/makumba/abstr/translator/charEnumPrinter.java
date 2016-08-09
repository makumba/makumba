package org.makumba.abstr.translator;

public class charEnumPrinter extends FieldPrinter
{
   public String valueToString()
   {
      String ret= super.valueToString() + "["+getWidth()+"] {";
      for(java.util.Enumeration e= getValues(); e.hasMoreElements(); )
        ret+= " "+e.nextElement()+" ";

      return ret+"}";
   }
}
