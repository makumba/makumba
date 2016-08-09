package org.makumba.devel.eclipse.mdd.ui.wizard;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jst.jsp.core.internal.util.FacetModuleCoreSupport;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;

@SuppressWarnings("restriction")
public class NewMDDFileWizardPage extends WizardNewFileCreationPage {

	private IContentType fContentType;

	public NewMDDFileWizardPage(String pageName, IStructuredSelection selection) {
		super(pageName, selection);
	}

	/**
	 * This method is overriden to set the selected folder to web contents
	 * folder if the current selection is outside the web contents folder.
	 */
	protected void initialPopulateContainerNameField() {
		super.initialPopulateContainerNameField();

		IPath fullPath = getContainerFullPath();
		IProject project = getProjectFromPath(fullPath);
		IPath webContentPath = getWebContentPath(project);
		IPath setPath = null;

		if (project != null) {
			final IVirtualComponent vc = ComponentCore.createComponent(project);
			IVirtualFolder vf = vc.getRootFolder().getFolder("WEB-INF/classes/dataDefinitions");
			IFolder ddFolder = (IFolder) vf.getUnderlyingFolder();
			if (ddFolder.exists()) {
				setPath = ddFolder.getFullPath();
			} else {
				vf = vc.getRootFolder().getFolder("WEB-INF/classes");
				ddFolder = (IFolder) vf.getUnderlyingFolder();
				if (ddFolder.exists()) {
					setPath = ddFolder.getFullPath();
				}
			}
		}

		if (setPath == null && webContentPath != null && !webContentPath.isPrefixOf(fullPath)) {
			setPath = webContentPath;
		}

		if (setPath != null) {
			setContainerFullPath(setPath);
		}

	}

	/**
	 * This method is overriden to set additional validation specific to jsp
	 * files.
	 */
	protected boolean validatePage() {
		setMessage(null);
		setErrorMessage(null);

		if (!super.validatePage()) {
			return false;
		}

		String fileName = getFileName();
		IPath fullPath = getContainerFullPath();
		if ((fullPath != null) && (fullPath.isEmpty() == false) && (fileName != null)) {
			// check that filename does not contain invalid extension
			if (!extensionValidForContentType(fileName)) {
				setErrorMessage("File exstension is not a valid MDD extension");
				return false;
			}
			// no file extension specified so check adding default
			// extension doesn't equal a file that already exists
			if (fileName.lastIndexOf('.') == -1) {
				String newFileName = addDefaultExtension(fileName);
				IPath resourcePath = fullPath.append(newFileName);

				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				IStatus result = workspace.validatePath(resourcePath.toString(), IResource.FOLDER);
				if (!result.isOK()) {
					// path invalid
					setErrorMessage(result.getMessage());
					return false;
				}

				if ((workspace.getRoot().getFolder(resourcePath).exists() || workspace.getRoot().getFile(resourcePath)
						.exists())) {
					setErrorMessage("File already exists");
					return false;
				}
			}

			// get the IProject for the selection path
			IProject project = getProjectFromPath(fullPath);
			if (project != null) {
				if (!isJavaProject(project)) {
					setMessage("File should be inside java project", WARNING);
				}
				// if inside web project, check if inside webContent folder
				if (isDynamicWebProject(project)) {
					//TODO: should be inside src folder
					// check that the path is inside the webContent folder
					//					IPath webContentPath = getWebContentPath(project);
					//					if (!webContentPath.isPrefixOf(fullPath)) {
					//						setMessage(JSPUIMessages._WARNING_FOLDER_MUST_BE_INSIDE_WEB_CONTENT, WARNING);
					//					}
				} else {
					setMessage("File should be inside Dynamic Web Project", WARNING);
				}
			}
		}

		return true;
	}

	/**
	 * Adds default extension to the filename
	 * 
	 * @param filename
	 * @return
	 */
	String addDefaultExtension(String filename) {
		return filename + ".mdd";
	}

	/**
	 * Get content type associated with this new file wizard
	 * 
	 * @return IContentType
	 */
	private IContentType getContentType() {
		if (fContentType == null)
			fContentType = Platform.getContentTypeManager().getContentType(
					"org.makumba.devel.eclipse.mdd.ui.mddContentType");
		return fContentType;
	}

	/**
	 * Verifies if fileName is valid name for content type. Takes base content
	 * type into consideration.
	 * 
	 * @param fileName
	 * @return true if extension is valid for this content type
	 */
	private boolean extensionValidForContentType(String fileName) {
		boolean valid = false;

		IContentType type = getContentType();
		// there is currently an extension
		if (fileName.lastIndexOf('.') != -1) {
			// check what content types are associated with current extension
			IContentType[] types = Platform.getContentTypeManager().findContentTypesFor(fileName);
			int i = 0;
			while (i < types.length && !valid) {
				valid = types[i].isKindOf(type);
				++i;
			}
		} else
			valid = true; // no extension so valid
		return valid;
	}

	/**
	 * Returns the project that contains the specified path
	 * 
	 * @param path
	 *            the path which project is needed
	 * @return IProject object. If path is <code>null</code> the return value is
	 *         also <code>null</code>.
	 */
	private IProject getProjectFromPath(IPath path) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = null;

		if (path != null) {
			if (workspace.validatePath(path.toString(), IResource.PROJECT).isOK()) {
				project = workspace.getRoot().getProject(path.toString());
			} else {
				project = workspace.getRoot().getFile(path).getProject();
			}
		}

		return project;
	}

	/**
	 * Checks if the specified project is a web project.
	 * 
	 * @param project
	 *            project to be checked
	 * @return true if the project is web project, otherwise false
	 */
	private boolean isDynamicWebProject(IProject project) {
		boolean is = FacetModuleCoreSupport.isDynamicWebProject(project);
		return is;
	}

	/**
	 * Checks if the specified project is a type of java project.
	 * 
	 * @param project
	 *            project to be checked (cannot be null)
	 * @return true if the project is a type of java project, otherwise false
	 */
	private boolean isJavaProject(IProject project) {
		boolean isJava = false;
		try {
			isJava = project.hasNature(JavaCore.NATURE_ID);
		} catch (CoreException e) {

		}

		return isJava;
	}

	/**
	 * Returns the web contents folder of the specified project
	 * 
	 * @param project
	 *            the project which web contents path is needed
	 * @return IPath of the web contents folder
	 */
	private IPath getWebContentPath(IProject project) {
		IPath path = FacetModuleCoreSupport.getWebContentRootPath(project);
		return path;
	}

}
