package org.makumba.devel.eclipse.mdd.ui.refactoring;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.parsetree.CompositeNode;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.makumba.devel.eclipse.mdd.MDD.FieldDeclaration;

import com.google.inject.Inject;

public class RenameRefactoringHandler extends AbstractHandler {

	@Inject
	private IResourceDescriptions resourceDescriptions;

	@Inject
	private ResourceSet resourceSet;

	@Inject
	private IQualifiedNameProvider nameProvider;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			String currentName = "";

			XtextEditor editor = (XtextEditor) HandlerUtil.getActiveEditor(event);

			final ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();

			final IEObjectDescription eObjectDescription = editor.getDocument().readOnly(
					new EObjectResolver(selection, resourceDescriptions));

			//TODO: debug this
			CompositeNode o = editor.getDocument().readOnly(
					new URIFragmentResolver(eObjectDescription.getEObjectURI().fragment()));

			if (o.getElement() instanceof FieldDeclaration) {
				FieldDeclaration r = (FieldDeclaration) o.getElement();
				currentName = r.getName();
			}

			RenameProcessor processor = new RenameProcessor(editor, resourceDescriptions, resourceSet, nameProvider);
			RenameRefactoring refactoring = new RenameRefactoring(processor);

			RenameRefactoringWizard wizard = new RenameRefactoringWizard(refactoring,
					RefactoringWizard.WIZARD_BASED_USER_INTERFACE, "Field");
			wizard.setRenameText(currentName);

			RefactoringWizardOpenOperation openOperation = new RefactoringWizardOpenOperation(wizard);

			openOperation.run(Display.getCurrent().getActiveShell(), "Refactoring not possible!");

		} catch (Exception e) {
			MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Rename Refactoring",
					"Error while applying refactoring to workbench/wizard: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

}
