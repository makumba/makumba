package org.makumba.devel.eclipse.mdd.ui.refactoring;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.widgets.Composite;

public class RenameRefactoringWizard extends RefactoringWizard {

	private RenameInputWizardPage wizardPage;
	private String renameText = "";
	private String renameType = "";

	public RenameRefactoringWizard(Refactoring refactoring, int flags, String renameType) {
		super(refactoring, flags);
		this.renameType = renameType;
	}

	@Override
	protected void addUserInputPages() {
		wizardPage = new RenameInputWizardPage("Refactoring", renameType);
		wizardPage.setRenameText(renameText);
		addPage(wizardPage);
	}

	public void setRenameText(String s) {
		renameText = s;
		if (wizardPage != null)
			wizardPage.setRenameText(s);
	}

	static class RenameInputWizardPage extends UserInputWizardPage {

		private RenamePageComposite composite;
		private String renameText;
		private String renameType;

		public RenameInputWizardPage(String name, String renameType) {
			super(name);
			this.renameType = renameType;
			setTitle("Rename Refactoring");
			setDescription("Automated reftoring finds and updates references to the changed elements");
		}

		public void createControl(Composite parent) {
			composite = new RenamePageComposite(parent, 0, renameType);
			composite.getRenameText().setText(renameText);
			composite.getRenameText().selectAll();
			setControl(composite);
		}

		public String getRenameText() {
			return composite.getRenameText().getText();
		}

		public void setRenameText(String s) {
			renameText = s;
			if (composite != null)
				composite.getRenameText().setText(renameText);
		}

		@Override
		public IWizardPage getNextPage() {
			RenameRefactoring refactoring = (RenameRefactoring) getRefactoring();
			if (refactoring != null) {
				refactoring.setRenameText(getRenameText());
			}
			return super.getNextPage();
		}

		@Override
		protected boolean performFinish() {
			RenameRefactoring refactoring = (RenameRefactoring) getRefactoring();
			if (refactoring != null) {
				refactoring.setRenameText(getRenameText());
			}

			return super.performFinish();
		}
	}

}
