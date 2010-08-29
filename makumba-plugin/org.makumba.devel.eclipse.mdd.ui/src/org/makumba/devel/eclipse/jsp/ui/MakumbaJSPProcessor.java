package org.makumba.devel.eclipse.jsp.ui;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jst.jsp.core.internal.Logger;
import org.eclipse.jst.jsp.core.internal.provisional.JSP11Namespace;
import org.eclipse.jst.jsp.core.internal.regions.DOMJSPRegionContexts;
import org.eclipse.jst.jsp.core.internal.validation.JSPDirectiveValidator;
import org.eclipse.jst.jsp.core.internal.validation.JSPValidator;
import org.eclipse.jst.jsp.core.taglib.ITaglibDescriptor;
import org.eclipse.jst.jsp.core.taglib.ITaglibRecord;
import org.eclipse.jst.jsp.core.taglib.TaglibIndex;
import org.eclipse.jst.jsp.ui.internal.contentassist.JSPTaglibCompletionProposalComputer;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionCollection;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
import org.eclipse.wst.sse.core.utils.StringUtils;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.w3c.dom.Node;

@SuppressWarnings("restriction")
public class MakumbaJSPProcessor {

	private IStructuredDocument fDocument;
	private Set<String> fPrefixes;
	private Set<Integer> fOffsets;
	private static Pattern FromRange = Pattern.compile("(\\w+(\\.\\w+)*)\\s+(\\w+)\\s*(,)?\\s*");

	public MakumbaJSPProcessor(IFile file) {
		IStructuredModel model = null;
		try {
			// get jsp model, get tranlsation
			model = StructuredModelManager.getModelManager().getModelForRead(file);
			if (model != null) {
				fDocument = model.getStructuredDocument();
				setMakPrefixes();

				//				if (model instanceof IDOMModel) {
				//					IDOMModel domModel = (IDOMModel) model;
				//					ModelHandlerForJSP.ensureTranslationAdapterFactory(domModel);
				//
				//					IDOMDocument xmlDoc = domModel.getDocument();
				//
				//					System.out.println(xmlDoc.getNodeName());
				//
				//				}

			}
		} catch (IOException e) {
			Logger.logException(e);
		} catch (CoreException e) {
			Logger.logException(e);
		} finally {
			if (model != null)
				model.releaseFromRead();
		}
	}

	public boolean isMakumbaNode(Node node) {
		return isMakumbaTaglibPrefix(node.getPrefix());
	}

	public boolean isMakumbaTaglibPrefix(String prefix) {
		if (fPrefixes != null)
			return fPrefixes.contains(prefix);
		return false;
	}

	public boolean hasMakumbaTaglib() {
		return fPrefixes.size() > 0;
	}

	public int findOffset(String path) {
		fDocument.getFirstStructuredDocumentRegion();

		FindReplaceDocumentAdapter frda = new FindReplaceDocumentAdapter(fDocument);
		try {
			IRegion match = frda.find(0, "name", true, true, false, true);
			while (match != null) {
				int offset = match.getOffset() + 1;
				int length = match.getLength() - 2;
				IStructuredDocumentRegion[] regions = fDocument.getStructuredDocumentRegions(offset, length);
				if (regions.length == 1) { //if it covers more regions, something is wrong
					IStructuredDocumentRegion region = regions[0];
					if (region.getType().equals(DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE)) {

					}
				}

			}

		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return -1;

	}

	private void setMakPrefixes() {
		IStructuredDocumentRegion region = fDocument.getFirstStructuredDocumentRegion();
		fPrefixes = new HashSet<String>();
		while (region != null) {
			if (region.getType() == DOMJSPRegionContexts.JSP_DIRECTIVE_NAME) {
				String directiveName = getDirectiveName(region);
				if (directiveName.equals("taglib")) { //$NON-NLS-1$
					ITextRegion uriValueRegion = getAttributeValueRegion(region, JSP11Namespace.ATTR_NAME_URI);
					ITextRegion prefixValueRegion = getAttributeValueRegion(region, JSP11Namespace.ATTR_NAME_PREFIX);
					if (prefixValueRegion != null && uriValueRegion != null) {
						String uri = region.getText(uriValueRegion);
						uri = StringUtils.stripQuotes(uri);
						if (uri.length() > 0) {
							ITaglibRecord reference = TaglibIndex.resolve(getBasePath(region).toString(), uri, false);
							if (isMakumbaTaglib(reference)) {
								String taglibPrefix = region.getText(prefixValueRegion);
								taglibPrefix = StringUtils.stripQuotes(taglibPrefix);
								if (!taglibPrefix.trim().isEmpty()) {
									fPrefixes.add(taglibPrefix);
								}
							}
						}
					}
				}
			}
			region = region.getNext();
		}
	}

	protected boolean isMakumbaTaglib(ITaglibRecord taglibRecord) {
		ITaglibDescriptor descriptor = taglibRecord.getDescriptor();
		if (descriptor.getShortName().equals("mak")) {
			return true;
		}
		return false;
	}

	/**
	 * Borrowed from {@link JSPDirectiveValidator}<br>
	 * 
	 * @param collection
	 * @return the jsp directive name
	 */
	private String getDirectiveName(ITextRegionCollection collection) {
		String name = ""; //$NON-NLS-1$
		ITextRegionList subRegions = collection.getRegions();
		for (int j = 0; j < subRegions.size(); j++) {
			ITextRegion subRegion = subRegions.get(j);
			if (subRegion.getType() == DOMJSPRegionContexts.JSP_DIRECTIVE_NAME) {
				name = collection.getText(subRegion);
				break;
			}
		}
		return name;
	}

	/**
	 * Borrowed from {@link JSPValidator}<br>
	 * 
	 * @param sdr
	 * @param attrName
	 * @return the ITextRegion for the attribute value of the given attribute
	 *         name, case sensitive, null if no matching attribute is found
	 */
	private ITextRegion getAttributeValueRegion(ITextRegionCollection sdr, String attrName) {
		ITextRegion valueRegion = null;
		ITextRegionList subRegions = sdr.getRegions();
		for (int i = 0; i < subRegions.size(); i++) {
			ITextRegion subRegion = subRegions.get(i);
			if (subRegion.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_NAME
					&& sdr.getText(subRegion).equals(attrName)) {
				for (int j = i; j < subRegions.size(); j++) {
					subRegion = subRegions.get(j);
					if (subRegion.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE) {
						valueRegion = subRegion;
						break;
					}
				}
				break;
			}
		}
		return valueRegion;
	}

	/**
	 * Borrowed from {@link JSPTaglibCompletionProposalComputer}<br>
	 * 
	 * Returns project request is in
	 * 
	 * @param request
	 * @return
	 */
	private IPath getBasePath(IStructuredDocumentRegion region) {
		IPath baselocation = null;
		if (region != null) {
			IDocument document = region.getParentDocument();
			IStructuredModel model = null;
			try {
				model = StructuredModelManager.getModelManager().getExistingModelForRead(document);
				if (model != null) {
					String location = model.getBaseLocation();
					if (location != null) {
						baselocation = new Path(location);
					}
				}
			} finally {
				if (model != null)
					model.releaseFromRead();
			}
		}
		return baselocation;
	}

}
