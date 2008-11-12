package org.makumba.demo;

import java.util.Dictionary;

import org.makumba.Attributes;
import org.makumba.Database;
import org.makumba.LogicException;

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

}
