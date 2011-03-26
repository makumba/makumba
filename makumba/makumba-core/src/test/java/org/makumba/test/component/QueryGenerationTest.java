package org.makumba.test.component;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.makumba.InvalidValueException;
import org.makumba.Transaction;
import org.makumba.providers.TransactionProvider;
import org.makumba.test.util.MakumbaTestSetup;

/**
 * Unit tests for the query generation process (from MQL to SQL, including parameter rewriting)
 * 
 * @author Manuel Bernhardt <manuel@makumba.org>
 * @version $Id: QueryGenerationTest.java,v 1.1 Apr 20, 2010 8:10:05 AM manu Exp $
 */
public class QueryGenerationTest extends TestCase {

    public static Test suite() {
        return new MakumbaTestSetup(new TestSuite(QueryGenerationTest.class), "oql");
    }

    /*
     * bug 1187 seems to be invalid, so the method is renamed so it won't be ran by junit, to ignore this test
     */
    public void invalidtestParameterNameWithDot() throws Exception {

        String query = "SELECT p.indiv.name as name FROM test.Person p WHERE p.indiv.name = $name.with.dot";
        Map<String, Object> arguments = new HashMap<String, Object>();
        arguments.put("name.with.dot", "bart");
        Vector<Dictionary<String, Object>> result = new Vector<Dictionary<String, Object>>();

        Transaction t = TransactionProvider.getInstance().getConnectionTo(
            TransactionProvider.getInstance().getDefaultDataSourceName());
        try {
            result = t.executeQuery(query, arguments);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            t.close();
        }

        assertEquals(1, result.size());
        Dictionary<String, Object> line = result.get(0);
        Object name = line.get("name");
        assertNotNull(name);
        assertEquals("bart", (String) name);
    }

    public void testVectorParameterError() {
        String query = "SELECT p.indiv.name as name FROM test.Person p WHERE p.indiv.name = $simulatedVector";
        Map<String, Object> arguments = new HashMap<String, Object>();
        Vector<String> v = new Vector<String>();
        v.add("bart");
        v.add("bart");
        arguments.put("simulatedVector", v);
        new Vector<Dictionary<String, Object>>();

        Transaction t = TransactionProvider.getInstance().getConnectionTo(
            TransactionProvider.getInstance().getDefaultDataSourceName());
        try {
            t.executeQuery(query, arguments);
        } catch (Exception e) {
            assertTrue(e instanceof InvalidValueException);
        } finally {
            t.close();
        }

    }

    public void testBatchQueries() throws IOException, URISyntaxException {
        String baseDir = "queries/";
        java.net.URL u = org.makumba.commons.ClassResource.get(baseDir);
        if (u != null) {
            java.io.File dir = new File(u.toURI());
            if (dir.isDirectory()) {
                String[] list = dir.list();
                for (String filename : list) {
                    if (filename.endsWith(".txt")) {
                        InputStream stream = this.getClass().getClassLoader().getResourceAsStream(baseDir + filename);

                        DataInputStream in = new DataInputStream(stream);
                        BufferedReader br = new BufferedReader(new InputStreamReader(in));
                        String query;
                        Transaction t = TransactionProvider.getInstance().getConnectionTo(
                            TransactionProvider.getInstance().getDefaultDataSourceName());
                        int line = 0;
                        try {
                            while ((query = br.readLine()) != null) {
                                line++;
                                if (!query.startsWith("#") && !query.trim().isEmpty()) {
                                    try {
                                        t.executeQuery(query, null);
                                        line++;
                                    } catch (Exception e) {
                                        fail("Error in '" + filename + "' on query at line " + line + ": " + query
                                                + "\nCause: " + e.getMessage());
                                    }
                                }
                            }
                        } finally {
                            t.close();
                        }
                    }

                }
            }
        }

    }

}
