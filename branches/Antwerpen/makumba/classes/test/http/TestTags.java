/*
 * Created on Apr 29, 2005
 *
 */
package test.http;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;

import junit.extensions.TestSetup;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.JspTestCase;
import org.apache.cactus.WebResponse;
import org.makumba.Database;
import org.makumba.MakumbaSystem;
import org.makumba.Pointer;
import org.makumba.Text;
import org.makumba.view.jsptaglib.MakumbaVersionTag;
import org.makumba.view.jsptaglib.QueryTag;


/**
 * @author jpeeters
 */
public class TestTags extends JspTestCase {

	static Database db;
	static Pointer ptr;
	static Pointer set;
	static Dictionary pc;
	static Vector v;
	static String readPerson = "SELECT p.indiv.name AS name, p.indiv.surname AS surname, p.gender AS gender, p.uniqChar AS uniqChar, p.uniqInt AS uniqInt, p.birthdate AS birthdate, p.weight AS weight, p.TS_modify AS TS_modify, p.TS_create AS TS_create, p.comment AS comment, a.description AS description, a.email AS email, a.usagestart AS usagestart FROM test.Person p, p.address a WHERE p= $1";

	static Object[][] languageData = { { "English", "en" }, { "French", "fr" },
			{ "German", "de" }, { "Italian", "it" }, { "Spanish", "sp" } };
	
	public static Test suite() {
		TestSetup setup = new TestSetup(new TestSuite(TestTags.class)) {
			ArrayList ptrs = new ArrayList();
			protected void setUp() {
				// do your one-time setup here!
				db = MakumbaSystem.getConnectionTo(MakumbaSystem.getDefaultDatabaseName("test/testDatabase.properties"));
				insertPerson();
				insertLanguages();				
			}
			
			protected void insertPerson() {
				Properties p = new Properties();

				p.put("indiv.name", "john");
				p.put("indiv.surname", "doe");
				
				Calendar c = Calendar.getInstance();
				c.clear();
				c.set(1977, 2, 5);
				Date birthdate = c.getTime();
				p.put("birthdate", birthdate);
						
				
				p.put("gender", new Integer(1));
				p.put("uniqChar", new String("testing \" character field"));
				
				p.put("weight", new Double(85.7d));
				
				p.put("comment", new Text("This is a text field. It's a comment about this person."));

				p.put("uniqInt", new Integer(255));
				
				Vector intSet = new Vector();
				intSet.addElement(new Integer(1));
				intSet.addElement(new Integer(0));
				p.put("intSet", intSet);

				ptr = db.insert("test.Person", p);
				
				p.clear();
				p.put("description", "");
				p.put("usagestart", birthdate);
				set=db.insert(ptr, "address", p);
				db.commit();
			}
			protected void deletePerson() {
				db.delete(set);
				db.delete(ptr);
			}
			
			protected void insertLanguages() {
				Dictionary p = new Hashtable();				
				for (int i = 0; i < languageData.length; i++) {
					p.put("name", languageData[i][0]);
					p.put("isoCode", languageData[i][1]);
					ptrs.add(db.insert("test.Language", p));
				}
				db.commit();
			}
			protected void deleteLanguages() {
				for (int i = 0; i<ptrs.size(); i++)
					db.delete((Pointer)ptrs.get(i));
			}
			
			protected void tearDown() {
				// do your one-time tear down here!
				deletePerson();
				deleteLanguages();
				db.close();
			}
		};
		return setup;
	}	
	
	public void testVersionTag() throws JspException, IOException {
		MakumbaVersionTag versionTag = new MakumbaVersionTag();
		versionTag.setPageContext(pageContext);
		versionTag.doStartTag();
		session.setAttribute("version", "0.0");
		Assert.assertEquals(1, versionTag.doStartTag());
		BodyContent bodyContent = pageContext.pushBody();
		bodyContent.println("Makumbaaaaaaaaaaa");
		bodyContent.print("Version 0");
		versionTag.doAfterBody();
		versionTag.doEndTag();
		pageContext.popBody();
	}

	public void testDataInput() throws JspException, JspException {
		assertNotNull(ptr);
		assertEquals(ptr.getType(), "test.Person");
	}
	
	public void testMakObjectTag() throws ServletException, IOException {
		QueryTag makobject = new QueryTag();
		pageContext.include("testMakObjectTag.jsp");		
	}
	public void endMakObjectTag(WebResponse response) {
		String output = response.getText();
		
		v = db.executeQuery(readPerson, ptr);

		assertEquals(1, v.size());
		pc = (Dictionary) v.elementAt(0);

		output = output.substring(output.indexOf("name") + 5);
		assertEquals(pc.get("name"), output.substring(0, output.indexOf("\r")));
		output = output.substring(output.indexOf("weight") + 7);
		assertEquals(pc.get("weight").toString(), output.substring(0, output
				.indexOf("\r")));
	}

	public void testMakListTag() throws ServletException, IOException {
		QueryTag maklist = new QueryTag();
		pageContext.include("testMakListTag.jsp");
	}
	public void endMakListTag(WebResponse response) {
		String output = response.getText();
		int i = 0, begin, end;

		while ((begin = output.indexOf("name")) != -1) {
			output = output.substring(begin + 5);
			assertEquals(languageData[i][0], output.substring(0, output
					.indexOf("\r")));
			output = output.substring(output.indexOf("isoCode") + 8);
			assertEquals(languageData[i][1], output.substring(0, output
					.indexOf("\r")));
			i++;
		}
		assertEquals(true, response.getText().indexOf("English") != -1);
		return;
	}

	public void testMakValueChar() throws ServletException, IOException {
		QueryTag makobject = new QueryTag();
		pageContext.include("testMakValueFieldTypes.jsp");		
	}	
	public void endMakValueChar(WebResponse response) {
		String output = response.getText();
		v = db.executeQuery(readPerson, ptr);
		
		pc = (Dictionary) v.elementAt(0);
				
		assertEquals(pc.get("name"), output.substring(output.indexOf("testName!")+9, output.indexOf("!endName")));
		assertEquals(pc.get("surname"), output.substring(output.indexOf("testSurname!")+12, output.indexOf("!endSurname")));
		assertEquals(((String)pc.get("uniqChar")).replaceAll("\"","&quot;"), output.substring(output.indexOf("testUniqCharAuto!")+17, output.indexOf("!endUniqCharAuto")));
//		TODO addTitle prints ' for "
//		assertEquals("<span title=\""+pc.get("uniqChar")+"\">"+((String)pc.get("uniqChar")).replaceAll("\"","&quot;")+"</span>", output.substring(output.indexOf("testUniqCharAutoAddTitleTrue!")+29, output.indexOf("!endUniqCharAutoAddTitleTrue")));		
		assertEquals(pc.get("uniqChar"), output.substring(output.indexOf("testUniqCharHtml!")+17, output.indexOf("!endUniqCharHtml")));
		assertEquals(((String)pc.get("uniqChar")).replaceAll("\"","&quot;"), output.substring(output.indexOf("testUniqCharNoHtml!")+19, output.indexOf("!endUniqCharNoHtml")));
		assertEquals(((String)pc.get("uniqChar")).substring(0,5)+"...", output.substring(output.indexOf("testUniqCharMaxLength!")+22, output.indexOf("!endUniqCharMaxLength")));
		assertEquals(((String)pc.get("uniqChar")).substring(0,5)+"---", output.substring(output.indexOf("testUniqCharMaxLengthEllipsis!")+30, output.indexOf("!endUniqCharMaxLengthEllipsis")));
//		assertEquals("<span title=\""+pc.get("uniqChar")+"\">"+((String)pc.get("uniqChar")).substring(0,5)+"---</span>", output.substring(output.indexOf("testUniqCharMaxLengthEllipsisAddTitleAuto!")+42, output.indexOf("!endUniqCharMaxLengthEllipsisAddTitleAuto")));
		
		
	}
	
	public void testMakValueDate() throws ServletException, IOException {
		QueryTag makobject = new QueryTag();
		pageContext.include("testMakValueFieldTypes.jsp");		
	}	
	public void endMakValueDate(WebResponse response) {
		String output = response.getText();
		v = db.executeQuery(readPerson, ptr);
		
		pc = (Dictionary) v.elementAt(0);
		
		DateFormat df = new SimpleDateFormat("dd MMMM yyyy");
		assertEquals(df.format(pc.get("birthdate")), output.substring(output.indexOf("testBirthdate!")+14, output.indexOf("!endBirthdate")));
		
		df = new SimpleDateFormat("dd-mm-yy");
		assertEquals(df.format(pc.get("birthdate")), output.substring(output.indexOf("testBirthdateFormat!")+20, output.indexOf("!endBirthdateFormat")));		
	}

	public void testMakValueInt() throws ServletException, IOException {
		QueryTag makobject = new QueryTag();
		pageContext.include("testMakValueFieldTypes.jsp");	
	}
	public void endMakValueInt(WebResponse response) {
		String output = response.getText();
		v = db.executeQuery(readPerson, ptr);
		
		pc = (Dictionary) v.elementAt(0);
		
		assertEquals("Male", output.substring(output.indexOf("testGender!")+11, output.indexOf("!endGender")));
		assertEquals(pc.get("uniqInt").toString(), output.substring(output.indexOf("testUniqInt!")+12, output.indexOf("!endUniqInt")));
	}
	
	public void testMakValueDouble() throws ServletException, IOException {
		QueryTag makobject = new QueryTag();
		pageContext.include("testMakValueFieldTypes.jsp");
	}
	public void endMakValueDouble(WebResponse response) {
		String output = response.getText();		
		v = db.executeQuery(readPerson, ptr);

		pc = (Dictionary) v.elementAt(0);

		assertEquals(pc.get("weight").toString(), output.substring(output.indexOf("testWeight!")+11, output.indexOf("!endWeight")));
	}
	
	public void testMakValueText() throws ServletException, IOException {
		QueryTag makobject = new QueryTag();
		pageContext.include("testMakValueFieldTypes.jsp");
	}
	public void endMakValueText(WebResponse response) {
		String output = response.getText();		
		v = db.executeQuery(readPerson, ptr);

		pc = (Dictionary) v.elementAt(0);

		assertEquals("<p>"+pc.get("comment")+"</p>", output.substring(output.indexOf("testComment!")+12, output.indexOf("!endComment")));
		assertEquals("<abc>"+pc.get("comment"), output.substring(output.indexOf("testCommentLineSeparator!")+25, output.indexOf("!endCommentLineSeparator")));		
		assertEquals(pc.get("comment").toString(), output.substring(output.indexOf("testCommentLongLineLength!")+26, output.indexOf("!endCommentLongLineLength")));
	}
	
	public void testMakValueSet() throws ServletException, IOException {
		QueryTag makobject = new QueryTag();
		pageContext.include("testMakValueFieldTypes.jsp");
	}
	public void endMakValueSet(WebResponse response) {
		String output = response.getText();		
		v = db.executeQuery(readPerson, ptr);

		pc = (Dictionary) v.elementAt(0);

		assertEquals(pc.get("description"), output.substring(output.indexOf("testAddressDescription!")+23, output.indexOf("!endAddressDescription")));
		assertEquals("N/A", output.substring(output.indexOf("testAddressDescriptionEmpty!")+28, output.indexOf("!endAddressDescriptionEmpty")));
		assertEquals("N/A", output.substring(output.indexOf("testAddressEmailDefault!")+24, output.indexOf("!endAddressEmailDefault")));
		
		DateFormat df = new SimpleDateFormat("dd MMMM yyyy");
		assertEquals(df.format(pc.get("usagestart")), output.substring(output.indexOf("testAddressUsagestart!")+22, output.indexOf("!endAddressUsagestart")));
	}
	
	public void testMakValueTS_create() throws ServletException, IOException {
		QueryTag makobject = new QueryTag();
		pageContext.include("testMakValueFieldTypes.jsp");		
	}
	public void endMakValueTS_create(WebResponse response) {
		String output = response.getText();		
		v = db.executeQuery(readPerson, ptr);

		pc = (Dictionary) v.elementAt(0);

		assertEquals(pc.get("TS_create").toString(), output.substring(output.indexOf("testTS_create!")+14, output.indexOf("!endTS_create")));
	}
	
	public void testMakValueTS_modify() throws ServletException, IOException {
		QueryTag makobject = new QueryTag();
		pageContext.include("testMakValueFieldTypes.jsp");		
	}
	public void endMakValueTS_modify(WebResponse response) {
		String output = response.getText();		
		v = db.executeQuery(readPerson, ptr);

		pc = (Dictionary) v.elementAt(0);

		assertEquals(pc.get("TS_modify").toString(), output.substring(output.indexOf("testTS_modify!")+14, output.indexOf("!endTS_modify")));
	}


}
