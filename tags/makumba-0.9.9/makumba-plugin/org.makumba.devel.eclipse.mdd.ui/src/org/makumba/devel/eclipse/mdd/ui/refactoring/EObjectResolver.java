package org.makumba.devel.eclipse.mdd.ui.refactoring;

import java.util.Iterator;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.xtext.resource.EObjectAtOffsetHelper;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class EObjectResolver implements IUnitOfWork<IEObjectDescription, XtextResource> {
	private final ITextSelection selection;
	private IResourceDescriptions resourceDescriptions;

	public EObjectResolver(ITextSelection selection, IResourceDescriptions resourceDescriptions) {
		this.selection = selection;
		this.resourceDescriptions = resourceDescriptions;
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