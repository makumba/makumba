package org.makumba.devel.eclipse.mdd.ui.contentassist;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.ui.editor.contentassist.ConfigurableCompletionProposal;
import org.eclipse.xtext.ui.editor.contentassist.PrefixMatcher;
import org.makumba.devel.eclipse.mdd.MDDUtils;
import org.makumba.devel.eclipse.mdd.MQLContext;
import org.makumba.devel.eclipse.mdd.MDD.DataDefinition;
import org.makumba.devel.eclipse.mdd.MDD.Declaration;
import org.makumba.devel.eclipse.mdd.MDD.EnumValue;
import org.makumba.devel.eclipse.mdd.MDD.FieldDeclaration;
import org.makumba.devel.eclipse.mdd.MDD.FunctionArgumentBody;
import org.makumba.devel.eclipse.mdd.MDD.FunctionArgumentDeclaration;
import org.makumba.devel.eclipse.mdd.MDD.FunctionDeclaration;
import org.makumba.devel.eclipse.mdd.MDD.IntEnum;
import org.makumba.devel.eclipse.mdd.MDD.PointerType;
import org.makumba.devel.eclipse.mdd.MDD.SetType;
import org.makumba.devel.eclipse.mdd.ui.MDDExecutableExtensionFactory;
import org.makumba.devel.eclipse.mdd.ui.labeling.MDDLabelProvider;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * @author filip
 * 
 */
public class MQLProposalProvider extends MDDExecutableExtensionFactory {

	private ResourceSet resourceSet; // = getInjector().getInstance(ResourceSet.class);;

	private ILabelProvider labelProvider; //= getInjector().getInstance(ILabelProvider.class);;

	MQLContext context;

	public MQLProposalProvider(MQLContext context, ResourceSet resourceSet, ILabelProvider labelProvider) {
		this.context = context;
		this.labelProvider = labelProvider;
		this.resourceSet = resourceSet;
	}

	public Set<ICompletionProposal> getPathProposals(String currentPath, int insertOffset, int priority,
			Predicate<Declaration> filter) {
		//TODO: change to some ordered set
		Set<ICompletionProposal> proposals = new LinkedHashSet<ICompletionProposal>();
		if (currentPath.indexOf(".") < 0) { //its a simple label 
			proposals.addAll(getLabelProposals(currentPath, insertOffset, priority));
		} else { //we have path situation label.field.field... (also label. is possible)
			String label = currentPath.substring(0, currentPath.indexOf(".")); //get the label
			String path = context.resolveLabel(label) + currentPath.substring(currentPath.indexOf("."));

			proposals.addAll(getPathWithoutLabelsProposals(path, insertOffset, priority, filter));

		}
		return proposals;
	}

	/**
	 * Creates and returns all the proposals that are found on the path. The
	 * path is consisted of data type (representing {@link DataDefinition} name)
	 * referencing fields (which are either {@link PointerType} or
	 * {@link SetType}) and match pattern combined in as:<br>
	 * <br>
	 * <code>dataTypeName.field.field.matchPattern</code> <br>
	 * <br>
	 * 
	 * It then gets the declarations on path
	 * <code>dataTypeName.field.field</code>, filters them with the given
	 * {@link Predicate} and matches their name to the remaining
	 * <code>matchPattern</code>. Finally the proposals are created from the
	 * remaining declarations on the given <code>inserOffset</code>.
	 * 
	 * @param currentPath
	 *            of syntax <code>dataTypeName.field.field.matchPattern</code>
	 * @param insertOffset
	 *            position where the proposal will insert the replacement text
	 * @param priority
	 *            TODO
	 * @param filter
	 *            used to filter the final declarations (not counting the
	 *            matchPattern that will be automatically filtered)
	 * @return set of created proposals
	 */
	public Set<ICompletionProposal> getPathWithoutLabelsProposals(String currentPath, int insertOffset, int priority,
			Predicate<Declaration> filter) {
		Set<ICompletionProposal> proposals = new LinkedHashSet<ICompletionProposal>();

		//we remove the match pattern from the path. we will use it later
		String path = currentPath.substring(0, currentPath.lastIndexOf("."));
		//we get the data definition that the path is starting with
		IEObjectDescription ddDescription = context.getDataDefinition(path);

		if (ddDescription != null) {
			//remove the data definition from the path
			path = path.substring(ddDescription.getName().length());
			//and remove the next dot if it remained
			if (!path.isEmpty() && path.charAt(0) == '.')
				path = path.substring(1);
			//we need to fill the resolve all the information in the resourceset so we can 
			//access the data inside
			EcoreUtil2.resolveAll(resourceSet);
			DataDefinition dd = (DataDefinition) EcoreUtil2.resolve((ddDescription.getEObjectOrProxy()), resourceSet);
			//get the declarations at the end of our path
			Iterable<Declaration> ddDeclarations = context.getDeclarationsOnPath(dd.getD(), path);
			//filter the declarations
			ddDeclarations = Iterables.filter(ddDeclarations, filter);
			//get the prefix of the declaration proposal name
			String matchPattern = currentPath.substring(currentPath.lastIndexOf(".") + 1);
			//and get all the proposals for from the declarations
			proposals.addAll(getDeclarationsProposals(matchPattern, insertOffset, priority, ddDeclarations));
			//if this filter was applied it means we are interested in all fields, so also timestamp
			//fields need to be returned
			if (filter.equals(MDDUtils.FieldOrFunction) && !Iterables.isEmpty(ddDeclarations)) {
				proposals.addAll(getTSFieldProposals(matchPattern, insertOffset, priority));
			}
		}
		return proposals;
	}

	/**
	 * Creates and returns proposal for given declarations that match the
	 * startPattern.
	 * 
	 * @param startPattern
	 *            the string with which the declaration's name must start with
	 * @param insertOffset
	 *            position where the proposal will insert the replacement text
	 * @param priority
	 *            TODO
	 * @param declarations
	 *            to create proposals for
	 * @return
	 */
	public Set<ICompletionProposal> getDeclarationsProposals(String startPattern, int insertOffset, int priority,
			Iterable<Declaration> declarations) {
		Set<ICompletionProposal> proposals = new LinkedHashSet<ICompletionProposal>();
		for (Declaration declaration : declarations) {
			if (declaration instanceof FieldDeclaration) {
				FieldDeclaration field = (FieldDeclaration) declaration;
				if (startsWith(field.getName(), startPattern)) {
					String replacement = field.getName().substring(startPattern.length());
					StyledString displayString = new StyledString();
					displayString.append(text(field));
					ICompletionProposal p = createProposal(replacement, insertOffset, replacement.length(),
							labelProvider.getImage(field), displayString, priority);
					proposals.add(p);
				}
			}
			if (declaration instanceof FunctionDeclaration) {
				FunctionDeclaration func = (FunctionDeclaration) declaration;
				if (startsWith(func.getName(), startPattern)) {
					String replacement = func.getName().substring(startPattern.length()) + "()";
					StyledString displayString = new StyledString();
					displayString.append(text(func));
					ICompletionProposal p = createProposal(replacement, insertOffset, replacement.length() - 1,
							labelProvider.getImage(func), displayString, priority);
					proposals.add(p);
				}
			}
		}
		return proposals;
	}

	public Set<ICompletionProposal> getLabelProposals(String startPattern, int insertOffset, int priority) {
		Set<ICompletionProposal> proposals = new LinkedHashSet<ICompletionProposal>();
		for (String label : context.getLabelsStartingWith(startPattern)) {
			if (context.isValidLabel(label)) {
				String replacement = label.substring(startPattern.length());
				StyledString displayString = new StyledString();
				displayString.append(label);
				displayString.append(" : " + context.getValue(label), StyledString.DECORATIONS_STYLER);
				ICompletionProposal p = createProposal(replacement, insertOffset, replacement.length(),
						labelProvider.getImage(MDDLabelProvider.Type.DATA_DEFINITION), displayString, priority);
				proposals.add(p);
			}
		}
		return proposals;
	}

	public Set<ICompletionProposal> getFieldLabelProposals(String startPattern, int insertOffset, int priority,
			Predicate<Declaration> filter) {
		Set<ICompletionProposal> proposals = new LinkedHashSet<ICompletionProposal>();
		for (String label : context.getLabelsStartingWith(startPattern)) {
			String path = context.resolveLabel(label) + "." + startPattern;
			proposals.addAll(getPathWithoutLabelsProposals(path, insertOffset, priority, filter));
		}
		return proposals;
	}

	public Set<ICompletionProposal> getTSFieldProposals(String startPattern, int insertOffset, int priority) {
		Set<ICompletionProposal> proposals = new LinkedHashSet<ICompletionProposal>();
		if (startPattern.indexOf(".") < 0) { //its a simple label
			for (String name : MQLContext.TSFields) {
				if (startsWith(name, startPattern)) {
					String replacement = name.substring(startPattern.length());
					StyledString displayString = new StyledString();
					displayString.append(name);
					displayString.append(" : date", StyledString.DECORATIONS_STYLER);
					ICompletionProposal p = createProposal(replacement, insertOffset, replacement.length(),
							labelProvider.getImage(MDDLabelProvider.Type.FIELD), displayString, priority);
					proposals.add(p);
				}
			}
		}
		return proposals;
	}

	public Set<ICompletionProposal> getQueryFunctionProposals(String startPattern, int insertOffset, int priority) {
		Set<ICompletionProposal> proposals = new LinkedHashSet<ICompletionProposal>();
		if (startPattern.indexOf(".") < 0) { //its a simple label
			SortedSet<String> functions = new TreeSet<String>();
			functions.addAll(Arrays.asList(MQLContext.QueryDateFunctions));
			functions.addAll(Arrays.asList(MQLContext.QueryStringFunctions));
			for (String name : functions) {
				if (startsWith(name, startPattern)) {
					String replacement = name.substring(startPattern.length()) + "()";
					StyledString displayString = new StyledString();
					displayString.append(name + "()");
					ICompletionProposal p = createProposal(replacement, insertOffset, replacement.length() - 1,
							labelProvider.getImage(MDDLabelProvider.Type.FUNCTION), displayString, priority);
					proposals.add(p);
				}
			}
		}
		return proposals;
	}

	public Set<ICompletionProposal> getFunctionArgumentsProposals(String startPattern, int insertOffset, int priority,
			FunctionArgumentDeclaration args) {
		Set<ICompletionProposal> proposals = new LinkedHashSet<ICompletionProposal>();
		for (FunctionArgumentBody arg : args.getF()) {
			String name = arg.getName();
			if (startsWith(name, startPattern)) {
				String replacement = name.substring(startPattern.length());
				StyledString displayString = new StyledString();
				displayString.append(name + " - function argument");
				ICompletionProposal p = createProposal(replacement, insertOffset, replacement.length(),
						labelProvider.getImage(MDDLabelProvider.Type.FUNCTION_ARGUMENT), displayString, priority);
				proposals.add(p);
			}
		}
		return proposals;
	}

	public ICompletionProposal createProposal(String replacementString, int replacementOffset, int cursorPosition,
			Image image, StyledString displayString, int priority) {
		return createProposal(replacementString, replacementOffset, 0, cursorPosition, image, displayString, priority);
	}

	public ICompletionProposal createProposal(String replacementString, int replacementOffset, int replacementLenght,
			int cursorPosition, Image image, StyledString displayString, int priority) {
		ConfigurableCompletionProposal proposal = new ConfigurableCompletionProposal(replacementString,
				replacementOffset, replacementLenght, cursorPosition, image, displayString, null, null);
		proposal.setPriority(priority);
		proposal.setMatcher(new PrefixMatcher.IgnoreCase());
		return proposal;
		//		return new ConfigurableCompletionProposal(replacementString, replacementOffset, replacementLength,
		//				cursorPosition, image, displayString);
	}

	public Set<ICompletionProposal> getAggregateFunctionProposals(String startPattern, int insertOffset, int priority) {
		Set<ICompletionProposal> proposals = new LinkedHashSet<ICompletionProposal>();
		if (startPattern.indexOf(".") < 0) { //its a simple label
			SortedSet<String> functions = new TreeSet<String>();
			functions.addAll(Arrays.asList(MQLContext.AggregateFunctions));
			for (String name : functions) {
				if (startsWith(name, startPattern)) {
					String replacement = name.substring(startPattern.length()) + "()";
					StyledString displayString = new StyledString();
					displayString.append(name + "()");
					ICompletionProposal p = createProposal(replacement, insertOffset, replacement.length() - 1,
							labelProvider.getImage(MDDLabelProvider.Type.FUNCTION), displayString, priority);
					proposals.add(p);
				}
			}
		}
		return proposals;
	}

	/**
	 * Creates and returns proposals for all {@link DataDefinition} who's name
	 * starts with the pattern.
	 * 
	 * @param startPattern
	 *            the beginning of the data definition name to look for
	 * @param insertOffset
	 *            position where the proposal will insert the replacement text
	 * @param priority
	 *            the priority of the proposal in the list, higher numbers means
	 *            higher position
	 * @return
	 */
	public Set<ICompletionProposal> getDataDefinitionProposals(String startPattern, int insertOffset, int priority) {
		Set<ICompletionProposal> proposals = new LinkedHashSet<ICompletionProposal>();
		for (IEObjectDescription od : context.getDataDefinitions()) {
			if (startsWith(od.getName(), startPattern)) {
				String replacement = od.getName();//.substring(startPattern.length());
				ICompletionProposal proposal = createProposal(replacement, insertOffset - startPattern.length(),
						replacement.length(), labelProvider.getImage(MDDLabelProvider.Type.DATA_DEFINITION),
						MDDLabelProvider.dataDefinitionLabel(od.getName()), priority);
				proposals.add(proposal);
			}
		}
		return proposals;
	}

	/**
	 * Creates and returns the proposals of possible values for the given enum
	 * field.<br>
	 * 
	 * @param enumPath
	 *            path in the form of <code>label.field.field.enumfield</code>
	 * @param startsWith
	 *            only values starting with this string will be returned
	 * @param insertOffset
	 *            position where the proposal will insert the replacement text
	 * @param priority
	 *            the priority of the proposal in the list, higher numbers means
	 *            higher position
	 * @return
	 */
	public Set<ICompletionProposal> getEnumValueProposals(String enumPath, String startsWith, int insertOffset,
			int priority) {
		Set<ICompletionProposal> proposals = new LinkedHashSet<ICompletionProposal>();
		if (enumPath.indexOf(".") > 0) { //otherwise it's a label and enum can't be just a label 		
			//we have path situation label.field.field... (also label. is possible)
			String label = enumPath.substring(0, enumPath.indexOf(".")); //get the label
			String currentPath = context.resolveLabel(label) + enumPath.substring(enumPath.indexOf("."));

			//we remove the match pattern from the path. we will use it later
			String path = currentPath.substring(0, currentPath.lastIndexOf("."));
			//we get the data definition that the path is starting with
			IEObjectDescription ddDescription = context.getDataDefinition(path);

			if (ddDescription != null) {
				//remove the data definition from the path
				path = path.substring(ddDescription.getName().length());
				//and remove the next dot if it remained
				if (!path.isEmpty() && path.charAt(0) == '.')
					path = path.substring(1);
				//we need to fill the resolve all the information in the resourceset so we can 
				//access the data inside
				EcoreUtil2.resolveAll(resourceSet);
				DataDefinition dd = (DataDefinition) EcoreUtil2.resolve((ddDescription.getEObjectOrProxy()),
						resourceSet);
				//get the declarations at the end of our path
				Iterable<Declaration> ddDeclarations = context.getDeclarationsOnPath(dd.getD(), path);
				//filter the declarations
				ddDeclarations = Iterables.filter(ddDeclarations, MDDUtils.EnumFields);
				//get the prefix of the declaration proposal name
				String matchPattern = currentPath.substring(currentPath.lastIndexOf(".") + 1);
				for (Declaration declaration : ddDeclarations) {
					if (declaration instanceof FieldDeclaration) {
						FieldDeclaration field = (FieldDeclaration) declaration;
						if (field.getName().equals(matchPattern) && MDDUtils.getFieldType(field) instanceof IntEnum) {
							IntEnum intEnum = (IntEnum) MDDUtils.getFieldType(field);
							for (EnumValue value : intEnum.getValues()) {
								String replacement = "'" + value.getName() + "'";
								if (startsWith(replacement, startsWith)) {
									StyledString displayString = new StyledString();
									displayString.append(text(value));
									ICompletionProposal p = createProposal(replacement,
											insertOffset - startsWith.length(), replacement.length(),
											labelProvider.getImage(value), displayString, priority);
									proposals.add(p);
								}
							}
						}
					}
				}
			}

		}
		return proposals;
	}

	private boolean startsWith(String value, String prefix) {
		return value.toLowerCase().startsWith(prefix.toLowerCase());
	}

	private StyledString text(Object in) {
		try {
			Object o = ((MDDLabelProvider) labelProvider).text(in);
			if (o instanceof StyledString) {
				return (StyledString) o;
			}
			if (o instanceof String) {
				return new StyledString((String) o);
			}
		} catch (Exception e) {

		}
		return new StyledString(labelProvider.getText(in));
	}

	/**
	 * Test implementation
	 * 
	 * @param currentPath
	 * @param insertOffset
	 * @param filter
	 * @return
	 */
	/*private Set<ICompletionProposal> getPathWithoutLabelsProposalsTest(String currentPath, int insertOffset,
			Predicate<Declaration> filter) {
		Set<ICompletionProposal> proposals = new LinkedHashSet<ICompletionProposal>();

		String path = currentPath.substring(0, currentPath.lastIndexOf("."));

		IEObjectDescription ddDescription = context.getDataDefinition(path);

		if (ddDescription != null) {
			path = path.substring(ddDescription.getName().length());
			if (!path.isEmpty() && path.charAt(0) == '.')
				path = path.substring(1);

			Iterable<IEObjectDescription> declarations = null;//context.getObjectsOnPath(ddDescription, path);

			for (IEObjectDescription declaration : declarations) {
				String name = declaration.getQualifiedName();
				name = name.substring(name.lastIndexOf(".") + 1);
				String prefix = currentPath.substring(currentPath.lastIndexOf(".") + 1);
				if (declaration.getEClass().equals(MDDPackage.Literals.FIELD_DECLARATION))
					if (startsWith(name, prefix)) {
						String replacement = name.substring(prefix.length());
						StyledString displayString = new StyledString();
						displayString.append(name);
						ICompletionProposal p = createProposal(replacement, insertOffset, replacement.length(), null,
								displayString, 0);
						proposals.add(p);
					}
			}
		}
		return proposals;
	}*/

}
