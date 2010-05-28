package org.makumba.guice.aop;

import java.lang.reflect.Field;

/**
 * The parent of all Hasselhoffs, knows how to recognize the method calls
 * @author manu
 *
 */
public class Hasselhoffer {

	protected boolean match(Object[] args) {
		return args.length == 2 && args[0] instanceof Field && args[1] instanceof String;
		
	}

}