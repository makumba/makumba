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
    //IMPROVE: should try to parse the printer output again as another MDD, 
    //then compare them (eg by comparing the printer output of the new and original MDD).
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


  public void testAllValidMdds()
  {
	String base="test/validMdds/";
	Vector mdds=mddsInDirectory(base);

	//we have to collect all errors if we want to run tests on all 
	//MDDs in directory instead of stoping at first fail()ure.
	Vector errors=new Vector();
	for (Enumeration e = mdds.elements() ; e.hasMoreElements() ;) 
	{
		String mdd=(String)e.nextElement();
		try {
			MakumbaSystem.getDataDefinition(mdd);
		} catch (DataDefinitionParseError ex) { 
			errors.add("\n ."+(errors.size()+1)+") Error reported in valid MDD <"+mdd+">:\n"+ex);
			//ex.printStackTrace();
		}
	}
	if(errors.size()>0)
		fail(errors.toString()); 
  }


  public void testIfAllBrokenMddsThrowErrors()
  {
	String base="test/brokenMdds/";
	Vector mdds=mddsInDirectory(base);

	//we have to collect all errors if we want to run tests on all 
	//MDDs in directory instead of stoping at first fail()ure.
	Vector errors=new Vector();
	for (Enumeration e = mdds.elements() ; e.hasMoreElements() ;) 
	{
		DataDefinitionParseError expected=new DataDefinitionParseError();
		DataDefinitionParseError actual=expected;
		String mdd=(String)e.nextElement();
		try {
			MakumbaSystem.getDataDefinition(mdd);
		} catch (DataDefinitionParseError thrown) { 
			actual=thrown;
		}

		if(expected==actual) errors.add("\n ."+(errors.size()+1)+") Error report missing from broken MDD <"+mdd+"> ");
		if(!expected.getClass().equals(actual.getClass()))
			errors.add(mdd+" threw <"+actual.getClass()+"> instead of expected <"+expected.getClass()+">");
	}
	if(errors.size()>0)
		fail(errors.toString()); 
  }


  /** 
  * Discover mdds in a directory in classpath.
  * @return filenames (with .mdd extension) as Vector of Strings. 
  */
  Vector mddsInDirectory(String dirInClasspath)
  {
	java.net.URL u=org.makumba.util.ClassResource.get(dirInClasspath);
	File dir=new File(u.getFile());
	String[] list= dir.list();
	Vector mdds=new Vector();
	for(int i=0; i<list.length; i++)
	{
		String s= list[i];
		if(s.endsWith(".mdd"))
		{
			s=s.substring(0, s.length()-4); //cut off the ".mdd"
			s=dirInClasspath+s;
			s=s.replace(File.separatorChar,'.'); 
			mdds.add(s);
		}
	}
	return mdds;
  }



}
