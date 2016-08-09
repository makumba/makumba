package org.makumba.devel.eclipse.mdd.ui.refactoring;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.parsetree.CompositeNode;
import org.eclipse.xtext.parsetree.NodeAdapter;
import org.eclipse.xtext.parsetree.NodeUtil;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;

public class URIFragmentResolver implements IUnitOfWork<CompositeNode, XtextResource> {
	private String uriFragment;

	public URIFragmentResolver(String uriFragment) {
		this.uriFragment = uriFragment;
	}

	public CompositeNode exec(XtextResource state) throws Exception {
		EObject o = state.getEObject(uriFragment);
		NodeAdapter node = NodeUtil.getNodeAdapter(o);

		return node.getParserNode();

	}
}
