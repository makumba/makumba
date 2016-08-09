package org.makumba.devel.eclipse.jsp.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jst.jsp.core.internal.provisional.contenttype.ContentTypeIdForJSP;

@SuppressWarnings("restriction")
public class MakumbaJSPFileVisitor implements IResourceProxyVisitor {

	private List<IFile> fFiles = new ArrayList<IFile>();
	private IContentType[] fContentTypes = null;

	public boolean visit(IResourceProxy proxy) throws CoreException {

		if (proxy.getType() == IResource.FILE) {

			if (isJSPType(proxy.getName())) {
				IFile file = (IFile) proxy.requestResource();
				if (file.exists()) {
					fFiles.add(file);
					return false;
				}
			}
		}
		return true;
	}

	public final IFile[] getFiles() {
		return (IFile[]) fFiles.toArray(new IFile[fFiles.size()]);
	}

	/**
	 * Gets list of content types this visitor is interested in
	 * 
	 * @return All JSP-related content types
	 */
	private IContentType[] getValidContentTypes() {
		if (fContentTypes == null) {
			// currently "hard-coded" to be jsp & jspf
			fContentTypes = new IContentType[] {
					Platform.getContentTypeManager().getContentType(ContentTypeIdForJSP.ContentTypeID_JSP),
					Platform.getContentTypeManager().getContentType(ContentTypeIdForJSP.ContentTypeID_JSPFRAGMENT) };
		}
		return fContentTypes;
	}

	/**
	 * Checks if fileName is some type of JSP (including JSP fragments)
	 * 
	 * @param fileName
	 * @return true if filename indicates some type of JSP, false otherwise
	 */
	private boolean isJSPType(String fileName) {
		boolean valid = false;

		IContentType[] types = getValidContentTypes();
		int i = 0;
		while (i < types.length && !valid) {
			valid = types[i].isAssociatedWith(fileName);
			++i;
		}
		return valid;
	}

}
