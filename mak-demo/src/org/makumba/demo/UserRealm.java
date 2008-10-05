package org.makumba.demo;

import org.apache.catalina.realm.*;
/*
import org.makumba.db.makumba.DBConnectionWrapper;
import org.makumba.db.makumba.Database;
import org.makumba.db.makumba.sql.TableManager;
import org.makumba.providers.TransactionProvider;
import org.securityfilter.example.Constants;
*/
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.Principal;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Vector;

import org.makumba.AttributeNotFoundException;
import org.makumba.Attributes;
import org.makumba.Database;
import org.makumba.LogicException;
import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.UnauthorizedException;
import org.makumba.providers.TransactionProvider;



public class UserRealm extends RealmBase 
{
	/**
	 * Return a short name for this Realm implementation.
	 */
	protected String getName() 
	{
		return null;
	}
	
	/**
	 * Return the password associated with the given principal's user name.
	 */
	protected String getPassword(String s) 
	{
		TransactionProvider tp = TransactionProvider.getInstance();
		Transaction db = null;
		try
		{
			db = tp.getConnectionTo(tp.getDefaultDataSourceName());
		
			try {
				s = new String(s.getBytes("ISO8859_1"), "UTF8");
			} catch (Exception e) {}
	
			Vector v = db.executeQuery("SELECT u as user, u.password as password FROM user.User u WHERE u.username = $1", s);
			if(v.size() > 0)
			{
				String ss = (String) ((Dictionary) v.elementAt(0)).get("password");
				return ss;
			}
		} finally {
			db.close();
		}
		
		return null;
	}

	/**
	 * Return the Principal associated with the given user name.
	 */
	protected Principal getPrincipal(String s) 
	{	
		TransactionProvider tp = TransactionProvider.getInstance();
		Transaction db = null;
		try
		{
			db = tp.getConnectionTo(tp.getDefaultDataSourceName());
		
			try {
				s = new String(s.getBytes("ISO8859_1"), "UTF8");
			} catch (Exception e) {}
	
			Vector v = db.executeQuery("SELECT u as user, u.password as password, u.activated as activated, u.isAdmin as isAdmin FROM user.User u WHERE u.username = $1", s);
	
			if(v.size() > 0)
			{
				if(((Dictionary) v.elementAt(0)).get("activated") != null &&
					(Integer)((Dictionary) v.elementAt(0)).get("activated") > 0)
				{
					String ss = (String) ((Dictionary) v.elementAt(0)).get("password");
	
					ArrayList<String> roleList = new ArrayList<String>();
					if(((Dictionary) v.elementAt(0)).get("isAdmin") != null &&
							(Integer)(((Dictionary) v.elementAt(0)).get("isAdmin")) == 1)
					{
						roleList.add("admin");
					}
					
					return new GenericPrincipal(this, s, ss, roleList);
				}
			}
		} finally {
			db.close();
		}
		return null;
	}
	
	public Principal authenticate(String username, String credentials)
	{
		Principal newPrincipal = super.authenticate(username, Tools.getMD5(credentials));
		
		// if (newPrincipal != null) add a log entry 
						   
		return newPrincipal;
	}
   
}
