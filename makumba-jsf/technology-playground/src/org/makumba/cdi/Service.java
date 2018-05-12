package org.makumba.cdi;

public class Service {
	
	public String getSomething() {
		return "Something";
	}
	
	@Override
	public String toString() {
		return "Service " + super.toString();
	}
	
}
