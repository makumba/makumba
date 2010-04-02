///////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003  http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//  -------------
//  $Id$
//  $Name$
/////////////////////////////////////

package test;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.makumba.ConfigurationError;
import org.makumba.Transaction;
import org.makumba.MakumbaSystem;
import org.makumba.providers.TransactionProvider;

/**
* Testing configuration related operations
* @author Stefan Baebler
*/
public class config extends TestCase
{
  public config(String name) {
    super(name);
  }

  public static void main (String[] args) {
    junit.textui.TestRunner.run (suite());
  }

  public static Test suite() {
    return new TestSuite(config.class);
  }
  
  private TransactionProvider tp = TransactionProvider.getInstance();

  public void testBuildInfo()
  {
    System.out.println(
	 "\nTesting Makumba version: "+MakumbaSystem.getVersion()
	+"\n		   built on: "+MakumbaSystem.getBuildDate()
	+"\n	       using locale: "+MakumbaSystem.getLocale()
    );
  }

  public void testNoDefaultDB() {
        try {
		String defaultDB=tp.getDefaultDataSourceName();
                fail("Should raise ConfigFileError, but found: "+defaultDB);
        } catch (ConfigurationError e) { }
  }

  /**
   * http://bugzilla.makumba.org/cgi-bin/bugzilla/show_bug.cgi?id=527
   * but wasn not discussed since then
   */
  public void DISABLED_testDBDiscovery()
  {
    Transaction db=tp.getConnectionTo("testDatabase");
    assertEquals("testDatabase", db.getName());
    db.close();
  }



}

