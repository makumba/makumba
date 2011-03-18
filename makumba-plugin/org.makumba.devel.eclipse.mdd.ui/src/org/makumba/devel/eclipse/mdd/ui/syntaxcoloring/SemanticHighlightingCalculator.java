package org.makumba.devel.eclipse.mdd.ui.syntaxcoloring;

import org.eclipse.xtext.CrossReference;
import org.eclipse.xtext.parsetree.AbstractNode;
import org.eclipse.xtext.parsetree.LeafNode;
import org.eclipse.xtext.parsetree.NodeUtil;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.syntaxcoloring.DefaultHighlightingConfiguration;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightedPositionAcceptor;
import org.eclipse.xtext.ui.editor.syntaxcoloring.ISemanticHighlightingCalculator;
import org.makumba.devel.eclipse.mdd.MDD.FromClassOrOuterQueryPath;
import org.makumba.devel.eclipse.mdd.MDD.IdentPrimary;

public class SemanticHighlightingCalculator implements ISemanticHighlightingCalculator {

	public void provideHighlightingFor(final XtextResource resource, IHighlightedPositionAcceptor acceptor) {
		if (resource == null)
			return;

		Iterable<AbstractNode> allNodes = NodeUtil.getAllContents(resource.getParseResult().getRootNode());
		for (AbstractNode abstractNode : allNodes) {
			if (abstractNode.getGrammarElement() instanceof CrossReference) {
				highlightNode(abstractNode, DefaultHighlightingConfiguration.NUMBER_ID, acceptor);
			} else if (abstractNode.getElement() instanceof FromClassOrOuterQueryPath) {
				FromClassOrOuterQueryPath from = (FromClassOrOuterQueryPath) abstractNode.getElement();
				for (LeafNode leaf : abstractNode.getLeafNodes()) {
					if (!leaf.isHidden()) {
						acceptor.addPosition(leaf.getOffset(), from.getPath().length(),
								DefaultHighlightingConfiguration.NUMBER_ID);
						break;
					}
				}
			} else if (abstractNode.getElement() instanceof IdentPrimary) {

				String content = "";
				boolean isFunction = false;
				boolean stop = false;
				int offset = -1;

				//we traverse the leafs and try to put the path together
				for (LeafNode leaf : abstractNode.getLeafNodes()) {
					if (!stop && leaf.getText() != null && !(content.isEmpty() && leaf.getText().matches("\\s*"))) { //we skip the empty leafs at the beginning
						if (leaf.getText().matches("([a-zA-Z]\\w*|\\.)")) { //if we have a part of identifier we concatenate it
							if (offset == -1)
								offset = leaf.getOffset();
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
				if (content.contains(".") || !isFunction)
					acceptor.addPosition(offset, content.length(), DefaultHighlightingConfiguration.NUMBER_ID);
			}
		}
	}

	private void highlightNode(AbstractNode node, String id, IHighlightedPositionAcceptor acceptor) {
		if (node == null)
			return;
		if (node instanceof LeafNode) {
			acceptor.addPosition(node.getOffset(), node.getLength(), id);
		} else {
			for (LeafNode leaf : node.getLeafNodes()) {
				if (!leaf.isHidden()) {
					acceptor.addPosition(leaf.getOffset(), leaf.getLength(), id);
				}
			}
		}
	}
}
