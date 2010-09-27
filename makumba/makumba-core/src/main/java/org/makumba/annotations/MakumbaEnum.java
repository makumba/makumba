package org.makumba.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Makumba Enum<br>
 * 
 * <pre>
 * @MakumbaEnum({@E(key=10, value="value"), @E(key=20, value="another value")})
 * </pre>
 * 
 * @author Manuel Gay
 * @version $Id: MakumbaEnum.java,v 1.1 Jun 21, 2010 6:32:39 PM manu Exp $
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD, ElementType.FIELD })
public @interface MakumbaEnum {
    E[] value();
}
