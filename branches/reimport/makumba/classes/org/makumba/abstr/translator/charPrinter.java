package org.makumba.abstr.translator;

public class charPrinter extends FieldPrinter
{
   public String valueToString()
   { return super.valueToString() + "["+getWidth()+"]";}
}
