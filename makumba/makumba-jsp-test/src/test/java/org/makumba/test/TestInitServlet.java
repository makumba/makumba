package org.makumba.test.util;

import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.commons.NameResolver;
import org.makumba.commons.NamedResources;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.TransactionProvider;

/**
 * This servlet manages the state of the server when running client-side tests (JSP test suites). It mostly does two
 * things:
 * <ul>
 * <li>cleaning all static caches. That's necessary because some values like e.g. Pointers are not evaluated anymore,
 * yet their database state changed. Since we do a diff-based test we want our pointer values to stay constant along
 * consecutive test runs. In real cases, you wouldn't do a comparison agains a static 'externalForm' pointer that
 * changes in the database, nor would you expect your pointer values to stay constant if the database PK value changes.</li>
 * <li>collecting the current maximum value of each table PK, so that we can use a {@link ReferenceUIDStrategy} in order
 * to force constant values along the test runs</li>
 * </ul>
 * <br />
 * All in all this is quite a hack but it allows us to run both MQL and HQL tests with auto-increment consecutively with
 * the current diff-based test mechanism.
 * 
 * @author manu
 * @version $Id: TestInitServlet.java,v 1.1 Sep 1, 2010 6:01:18 PM manu Exp $
 */
public class TestInitServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final static String[] testTypes = new String[] { "test.Person", "test.Language", "test.Individual" };

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // clean all the bloody caches that hold state, which is toxic for consecutive runs
        // in our case, some Pointer-s are not re-computed, which is perfectly fine in the real world where the data in
        // the db does not change. only here it does (the PK values change on auto-increment)
        NamedResources.cleanupStaticCaches();

        Transaction db = TransactionProvider.getInstance().getConnectionToDefault();
        boolean hql = TransactionProvider.getInstance().getQueryLanguage().equals("hql");

        // initialise maximum PK values to allow repeatable execution of diff-based tests

        Map<String, Long> references = new HashMap<String, Long>();
        Map<String, String> types = new HashMap<String, String>();
        for (String type : testTypes) {
            DataDefinition t = DataDefinitionProvider.getInstance().getDataDefinition(type);
            String primaryKey = t.getFieldDefinition(0).getName();
            types.put(t.getName(), primaryKey);
            findRelated(t, types);
        }

        // for each type, query the max of its PK value
        for (String key : types.keySet()) {
            String maxQuery = null;
            if (hql && key.contains("->")) {
                int i = key.indexOf("->");
                maxQuery = "SELECT max(st.id) as maximum FROM " + key.substring(0, i) + " t, t." + key.substring(i + 2)
                        + " st";
            } else if (hql) {
                maxQuery = "SELECT max(t.id) as maximum FROM " + key + " t";
            } else {
                maxQuery = "SELECT max(t." + types.get(key) + ") as maximum FROM " + key + " t";
            }
            Vector<Dictionary<String, Object>> res = db.executeQuery(maxQuery, new Object[] {});
            Object max = res.get(0).get("maximum");

            if (max == null) {
                references.put(key, 0l);
                references.put(NameResolver.arrowToDoubleUnderscore(key), 0l);
            } else {
                // hibernate-db sometimes returns an Integer, sometimes a Pointer...the DB layer clearly needs some
                // improvement
                if (max instanceof Integer) {
                    references.put(key, ((Integer) max).longValue());
                    references.put(NameResolver.arrowToDoubleUnderscore(key), ((Integer) max).longValue());
                } else if (max instanceof Pointer) {
                    references.put(key, ((Pointer) max).longValue());
                    references.put(NameResolver.arrowToDoubleUnderscore(key), ((Pointer) max).longValue());
                }
            }
        }

        // uncomment this if you need to retrieve the initial pointer reference values (on an empty db)
        /*
        for (String r : references.keySet()) {
            System.out.println(r + " " + references.get(r));
        }
        */

        // hibernate-db sometimes asks for the key with pointer, sometimes with double underscore, so we give it both...
        Map<String, Long> initial = new HashMap<String, Long>();
        initial.put("test.Individual", 2l);
        initial.put("test.Language", 5l);
        initial.put("test.Person", 2l);
        initial.put("test.Person->toys", 2l);
        initial.put("test.Person->address", 1l);
        initial.put("test.Person->intSet", 2l);
        initial.put("test.Person->charSet", 0l);
        initial.put(NameResolver.arrowToDoubleUnderscore("test.Person->toys"), 2l);
        initial.put(NameResolver.arrowToDoubleUnderscore("test.Person->address"), 1l);
        initial.put(NameResolver.arrowToDoubleUnderscore("test.Person->intSet"), 2l);
        initial.put(NameResolver.arrowToDoubleUnderscore("test.Person->charSet"), 0l);

        ReferenceUIDStrategy.setInitialReferences(initial);

        ReferenceUIDStrategy.setReferences(references);

        db.close();

        resp.setStatus(HttpStatus.SC_OK);

    }

    private void findRelated(DataDefinition type, Map<String, String> types) {
        for (String field : type.getFieldNames()) {
            FieldDefinition fieldDefinition = type.getFieldDefinition(field);
            if (fieldDefinition.isSetType() || fieldDefinition.isPointer()
                    || fieldDefinition.getType().equals(FieldDefinition._ptrOne)) {
                DataDefinition pointedType = fieldDefinition.getPointedType();
                if (!types.containsKey(pointedType.getName())) {
                    types.put(pointedType.getName(), pointedType.getFieldDefinition(0).getName());
                    findRelated(pointedType, types);
                }
            }
        }

    }

}
