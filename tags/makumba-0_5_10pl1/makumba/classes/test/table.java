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
* Testing table operations
* @author Cristian Bogdan
*/
public class table extends TestCase
{
  static Database db;
  static long epsilon=2000;

  public table(String name) {
    super(name);
  }

  public static void main (String[] args) {
    junit.textui.TestRunner.run (suite());
  }

  public static Test suite() {
    return new TestSuite(table.class);
  }
  
  public void setUp()
  {
    db=MakumbaSystem.getConnectionTo(MakumbaSystem.getDefaultDatabaseName("test/testDatabase.properties"));
  }

    public void tearDown() { db.close(); }
  static Pointer ptr, ptr1;
  static Date create;
  static String[] personFields={"TS_modify", "TS_create", "extraData", "birthdate"};
  static String[] ptrOneFields={"something"};
  static String[] subsetFields={"description"};
  
  static Dictionary pc, pc1;
  static Date now;
  static Pointer ptrOne;
  static Pointer set1, set2;

  String readPerson= "SELECT p.indiv.name AS name, p.indiv.surname AS surname, p.birthdate AS birthdate, p.TS_modify as TS_modify, p.TS_create as TS_create, p.extraData.something as something, p.extraData as extraData FROM test.Person p WHERE p= $1";

  String readPerson1= "SELECT p.indiv.name AS name, p.indiv.surname AS surname, p.birthdate AS birthdate, p.TS_modify as TS_modify, p.TS_create as TS_create, p.extraData.something as something, p.extraData as extraData, p.comment as comment FROM test.Person p WHERE p= $1";

  String readIntSet="SELECT i as member FROM test.Person p, p.intSet i WHERE p=$1 ORDER BY i";
  String readCharSet="SELECT c as member FROM test.Person p, p.charSet c WHERE p=$1 ORDER BY c";

  static InputStream getExampleData()
  {
    try{
      return new BufferedInputStream(new FileInputStream("../../util-java/lib/antlr.jar".replace('/', File.separatorChar)));
    }catch(IOException e) {e.printStackTrace(); return null;}
  }

  public void testQueryValidMdds()
  {
        Vector v= org.makumba.MakumbaSystem.mddsInDirectory("test/validMdds");
	Vector errors=new Vector();
	for(int i=0; i<v.size(); i++)
	{
	  try{
		Vector v1=db.executeQuery("SELECT t FROM test.validMdds."+(String)v.elementAt(i)+" t",null);
	  } catch(Exception e) {
		errors.add("\n ."+(errors.size()+1)+") Error querying valid MDD <"+(String)v.elementAt(i)+">:\n\t "+e);
	  }
	}
	if(errors.size()>0)
		fail("\n  Tested "+v.size()+" valid MDDs, of which "+errors.size()+" cant be used for DB queries:"+errors.toString());   }


  public void testInsert()
  {
    Properties p=new Properties();
    Calendar c= Calendar.getInstance();
    c.clear();
    c.set(1977, 2, 5);
    Date birth= c.getTime();

    p.put("birthdate", birth);
    p.put("comment", new Text(getExampleData()));

    p.put("indiv.name", "john");
    p.put("indiv.surname", "doe");
    p.put("extraData.something", "else");

    Vector setintElem=new Vector();
    setintElem.addElement(new Integer(1));
    setintElem.addElement(new Integer(0));

    Vector setcharElem=new Vector();
    setcharElem.addElement("f");
    setcharElem.addElement("e");


    p.put("intSet", setintElem);
    p.put("charSet", setcharElem);

    ptr= db.insert("test.Person", p);
    assertNotNull(ptr);

    now= new Date();

    Vector v= db.executeQuery(readPerson1, ptr);

    assertEquals(1, v.size());
    
    pc=(Dictionary)v.elementAt(0);
    
    create= (Date)pc.get("TS_create");
    ptrOne=(Pointer)pc.get("extraData");
    assertEquals("Name","john", pc.get("name"));
    assertEquals("Surname", "doe", pc.get("surname"));
    assertEquals("Birthdate", birth, pc.get("birthdate"));
    assertEquals("Something else", "else", pc.get("something"));
    assertEquals("Comment text", pc.get("comment"), new Text(getExampleData()) );
    assertNotNull(ptrOne);

    v=db.executeQuery(readIntSet, ptr);
    assertEquals(2, v.size());
    assertEquals(new Integer(0), ((Dictionary)v.elementAt(0)).get("member"));
    assertEquals(new Integer(1), ((Dictionary)v.elementAt(1)).get("member"));

    v=db.executeQuery(readCharSet, ptr);
    assertEquals(v.size(), 2);
    assertEquals("e", ((Dictionary)v.elementAt(0)).get("member"));
    assertEquals("f", ((Dictionary)v.elementAt(1)).get("member"));

    assertEquals(create, pc.get("TS_modify"));
    assertTrue(now.getTime()- create.getTime()<3*epsilon);
  }

  static String subsetQuery= "SELECT a.description, a, a.description, a.sth.aaa FROM test.Person p, p.address a WHERE p=$1 ORDER BY a.description";

  public void testSetInsert()
  {
    Dictionary p= new Hashtable();
    p.put("description", "home");
    p.put("sth.aaa", "bbb");

    set1=db.insert(ptr, "address", p);

    assertNotNull(set1);
    Vector v= db.executeQuery(subsetQuery, ptr);
    assertEquals(1, v.size());
    assertEquals("home", ((Dictionary)v.elementAt(0)).get("col1")); 
    assertEquals(set1, ((Dictionary)v.elementAt(0)).get("col2")); 
    assertEquals("home", ((Dictionary)v.elementAt(0)).get("col3")); 
    assertEquals("bbb", ((Dictionary)v.elementAt(0)).get("col4")); 

    p.put("description", "away");

    set2=db.insert(ptr, "address", p);
    assertNotNull(set2);
    assertEquals("away", db.read(set2, subsetFields).get("description"));
    v= db.executeQuery(subsetQuery, ptr);
    assertEquals(2, v.size());
    assertEquals("away", ((Dictionary)v.elementAt(0)).get("col1"));
    assertEquals(set2, ((Dictionary)v.elementAt(0)).get("col2")); 
    assertEquals("home", ((Dictionary)v.elementAt(1)).get("col1"));
    assertEquals(set1, ((Dictionary)v.elementAt(1)).get("col2")); 
  }
  
  public void testSetMemberUpdate()
  {
    Dictionary p= new Hashtable();
    p.put("description", "somewhere");

    db.update(set2, p);

    Vector v= db.executeQuery(subsetQuery, ptr);

    assertEquals("somewhere", db.read(set2, subsetFields).get("description"));
    v= db.executeQuery(subsetQuery, ptr);
    assertEquals(v.size(), 2);
    assertEquals("home", ((Dictionary)v.elementAt(0)).get("col1"));
    assertEquals(set1, ((Dictionary)v.elementAt(0)).get("col2")); 
    assertEquals("somewhere", ((Dictionary)v.elementAt(1)).get("col1"));
    assertEquals(set2, ((Dictionary)v.elementAt(1)).get("col2")); 
  }
  
  public void testSetMemberDelete()
  {
    db.delete(set1);
    assertNull(db.read(set1, subsetFields));
    Vector v= db.executeQuery(subsetQuery, ptr);
    assertEquals(1,v.size());
    assertEquals("somewhere", ((Dictionary)v.elementAt(0)).get("col1")); 
    assertEquals(set2, ((Dictionary)v.elementAt(0)).get("col2")); 

    // we put it back
    Dictionary p= new Hashtable();
    p.put("description", "home");

    set1=db.insert(ptr, "address", p);
  }

  public void testSubrecordUpdate()
  {
    Dictionary p= new Hashtable();
    p.put("something", "else2");

    db.update(ptrOne, p);

    Dictionary d=db.read(ptr, personFields);
    assertNotNull(d);
    assertEquals(ptrOne, d.get("extraData"));

    d=db.read(ptrOne, ptrOneFields);
    assertNotNull(d);
    assertEquals("else2", d.get("something"));
  }

  static Object [][] languageData= 
  {
    {"English", "en"}
    ,{"French", "fr"}
    ,{"German", "de"}
    ,{"Italian", "it"}
    ,{"Spanish", "sp"}
  };
  static String [] toInsert={ "German", "Italian" };

  static String langQuery="SELECT l FROM test.Language l WHERE l.name=$1";

  static String speaksQuery="SELECT l as k, l.name as name FROM test.Person p, p.speaks l WHERE p=$1";

  static String checkSpeaksQuery="SELECT l, l.Language FROM test.Person.speaks l WHERE l.Person=$1";

  void workWithSet(String[] t)
  {
    Vector v= new Vector();
    for(int i=0; i<t.length; i++)
      v.addElement(((Dictionary)(db.executeQuery(langQuery, t[i]).elementAt(0))).get("col1"));

    Hashtable dt=new Hashtable();
    dt.put("speaks", v);
    db.update(ptr, dt);

    Vector result= db.executeQuery(speaksQuery, ptr);
    Vector result1= db.executeQuery(checkSpeaksQuery, ptr);

    assertEquals(t.length, result.size());
    assertEquals(t.length, result1.size());
    
    for(int i=0; i<t.length; i++)
      {
	for(int j= 0; j<result.size();j++)
	  {
	    Dictionary d= (Dictionary)result.elementAt(j);
	    if(d.get("name").equals( t[i]))
	      {
		for(int k= 0; j<result1.size();k++)
		  if(((Dictionary)result1.elementAt(k)).get("col2").equals(d.get("k")))
		    {
		      result1.removeElementAt(k);
		      break;
		    }		
		result.removeElementAt(j);
		break;
	      }
	  }
      }
    assertEquals(0, result.size());
    assertEquals(0, result1.size());
  }

  public void testSetUpdate()
  {
    Dictionary p= new Hashtable();
    if(db.executeQuery("SELECT l FROM test.Language l", null).size()==0)
      for(int i=0; i<languageData.length; i++)
      {
	p.put("name", languageData[i][0]);
	p.put("isoCode", languageData[i][1]);
	db.insert("test.Language", p);
      }    
    p=new Hashtable();

    workWithSet(toInsert);

  }

  static String [] toInsert2={ "English", "Italian", "French"};

  public void testSetUpdate2()
  {
    workWithSet(toInsert2);
  }

  static String [] toInsert3={ "English", "German", "French"};

  public void testSetDelete()
  {
    Dictionary p=new Hashtable();

    Vector v= new Vector();

    Hashtable dt= new Hashtable();
    dt.put("speaks", new Vector());
    
    db.update(ptr, dt);
    Vector result= db.executeQuery(speaksQuery, ptr);
    assertEquals(0, result.size());
    
    assertEquals(0, db.executeQuery("SELECT l FROM  test.Person.speaks l WHERE l.Person=$1", ptr).size());

    workWithSet(toInsert3);
  }

  public void testPtrOneDelete() 
  {
    db.delete(ptrOne);

    Dictionary d=db.read(ptr, personFields);
    assertNotNull(d);
    assertNull(d.get("extraData"));

    assertNull(db.read(ptrOne, ptrOneFields));
  }

  public void testPtrOneReInsert()
  {
    Dictionary p= new Hashtable();
    p.put("extraData.something", "else2");
    db.update(ptr, p);
    Dictionary d=db.read(ptr, personFields);
    ptrOne=(Pointer)d.get("extraData");
    assertNotNull(ptrOne);    
    Dictionary read;
    assertNotNull(read=db.read(ptrOne, ptrOneFields));
    assertEquals("else2", read.get("something"));
  }
  
  public void testUpdate()
  {
    Properties pmod=new Properties();
    String val="A completely new guy";
    pmod.put("indiv.name", val);
    
    Vector setintElem=new Vector();
    setintElem.addElement(new Integer(2));

    Vector setcharElem=new Vector();
    setcharElem.addElement("d");


    pmod.put("intSet", setintElem);
    pmod.put("charSet", setcharElem);


    db.update(ptr, pmod);
    
    now= new Date();
    Vector v= db.executeQuery(readPerson, ptr);
    assertEquals(1, v.size());

    Dictionary modc=(Dictionary)v.elementAt(0);

    assertNotNull(modc);
    create= (Date)modc.get("TS_create");
    assertEquals(val, modc.get("name"));
    assertEquals("doe", modc.get("surname"));
    assertEquals(create, pc.get("TS_create"));
    assertTrue(now.getTime()-((Date)modc.get("TS_modify")).getTime()<epsilon);
    assertNotNull(db.read(ptrOne, ptrOneFields));

    v=db.executeQuery(readIntSet, ptr);
    assertEquals(1, v.size());
    assertEquals(new Integer(2), ((Dictionary)v.elementAt(0)).get("member"));

    v=db.executeQuery(readCharSet, ptr);
    assertEquals(1, v.size());
    assertEquals("d", ((Dictionary)v.elementAt(0)).get("member"));
  }

  public void testDelete()
  {
    db.delete(ptr);

    assertNull(db.read(ptr, personFields));
    assertNull(db.read(ptrOne, ptrOneFields));
    assertEquals(0, db.executeQuery(subsetQuery, ptr).size());
    assertNull(db.read(set1, subsetFields));
    assertNull(db.read(set2, subsetFields));
    assertEquals(0, db.executeQuery(speaksQuery, ptr).size());
    assertEquals(0, db.executeQuery(readIntSet, ptr).size());
    assertEquals(0, db.executeQuery(readCharSet, ptr).size());
    assertEquals(0, db.executeQuery("SELECT l FROM  test.Person.speaks l WHERE l.Person=$1", ptr).size());
    assertEquals(0, db.executeQuery("SELECT l FROM  test.Person.intSet l WHERE l.Person=$1", ptr).size());
    assertEquals(0, db.executeQuery("SELECT l FROM  test.Person.charSet l WHERE l.Person=$1", ptr).size());
  }

  public void testCopy()
  {
    Properties p=new Properties();
    p.put("birthdate", new Date(77, 7, 7));
    p.put("indiv.name", "john");
    p.put("indiv.surname", "Copy");
    p.put("extraData.something", "else");

    Calendar c= Calendar.getInstance();
    c.clear();
    c.set(1976, 2, 9);

    Date cr= c.getTime();
    p.put("TS_create", cr);

    c.clear();
    c.set(1976, 2, 10);

    Date mod= c.getTime();
    p.put("TS_modify", mod);

    ptr1= db.insert("test.Person", p);
    assertNotNull(ptr1);

    now= new Date();
    Vector v= db.executeQuery(readPerson, ptr1);
    assertEquals(1, v.size());

    pc1=(Dictionary)v.elementAt(0);
    assertNotNull(pc1);

    assertEquals("john", pc1.get("name"));
    assertEquals("Copy", pc1.get("surname"));
    assertEquals(cr, new Date(((Date)pc1.get("TS_create")).getTime()));
    assertEquals(mod, new Date(((Date)pc1.get("TS_modify")).getTime()));
    db.delete(ptr1);
    String nm=MakumbaSystem.getDefaultDatabaseName("test/testDatabase.properties");

    System.out.println
	(
	 "\nworked with: "+MakumbaSystem.getDatabaseProperty(nm, "sql_engine.name")
	 +" version: "+MakumbaSystem.getDatabaseProperty(nm, "sql_engine.version")
	 +"\njdbc driver: "+MakumbaSystem.getDatabaseProperty(nm, "jdbc_driver.name")
	 +" version: "+MakumbaSystem.getDatabaseProperty(nm, "jdbc_driver.version")
	 +"\njdbc connections allocated: "+MakumbaSystem.getDatabaseProperty(nm, "jdbc_connections")
	 +"\ncaches: "+org.makumba.util.NamedResources.getCacheInfo()

	 );
  }
}
