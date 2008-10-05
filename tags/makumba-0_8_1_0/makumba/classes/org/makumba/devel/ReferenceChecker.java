package org.makumba.devel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Stack;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaSystem;
import org.makumba.commons.SQLPointer;
import org.makumba.db.makumba.DBConnection;
import org.makumba.db.makumba.DBConnectionWrapper;
import org.makumba.db.makumba.Database;
import org.makumba.db.makumba.MakumbaTransactionProvider;
import org.makumba.db.makumba.sql.SQLDBConnection;
import org.makumba.db.makumba.sql.TableManager;
import org.makumba.providers.Configuration;
import org.makumba.providers.DataDefinitionProvider;

/**
 * Developer support servlet that checks for the existence of broken references (foreign keys) on the database.
 * 
 * @author Rudolf Mayer
 * @version $Id: ReferenceChecker.java,v 1.1 12.10.2007 05:17:31 Rudolf Mayer Exp $
 */
public class ReferenceChecker extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private String dbName = MakumbaSystem.getDefaultDatabaseName();

    SQLDBConnection sqlConnection;

    Database sqlDb = MakumbaTransactionProvider.getDatabase(dbName);

    public ReferenceChecker() {
        DBConnection connection = sqlDb.getDBConnection();
        if (connection instanceof DBConnectionWrapper) {
            connection = ((DBConnectionWrapper) connection).getWrapped();
        }
        sqlConnection = ((SQLDBConnection) connection);
    }

    private int count(DataDefinition mdd) {
        TableManager table = getSqlTable(mdd);
        String query = "SELECT COUNT(*) FROM " + table.getDBName();
        return executeIntQuery(query);
    }

    private int countMissing(DataDefinition ddParent, DataDefinition ddChild, FieldDefinition fdChild) {
        String query = getQueryString(ddParent, ddChild, fdChild, true);
        return executeIntQuery(query);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String contextPath = req.getContextPath();
        resp.setContentType("text/html");
        PrintWriter w = resp.getWriter();
        DevelUtils.writePageBegin(w);
        DevelUtils.writeStylesAndScripts(w, contextPath);

        String param = req.getParameter("mdd");
        if (param != null) { // check a specific MDD
            String field = req.getParameter("field");
            printBrokenRefsInTable(contextPath, w, param, field);
        } else {
            printAllBrokenRefs(contextPath, w);
        }
        DevelUtils.writePageEnd(w);
    }

    private int executeIntQuery(String query) {
        PreparedStatement ps = sqlConnection.getPreparedStatement(query);
        try {
            ResultSet result = ps.executeQuery();
            try {
                result.next();
                return result.getInt(1);
            } finally {
                result.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                ps.close();
            } catch (SQLException e) {
                throw new org.makumba.DBError(e);
            }
        }
        return -1;
    }

    private ResultSet executeQuery(String query) {
        PreparedStatement ps = sqlConnection.getPreparedStatement(query);
        // FIXME: this PreparedStatement is never closed, and it probably cannot be closed before closing the result set
        // which we return....
        try {
            return ps.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getQueryString(DataDefinition ddParent, DataDefinition ddChild, FieldDefinition fdChild,
            boolean countOnly) {
        TableManager parentTable = getSqlTable(ddParent);
        TableManager childTable = getSqlTable(ddChild);

        String childField = "child." + childTable.getFieldDBName(fdChild.getName());
        String childNameField = "child." + childTable.getFieldDBName(ddChild.getTitleFieldName());
        String childPtr = "child." + childTable.getFieldDBName(fdChild.getDataDefinition().getIndexPointerFieldName());
        String parentPtr = "parent." + parentTable.getFieldDBName(ddParent.getIndexPointerFieldName());

        String query = "SELECT ";
        if (countOnly) {
            query += "COUNT(*)";
        } else {
            query += childNameField + " AS titleField, " + childField + " AS brokenRef, " + childPtr + " AS ptr";
        }
        query += " FROM ";
        query += childTable.getDBName() + " child WHERE " + childField + " is not null AND " + childField
                + " NOT IN (SELECT " + parentPtr + " FROM " + parentTable.getDBName() + " parent)";
        return query;
    }

    private String getRefText(int countMissing, DataDefinition reference, DataDefinition base, FieldDefinition f) {
        String s = ": <b style=\"" + (countMissing > 0 ? "color: red" : "") + "\">" + countMissing
                + " invalid references";
        if (reference != null) {
            s += " to " + reference.getName();
        }
        s += "</b>";
        if (countMissing > 0) {
            s += " <a href=\"referenceChecker?mdd=" + base.getName() + "&field=" + f.getName() + "\">[list]</a>";
        }
        return s;
    }

    private void printAllBrokenRefs(String contextPath, PrintWriter w) throws IOException {
        String title = "Broken references in " + MakumbaSystem.getDefaultDatabaseName();
        DevelUtils.writeTitleAndHeaderEnd(w, title);
        DevelUtils.printPageHeader(w, title);
        writeHeader(w, contextPath);
        Vector<String> mdds = MakumbaSystem.mddsInDirectory("dataDefinitions");
        Vector<String> clean = (Vector<String>) mdds.clone();
        for (int i = 0; i < mdds.size(); i++) {
            String element = (String) mdds.get(i);
            if (element.contains("broken") || element.contains("dataDefinitions")) {
                clean.remove(element);
            }
        }
        mdds = clean;
        Collections.sort(mdds);
        w.println("<div style=\"float:right; border: 1px solid #000; margin: 0px 0px 20px 20px; padding: 5px; background: #ddd;\">");
        for (Enumeration<String> mddse = mdds.elements(); mddse.hasMoreElements();) {
            String mddName = (String) mddse.nextElement();
            w.println("<a href=\"#" + mddName + "\">" + mddName + "</a><br/>");
        }
        w.println("</div>");

        Collections.sort(mdds, new Comparator<String>() {
            public int compare(String o1, String o2) {
                return o2.compareTo(o1);
            }
        });
        Stack<String> stack = new Stack<String>();
        stack.addAll(mdds);
        while (!stack.isEmpty()) {
            String mddName = stack.pop();

            try {
                DataDefinition dd = MakumbaSystem.getDataDefinition(mddName);
                w.println("<h3><a name=\"" + mddName + "\" href=\"" + contextPath + "/dataDefinitions/" + mddName
                        + "\">" + mddName + "</a> (" + count(dd) + ")</h3>");
                for (Enumeration<FieldDefinition> fnse = dd.getReferenceFields().elements(); fnse.hasMoreElements();) {
                    FieldDefinition f = (FieldDefinition) fnse.nextElement();
                    String ft = f.getType();
                    w.println(f.getName() + " = " + ft);
                    if (f.isPointer()) {
                        DataDefinition pointerDd = f.getPointedType();
                        String pointerDdName = pointerDd.getName();
                        w.println("&rarr; " + pointerDdName + "(" + count(pointerDd) + ")");
                        String query = getQueryString(pointerDd, dd, f, true);
                        printHiddenQuery(w, getDivID(mddName, f), query);
                        w.println(printDetails(executeIntQuery(query), dd, f));
                        printForeignKey(w, dd, f);
                    }
                    if (f.isExternalSet()) {
                        DataDefinition pointerDd = f.getPointedType();
                        String pointerDdName = pointerDd.getName();
                        DataDefinition setDd = f.getSubtable();
                        w.println(" &larr;[" + setDd + " (" + count(setDd) + ")]&rarr " + pointerDdName + " ("
                                + count(pointerDd) + ")");
                        FieldDefinition backPtr = setDd.getFieldDefinition(dd.getIndexPointerFieldName());
                        String query = getQueryString(dd, setDd, backPtr, true);
                        printHiddenQuery(w, getDivID(mddName, backPtr), query);
                        w.println(printDetails(countMissing(dd, setDd, backPtr), dd, f));
                        printForeignKey(w, setDd, backPtr);
                        FieldDefinition backPtr2 = setDd.getFieldDefinition(pointerDd.getIndexPointerFieldName());
                        query = getQueryString(pointerDd, setDd, backPtr2, true);
                        printHiddenQuery(w, getDivID(mddName, backPtr2), query);
                        w.println(printDetails(countMissing(pointerDd, setDd, backPtr2), pointerDd, f));
                        printForeignKey(w, setDd, backPtr2);
                    }
                    if (f.isComplexSet()) {
                        DataDefinition setDd = f.getSubtable();
                        w.println(" &larr;[" + setDd + " (" + count(setDd) + ")]");
                        FieldDefinition backPtr = setDd.getFieldDefinition(dd.getIndexPointerFieldName());
                        String query = getQueryString(dd, setDd, backPtr, true);
                        printHiddenQuery(w, getDivID(mddName, backPtr), query);
                        w.println(printDetails(countMissing(dd, setDd, backPtr), dd, f));
                        printForeignKey(w, setDd, backPtr);
                        stack.add(setDd.getName());
                    }
                    if (fnse.hasMoreElements()) {
                        w.println("<br>");
                    }

                }

            } catch (Exception ex) {
                w.println(" <font color=\"red\">" + ex + "</font></b>  ");
            }
        }
    }

    private String getDivID(String mddName, FieldDefinition f) {
        return (mddName + f.getName()).replace('.', '_').replaceAll("->", "__");
    }

    private void printHiddenQuery(PrintWriter w, String idName, String query) {
        w.println("<a id=\"" + idName + "Ref\" href=\"javascript:toggleSQLDisplay(" + idName + ", " + idName
                + "Ref)\">[+]</a>");
        w.println("<div id=\"" + idName + "\" style=\"display:none;\">" + query + "</div> ");
    }

    private void printForeignKey(PrintWriter w, DataDefinition dd, FieldDefinition f) {
        if (getSqlTable(dd).hasForeignKey(f.getName())) {
            w.println("<span style=\"font-weight: bold; color:green\">[Valid foreign key]</span>");
        } else {
            w.println("<span style=\"font-weight: bold; color:red\">[NO foreign key!]</span>");
        }
    }

    private TableManager getSqlTable(DataDefinition dd) {
        return (TableManager) sqlDb.getTable(dd);
    }

    private void printBrokenRefsInTable(String contextPath, PrintWriter w, String param, String field)
            throws IOException {
        DataDefinition dd = DataDefinitionProvider.getInstance().getDataDefinition(param);
        FieldDefinition fd = dd.getFieldDefinition(field);
        String query = getQueryString(fd.getPointedType(), dd, fd, false);

        String title = "Broken references in " + dd.getName() + "#" + fd.getName();
        DevelUtils.writeTitleAndHeaderEnd(w, title);
        DevelUtils.printPageHeader(w, title);
        writeHeader(w, contextPath);
        w.println("<h3>Type: " + dd.getName() + "</h3>");
        w.println("<h3>Field: " + fd.getName() + "</h3>");

        w.println("Query: " + query);

        ResultSet rs = executeQuery(query);

        w.println("<br><br>");
        w.println("<table>");
        w.println("<tr> <th>#</th> <th>Pointer</th>  <th>Title field: " + dd.getTitleFieldName()
                + "</th> <th title=\"field " + fd.getName() + "\">Broken ref in " + fd.getName() + "</th> </tr>");
        try {
            for (int i = 0; rs != null && rs.next(); i++) {
                int ptrInt = rs.getInt("ptr");
                int brokenRef = rs.getInt("brokenRef");
                Object titleField = rs.getObject("titleField");
                SQLPointer ptr = new SQLPointer(dd.getName(), new Integer(ptrInt));
                SQLPointer brokenRefPtr = new SQLPointer(fd.getPointedType().getName(), new Integer(brokenRef));
                w.println("<tr class=\"" + (i % 2 == 0 ? "even" : "odd") + "\">");
                w.println("<td>" + (i + 1) + "</td>");
                w.println("<td>" + DevelUtils.writePointerValueLink(contextPath, ptr) + "</td>");
                w.println("<td>" + titleField + "</td>");
                w.println("<td>" + DevelUtils.writePointerValueLink(contextPath, brokenRefPtr) + "</td>");
                w.println("</tr>");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            e.printStackTrace(w);
        }

        w.println("</table>");
    }

    private Object printDetails(int countMissing, DataDefinition base, FieldDefinition f) {
        return getRefText(countMissing, null, base, f);
    }

    private void writeHeader(PrintWriter w, String contextPath) {
        w.println("<div>Checking " + MakumbaSystem.getDefaultDatabaseName()
                + " <span style=\"font-size: small\">using Makumba version " + MakumbaSystem.getVersion()
                + "</span></div>");
        w.println("</td>");

        w.println("<td align=\"right\">");
        DevelUtils.writeDevelUtilLinks(w, Configuration.KEY_REFERENCE_CHECKER, contextPath);
        w.println("</td>");
        
        w.println("</tr>");
        w.println("</table>");
    }
}
