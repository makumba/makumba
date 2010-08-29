package org.makumba.devel.eclipse.mdd.ui.codegenerator;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class NewGeneratedJSPTypeWizardPage extends WizardPage {

	private Combo type;

	protected NewGeneratedJSPTypeWizardPage() {
		super("org.makumba.devel.eclipse.mdd.ui.newJSPType");
		setDescription("Select the type of the generated JSP page");
		setTitle("New generated JSP File");
	}

	public void createControl(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		final Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		label.setText("Type:");

		type = new Combo(composite, SWT.READ_ONLY);
		type.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		type.setItems(MakumbaJSPCodeGenerator.GeneratorTypes);

		type.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (type.getSelectionIndex() >= 0) {
					setPageComplete(true);
				}
			}
		});
		setControl(composite);
	}

	public String getSelectedType() {
		if (type.getSelectionIndex() >= 0) {
			return type.getItems()[type.getSelectionIndex()];
		}
		return null;
	}

	public boolean canFlipToNextPage() {
		return type.getSelectionIndex() >= 0;
	}

}
