package org.makumba.abstr.printer;

public class charPrinter extends FieldPrinter
{
   public String valueToString()
   { return super.valueToString() + "["+getWidth()+"]";}
}