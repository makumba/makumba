package org.makumba.devel.eclipse.mdd.ui.hyperlinking;

import java.util.Map;
import java.util.Vector;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jface.text.Region;
import org.eclipse.xtext.parsetree.CompositeNode;
import org.eclipse.xtext.parsetree.LeafNode;
import org.eclipse.xtext.parsetree.NodeUtil;
import org.eclipse.xtext.resource.EObjectAtOffsetHelper;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.scoping.impl.AbstractGlobalScopeProvider;
import org.eclipse.xtext.ui.editor.hyperlinking.HyperlinkHelper;
import org.eclipse.xtext.ui.editor.hyperlinking.IHyperlinkAcceptor;
import org.eclipse.xtext.util.TextLocation;
import org.makumba.devel.eclipse.mdd.MDDUtils;
import org.makumba.devel.eclipse.mdd.MQLContext;
import org.makumba.devel.eclipse.mdd.MQLQuerySegment;
import org.makumba.devel.eclipse.mdd.MDD.Atom;
import org.makumba.devel.eclipse.mdd.MDD.FromClassOrOuterQueryPath;
import org.makumba.devel.eclipse.mdd.MDD.IdentPrimary;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

public class MQLHyperlinkHelper extends HyperlinkHelper {

	@Inject
	private IResourceDescriptions resourceDescriptions;

	@Inject
	private MDDUtils utils;

	@Inject
	private Provider<ResourceSet> resourceSet;

	@Inject
	@Named(AbstractGlobalScopeProvider.NAMED_BUILDER_SCOPE)
	private Provider<IResourceDescriptions> builderScopeResourceDescriptionsProvider;

	/**
	 * Gets the right resource description depending on the context
	 * 
	 * @param object
	 * @return
	 */
	public IResourceDescriptions getResourceDescriptions(final EObject object) {
		IResourceDescriptions descriptions = resourceDescriptions;
		Map<Object, Object> loadOptions = object.eResource().getResourceSet().getLoadOptions();
		if (Boolean.TRUE.equals(loadOptions.get(AbstractGlobalScopeProvider.NAMED_BUILDER_SCOPE))) {
			descriptions = builderScopeResourceDescriptionsProvider.get();
		}
		if (descriptions instanceof IResourceDescriptions.IContextAware) {
			((IResourceDescriptions.IContextAware) descriptions).setContext(object);
		}
		return descriptions;
	}

	@Override
	public void createHyperlinksByOffset(XtextResource resource, int offset, IHyperlinkAcceptor acceptor) {
		TextLocation textLocation = new TextLocation();
		EObject crossLinkedEObject = EObjectAtOffsetHelper.resolveCrossReferencedElementAt(resource, offset,
				textLocation);
		if (crossLinkedEObject != null && !crossLinkedEObject.eIsProxy()) {
			Region region = new Region(textLocation.getOffset(), textLocation.getLength());
			createHyperlinksTo(resource, region, crossLinkedEObject, acceptor);
		} else {
			EObject object = EObjectAtOffsetHelper.resolveElementAt(resource, offset, textLocation);
			if (object instanceof FromClassOrOuterQueryPath) {
				FromClassOrOuterQueryPath from = (FromClassOrOuterQueryPath) object;
				String path = from.getPath();
				MQLContext mqlContext = new MQLContext(utils.getLabels(from), getResourceDescriptions(from), from);
				try {
					Vector<MQLQuerySegment> segments = mqlContext.resolveFromPath(path, resourceSet.get());
					for (int i = segments.size() - 1; i >= 0; i--) {
						if (path.endsWith(segments.get(i).getSegment())) {
							path = path.substring(0, path.lastIndexOf(segments.get(i).getSegment()));
							if (segments.get(i).getReference() != null) {
								Region region = new Region(textLocation.getOffset() + path.length(), segments.get(i)
										.getSegment().length());
								createHyperlinksTo(resource, region, segments.get(i).getReference(), acceptor);
							}
							if (path.endsWith("."))
								path = path.substring(0, path.lastIndexOf("."));
						}
					}
				} catch (Exception e) {

				}
			} else if (object instanceof Atom || object instanceof IdentPrimary) {

				//we get the whole atom node
				CompositeNode node = NodeUtil.getNode(object);
				String content = "";
				boolean isFunction = false;
				boolean stop = false;

				//we traverse the leafs and try to put the path together
				for (LeafNode leaf : node.getLeafNodes()) {
					if (!stop && leaf.getText() != null && !(content.isEmpty() && leaf.getText().matches("\\s*"))) { //we skip the empty leafs at the beginning
						if (leaf.getText().matches("([a-zA-Z]\\w*|\\.)")) { //if we have a part of identifier we concatenate it
							content = content + leaf.getText();
						} else { //otherwise we stop matching the identifier
							stop = true;
						}
					}
					if (stop && leaf.getText() != null) {
						if (leaf.getText().equals("(")) { //check if it's open bracket
							isFunction = true;
						} else if (!leaf.getText().matches("\\s*")) { //if it's not space we are done
							break;
						}
					}
				}

				if (!content.isEmpty()) {
					//we form the mql context
					MQLContext mqlContext = new MQLContext(utils.getLabels(object), getResourceDescriptions(object),
							object);
					//add params to context
					mqlContext.setParams(utils.getParams(object));

					//do the validation
					try {
						EObject declaration = mqlContext.resolveQueryIdentifier(content, resourceSet.get(), isFunction);
						Region region = new Region(textLocation.getOffset(), content.length());
						createHyperlinksTo(resource, region, declaration, acceptor);
					} catch (Exception e) {

					}

				}

			}
		}
	}

}
