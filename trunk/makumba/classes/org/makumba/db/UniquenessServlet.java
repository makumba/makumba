package org.makumba.db;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.makumba.commons.Configuration;
import org.makumba.db.Database;
import org.makumba.db.sql.TableManager;
import org.makumba.providers.TransactionProvider;
import org.makumba.FieldDefinition;


/**
 * This servlet provides resources needed by makumba, e.g. JavaScript for the date editor {@link KruseCalendarEditor}
 * and live validation {@link LiveValidationProvider}.
 * 
 * @author Rudolf Mayer
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
        
        // get the database
        TransactionProvider tp = new TransactionProvider(new Configuration());
        DBConnectionWrapper dbc = (DBConnectionWrapper)tp.getConnectionTo(tp.getDefaultDataSourceName());
        Database db = dbc.getHostDatabase();
        TableManager table = null;
        
        // check if the table exists
        try
        {
            table = (TableManager)db.getTable(req.getParameter("table"));
        } catch(org.makumba.DataDefinitionNotFoundError e) {
            writer.println("No such table!");
            db.close();
            return;
        }
        
        // check if the field exists
        FieldDefinition fd = table.getFieldDefinition(req.getParameter("field"));
        if(fd == null)
        {
            writer.println("No such field!");
            db.close();
            return;
        }
        
        String OQL = "select 1 from "+req.getParameter("table")+" p where p."+req.getParameter("field")+"=$1";
        //writer.println(OQL);
        
        Vector v = new Vector();
        
        // if it's an integer
        if(fd.isIntegerType())
        {
            v = dbc.executeQuery(OQL, Integer.valueOf(req.getParameter("value")));                
        }
        // if it's a date
        else if(fd.isDateType())
        {
            if(req.getParameter("year") != null && req.getParameter("month") != null && req.getParameter("day") != null
                      && req.getParameter("year").matches("/[0-9]+/")
                      && req.getParameter("month").matches("/[0-9]+/")
                      && req.getParameter("day").matches("/[0-9]+/"))
            {
                Calendar c = Calendar.getInstance();
                c.clear();
                c.set(Integer.valueOf(req.getParameter("year")), Integer.valueOf(req.getParameter("month")), Integer.valueOf(req.getParameter("day")));
                Date date = c.getTime();
                v = dbc.executeQuery(OQL, date);
            }
            else
            {
                writer.println("incorrect date");
                db.close();
                return;
            }
        }
        // if it's a string
        else
        {
            v = dbc.executeQuery(OQL, req.getParameter("value"));
        }
        
        if(v.size() > 0) 
        {
            writer.print("not unique");
        }
        else
        {
            writer.print("unique");
        }
        db.close();
    }

}
