package org.makumba.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Element of a {@link MakumbaEnum}
 * 
 * @author Manuel Gay
 * @version $Id: E.java,v 1.1 Jun 21, 2010 6:36:12 PM manu Exp $
 */
@Target(value = {})
@Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface E {
    int key();

    String value();

    boolean deprecated() default false;

}
