package org.makumba.abstr.translator;

public class intEnumPrinter extends FieldPrinter
{
   public String valueToString()
   {
      String ret= super.valueToString() +" {";
      for(java.util.Enumeration e= getValues(), f= getNames()
      ; e.hasMoreElements(); )
        ret+= " "+f.nextElement()+"="+e.nextElement()+" ";

      return ret+"}";
   }
}
