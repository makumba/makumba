package org.makumba.guice.aop;

import java.lang.reflect.Field;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Another variant of the Hasselhoff concern, which modifies the accessibility of a field
 * @author manu
 *
 */
public class AnotherHasselhoff extends Hasselhoffer implements MethodInterceptor {
	
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		
		System.out.println("AnotherHasselhoff.invoke(): invoked method " + invocation.getMethod().getName());
		
		// do these match a AnotherHasselhoff?
		
		Object[] args = invocation.getArguments();
		if(!match(args)) {
			// we return without doing anything
			return invocation.proceed();
		} else {
			// let's do something to that field
			Field f = (Field) args[0];
			
			System.out.println("AnotherHasselhoff.invoke(): the call had the field '" + f.getName() + "' which was " + (f.isAccessible()?"":"not") + " accessible");
			f.setAccessible(true);
			return invocation.proceed();
		}
	}
}
