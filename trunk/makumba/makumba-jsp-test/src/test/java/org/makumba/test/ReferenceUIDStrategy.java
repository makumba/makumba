package org.makumba.test;

import org.makumba.MakumbaError;
import org.makumba.Pointer;
import org.makumba.UIDStrategy;

import java.util.Map;

/**
 * Pointer conversion strategy for test cases, that ensures that all the {@link Pointer#toExternalForm()} values remain
 * constant on consecutive runs. We compute an offset based on the difference between the maximum of the initial primary
 * key values (when inserted in an empty database) and the maximum of the current primary key values (growing with each
 * insert, due to database auto-increment).
 * 
 * @author manu
 */
public class ReferenceUIDStrategy implements UIDStrategy {

    private static Map<String, Long> references;

    private static Map<String, Long> initialReferences;

    public static void setInitialReferences(Map<String, Long> initial) {
        initialReferences = initial;
    }

    public static Map<String, Long> getInitialReferences() {
        return initialReferences;
    }

    public static void setReferences(Map<String, Long> r) {
        references = r;
    }

    public long readFrom(String type, long n) {
        if (!checkType(type)) {
            return n;
        }
        long offset = references.get(type) - initialReferences.get(type);
        return n + offset;
    }

    public long writeTo(String type, long n) {
        if (!checkType(type)) {
            return n;
        }
        try {
            long offset = references.get(type) - initialReferences.get(type);
            return n - offset;

        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
        return 0;
    }

    private boolean checkType(String type) throws MakumbaError {
        if (references == null) {
            return false;
        }
        if (!references.containsKey(type)) {
            throw new MakumbaError("No reference value provided for type " + type);
        }
        return true;
    }

}
