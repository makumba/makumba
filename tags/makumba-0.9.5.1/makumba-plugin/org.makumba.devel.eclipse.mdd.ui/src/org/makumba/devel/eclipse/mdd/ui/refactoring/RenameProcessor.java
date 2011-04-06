package org.makumba.devel.eclipse.mdd.ui.refactoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.core.text.TextSearchMatchAccess;
import org.eclipse.search.core.text.TextSearchRequestor;
import org.eclipse.search.core.text.TextSearchScope;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEditGroup;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.parsetree.CompositeNode;
import org.eclipse.xtext.parsetree.NodeAdapter;
import org.eclipse.xtext.parsetree.NodeUtil;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.makumba.devel.eclipse.jsp.ui.MakumbaJSPFileVisitor;
import org.makumba.devel.eclipse.jsp.ui.MakumbaJSPProcessor;
import org.makumba.devel.eclipse.mdd.MDD.DataDefinition;
import org.makumba.devel.eclipse.mdd.MDD.FieldDeclaration;
import org.makumba.devel.eclipse.mdd.MDD.SubFieldDeclaration;
import org.makumba.devel.eclipse.mdd.validation.MDDJavaValidator;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * @author filip
 * 
 *         TODO: add MDD mql checking TODO: add JSP reference checking TODO:
 *         refactor a bit (insead of using maps, create a special class to store
 *         all found matches)
 * 
 */
public class RenameProcessor extends org.eclipse.ltk.core.refactoring.participants.RenameProcessor {

	private XtextEditor editor;
	private IResourceDescriptions resourceDescriptions;
	private List<CompositeNode> localReferences = null;
	private Map<IResourceDescription, List<CompositeNode>> references = new HashMap<IResourceDescription, List<CompositeNode>>();
	private Map<IFile, List<Integer>> jspRefrences = new HashMap<IFile, List<Integer>>();
	private Map<IFile, Set<Integer>> jspTextReferences = new HashMap<IFile, Set<Integer>>();
	private Map<IFile, Set<Integer>> javaTextReferences = new HashMap<IFile, Set<Integer>>();
	private String currentName;
	private CompositeNode declaration;
	private IFile file;
	private ResourceSet resourceSet;
	private IQualifiedNameProvider nameProvider;
	private String reportFail = null;
	private IEObjectDescription eObjectDescription;

	public RenameProcessor(XtextEditor editor, IResourceDescriptions resourceDescriptions, ResourceSet resourceSet,
			IQualifiedNameProvider nameProvider) {
		this.editor = editor;
		this.resourceDescriptions = resourceDescriptions;
		this.resourceSet = resourceSet;
		this.nameProvider = nameProvider;

		final ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();
		eObjectDescription = editor.getDocument().readOnly(new EObjectResolver(selection, resourceDescriptions));

		declaration = editor.getDocument().readOnly(
				new URIFragmentResolver(eObjectDescription.getEObjectURI().fragment()));

		if (declaration.getElement() instanceof FieldDeclaration) {
			FieldDeclaration r = (FieldDeclaration) declaration.getElement();
			currentName = r.getName();

			file = (IFile) editor.getEditorInput().getAdapter(IFile.class);

			if (eObjectDescription != null) {
				findReferenceDescriptions();
				//findJSPReferences(eObjectDescription);
				findJSPTextResults();
				findJavaTextResults();
			}
		} else {
			reportFail = "You can only rename fields this way!";
		}

	}

	private void findJSPTextResults() {
		IFile ddFile = ResourcesPlugin.getWorkspace().getRoot()
				.getFile(new Path(eObjectDescription.getEObjectURI().toPlatformString(true)));
		IProject project = ddFile.getProject();

		final IVirtualFolder vf = ComponentCore.createComponent(project).getRootFolder();
		IFolder contentFolder = (IFolder) vf.getUnderlyingFolder();

		TextSearchEngine tse = TextSearchEngine.create();
		IResource[] res = { contentFolder };
		TextSearchScope tss = TextSearchScope.newSearchScope(res, Pattern.compile(".+\\.jsp"), true);
		TextSearchRequestor tsr = new TextSearchRequestor() {
			@Override
			public boolean acceptPatternMatch(TextSearchMatchAccess matchAccess) throws CoreException {

				Set<Integer> offsets;
				if (jspTextReferences.containsKey(matchAccess.getFile())) {
					offsets = jspTextReferences.get(matchAccess.getFile());
					offsets.add(matchAccess.getMatchOffset());
				} else {
					offsets = new HashSet<Integer>();
					offsets.add(matchAccess.getMatchOffset());
					jspTextReferences.put(matchAccess.getFile(), offsets);
				}

				return true;
			}
		};
		String fieldName = ((FieldDeclaration) declaration.getElement()).getName();
		tse.search(tss, tsr, Pattern.compile("[^a-zA-Z0-9_]" + fieldName + "[^a-zA-Z0-9_]"), null);
	}

	private void findJavaTextResults() {
		IFile ddFile = ResourcesPlugin.getWorkspace().getRoot()
				.getFile(new Path(eObjectDescription.getEObjectURI().toPlatformString(true)));
		IProject project = ddFile.getProject();

		final IVirtualFolder vf = ComponentCore.createComponent(project).getRootFolder().getFolder("WEB-INF/classes");
		IFolder contentFolder = (IFolder) vf.getUnderlyingFolder();

		TextSearchEngine tse = TextSearchEngine.create();
		IResource[] res = { contentFolder };
		TextSearchScope tss = TextSearchScope.newSearchScope(res, Pattern.compile(".+\\.java"), true);
		TextSearchRequestor tsr = new TextSearchRequestor() {
			@Override
			public boolean acceptPatternMatch(TextSearchMatchAccess matchAccess) throws CoreException {

				Set<Integer> offsets;
				if (javaTextReferences.containsKey(matchAccess.getFile())) {
					offsets = javaTextReferences.get(matchAccess.getFile());
					offsets.add(matchAccess.getMatchOffset());
				} else {
					offsets = new HashSet<Integer>();
					offsets.add(matchAccess.getMatchOffset());
					javaTextReferences.put(matchAccess.getFile(), offsets);
				}

				return true;
			}
		};
		String fieldName = ((FieldDeclaration) declaration.getElement()).getName();
		tse.search(tss, tsr, Pattern.compile("[^a-zA-Z0-9_]" + fieldName + "[^a-zA-Z0-9_]"), null);
	}

	private void findJSPReferences(IEObjectDescription eObjectDescription) {
		EObject object = resourceSet.getEObject(eObjectDescription.getEObjectURI(), true);
		if (object instanceof FieldDeclaration) {
			FieldDeclaration field = (FieldDeclaration) object;
			String qualifiedName = field.getName();
			EObject current = field.eContainer();
			while (current != null && !(current instanceof DataDefinition)) {
				if (current instanceof SubFieldDeclaration) {
					qualifiedName = ((SubFieldDeclaration) current).getSubFieldOf().getName() + "." + qualifiedName;
				}
				current = current.eContainer();
			}
			if (current != null) {
				DataDefinition dataDefinition = (DataDefinition) current;
				qualifiedName = nameProvider.getQualifiedName(dataDefinition) + "." + qualifiedName;

			}
			IFile ddFile = ResourcesPlugin.getWorkspace().getRoot()
					.getFile(new Path(eObjectDescription.getEObjectURI().toPlatformString(true)));
			IProject project = ddFile.getProject();
			MakumbaJSPFileVisitor visitor = new MakumbaJSPFileVisitor();

			try {
				project.accept(visitor, IResource.DEPTH_INFINITE);
			} catch (CoreException e) {
				e.printStackTrace();
			}
			IFile[] files = visitor.getFiles();
			for (IFile file : files) {
				MakumbaJSPProcessor mjp = new MakumbaJSPProcessor(file);
				if (mjp.hasMakumbaTaglib()) {
					List<Integer> offsets = new ArrayList<Integer>();
					offsets.add(mjp.findOffset(""));
					if (offsets.size() > 0) {
						jspRefrences.put(file, offsets);
					}
				}
			}

		}
	}

	@Override
	public Object[] getElements() {
		return null;
	}

	@Override
	public String getIdentifier() {
		return "Rename Processor Identifier";
	}

	@Override
	public String getProcessorName() {
		return "Rename Processor";
	}

	@Override
	public boolean isApplicable() throws CoreException {
		return (localReferences != null);
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();

		if (reportFail != null) {
			status.addFatalError(reportFail);
		} else if (localReferences == null)
			status.addFatalError("Could not obtain references!");

		return status;
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context)
			throws CoreException, OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		RenameRefactoring refactoring = (RenameRefactoring) getRefactoring();
		if (currentName.equals(refactoring.getRenameText())) {
			status.addFatalError("Name unchanged!");
		} else if (refactoring.getRenameText().trim().length() <= 0) {
			status.addFatalError("Name must not be empty!");
		} else if (MDDJavaValidator.checkValidName(refactoring.getRenameText()) != null) {
			status.addFatalError(MDDJavaValidator.checkValidName(refactoring.getRenameText()));
		}
		return status;
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		CompositeChange compositeChange = new CompositeChange("Field rename");
		pm.beginTask("Rename Refactoring", localReferences.size());

		//Get the refactoring information
		RenameRefactoring refactoring = (RenameRefactoring) getRefactoring();
		String replaceText = refactoring.getRenameText();
		int replacementLength = ((FieldDeclaration) declaration.getElement()).getName().length();

		CompositeChange mddChange = new CompositeChange("MDD Changes");

		//Prepare changes in current mdd file
		MultiTextEdit multiEdit = new MultiTextEdit();
		TextFileChange fileChange = new TextFileChange("Declaraction Renaming", file);
		fileChange.setEdit(multiEdit);
		fileChange.setTextType("mdd");
		mddChange.add(fileChange);

		//Creating changes for declaration in current mdd file
		ReplaceEdit replaceEdit = new ReplaceEdit(declaration.getOffset(), replacementLength, replaceText);
		multiEdit.addChild(replaceEdit);
		TextEditGroup editGroup = new TextEditGroup("declaration update", replaceEdit);
		fileChange.addTextEditGroup(editGroup);

		//Creating changes for references in current mdd file
		createReplaceEdits(localReferences, replacementLength, replaceText, "reference update", multiEdit, fileChange);

		//Creating changes for references in other mdd files
		for (IResourceDescription rd : references.keySet()) {
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(rd.getURI().toPlatformString(true)));
			multiEdit = new MultiTextEdit();
			fileChange = new TextFileChange("Reference Renaming", file);
			fileChange.setEdit(multiEdit);
			fileChange.setTextType("mdd");
			mddChange.add(fileChange);
			createReplaceEdits(references.get(rd), replacementLength, replaceText, "reference update", multiEdit,
					fileChange);
		}

		CompositeChange jspChange = new CompositeChange("JSP Changes");

		for (IFile file : jspRefrences.keySet()) {
			multiEdit = new MultiTextEdit();
			fileChange = new TextFileChange("Reference Renaming", file);
			fileChange.setEdit(multiEdit);
			fileChange.setTextType("jsp");
			jspChange.add(fileChange);
			for (Integer offset : jspRefrences.get(file)) {
				ReplaceEdit replaceEdit1 = new ReplaceEdit(offset, replacementLength, replaceText);
				multiEdit.addChild(replaceEdit1);
				TextEditGroup editGroup1 = new TextEditGroup("refrence update", replaceEdit1);
				fileChange.addTextEditGroup(editGroup1);
			}
		}

		for (IFile file : jspTextReferences.keySet()) {
			multiEdit = new MultiTextEdit();
			fileChange = new TextFileChange("Text reference renaming", file);
			fileChange.setEdit(multiEdit);
			fileChange.setTextType("jsp");
			jspChange.add(fileChange);
			for (Integer offset : jspTextReferences.get(file)) {
				ReplaceEdit replaceEdit1 = new ReplaceEdit(offset + 1, replacementLength, replaceText);
				multiEdit.addChild(replaceEdit1);
				TextEditGroup editGroup1 = new TextEditGroup("text refrence update", replaceEdit1);
				fileChange.addTextEditGroup(editGroup1);
			}
		}

		CompositeChange javaChange = new CompositeChange("Java Changes");

		for (IFile file : javaTextReferences.keySet()) {
			multiEdit = new MultiTextEdit();
			fileChange = new TextFileChange("Text reference renaming", file);
			fileChange.setEdit(multiEdit);
			fileChange.setTextType("java");
			javaChange.add(fileChange);
			for (Integer offset : javaTextReferences.get(file)) {
				ReplaceEdit replaceEdit1 = new ReplaceEdit(offset + 1, replacementLength, replaceText);
				multiEdit.addChild(replaceEdit1);
				TextEditGroup editGroup1 = new TextEditGroup("text refrence update", replaceEdit1);
				fileChange.addTextEditGroup(editGroup1);
			}
		}

		compositeChange.add(mddChange);
		compositeChange.add(jspChange);
		compositeChange.add(javaChange);
		return compositeChange;
	}

	private void createReplaceEdits(List<CompositeNode> nodes, int replacementLength, String replacementText,
			String replacementType, MultiTextEdit multiEdit, TextFileChange fileChange) {
		for (CompositeNode node : nodes) {
			ReplaceEdit replaceEdit = new ReplaceEdit(node.getOffset(), replacementLength, replacementText);
			multiEdit.addChild(replaceEdit);
			TextEditGroup editGroup = new TextEditGroup(replacementType, replaceEdit);
			fileChange.addTextEditGroup(editGroup);
		}
	}

	@Override
	public RefactoringParticipant[] loadParticipants(RefactoringStatus status, SharableParticipants sharedParticipants)
			throws CoreException {
		return null;
	}

	private void findReferenceDescriptions() {
		localReferences = new ArrayList<CompositeNode>();

		for (IResourceDescription resourceDescription : resourceDescriptions.getAllResourceDescriptions()) {
			List<CompositeNode> refs = new ArrayList<CompositeNode>();
			Iterable<IReferenceDescription> matchingReferenceDescriptors = Iterables.filter(
					resourceDescription.getReferenceDescriptions(), new Predicate<IReferenceDescription>() {
						public boolean apply(IReferenceDescription input) {
							return eObjectDescription.getEObjectURI().equals(input.getTargetEObjectUri());
						}
					});
			for (IReferenceDescription matchingReferenceDescription : matchingReferenceDescriptors) {

				if (eObjectDescription.getEObjectURI().trimFragment()
						.equals(matchingReferenceDescription.getSourceEObjectUri().trimFragment())) {
					CompositeNode node = editor.getDocument().readOnly(
							new URIFragmentResolver(matchingReferenceDescription.getSourceEObjectUri().fragment()));
					localReferences.add(node);
				} else {
					EObject o = resourceSet.getEObject(matchingReferenceDescription.getContainerEObjectURI(), true);
					EObject f = o.eResource().getEObject(matchingReferenceDescription.getSourceEObjectUri().fragment());
					NodeAdapter node = NodeUtil.getNodeAdapter(f);
					CompositeNode n = node.getParserNode();
					refs.add(n);
				}
			}
			if (refs.size() > 0) {
				references.put(resourceDescription, refs);
			}
		}
	}

}
