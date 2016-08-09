package org.makumba.cdi;

import javax.inject.Inject;

public class Client {
	
	@Inject Service s;
	
	public void doSomething() {
		System.out.println("Client is using service which gives " + s.getSomething());
	}

}
