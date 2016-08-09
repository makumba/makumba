package org.makumba.abstr.translator;
import org.makumba.abstr.*;

/** printer handler family's field handler */
public class nothingPrinter extends FieldPrinter
{
  /** all the information retrieved from the field info */
  public String valueToString(){ return getType(); }
  
  /** writes the field name, attributes, then calls valueToString(), then writes the description */
  public String toString()
    { 
        return "";
    }
}
