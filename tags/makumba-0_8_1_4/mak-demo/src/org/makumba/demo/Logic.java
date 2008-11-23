package org.makumba.demo;

import java.util.Dictionary;
import java.util.Hashtable;

import org.makumba.Attributes;
import org.makumba.Database;
import org.makumba.LogicException;
import org.makumba.Pointer;

/**
 * Makumba Java Business Logic
 * @author Marius Andra
 *
 */
public class Logic {
  
  public void on_newUserUser(Dictionary d, Attributes a, Database db) throws LogicException {
      
    // encrypt the password before uploading
        d.put("password", Tools.getMD5((String)d.get("password")));
        d.put("activated", 1);

    }
  public boolean increaseField(Pointer pointer, String fieldname, Database db) {
      //Get field contents from db
      Dictionary record = new Hashtable();
      record = db.read(pointer,fieldname);
      if (record != null) {
          
       Integer a = Integer.parseInt(record.get(fieldname).toString());
       record.put(fieldname, a + 1);
       db.update(pointer, record);
       return true;
      } else {
          return false;
      }
  }
}
