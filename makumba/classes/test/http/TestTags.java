/*
 * Created on Apr 29, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package test.http;

import java.io.IOException;
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
import org.makumba.view.jsptaglib.MakumbaVersionTag;
import org.makumba.view.jsptaglib.QueryTag;

/**
 * @author jpeeters
 */
public class TestTags extends JspTestCase {

	static Database db;
	static Pointer ptr;
	static Dictionary pc;
	static Vector v;

	public static Test suite() {
		TestSetup setup = new TestSetup(new TestSuite(TestTags.class)) {
			protected void setUp() {
				// do your one-time setup here!
				db = MakumbaSystem.getConnectionTo(MakumbaSystem.getDefaultDatabaseName("test/testDatabase.properties"));
			}

			protected void tearDown() {
				// do your one-time tear down here!
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

	public void testMakObjectTag() throws ServletException, IOException {
		Properties p = new Properties();
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(1977, 2, 5);
		Date birth = c.getTime();
		String myComment = "this is the comment";

		p.put("birthdate", birth);
		p.put("comment", myComment);

		p.put("weight", new Double(85.7d));

		p.put("indiv.name", "john");
		p.put("indiv.surname", "doe");
		p.put("extraData.something", "else");

		Vector setintElem = new Vector();
		setintElem.addElement(new Integer(1));
		setintElem.addElement(new Integer(0));

		Vector setcharElem = new Vector();
		setcharElem.addElement("f");
		setcharElem.addElement("e");

		p.put("intSet", setintElem);
		p.put("charSet", setcharElem);

		ptr = db.insert("test.Person", p);
		db.commit();
		assertNotNull(ptr);
		assertEquals(ptr.getType(), "test.Person");

		QueryTag qt = new QueryTag();

		pageContext.include("testMakObjectTag.jsp");

		String readPerson = "SELECT p.indiv.name AS name, p.indiv.surname AS surname, p.birthdate AS birthdate, p.weight as weight, p.TS_modify as TS_modify, p.TS_create as TS_create, p.extraData.something as something, p.extraData as extraData, p.comment as comment FROM test.Person p WHERE p= $1";
		v = db.executeQuery(readPerson, ptr);
		db.delete(ptr);
		db.commit();
	}

	public void endMakObjectTag(WebResponse response) {
		String output = response.getText();

		assertEquals(1, v.size());
		pc = (Dictionary) v.elementAt(0);

		output = output.substring(output.indexOf("name") + 5);
		assertEquals(pc.get("name"), output.substring(0, output.indexOf("\r")));
		output = output.substring(output.indexOf("weight") + 7);
		assertEquals(pc.get("weight").toString(), output.substring(0, output
				.indexOf("\r")));
	}

	static Object[][] languageData = { { "English", "en" }, { "French", "fr" },
			{ "German", "de" }, { "Italian", "it" }, { "Spanish", "sp" } };

	public void testMakListTag() throws ServletException, IOException {
		Dictionary p = new Hashtable();
		if (db.executeQuery("SELECT l FROM test.Language l", null).size() == 0)
			for (int i = 0; i < languageData.length; i++) {
				p.put("name", languageData[i][0]);
				p.put("isoCode", languageData[i][1]);
				db.insert("test.Language", p);
			}
		db.commit();
		p = new Hashtable();

		QueryTag maklist = new QueryTag();

		pageContext.include("testMakListTag.jsp");

		for (int i = 0; i < languageData.length; i++) {
			db.delete("test.Language l", "l.name = '" + languageData[i][0]
					+ "'", null);
		}
		db.commit();
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

}
