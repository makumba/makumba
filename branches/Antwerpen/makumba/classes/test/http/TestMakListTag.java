package test.http;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import javax.servlet.ServletException;
import org.apache.cactus.JspTestCase;
import org.apache.cactus.WebResponse;
import org.makumba.Database;
import org.makumba.MakumbaSystem;
import org.makumba.view.jsptaglib.QueryTag;

public class TestMakListTag extends JspTestCase {

	static Database db;

	public TestMakListTag() {
	}

	public void setUp() {
		db = MakumbaSystem.getConnectionTo(MakumbaSystem
				.getDefaultDatabaseName("test/testDatabase.properties"));
	}

	public void tearDown() {
		db.close();
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

		pageContext.include("index.jsp");

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