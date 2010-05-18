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

import org.apache.commons.lang.StringUtils;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.Transaction;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.TransactionProvider;

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

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("application/json");

        // get the writer
        PrintWriter writer = resp.getWriter();

        String value = req.getParameter("value");
        String tableName = req.getParameter("table");
        String fieldName = req.getParameter("field");
        if (StringUtils.isBlank(value) || StringUtils.isBlank(tableName) || StringUtils.isBlank(fieldName)) {
            writer.println("{error: \"All 'value', 'table' and 'field' parameters need to be not-empty!\"}");
            return;
        }

        Transaction transaction = null;
        try {
            DataDefinition dd;
            transaction = TransactionProvider.getInstance().getConnectionTo(
                TransactionProvider.getInstance().getDefaultDataSourceName());
            // check if the table exists
            try {
                dd = DataDefinitionProvider.getInstance().getDataDefinition(tableName);
                if (dd == null) {
                    writer.println("{error: \"No such table!\"}");
                    return;
                }
            } catch (Throwable e) {
                writer.println("{error: \"No such table!\"}");
                return;
            }

            // check if the field exists
            FieldDefinition fd = dd.getFieldDefinition(fieldName);
            if (fd == null) {
                writer.println("{error: \"No such field!\"}");
                return;
            }

            String OQL = "select 1 from " + tableName + " p where p." + fieldName + "=$1";

            Vector<Dictionary<String, Object>> v = new Vector<Dictionary<String, Object>>();

            // if it's an integer
            if (fd.isIntegerType()) {
                try {
                    Integer valueOf = Integer.valueOf(value);
                    v = transaction.executeQuery(OQL, valueOf);
                } catch (NumberFormatException e) {
                    // if it is not an integer, do nothing, we'll output "unique" later on
                }
            } else if (fd.isDateType()) { // if it's a date
                // FIXME: maybe we can use makumba attributes for getting the date value
                if (req.getParameter("year") != null && req.getParameter("month") != null
                        && req.getParameter("day") != null && req.getParameter("year").matches("/[0-9]+/")
                        && req.getParameter("month").matches("/[0-9]+/") && req.getParameter("day").matches("/[0-9]+/")) {
                    Calendar c = Calendar.getInstance();
                    c.clear();
                    c.set(Integer.valueOf(req.getParameter("year")), Integer.valueOf(req.getParameter("month")),
                        Integer.valueOf(req.getParameter("day")));
                    Date date = c.getTime();
                    v = transaction.executeQuery(OQL, date);
                } else {
                    writer.println("{error: \"incorrect date\"}");
                    return;
                }
            }
            // if it's a string
            else {
                v = transaction.executeQuery(OQL, value);
            }

            if (v.size() > 0) {
                writer.print("{success: \"not unique\"}");
            } else {
                writer.print("{success: \"unique\"}");
            }
        } finally {
            if (transaction != null) {
                transaction.close();
            }
        }
        writer.flush();
        writer.close();
    }

}
