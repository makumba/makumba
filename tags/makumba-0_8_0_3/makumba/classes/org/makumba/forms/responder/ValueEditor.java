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

import org.makumba.db.makumba.sql.TableManager;
import org.makumba.db.makumba.DBConnectionWrapper;
import org.makumba.providers.TransactionProvider;
import org.makumba.db.makumba.Database;
import org.makumba.FieldDefinition;
import org.makumba.Pointer;


/**
 * This servlet updates values in the database
 * 
 * @author Rudolf Mayer
 * @version $Id: MakumbaResourceServlet.java,v 1.1 Sep 22, 2007 2:02:17 AM rudi Exp $
 */
public class ValueEditor extends HttpServlet {
    public static final String resourceDirectory = "makumbaResources";

    private static final long serialVersionUID = 1L;

    public static final String RESOURCE_PATH_JAVASCRIPT = "javaScript/";

    public static final SimpleDateFormat dfLastModified = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");

    TransactionProvider tp = null;
    DBConnectionWrapper dbc = null;
    Database db = null;
    TableManager table = null;

    @Override
    public void init() throws ServletException {
        // get the database
        tp = TransactionProvider.getInstance();
        
        super.init();
    }
    
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // get the writer
        PrintWriter writer = resp.getWriter();
        dbc = (DBConnectionWrapper)tp.getConnectionTo(tp.getDefaultDataSourceName());
        db = dbc.getHostDatabase();
        
        resp.setContentType("text/plain");
        
        try
        {
            
            if(req.getParameter("table") == null || req.getParameter("field") == null || req.getParameter("value") == null)
            {
                writer.println("Tastes like chicken");
                return;
            }
            
            // check if the table exists
            try
            {
                table = (TableManager)db.getTable(req.getParameter("table"));
            } catch(org.makumba.DataDefinitionNotFoundError e) {
                writer.println("No such table!");
                return;
            }
            
            // check if the field exists
            FieldDefinition fd = table.getFieldDefinition(req.getParameter("field"));
            if(fd == null)
            {
                writer.println("No such field!");
                return;
            }
            
            Pointer ptr = new Pointer(req.getParameter("table"), req.getParameter("pointer"));
            //System.out.println("SELECT l FROM "+req.getParameter("table")+" l WHERE l=$1");
            Vector v = dbc.executeQuery("SELECT l FROM "+req.getParameter("table")+" l WHERE l=$1", ptr);
            
            if(v.size() < 1){
                writer.println("No such data");
            }
            
            Dictionary p = new Hashtable();
            
            // if it's an integer
            if(fd.isIntegerType())
            {
                p.put(req.getParameter("field"), Integer.valueOf(req.getParameter("value")));
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
                    p.put(req.getParameter("field"), date);
                }
                else
                {
                    writer.println("incorrect date");
                    return;
                }
            }
            // if it's a string
            else
            {
                p.put(req.getParameter("field"), req.getParameter("value"));
            }
    
            dbc.update(ptr, p);
            writer.println("success");
        }
        finally
        {
            dbc.close();
            db.close();
        }
    }
}
