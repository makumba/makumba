package org.makumba;

import java.lang.annotation.Annotation;

import javax.persistence.Column;

import org.makumba.annotations.Description;
import org.makumba.annotations.MakumbaEnum;
import org.makumba.annotations.Messages;
import org.makumba.providers.AnnotationMetadataReader;

/**
 * Various meta-data aspects provided through annotations can be registered here.<br>
 * The idea is that each kind of annotation values can be retrieved using the same mechanism, by calling methods of the
 * {@link AnnotationMetadataReader}.
 * 
 * @author Manuel Gay
 * @version $Id: MetadataAspect.java,v 1.1 Jun 21, 2010 6:42:30 PM manu Exp $
 */
public enum MetadataAspect {

    // not null
    NULLABLE(Column.class, "nullable"),

    // unique
    UNIQUE(Column.class, "unique"),

    // fixed
    UPDATABLE(Column.class, "updatable"),

    // character length of a string field
    LENGTH(Column.class, "length"),

    // field description
    DESCRIPTION(Description.class, "value"),

    // the makumba enum values
    MAKUMBA_INT_ENUM(MakumbaEnum.class, "value"),

    // messages to the user
    MESSAGES(Messages.class, "value");

    private Class<? extends Annotation> annotationClass;

    private String attributeName;

    private Class<? extends Annotation> nestedAnnotationClass;

    /**
     * An meta-data aspect, making it possible to read a specific annotation-based aspect
     * 
     * @param annotationClass
     *            the qualifier class for the aspect
     * @param attributeName
     *            the name of the annotation attribute to read from
     */
    private MetadataAspect(Class<? extends Annotation> annotationClass, String attributeName) {
        this.annotationClass = annotationClass;
        this.attributeName = attributeName;
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
