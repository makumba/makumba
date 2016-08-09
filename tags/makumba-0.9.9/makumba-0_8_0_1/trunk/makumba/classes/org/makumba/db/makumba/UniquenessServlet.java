package org.makumba.db.makumba;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.makumba.db.makumba.Database;
import org.makumba.db.makumba.sql.TableManager;
import org.makumba.providers.TransactionProvider;
import org.makumba.FieldDefinition;

/**
 * This servlet checks if a field is unique or not
 * 
 * @author Marius Andra
 * @version $Id: MakumbaResourceServlet.java,v 1.1 Sep 22, 2007 2:02:17 AM rudi Exp $
 */
public class UniquenessServlet extends HttpServlet {
    public static final String resourceDirectory = "makumbaResources";

    private static final long serialVersionUID = 1L;

    public static final String RESOURCE_PATH_JAVASCRIPT = "javaScript/";

    public static final SimpleDateFormat dfLastModified = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // get the writer
        PrintWriter writer = resp.getWriter();

        String value = req.getParameter("value");
        String tableName = req.getParameter("table");
        String fieldName = req.getParameter("field");

        // get the database
        TransactionProvider tp = TransactionProvider.getInstance();
        Database db = null;
        DBConnectionWrapper dbc = null;
        try {
            dbc = (DBConnectionWrapper) tp.getConnectionTo(tp.getDefaultDataSourceName());
            db = dbc.getHostDatabase();
            TableManager table = null;

            // check if the table exists
            try {
                table = (TableManager) db.getTable(tableName);
            } catch (org.makumba.DataDefinitionNotFoundError e) {
                writer.println("No such table!");
                db.close();
                return;
            }

            // check if the field exists
            FieldDefinition fd = table.getFieldDefinition(fieldName);
            if (fd == null) {
                writer.println("No such field!");
                db.close();
                return;
            }

            String OQL = "select 1 from " + tableName + " p where p." + fieldName + "=$1";
            // writer.println(OQL);

            Vector<Dictionary<String, Object>> v = new Vector<Dictionary<String, Object>>();

            // if it's an integer
            if (fd.isIntegerType()) {
                try {
                    Integer valueOf = Integer.valueOf(value);
                    v = dbc.executeQuery(OQL, valueOf);
                } catch (NumberFormatException e) {
                    // if it is not an integer, do nothing, we'll output "unique" later on
                }
            } else if (fd.isDateType()) { // if it's a date
                if (req.getParameter("year") != null && req.getParameter("month") != null
                        && req.getParameter("day") != null && req.getParameter("year").matches("/[0-9]+/")
                        && req.getParameter("month").matches("/[0-9]+/") && req.getParameter("day").matches("/[0-9]+/")) {
                    Calendar c = Calendar.getInstance();
                    c.clear();
                    c.set(Integer.valueOf(req.getParameter("year")), Integer.valueOf(req.getParameter("month")),
                        Integer.valueOf(req.getParameter("day")));
                    Date date = c.getTime();
                    v = dbc.executeQuery(OQL, date);
                } else {
                    writer.println("incorrect date");
                    db.close();
                    return;
                }
            }
            // if it's a string
            else {
                v = dbc.executeQuery(OQL, value);
            }

            if (v.size() > 0) {
                writer.print("not unique");
            } else {
                writer.print("unique");
            }
        } finally {
            if (dbc != null) {
                dbc.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

}
