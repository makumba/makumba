package org.makumba.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Starting point of the appliction, for a web-app that would be a ServletContextListener.
 * 
 * This is some code that demonstrates dependency injection, or rather an intermediate form thereof.
 * 
 * @author manu
 *
 */
public class StartingPoint {
	
	
	public static void main(String... args) {
		
		System.out.println("==== Computing some type analysis, the old way");
		
		SomeQueryAnalysis sqA = new SomeQueryAnalysis();
		
		sqA.doHeavyAnalysisWorkWithStaticCall("general.Person");
		
		
		
		System.out.println("==== Computing some type analysis, the intermediate way");
		
		// we need to create an injector
		// we do not need to use it though to instantiate our client code!
		// this would go in the servlet context listener
		Injector injector = Guice.createInjector(new MetaDataModule());
		
		sqA.doHeavyAnalysisWorkWithoutStaticCall("general.Car");
		
		
		// the next step is to go for full dependency injection by doing
		
//		SomeQueryAnalysis nsqA = injector.getInstance(SomeQueryAnalysis.class);
//		nsqA.doHeavyAnalysisWorkWithoutStaticCall("general.Car");
		
		// and not use the static injection anymore in SomeQueryAnalysis, but do the injection through the constructor
		// however it means creating the instance through Guice
		
		
		
	}

}
