package org.makumba;

public interface TypeMetadata {

    public DataDefinition getDataDefinition();

    public Class<?> getClazz();

    public Object getAspect(MetadataAspect a);

}
