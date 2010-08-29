package org.makumba.devel.eclipse.mdd.ui.navigator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.graphics.Image;

public class DataDefinitionsRoot implements IAdaptable {

	private IProject project;

	public DataDefinitionsRoot(StructuredViewer viewer, IProject project) {
		this.project = project;
	}

	public String getLabel() {
		return "Data Definitions";
	}

	public Image getImage() {
		return null;
	}

	public IProject getProject() {
		return project;
	}

	public Object getAdapter(Class adapter) {
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}
}
