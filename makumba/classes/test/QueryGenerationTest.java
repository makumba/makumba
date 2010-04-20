package test;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.makumba.InvalidValueException;
import org.makumba.Transaction;
import org.makumba.providers.TransactionProvider;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit tests for the query generation process (from MQL to SQL, including parameter rewriting)
 * 
 * @author Manuel Gay
 * @version $Id: QueryGenerationTest.java,v 1.1 Apr 20, 2010 8:10:05 AM manu Exp $
 */
public class QueryGenerationTest extends TestCase {
    
    private static final class Suite extends MakumbaTestSetup {
        private Suite(Test test) {
            super(test, "oql");
        }
    }
    
    public static Test suite() {
        return new Suite(new TestSuite(QueryGenerationTest.class));
    }
    
    public void testParameterNameWithDot() throws Exception {
        
        String query = "SELECT p.indiv.name as name FROM test.Person p WHERE p.indiv.name = $name.with.dot";
        Map<String, Object> arguments = new HashMap<String, Object>();
        arguments.put("name.with.dot", "bart");
        Vector<Dictionary<String, Object>> result = new Vector<Dictionary<String,Object>>();
        
        Transaction t = TransactionProvider.getInstance().getConnectionTo(TransactionProvider.getInstance().getDefaultDataSourceName());
        try {
            result = t.executeQuery(query, arguments);
        } catch(Exception e) {
            fail(e.getMessage());
        } finally {
            t.close();
        }
        
        assertEquals(1, result.size());
        Dictionary<String, Object> line = result.get(0);
        Object name = line.get("name");
        assertNotNull(name);
        assertEquals("bart", (String)name);
    }
    
    public void testVectorParameterError() {
        String query = "SELECT p.indiv.name as name FROM test.Person p WHERE p.indiv.name = $simulatedVector";
        Map<String, Object> arguments = new HashMap<String, Object>();
        Vector<String> v = new Vector<String>();
        v.add("bart");
        v.add("bart");
        arguments.put("simulatedVector", v);
        Vector<Dictionary<String, Object>> result = new Vector<Dictionary<String,Object>>();
        
        Transaction t = TransactionProvider.getInstance().getConnectionTo(TransactionProvider.getInstance().getDefaultDataSourceName());
        try {
            result = t.executeQuery(query, arguments);
        } catch(Exception e) {
            assertTrue(e instanceof InvalidValueException);
        } finally {
            t.close();
        }
        
    }

}
