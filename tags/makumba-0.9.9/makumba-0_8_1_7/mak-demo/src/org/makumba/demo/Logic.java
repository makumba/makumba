package org.makumba.demo;

import java.util.Dictionary;
import java.util.Hashtable;

import org.makumba.Attributes;
import org.makumba.Database;
import org.makumba.LogicException;
import org.makumba.Pointer;

/**
 * Makumba Java Business Logic
 * @author Marius Andra, Jasper van Bourgognie
 *
 */
public class Logic {
  
  public void on_newUserUser(Dictionary d, Attributes a, Database db) throws LogicException {
      
    // encrypt the password before uploading
        d.put("password", Tools.getMD5((String)d.get("password")));
        d.put("activated", 1);

    }
  public void addToField(Pointer pointer, String fieldname, Database db, Integer amount) throws LogicException {
      Dictionary record = new Hashtable();
      record = db.read(pointer,fieldname);
      if (record != null) {
          
       Integer a = Integer.parseInt(record.get(fieldname).toString());
       record.put(fieldname, a + amount);
       db.update(pointer, record);
      } else {
          throw new LogicException("Couldn't find the record/fieldname with pointer "+ pointer + pointer.toExternalForm() +" and field "+fieldname+" where to decrease the value from");
      }
  }
  public void increaseField(Pointer pointer, String fieldname, Database db) throws LogicException{
      //Get field contents from db
      addToField(pointer, fieldname, db, 1);
  }
  public void decreaseField(Pointer pointer, String fieldname, Database db) throws LogicException{
      //Get field contents from db
      addToField(pointer, fieldname, db, -1);
  }
}
