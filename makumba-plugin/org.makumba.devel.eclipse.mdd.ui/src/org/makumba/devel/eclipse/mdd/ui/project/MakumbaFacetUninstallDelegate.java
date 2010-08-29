package org.makumba.devel.eclipse.mdd.ui.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.common.project.facet.core.IDelegate;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.xtext.builder.nature.ToggleXtextNatureAction;
import org.makumba.devel.eclipse.mdd.ui.internal.MDDActivator;

public class MakumbaFacetUninstallDelegate implements IDelegate {

	private ToggleXtextNatureAction toggleNature = MDDActivator.getInstance()
			.getInjector("org.makumba.devel.eclipse.mdd.MDD").getInstance(ToggleXtextNatureAction.class);

	public void execute(IProject project, IProjectFacetVersion arg1, Object arg2, IProgressMonitor arg3)
			throws CoreException {

		if (toggleNature.hasNature(project) && project.isAccessible()) {
			toggleNature.toggleNature(project);
		}

		// TODO Maybe do some stuff on uninstall

	}

}
