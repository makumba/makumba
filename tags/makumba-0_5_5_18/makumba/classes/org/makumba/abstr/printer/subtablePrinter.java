package org.makumba.abstr.printer;

public class subtablePrinter extends FieldPrinter
{
   public String valueToString()
   { 
       return super.valueToString()+" "+new RecordPrinter(getSubtable()); 
   }
}