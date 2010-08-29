
package org.makumba.devel.eclipse.mdd;

import org.makumba.devel.eclipse.mdd.MDDStandaloneSetupGenerated;

/**
 * Initialization support for running Xtext languages 
 * without equinox extension registry
 */
public class MDDStandaloneSetup extends MDDStandaloneSetupGenerated{

	public static void doSetup() {
		new MDDStandaloneSetup().createInjectorAndDoEMFRegistration();
	}
}

