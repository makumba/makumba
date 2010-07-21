package org.makumba.guice;

import com.google.inject.Singleton;

/**
 * New kind of meta-data provider. It is a singleton but because Guice makes it
 * so. It gets injected by Guice into the client classes, no getInstance() calls to it are made
 * 
 * @author manu
 * 
 */
@Singleton
public class NonStaticMetaDataProvider {

	public String getVitalMetaData(String something) {
		return "Vital " + something;
	}

}
