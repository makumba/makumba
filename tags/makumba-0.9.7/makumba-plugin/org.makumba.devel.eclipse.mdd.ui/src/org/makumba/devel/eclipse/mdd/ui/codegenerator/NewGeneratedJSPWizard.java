package org.makumba.devel.eclipse.mdd.ui.codegenerator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.ide.IDE;
import org.makumba.devel.eclipse.mdd.MDDUtils;
import org.makumba.devel.eclipse.mdd.MDD.DataDefinition;

public class NewGeneratedJSPWizard extends Wizard {

	private NewGeneratedJSPFileWizardPage fNewFilePage;
	private NewGeneratedJSPTypeWizardPage fNewTypePage;
	private Display fDisplay;
	private DataDefinition fDataDefinition;
	private IPreferenceStore preferenceStore;
	private MakumbaJSPCodeGenerator codeGenerator;

	public NewGeneratedJSPWizard(DataDefinition dataDefinition, IPreferenceStore preferenceStore,
			MakumbaJSPCodeGenerator codeGenerator) {
		fDataDefinition = dataDefinition;
		this.preferenceStore = preferenceStore;
		this.codeGenerator = codeGenerator;
		setWindowTitle("New generated JSP file");
	}

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

		fNewTypePage = new NewGeneratedJSPTypeWizardPage();

		addPage(fNewTypePage);

		fNewFilePage = new NewGeneratedJSPFileWizardPage("JSPWizardNewFileCreationPage", new StructuredSelection()); //$NON-NLS-1$ 
		fNewFilePage.setTitle("New generated JSP File");
		fNewFilePage.setDescription("Create new JSP based on Makumba Data Definition");
		fNewFilePage.setProject(MDDUtils.getProject(fDataDefinition));

		addPage(fNewFilePage);

	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page instanceof NewGeneratedJSPTypeWizardPage && fNewTypePage.getSelectedType() != null
				&& fNewFilePage.getFileName() != "") {
			String newFileName = fDataDefinition.eResource().getURI().trimFileExtension().lastSegment().toLowerCase()
					+ fNewTypePage.getSelectedType();
			fNewFilePage.setFileName(newFileName);
		}
		return super.getNextPage(page);
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

		ContributionTemplateStore templateStore = new ContributionTemplateStore(preferenceStore,
				"org.makumba.generator_templates");
		try {
			templateStore.load();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Template template = templateStore.findTemplateById("org.makumba.jsp.generator");

		// if there was problem with creating file, it will be null, so make
		// sure to check
		if (file != null) {

			//TODO:get actual object

			String generatedCode = codeGenerator.getGeneratedCode(fDataDefinition, fNewTypePage.getSelectedType());

			String templateString = template.getPattern();
			if (templateString.indexOf("${insert}") >= 0) {
				String temp = templateString.substring(0, templateString.indexOf("${insert}"));
				temp += generatedCode;
				temp += templateString.substring(templateString.indexOf("${insert}") + "${insert}".length(),
						templateString.length());
				templateString = temp;
			}

			try {
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				OutputStreamWriter outputStreamWriter = null;
				outputStreamWriter = new OutputStreamWriter(outputStream);
				outputStreamWriter.write(templateString);
				outputStreamWriter.flush();
				outputStreamWriter.close();
				ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
				file.setContents(inputStream, true, false, null);
				inputStream.close();
			} catch (Exception e) {

			}

			// open the file in editor
			if (fShouldOpenEditorOnFinish)
				openEditor(file);

			// everything's fine
			performedOK = true;
		}
		return performedOK;
	}
}
