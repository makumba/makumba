package org.makumba.devel.eclipse.mdd.ui.refactoring;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.parsetree.CompositeNode;
import org.eclipse.xtext.resource.EObjectAtOffsetHelper;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;
import org.makumba.devel.eclipse.mdd.MDD.FieldDeclaration;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class RenameRefactoringHandler extends AbstractHandler {

	private class EObjectResolver implements IUnitOfWork<IEObjectDescription, XtextResource> {
		private final ITextSelection selection;

		private EObjectResolver(ITextSelection selection) {
			this.selection = selection;
		}

		public IEObjectDescription exec(XtextResource state) throws Exception {
			EObject element = EObjectAtOffsetHelper.resolveElementAt(state, selection.getOffset(), null);
			if (element != null) {
				final URI eObjectURI = EcoreUtil.getURI(element);
				IResourceDescription resourceDescription = resourceDescriptions.getResourceDescription(eObjectURI
						.trimFragment());
				if (resourceDescription != null) {
					Iterator<IEObjectDescription> eObjectDescriptions = Iterables.filter(
							resourceDescription.getExportedObjects(), new Predicate<IEObjectDescription>() {
								public boolean apply(IEObjectDescription input) {
									return input.getEObjectURI().equals(eObjectURI);
								}
							}).iterator();
					if (eObjectDescriptions.hasNext()) {
						return eObjectDescriptions.next();
					}
				}
			}
			return null;
		}
	}

	@Inject
	private IResourceDescriptions resourceDescriptions;

	@Inject
	private Provider<ResourceSet> resourceSet;

	@Inject
	private IQualifiedNameProvider nameProvider;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			String currentName = "";

			XtextEditor editor = (XtextEditor) HandlerUtil.getActiveEditor(event);

			final ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();

			final IEObjectDescription eObjectDescription = editor.getDocument()
					.readOnly(new EObjectResolver(selection));

			//TODO: debug this
			CompositeNode o = editor.getDocument().readOnly(
					new URIFragmentResolver(eObjectDescription.getEObjectURI().fragment()));

			if (o.getElement() instanceof FieldDeclaration) {
				FieldDeclaration r = (FieldDeclaration) o.getElement();
				currentName = r.getName();
			}

			RenameProcessor processor = new RenameProcessor(editor, resourceDescriptions, resourceSet.get(),
					nameProvider);
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
