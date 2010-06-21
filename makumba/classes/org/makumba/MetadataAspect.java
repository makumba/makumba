package org.makumba;

import java.lang.annotation.Annotation;

import javax.persistence.Column;

import org.makumba.annotations.E;
import org.makumba.annotations.MakumbaEnum;
import org.makumba.providers.MetadataAspectReader;

/**
 * Various meta-data aspects provided through annotations can be registered here.<br>
 * The idea is that each kind of annotation values can be retrieved using the same mechanism, by calling methods of the
 * {@link MetadataAspectReader}.
 * 
 * @author Manuel Gay
 * @version $Id: MetadataAspect.java,v 1.1 Jun 21, 2010 6:42:30 PM manu Exp $
 */
public enum MetadataAspect {

    // field uniqueness
    UNIQUE(Column.class, AspectType.SIMPLE, "unique", null),

    // the makumba enum values
    MAKUMBA_INT_ENUM(MakumbaEnum.class, AspectType.ANNOTATION_ARRAY, "value", E.class);

    public enum AspectType {
        SIMPLE, ARRAY, ANNOTATION_ARRAY
    }

    private AspectType type;

    private Class<? extends Annotation> annotationClass;

    private String attributeName;

    private Class<? extends Annotation> nestedAnnotationClass;

    /**
     * An meta-data aspect, making it possible to read a specific annotation-based aspect
     * 
     * @param annotationClass
     *            the qualifier class for the aspect
     * @param type
     *            the {@link AspectType} which can be either a simple property, an array of values or a map (i.e. an
     *            array of annotations)
     * @param attributeName
     *            the name of the annotation attribute to read from
     */
    private MetadataAspect(Class<? extends Annotation> annotationClass, AspectType type, String attributeName,
            Class<? extends Annotation> nestedAnnotationClass) {
        this.annotationClass = annotationClass;
        this.type = type;
        this.attributeName = attributeName;
        this.nestedAnnotationClass = nestedAnnotationClass;
    }

    public AspectType getType() {
        return type;
    }

    public Class<? extends Annotation> getAnnotationClass() {
        return annotationClass;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public Class<? extends Annotation> getNestedAnnotationClass() {
        return nestedAnnotationClass;
    }
}
