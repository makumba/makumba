package org.makumba.guice.aop;


import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Europtrip variant of the Hasselhoff concern
 * @author manu
 *
 */
public class EurotripHasselhoff extends Hasselhoffer implements MethodInterceptor {
	
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		
		System.out.println("EurotripHasselhoff.invoke(): invoked method " + invocation.getMethod().getName());
		
		// do these match a EurotripHasselhoff?
		
		Object[] args = invocation.getArguments();
		if(!match(args)) {
			// we return without doing anything
			return invocation.proceed();
		} else {
			// all your data are belong to us
			System.out.println("EurotripHasselhoff.invoke(): the call had the data '" + args[1] + "'");
			invocation.getArguments()[1] = "Duuuuu, du allein kannst mich verstehen!";
			return invocation.proceed();
		}
		
		
	}

}
