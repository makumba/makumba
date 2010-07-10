package org.makumba.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for messages to the user
 * 
 * @author Manuel Gay
 * @version $Id: Description.java,v 1.1 Jun 21, 2010 6:32:24 PM manu Exp $
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD, ElementType.FIELD })
public @interface Messages {
    Message[] value();
}
