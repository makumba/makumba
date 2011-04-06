package org.makumba.devel.eclipse.mdd.ui.project;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.common.project.facet.core.IActionConfigFactory;

public class FacetInstallActionConfigFactory implements IActionConfigFactory {

	private boolean installExample = false;
	private boolean installLibs = false;

	public Object create() throws CoreException {

		return new FacetInstallActionConfigFactory();
	}

	public boolean isInstallExample() {
		return installExample;
	}

	public void setInstallExample(boolean installExample) {
		this.installExample = installExample;
	}

	public boolean isInstallLibs() {
		return installLibs;
	}

	public void setInstallLibs(boolean installLibs) {
		this.installLibs = installLibs;
	}

}
