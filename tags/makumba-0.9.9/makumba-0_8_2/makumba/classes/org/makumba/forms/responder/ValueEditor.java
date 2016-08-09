package org.makumba.forms.responder;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.TransactionProvider;

/**
 * This servlet updates values in the database
 * 
 * @author Manuel Gay
 * @version $Id: MakumbaResourceServlet.java,v 1.1 Sep 22, 2007 2:02:17 AM rudi Exp $
 */
public class ValueEditor extends HttpServlet {
    public static final String resourceDirectory = "makumbaResources";

    private static final long serialVersionUID = 1L;

    public static final String RESOURCE_PATH_JAVASCRIPT = "javaScript/";

    public static final SimpleDateFormat dfLastModified = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // get the writer
        PrintWriter writer = resp.getWriter();

        resp.setContentType("text/plain");

        // FIXME: a lot of code is similar to UniquenessServlet, and should be unified

        String value = req.getParameter("value");
        String tableName = req.getParameter("table");
        String fieldName = req.getParameter("field");
        String pointer = req.getParameter("pointer");
        if (StringUtils.isBlank(value) || StringUtils.isBlank(tableName) || StringUtils.isBlank(fieldName)
                || StringUtils.isBlank(pointer)) {
            writer.println("All 'value', 'table', 'pointer' and 'field' parameters need to be not-empty!");
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
                    writer.println("No such table!");
                    return;
                }
            } catch (Throwable e) {
                writer.println("No such table!");
                return;
            }

            // check if the field exists
            FieldDefinition fd = dd.getFieldDefinition(fieldName);
            if (fd == null) {
                writer.println("No such field!");
                return;
            }

            // check if the pointer exists
            Pointer ptr = new Pointer(tableName, pointer);
            Vector<Dictionary<String, Object>> v = transaction.executeQuery("SELECT l FROM " + tableName + " l WHERE l=$1", ptr);
            if (v.size() < 1) {
                writer.println("No such data");
            }

            Hashtable<String, Object> p = new Hashtable<String, Object>();

            // if it's an integer
            if (fd.isIntegerType()) {
                p.put(fieldName, Integer.valueOf(value));
            }
            // if it's a date
            else if (fd.isDateType()) {
                if (req.getParameter("year") != null && req.getParameter("month") != null
                        && req.getParameter("day") != null && req.getParameter("year").matches("/[0-9]+/")
                        && req.getParameter("month").matches("/[0-9]+/") && req.getParameter("day").matches("/[0-9]+/")) {
                    Calendar c = Calendar.getInstance();
                    c.clear();
                    c.set(Integer.valueOf(req.getParameter("year")), Integer.valueOf(req.getParameter("month")),
                        Integer.valueOf(req.getParameter("day")));
                    Date date = c.getTime();
                    p.put(fieldName, date);
                } else {
                    writer.println("incorrect date");
                    return;
                }
            }
            // if it's a string
            else {
                p.put(fieldName, value);
            }

            transaction.update(ptr, p);
            writer.println("success");
        } finally {
            if (transaction != null) {
                transaction.close();
            }
        }
    }
}
