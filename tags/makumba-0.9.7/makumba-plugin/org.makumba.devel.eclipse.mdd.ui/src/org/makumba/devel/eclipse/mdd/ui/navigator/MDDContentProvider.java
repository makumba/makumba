package org.makumba.devel.eclipse.mdd.ui.navigator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.IPipelinedTreeContentProvider2;
import org.eclipse.ui.navigator.PipelinedShapeModification;
import org.eclipse.ui.navigator.PipelinedViewerUpdate;
import org.eclipse.xtext.ui.XtextProjectHelper;

public class MDDContentProvider implements IPipelinedTreeContentProvider2 {

	private static final Object[] NO_CHILDREN = new Object[0];
	private static final String JAVA_EXTENSION_ID = "org.eclipse.jdt.java.ui.javaContent"; //$NON-NLS-1$

	private CommonViewer commonViewer;
	private final Map<IProject, DataDefinitionsRoot> dataDefinitionsRoots = new HashMap<IProject, DataDefinitionsRoot>();

	public void getPipelinedChildren(Object aParent, Set theCurrentChildren) {
		try {
			if (aParent instanceof IProject && ((IProject) aParent).hasNature(JavaCore.NATURE_ID)
					&& JavaEEProjectUtilities.isDynamicWebProject((IProject) aParent)
					&& ((IProject) aParent).hasNature(XtextProjectHelper.NATURE_ID)) {
				theCurrentChildren.add(getDataDefinitionsNode((IProject) aParent));
				IJavaElement element = null;
				//				for (Iterator iter = theCurrentChildren.iterator(); iter.hasNext();) {
				//					Object child = iter.next();
				//					if (child instanceof IResource && ((element = JavaCore.create((IResource) child)) != null)
				//							&& element.exists())
				//						iter.remove();
				//					else if (child instanceof IJavaElement)
				//						iter.remove();
				//				}
				theCurrentChildren.add(getDataDefinitionsNode((IProject) aParent));
			}
		} catch (CoreException e) {
			//TODO:
			//MakumbaWebUIPlugin.createErrorStatus(e.getMessage(), e);
		}

	}

	private DataDefinitionsRoot getDataDefinitionsNode(IProject project) {
		if (!JavaEEProjectUtilities.isDynamicWebProject(project))
			return null;
		DataDefinitionsRoot result = (DataDefinitionsRoot) dataDefinitionsRoots.get(project);
		if (result == null) {
			dataDefinitionsRoots.put(project, result = new DataDefinitionsRoot(commonViewer, project));
		}
		return result;
	}

	public void getPipelinedElements(Object anInput, Set theCurrentElements) {

	}

	public Object getPipelinedParent(Object anObject, Object aSuggestedParent) {
		// TODO Auto-generated method stub
		return aSuggestedParent;
	}

	public PipelinedShapeModification interceptAdd(PipelinedShapeModification anAddModification) {
		// TODO Auto-generated method stub
		return anAddModification;
	}

	public PipelinedShapeModification interceptRemove(PipelinedShapeModification aRemoveModification) {
		// TODO Auto-generated method stub
		return aRemoveModification;
	}

	public boolean interceptRefresh(PipelinedViewerUpdate aRefreshSynchronization) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean interceptUpdate(PipelinedViewerUpdate anUpdateSynchronization) {
		// TODO Auto-generated method stub
		return false;
	}

	public void init(ICommonContentExtensionSite aConfig) {
		// TODO Auto-generated method stub

	}

	public Object[] getElements(Object inputElement) {
		return new Object[0];
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IPackageFragment) {
			try {
				return ((IPackageFragment) parentElement).getChildren();
			} catch (JavaModelException e) {
				// TODO 
				//MakumbaWebUIPlugin.createErrorStatus(e.getMessage(), e);
			}
		}
		return null;
	}

	public Object getParent(Object element) {
		if (element instanceof DataDefinitionsRoot) {
			return ((DataDefinitionsRoot) element).getProject();
		}
		return null;
	}

	public boolean hasChildren(Object element) {
		//TODO:
		return element instanceof IPackageFragment;
	}

	public void dispose() {
		dataDefinitionsRoots.clear();

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (viewer instanceof CommonViewer) {
			commonViewer = (CommonViewer) viewer;
			/*INavigatorContentService service = commonViewer.getNavigatorContentService();
			INavigatorContentExtension javaext = service.getContentExtensionById(JAVA_EXTENSION_ID);
			if (javaext != null)
				delegateContentProvider = javaext.getContentProvider();*/
			dataDefinitionsRoots.clear();
		}
	}

	public void restoreState(IMemento aMemento) {

	}

	public void saveState(IMemento aMemento) {

	}

	public boolean hasPipelinedChildren(Object anInput, boolean currentHasChildren) {
		// TODO Auto-generated method stub
		return false;
	}

}
