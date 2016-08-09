package org.makumba.abstr;
import org.makumba.*;

public class ptrHandler extends ptrRelHandler 
{
    public String getTitleField()
    { 
        if(hasTitleFieldIndicated())
            return (String)fi.extra2; 
        return getForeignTable().getTitleField();
    }

    public boolean hasTitleFieldIndicated()
    {
        return fi.extra2!= null;
    }
  public Object getNull() { return Pointer.Null; }
}
