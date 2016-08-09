package org.makumba.providers.datadefinition;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.makumba.FieldGroup;
import org.makumba.FieldMetadata;

public class FieldGroupImpl implements FieldGroup {

    public Map<String, FieldMetadata> fields = Collections.synchronizedMap(new LinkedHashMap<String, FieldMetadata>());

    public FieldMetadata getField(String name) {
        return this.fields.get(name);
    }

    public Collection<FieldMetadata> getFields() {
        return fields.values();
    }

    public FieldGroupImpl() {

    }

    public FieldGroupImpl(LinkedHashMap<String, FieldMetadata> fields) {
        this.fields = fields;
    }

    public static void main(String... args) {
        FieldGroupImpl n = new FieldGroupImpl();
        n.fields.put("one", new FieldMetadataImpl("test.Person", "firstSex"));
        n.fields.put("two", new FieldMetadataImpl("test.Person", "gender"));
        n.fields.put("three", new FieldMetadataImpl("test.Person", "field"));
        for (FieldMetadata f : n.getFields()) {
            System.out.println(f.getFieldDefinition().getName());
        }
    }
}
