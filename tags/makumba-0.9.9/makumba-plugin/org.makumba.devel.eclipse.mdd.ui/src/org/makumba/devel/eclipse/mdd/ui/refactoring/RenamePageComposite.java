package org.makumba.devel.eclipse.mdd.ui.refactoring;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class RenamePageComposite extends Composite {
	private Text renameText;

	public RenamePageComposite(Composite parent, int style, String renameType) {
		super(parent, style);
		final GridLayout gridLayout = new GridLayout();
		setLayout(gridLayout);

		final Composite composite = new Composite(this, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 2;
		composite.setLayout(gridLayout_1);

		final Label renameLabel = new Label(composite, SWT.NONE);
		renameLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, true));
		renameLabel.setText("New " + renameType + " name:");

		renameText = new Text(composite, SWT.BORDER);
		renameText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, true));
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	public Text getRenameText() {
		return renameText;
	}
}
