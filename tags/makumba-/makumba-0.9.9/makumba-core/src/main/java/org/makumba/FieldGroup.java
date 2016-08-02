package org.makumba;

import java.util.Collection;

public interface FieldGroup {

    public FieldMetadata getField(String name);

    public Collection<FieldMetadata> getFields();

}
