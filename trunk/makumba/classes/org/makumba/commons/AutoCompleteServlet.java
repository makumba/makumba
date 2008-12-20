package org.makumba.commons;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Dictionary;
import java.util.Iterator;
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
 * This servlet figures out a list of possible values given a beginning string on a field of a given type.
 * 
 * Results are returned as described at http://github.com/madrobby/scriptaculous/wikis/ajax-autocompleter
 * 
 * TODO: this should work with the currently used query language
 * 
 * @author Manuel Gay
 * @version $Id: MakumbaResourceServlet.java,v 1.1 Sep 22, 2007 2:02:17 AM rudi Exp $
 */
public class AutoCompleteServlet extends HttpServlet {
    public static final String resourceDirectory = "makumbaResources";

    private static final long serialVersionUID = 1L;

    public static final String RESOURCE_PATH_JAVASCRIPT = "javaScript/";

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
        resp.setContentType("application/json");
        
        // get the writer
        PrintWriter writer = resp.getWriter();

        String value = req.getParameter("value");
        String typeName = req.getParameter("type");
        String fieldName = req.getParameter("field");
        if (StringUtils.isBlank(typeName) || StringUtils.isBlank(fieldName)) {
            writer.println("{error: \"All 'type' and 'field' parameters need to be not-empty!\"}");
            return;
        }

        Transaction transaction = null;
        try {
            DataDefinition dd;
            transaction = TransactionProvider.getInstance().getConnectionTo(
                TransactionProvider.getInstance().getDefaultDataSourceName());
            // check if the table exists
            try {
                dd = DataDefinitionProvider.getInstance().getDataDefinition(typeName);
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

            String OQL = "select p."+fieldName+" as possibility from " + typeName + " p where p." + fieldName + " like '"+value+"%' group by p."+fieldName;

            Vector<Dictionary<String, Object>> v = new Vector<Dictionary<String, Object>>();
            v = transaction.executeQuery(OQL, value);
            
            if (v.size() > 0) {
                String result = "<ul>";
                for (Iterator iterator = v.iterator(); iterator.hasNext();) {
                    Dictionary<String, Object> dictionary = (Dictionary<String, Object>) iterator.next();
                    String possibility = (String)dictionary.get("possibility");
                    result += "<li>"+possibility+"</li>";
                }        
                result +="</ul>";
                
                writer.print(result);
                
            } else {
                // we return an empty list
                writer.print("<ul></ul>");
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
