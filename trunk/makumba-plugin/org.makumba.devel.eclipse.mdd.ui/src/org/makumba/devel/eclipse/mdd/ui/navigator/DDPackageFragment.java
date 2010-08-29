package org.makumba.devel.eclipse.mdd.ui.navigator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.swt.graphics.Image;

public class DDPackageFragment implements IAdaptable {

	private IPackageFragment fragment;

	public DDPackageFragment(IPackageFragment fragment) {
		this.fragment = fragment;
	}

	public String getLabel() {
		return fragment.getElementName();
	}

	public Image getImage() {
		//TODO: add package fragment image
		return null;
	}

	public Object getAdapter(Class adapter) {
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}
}
