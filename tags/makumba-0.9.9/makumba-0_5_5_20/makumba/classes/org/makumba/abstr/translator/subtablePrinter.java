package org.makumba.abstr.translator;

public class subtablePrinter extends FieldPrinter
{
   public String valueToString()
   {
       return super.valueToString()+" "+new RecordPrinter(getSubtable());
   }
}
