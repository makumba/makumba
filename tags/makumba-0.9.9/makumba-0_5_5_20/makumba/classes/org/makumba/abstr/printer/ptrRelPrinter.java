package org.makumba.abstr.printer;

public class ptrRelPrinter extends FieldPrinter
{
   public String valueToString()
   { return super.valueToString()
        +" "+getForeignTable().getName();
   }
}
