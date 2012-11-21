package org.makumba;

import java.lang.reflect.Member;

public interface FieldMetadata {

    public FieldDefinition getFieldDefinition();

    public Member getField();

    public String getStringAspectValue(MetadataAspect a);

    public Integer getIntegerAspectValue(MetadataAspect a);

    public Boolean getBooleanAspectValue(MetadataAspect a);

    public Object getAspectValue(MetadataAspect a);

}
