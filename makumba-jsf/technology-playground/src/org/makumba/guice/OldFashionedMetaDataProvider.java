package org.makumba.guice;

public class OldFashionedMetaDataProvider {
	
	/**
	 * Static call, the kind that is hard to test
	 */
	public static String getVitalMetaData(String something) {
		return "Vital " + something;
	}

}
