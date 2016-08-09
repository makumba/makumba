package org.makumba.abstr.printer;

public class ptrPrinter extends FieldPrinter
{
   public String valueToString()
   { return super.valueToString()
        +" "+getForeignTable().getName()
        +(hasTitleFieldIndicated()?" title: "+getTitleField():"");
   }
}
