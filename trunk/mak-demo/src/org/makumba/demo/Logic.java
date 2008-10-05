package org.makumba.demo;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import org.makumba.AttributeNotFoundException;
import org.makumba.Attributes;
import org.makumba.Database;
import org.makumba.LogicException;
import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.UnauthorizedException;


public class Logic {
	
	public void on_newUserUser(Dictionary d, Attributes a, Database db) throws LogicException {
    	
		// encrypt the password before uploading
        d.put("password", Tools.getMD5((String)d.get("password")));
        d.put("activated", 1);

    }

}
