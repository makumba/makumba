package org.makumba.guice.aop;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

/**
 * Guice module for the Hasselhoff concern
 * @author manu
 *
 */
public class HasselhoffModule extends AbstractModule {
	
	@Override
	protected void configure() {
		// here we bind all our Hasselhoff interceptors
		bindInterceptor(Matchers.any(), Matchers.annotatedWith(Hasselhoff.class), new EurotripHasselhoff());
		bindInterceptor(Matchers.any(), Matchers.annotatedWith(Hasselhoff.class), new AnotherHasselhoff());
	}

}
