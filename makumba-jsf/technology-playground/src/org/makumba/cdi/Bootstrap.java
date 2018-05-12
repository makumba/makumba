package org.makumba.cdi;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

public class Bootstrap {

	public static void main(String[] args) {
		WeldContainer weld = new Weld().initialize();
		Client c = weld.instance().select(Client.class).get();
		c.doSomething();

	}

}
