package org.makumba.devel.eclipse.mdd.ui.project;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.common.project.facet.ui.AbstractFacetWizardPage;

public class MakumbaFacetInstallWizardPage extends AbstractFacetWizardPage {

	private FacetInstallActionConfigFactory config;
	private Button installExample;
	private Button installLibs;

	public MakumbaFacetInstallWizardPage() {
		super("org.makumba.devel.eclipse.plugin.mdd.ui.project.facet.install.page");
		setTitle("Makumba");
		setDescription("Configure Makumba project.");
	}

	public void setConfig(Object config) {
		this.config = (FacetInstallActionConfigFactory) config;

	}

	public void createControl(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		installExample = new Button(composite, SWT.CHECK);
		installExample.setText("Install example content");
		installExample.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		installExample.setSelection(true);

		installLibs = new Button(composite, SWT.CHECK);
		installLibs.setText("Install required libraries");
		installLibs.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		installLibs.setSelection(true);

		setControl(composite);

	}

	public void transferStateToConfig() {
		config.setInstallExample(installExample.getSelection());
		config.setInstallLibs(installLibs.getSelection());
	}
}
