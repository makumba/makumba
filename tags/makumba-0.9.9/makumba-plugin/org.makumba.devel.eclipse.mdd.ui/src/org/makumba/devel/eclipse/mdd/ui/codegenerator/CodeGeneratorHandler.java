package org.makumba.devel.eclipse.mdd.ui.codegenerator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.parsetree.CompositeNode;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.makumba.devel.eclipse.mdd.MDD.DataDefinition;
import org.makumba.devel.eclipse.mdd.ui.refactoring.EObjectResolver;
import org.makumba.devel.eclipse.mdd.ui.refactoring.URIFragmentResolver;

import com.google.inject.Inject;

public class CodeGeneratorHandler extends AbstractHandler {

	@Inject
	private IResourceDescriptions resourceDescriptions;

	@Inject
	private IPreferenceStore preferenceStore;

	@Inject
	private MakumbaJSPCodeGenerator codeGenerator;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		XtextEditor editor = (XtextEditor) HandlerUtil.getActiveEditor(event);

		final ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();

		final IEObjectDescription eObjectDescription = editor.getDocument().readOnly(
				new EObjectResolver(selection, resourceDescriptions));

		CompositeNode o = editor.getDocument().readOnly(
				new URIFragmentResolver(eObjectDescription.getEObjectURI().fragment()));

		while (o.getParent() != null && !(o.getElement() instanceof DataDefinition)) {
			o = o.getParent();
		}

		if (o.getElement() instanceof DataDefinition) {
			NewGeneratedJSPWizard wizard = new NewGeneratedJSPWizard((DataDefinition) o.getElement(), preferenceStore,
					codeGenerator);
			Dialog dialog = new WizardDialog(Display.getCurrent().getActiveShell(), wizard);
			dialog.create();
			dialog.open();
		}
		return null;
	}

}
