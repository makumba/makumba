package org.makumba.db;
import org.makumba.abstr.*;

/** At RecordHandler building, this FieldHandler will replace itself with 
 * a record handler of class type+DB from the same package */
public class foreignHandler extends FieldHandler
{
  public Object replaceIn(RecordHandler rh)
  {
    Table t= (Table)rh;
    //t.relatedTables.put(getName(), t.getDatabase().getTable(getForeignTable()));
    t.relatedTables.put(getName(), getForeignTable());
    return rh.makeHandler(getType()+"DB");
  }
}
