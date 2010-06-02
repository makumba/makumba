package org.makumba;

import java.util.Iterator;

public interface FieldGroup {

    public FieldMetadata getField(String name);

    public Iterator<FieldMetadata> getFields();

}
