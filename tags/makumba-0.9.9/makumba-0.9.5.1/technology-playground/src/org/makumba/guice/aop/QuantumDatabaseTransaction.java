package org.makumba.guice.aop;

import java.lang.reflect.Field;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Database transaction using quantum energy to get the data to the database before it was even produced.
 * @author manu
 *
 */
public class QuantumDatabaseTransaction {
	
	public String dummy = "yakayaka";
	
	public void insert(Field field, String data) {
		
		traceInsert(field, data);
	}
	
	@Hasselhoff
	public void insertSuperDuper(Field field, String data) {
		traceInsert(field, data);

	}
	
	private void traceInsert(Field field, String data) {
		System.out.println("Inserting data at quantum level:");
		System.out.println("== into field '" + field.getName() + "' which " + (field.isAccessible()?"is accessible":"is not accessible"));
		System.out.println("== with data '" + data + "'");
	}
	
	public static void main(String... args) throws SecurityException, NoSuchFieldException {
		
		// boilerplate for Guice modules
		Injector injector = Guice.createInjector(new HasselhoffModule());
		
		// we need to create our client code through Guice invocation
		// if this is too hard we can for now use Static Injections, see http://code.google.com/p/google-guice/wiki/Injections
		QuantumDatabaseTransaction t = injector.getInstance(QuantumDatabaseTransaction.class);
		
		System.out.println("== Inserting into quantum database without Hasselhoff annotation");
		t.insert(t.getClass().getField("dummy"), "My precious");
		
		System.out.println();
		
		System.out.println("== Inserting into quantum database with Hasselhoff annotation");
		t.insertSuperDuper(t.getClass().getField("dummy"), "My precious");
	}

}
