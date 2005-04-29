package test.http;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletException;
import org.apache.cactus.JspTestCase;
import org.apache.cactus.WebResponse;
import org.makumba.Database;
import org.makumba.MakumbaSystem;
import org.makumba.Pointer;
import org.makumba.view.jsptaglib.QueryTag;

public class TestMakObjectTag extends JspTestCase {

	static Database db;

	public TestMakObjectTag() {
	}

	public void setUp() {
		db = MakumbaSystem.getConnectionTo(MakumbaSystem
				.getDefaultDatabaseName("test/testDatabase.properties"));
	}

	public void tearDown() {
		db.close();
	}

	static Pointer ptr;
	
	static Dictionary pc;
	
	static Vector v;
	
	final String readPerson1 = "SELECT p.indiv.name AS name, p.indiv.surname AS surname, p.birthdate AS birthdate, p.weight as weight, p.TS_modify as TS_modify, p.TS_create as TS_create, p.extraData.something as something, p.extraData as extraData, p.comment as comment FROM test.Person p WHERE p= $1";

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

		pageContext.include("person.jsp");
		
		v = db.executeQuery(readPerson1, ptr);
		db.delete(ptr);
		db.commit();
	}

	public void endMakObjectTag(WebResponse response) {
		String output = response.getText();

		assertEquals(1, v.size());
		pc = (Dictionary) v.elementAt(0);
		
		output = output.substring(output.indexOf("name")+5);
		assertEquals(pc.get("name"),output.substring(0,output.indexOf("\r")));
		output = output.substring(output.indexOf("weight")+7);
		assertEquals(pc.get("weight").toString(),output.substring(0,output.indexOf("\r")));
	}
}