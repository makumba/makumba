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
import junit.framework.*;
import junit.extensions.*;
import java.util.*;
import org.makumba.*;
import java.io.*;

/**
* Testing mdd handling & parsing
* @author Stefan Baebler
*/
public class mdd extends TestCase
{
  public mdd(String name) {
    super(name);
  }

  public static void main (String[] args) {
    junit.textui.TestRunner.run (suite());
  }

  public static Test suite() {
    return new TestSuite(mdd.class);
  }
  

  public void testMdd()
  {
    MakumbaSystem.getDataDefinition("test.Person");
    MakumbaSystem.getDataDefinition("test.Person.address.sth");
  }

  public void testMddPrinter()
  {
    System.out.println("\n"+new org.makumba.abstr.printer.RecordPrinter("test.Individual"));
    String personMdd=new org.makumba.abstr.printer.RecordPrinter("test.Person").toString();
  }

  public void testNonexistingMdd() {
	try {
		MakumbaSystem.getDataDefinition("test.brokenMdds.NonexistingMdd");
		fail("Should raise DataDefinitionNotFoundError");
	} catch (DataDefinitionNotFoundError e) { }
  }

  public void testWronglyCapitalizedMdd() {
	try {
		MakumbaSystem.getDataDefinition("test.person");
		fail("Should raise DataDefinitionNotFoundError");
	} catch (DataDefinitionNotFoundError e) { }
  }


  /** See <a href="see http://bugs.best.eu.org/show_bug.cgi?id=526">bug 526</a>. */
  public void testBrokenMddBug526() {
	try {
		MakumbaSystem.getDataDefinition("test.brokenMdds.Bug526");
		fail("Should raise DataDefinitionParseError");
	} catch (DataDefinitionParseError e) { }
  }

  public void testBrokenMddBug() {
	try {
		MakumbaSystem.getDataDefinition("test.brokenMdds.BrokenType");
		fail("Should raise DataDefinitionParseError");
	} catch (DataDefinitionParseError e) { }
  }

  public void testRepeatedFieldName() {
	try {
		MakumbaSystem.getDataDefinition("test.brokenMdds.RepeatedField");
		fail("Should raise DataDefinitionParseError");
	} catch (DataDefinitionParseError e) { }
  }

  public void testbadTitle() {
	try {
		MakumbaSystem.getDataDefinition("test.brokenMdds.BadTitle");
		fail("Should raise DataDefinitionParseError");
	} catch (DataDefinitionParseError e) { }
  }

}
