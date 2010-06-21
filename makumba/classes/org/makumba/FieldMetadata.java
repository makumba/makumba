package org.makumba;

import java.lang.reflect.Member;

public interface FieldMetadata {

    public FieldDefinition getFieldDefinition();

    public Member getField();

    public String getStringAspect(MetadataAspect a);

    public Integer getIntegerAspect(MetadataAspect a);

    public Boolean getBooleanAspect(MetadataAspect a);

    public Object getAspect(MetadataAspect a);

}
