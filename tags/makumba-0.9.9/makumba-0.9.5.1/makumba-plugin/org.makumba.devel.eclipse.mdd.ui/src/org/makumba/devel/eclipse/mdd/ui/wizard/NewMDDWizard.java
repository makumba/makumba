package org.makumba.devel.eclipse.mdd.ui.wizard;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class NewMDDWizard extends Wizard implements INewWizard {

	private NewMDDFileWizardPage fNewFilePage;
	private IStructuredSelection fSelection;
	private Display fDisplay;

	private boolean fShouldOpenEditorOnFinish = true;

	public void createPageControls(Composite pageContainer) {
		fDisplay = pageContainer.getDisplay();
		super.createPageControls(pageContainer);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=248424
	public void setOpenEditorOnFinish(boolean openEditor) {
		this.fShouldOpenEditorOnFinish = openEditor;
	}

	public void addPages() {
		fNewFilePage = new NewMDDFileWizardPage(
				"JSPWizardNewFileCreationPage", new StructuredSelection(IDE.computeSelectedResources(fSelection))); //$NON-NLS-1$ 
		fNewFilePage.setTitle("New MDD File");
		fNewFilePage.setDescription("Create new Makumba Data Definition file");
		addPage(fNewFilePage);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		fSelection = selection;
		setWindowTitle("New MDD File");

		ImageDescriptor descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
				"org.makumba.devel.eclipse.mdd.ui", "icons/newmddwizard.gif");
		setDefaultPageImageDescriptor(descriptor);

	}

	private void openEditor(final IFile file) {
		if (file != null) {
			fDisplay.asyncExec(new Runnable() {
				public void run() {
					if (!PlatformUI.isWorkbenchRunning())
						return;
					try {
						IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
						IDE.openEditor(page, file, true);
					} catch (PartInitException e) {
						//Logger.log(Logger.WARNING_DEBUG, e.getMessage(), e);
					}
				}
			});
		}
	}

	public boolean performFinish() {
		boolean performedOK = false;

		// no file extension specified so add default extension
		String fileName = fNewFilePage.getFileName();
		if (fileName.lastIndexOf('.') == -1) {
			String newFileName = fNewFilePage.addDefaultExtension(fileName);
			fNewFilePage.setFileName(newFileName);
		}

		// create a new empty file
		IFile file = fNewFilePage.createNewFile();

		// if there was problem with creating file, it will be null, so make
		// sure to check
		if (file != null) {

			// open the file in editor
			if (fShouldOpenEditorOnFinish)
				openEditor(file);

			// everything's fine
			performedOK = true;
		}
		return performedOK;
	}

}
