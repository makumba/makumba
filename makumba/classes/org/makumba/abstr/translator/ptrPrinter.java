package org.makumba.abstr.translator;

public class ptrPrinter extends FieldPrinter
{
   public String valueToString()
   { return super.valueToString()
        +" "+getForeignTable().getName()
        +(hasTitleFieldIndicated()?" title: "+getTitleField():"");
   }
}
