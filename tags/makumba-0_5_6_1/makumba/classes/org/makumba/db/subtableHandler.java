package org.makumba.db;
import org.makumba.abstr.*;

/** At RecordHandler building, this FieldHandler will replace itself with 
 * a record handler of class type+DB from the same package */
public class subtableHandler extends FieldHandler
{
  public Object replaceIn(RecordHandler rh)
  {
    Table t= (Table)rh;
    //    t.relatedTables.put(getName(), t.getDatabase().getTable(getSubtable()));
    t.relatedTables.put(getName(), getSubtable());
    return rh.makeHandler(getType()+"DB");
  }
}
