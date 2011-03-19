package org.makumba.devel.eclipse.jsp.ui.contentassist;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jst.jsp.core.taglib.ITaglibDescriptor;
import org.eclipse.jst.jsp.core.taglib.ITaglibRecord;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.eclipse.wst.xml.ui.internal.contentassist.DefaultXMLCompletionProposalComputer;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.makumba.devel.eclipse.jsp.ui.MakumbaJSPProcessor;
import org.makumba.devel.eclipse.mdd.MDDUtils;
import org.makumba.devel.eclipse.mdd.MQLContext;
import org.makumba.devel.eclipse.mdd.ui.contentassist.MQLProposalProvider;
import org.w3c.dom.Node;

import com.google.inject.Inject;

@SuppressWarnings("restriction")
// TODO: rewrite class so it's not using restricted API
// might not be needed if the API will become public
public class MakumbaJSPCompletionProposalComputer extends DefaultXMLCompletionProposalComputer {

	@Inject
	private IResourceDescriptions resourceDescriptions;

	@Inject
	private ResourceSet resourceSet;

	@Inject
	private ILabelProvider labelProvider;

	private ContentAssistRequest request;

	private MakumbaJSPProcessor processor;

	protected LinkedHashMap<String, String> objects;

	//protected Set<String> prefixes;

	private static Pattern Path = Pattern.compile("\\$?\\w+(\\.\\w+)*\\.?$");
	private static Pattern StringConstant = Pattern.compile("'[^']*'?$");
	private static Pattern ComapreToPath = Pattern.compile("(\\w+(\\.\\w+)*\\s*)((=)|(<>)|(>)|(<)|(>=)|(<=)|(!=))\\s*");
	private static Pattern FromRange = Pattern.compile("(\\w+(\\.\\w+)*)\\s+(\\w+)\\s*(,)?\\s*");

	@Override
	protected void addAttributeValueProposals(ContentAssistRequest contentAssistRequest,
			CompletionProposalInvocationContext context) {

		request = contentAssistRequest;

		processor = new MakumbaJSPProcessor(getFile(context.getDocument()));

		Node node = request.getNode();
		if (processor.isMakumbaNode(node)) {

			//compute the parent mak objects (list and object tags) and add them to the query context
			MQLContext qContext = new MQLContext(getAllLabels(), resourceDescriptions, getFile(context.getDocument()));

			MQLProposalProvider mpp = new MQLProposalProvider(qContext, resourceSet, labelProvider);

			String tagName = node.getLocalName(); //get the tag name (without the prefix)
			String attributeName = getAttributName(request); //compute attribute name
			if (attributeName == null) // if we don't know the attribute name we have nothing to do here
				return;

			//TODO:Improve the proposals to also look ahead and replace the content

			String match = stripQuotes(request.getMatchString()).replaceAll("^\\s*", "");
			int offset = context.getInvocationOffset();

			if (tagName.equals("list") || tagName.equals("object")) {
				if (attributeName.equals("from")) {

					match = match.contains(",") ? match.substring(match.lastIndexOf(",") + 1).replaceAll("^\\s*", "")
							: match;

					addProposals(mpp.getPathProposals(match, offset, 1100, MDDUtils.PointerOrSetFilter));
					addProposals(mpp.getDataDefinitionProposals(match, offset, 1090));

				} else if (attributeName.equals("where") || attributeName.equals("orderBy")
						|| attributeName.equals("groupBy")) {
					expressionProposals(mpp, match, offset);
				} else if (attributeName.equals("orderBy")) {

				}
			} else if ((tagName.equals("value") && attributeName.equals("expr"))
					|| (tagName.equals("if") && attributeName.equals("test"))
					|| (tagName.equals("input") && attributeName.equals("value"))) {
				expressionProposals(mpp, match, offset);
			} else if (tagName.equals("addForm")) {
				if (Pattern.matches("\\s*\\w*", match)) {//if we have a simple field
					if (attributeName.equals("object")) { //object attribute, get labels
						addProposals(mpp.getLabelProposals(match, offset, 1000));
					} else if (attributeName.equals("field")) { //field attribute, get setComplex fields
						Node tag = request.getParent(); //get the tag node
						Node object = tag.getAttributes().getNamedItem("object"); //get tag's attribute 'object'
						if (object != null) { // compute the proposals
							addProposals(mpp.getPathProposals(object.getNodeValue() + "." + match.trim(), offset, 1000,
									MDDUtils.SetComplex));
						}
					}
				}
			} else if ((tagName.equals("delete") || tagName.equals("deleteLink") || tagName.equals("editForm"))
					&& attributeName.equals("object")) {
				if (Pattern.matches("\\s*\\w*", match)) {
					addProposals(mpp.getLabelProposals(match, offset, 1000));
				}
			} else if (tagName.equals("newForm")) {
				if (attributeName.equals("type")) {
					if (Pattern.matches("\\s*\\w*", match)) {
						//TODO: check what types can come in this situation
						addProposals(mpp.getFieldLabelProposals(match, offset, 1100, MDDUtils.PtrOne));
					}
					addProposals(mpp.getDataDefinitionProposals(match, offset, 1000));
				}
			} else if (tagName.equals("input")) {
				if (attributeName.equals("field") || attributeName.equals("name")) {
					if (Pattern.matches("\\s*\\w*", match)) {
						Node parent = getParentObjectNode();
						String path = getObjectLabel(parent) + "." + match;
						if (parent.getLocalName().equals("editForm")) {
							addProposals(mpp.getPathProposals(path, offset, 1000, MDDUtils.NotFixedField));
						} else {
							addProposals(mpp.getPathProposals(path, offset, 1000, MDDUtils.Field));
						}
					}
				}
			} else if (tagName.equals("searchForm")) {
				if (attributeName.equals("in")) {
					addProposals(mpp.getDataDefinitionProposals(match, offset, 1000));
				}
			}

		}
	}

	private void expressionProposals(MQLProposalProvider mpp, String match, int offset) {
		Matcher m = Path.matcher(match);

		//we see if we are in comparison situation and then check for 
		//enum proposals
		Matcher cm = ComapreToPath.matcher(match);
		Matcher vm = StringConstant.matcher(match);
		String value = vm.find() ? vm.group() : "";
		while (cm.find()) {
			//we check if LHS of the comparison is the one we are looking for (the last one)
			if ((value == "" && cm.end() == match.length()) || (value != "" && cm.end() == vm.start())) {
				addProposals(mpp.getEnumValueProposals(cm.group(1), value, offset, 1200));
			}
		}
		match = m.find() ? m.group() : "";

		addProposals(mpp.getPathProposals(match, offset, 1100, MDDUtils.FieldOrFunction));
		addProposals(mpp.getQueryFunctionProposals(match, offset, 1090));
		addProposals(mpp.getAggregateFunctionProposals(match, offset, 1080));
	}

	private void addProposals(Collection<ICompletionProposal> proposals) {
		for (ICompletionProposal proposal : proposals) {
			request.addProposal(proposal);
		}
	}

	private String stripQuotes(String matchString) {
		String result = matchString;
		if (result != null && !result.isEmpty()) {
			if (result.charAt(0) == '"')
				result = result.substring(1);
			if (!result.isEmpty() && result.charAt(result.length() - 1) == '"')
				result = result.substring(0, result.length() - 1);
		}
		return result;
	}

	//TODO:deal with ending quite
	//TODO:get subfields (ptrs or sets) from parent

	private Node getParentObjectNode() {
		Node node = request.getParent();
		String tagName = node.getLocalName();
		while (node != null) {
			if (processor.isMakumbaNode(node)
					&& (tagName.equals("addForm") || tagName.equals("editForm") || tagName.equals("newForm"))) {
				return node;
			}
			node = node.getParentNode();
			if (node != null)
				tagName = node.getLocalName();
		}
		return node;
	}

	private String getObjectLabel(Node node) {
		if (node != null) {
			String tagName = node.getLocalName();
			if (tagName.equals("addForm")) {
				Node object = node.getAttributes().getNamedItem("object");
				Node field = node.getAttributes().getNamedItem("field");
				if (object != null && field != null) {
					return object.getNodeValue() + "." + field.getNodeValue();
				}
			} else if (tagName.equals("editForm")) {
				Node object = node.getAttributes().getNamedItem("object");
				if (object != null) {
					return object.getNodeValue();
				}
			} else if (tagName.equals("newForm")) {
				Node type = node.getAttributes().getNamedItem("type");
				if (type != null) {
					return type.getNodeValue();
				}
			}
		}
		return "";
	}

	private LinkedHashMap<String, String> getAllLabels() {
		LinkedHashMap<String, String> labels = new LinkedHashMap<String, String>();
		Node current = request.getParent();
		while (current != null) {
			if (processor.isMakumbaNode(current)) {
				if (current.getLocalName().equals("list") || current.getLocalName().equals("object")) {
					Node listNode = current.getAttributes().getNamedItem("from");
					labels.putAll(parseFrom(listNode.getNodeValue()));
				}
			}
			current = current.getParentNode();
		}
		return labels;
	}

	private LinkedHashMap<String, String> parseFrom(String from) {
		LinkedHashMap<String, String> labels = new LinkedHashMap<String, String>();
		if (from != null && !from.trim().isEmpty()) {
			Matcher fromRanges = FromRange.matcher(from);
			while (fromRanges.find()) {
				labels.put(fromRanges.group(3), fromRanges.group(1));
			}
		}
		return labels;
	}

	protected boolean isMakumbaTaglib(ITaglibRecord taglibRecord) {
		ITaglibDescriptor descriptor = taglibRecord.getDescriptor();
		if (descriptor.getShortName().equals("mak")) {
			return true;
		}
		return false;
	}

	private String getAttributName(ContentAssistRequest contentAssistRequest) {
		int currentRegionId = contentAssistRequest.getDocumentRegion().getRegions()
				.indexOf(contentAssistRequest.getRegion());
		try {
			ITextRegion attributeNameRegion = contentAssistRequest.getDocumentRegion().getRegions()
					.get(currentRegionId - 2);
			if (attributeNameRegion.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_NAME)
				return contentAssistRequest.getDocumentRegion().getFullText(attributeNameRegion);
		} catch (Exception e) {
			//TODO:
			//MDDUiModule.createErrorStatus(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Returns the eclipse resource of the given document. The resource might
	 * not exist, but it's not the responsibility of this method to check it.
	 * 
	 * @param document
	 * @return
	 */
	public final IFile getFile(IDocument document) {
		IFile resource = null;
		String baselocation = null;
		if (document != null) {
			IStructuredModel model = null;
			try {
				model = org.eclipse.wst.sse.core.StructuredModelManager.getModelManager().getExistingModelForRead(
						document);
				if (model != null) {
					baselocation = model.getBaseLocation();
				}
			} finally {
				if (model != null) {
					model.releaseFromRead();
				}
			}
		}
		if (baselocation != null) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IPath filePath = new Path(baselocation);
			if (filePath.segmentCount() > 0) {
				resource = root.getFile(filePath);
			}
		}

		return resource;

	}
}
