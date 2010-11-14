package org.makumba.providers.datadefinition;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.makumba.FieldMetadata;
import org.makumba.MetadataAspect;
import org.makumba.annotations.MessageType;
import org.makumba.providers.AnnotationMetadataReader;
import org.makumba.providers.bytecode.AbstractAnnotation;

/**
 * Helper class to read makumba annotations data
 * 
 * @author Manuel Bernhardt <manuel@makumba.org>
 * @version $Id: MakumbaAnnotationReader.java,v 1.1 Jun 23, 2010 12:26:54 PM manu Exp $
 */
public class MakumbaAnnotationReader {

    private static MakumbaAnnotationReader instance;

    public static MakumbaAnnotationReader getInstance() {
        if (instance == null) {
            instance = new MakumbaAnnotationReader();
        }
        return instance;
    }

    private MakumbaAnnotationReader() {

    }

    private AnnotationMetadataReader r = AnnotationMetadataReader.getInstance();

    public HashMap<MessageType, String> getMessages(FieldMetadata f) {
        HashMap<MessageType, String> result = new HashMap<MessageType, String>();
        Object v = r.readAspectValue(f.getField(), MetadataAspect.MESSAGES);
        AbstractAnnotation[] messages = (AbstractAnnotation[]) v;
        for (AbstractAnnotation m : messages) {
            result.put((MessageType) m.getAttribues().get("type"), (String) m.getAttribues().get("message"));
        }
        return result;
    }

    public LinkedHashMap<Integer, String> getEnumValues(FieldMetadata f) {
        return getValues(f, false);
    }

    public LinkedHashMap<Integer, String> getDeprecatedEnumValues(FieldMetadata f) {
        return getValues(f, true);
    }

    private LinkedHashMap<Integer, String> getValues(FieldMetadata f, boolean deprecated) {
        LinkedHashMap<Integer, String> result = new LinkedHashMap<Integer, String>();
        Object v = r.readAspectValue(f.getField(), MetadataAspect.MAKUMBA_INT_ENUM);
        AbstractAnnotation[] pairs = (AbstractAnnotation[]) v;
        for (AbstractAnnotation a : pairs) {
            // pair
            Integer key = (Integer) a.getAttribues().get("key");
            String value = (String) a.getAttribues().get("value");
            boolean d = (Boolean) a.getAttribues().get("deprecated");
            if (d && deprecated) {
                result.put(key, value);
            } else if (!d && !deprecated) {
                result.put(key, value);
            }
        }
        return result;
    }
}
