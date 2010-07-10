package org.makumba.guice;

import com.google.inject.AbstractModule;

/**
 * Module for the meta-data. 
 * @author manu
 *
 */
public class MetaDataModule extends AbstractModule {
	
	@Override
	protected void configure() {
		requestStaticInjection(SomeQueryAnalysis.class);
	}

}
