package org.makumba.devel.eclipse.mdd.validation;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.parsetree.CompositeNode;
import org.eclipse.xtext.parsetree.LeafNode;
import org.eclipse.xtext.parsetree.NodeUtil;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.scoping.impl.AbstractGlobalScopeProvider;
import org.eclipse.xtext.validation.Check;
import org.makumba.devel.eclipse.mdd.MDDUtils;
import org.makumba.devel.eclipse.mdd.MQLContext;
import org.makumba.devel.eclipse.mdd.MDD.Atom;
import org.makumba.devel.eclipse.mdd.MDD.CharType;
import org.makumba.devel.eclipse.mdd.MDD.DataDefinition;
import org.makumba.devel.eclipse.mdd.MDD.FieldDeclaration;
import org.makumba.devel.eclipse.mdd.MDD.FromClassOrOuterQueryPath;
import org.makumba.devel.eclipse.mdd.MDD.FunctionCall;
import org.makumba.devel.eclipse.mdd.MDD.IncludeDeclaration;
import org.makumba.devel.eclipse.mdd.MDD.MDDPackage;
import org.makumba.devel.eclipse.mdd.MDD.RegexValidationRuleDeclaration;
import org.makumba.devel.eclipse.mdd.MDD.SetType;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

public class MDDJavaValidator extends AbstractMDDJavaValidator {

	@Inject
	private IResourceDescriptions resourceDescriptions;

	@Inject
	private MDDUtils utils;

	@Inject
	private MQLValidator mqlValidator;

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

	/**
	 * Checks unique field name inside a level of data definition.
	 * 
	 * @param field
	 */
	@Check
	public void checkUniqueFieldName(final FieldDeclaration field) {
		Iterable<FieldDeclaration> siblings = Iterables.filter(MDDUtils.getSiblingsOf(field), FieldDeclaration.class);
		Iterable<FieldDeclaration> result = Iterables.filter(siblings, new Predicate<FieldDeclaration>() {
			public boolean apply(FieldDeclaration input) {
				if (!input.equals(field) && input.getName().equals(field.getName()))
					return true;
				return false;
			}
		});
		if (result.iterator().hasNext()) {
			error("Field name should be unique.", MDDPackage.FIELD_DECLARATION__NAME);
		}
	}

	/**
	 * Checks that the only the IDDs and not MDDs are included and checks for
	 * the recursive includes.
	 * 
	 * @param include
	 */
	@Check
	public void checkInclude(final IncludeDeclaration include) {
		if (include.getImportedNamespace() != null) {
			DataDefinition dd = (DataDefinition) include.getImportedNamespace();
			if (!dd.eResource().getURI().fileExtension().equals("idd")) {
				error("Can include only Include Data Definitions (.idd file extension).",
						MDDPackage.INCLUDE_DECLARATION__IMPORTED_NAMESPACE);
			} else {
				EObject current = include;
				while (!(current instanceof DataDefinition) && current != null) {
					current = current.eContainer();
				}
				if (current instanceof DataDefinition) {
					if (((DataDefinition) current).equals(include.getImportedNamespace()))
						error("Cannot include itself.", MDDPackage.INCLUDE_DECLARATION__IMPORTED_NAMESPACE);
				}
			}
		}
	}

	/**
	 * Checks that the char field length is not bigger then 255
	 * 
	 * @param charType
	 */
	@Check
	public void checkCharSize(final CharType charType) {
		if (charType.getLength() > 255) {
			error("Char has a maximum length of 255.", MDDPackage.CHAR_TYPE__LENGTH);
		}

	}

	/**
	 * Checks that the set field declarations don't have the unique modifier.
	 * 
	 * @param field
	 */
	@Check
	public void checkSetModifiers(final FieldDeclaration field) {
		//TODO: add check for setEnums (char and int)
		if (field.getTypedef() instanceof SetType && field.getModifiers().isUnique()) {
			error("Set type cannot be unique.", MDDPackage.MODIFIERS__UNIQUE);
		}
	}

	/**
	 * Checks the validity of the field name.
	 * 
	 * @param field
	 */
	@Check
	public void checkFieldName(final FieldDeclaration field) {
		String error = checkValidName(field.getName());
		if (error != null)
			error(error, MDDPackage.FIELD_DECLARATION__NAME);
	}

	/**
	 * Checks the title function call. The function call should be made to
	 * simple functions that take no arrguments.
	 * 
	 * @param functionCall
	 */
	@Check
	public void checkTitleFunctionCall(final FunctionCall functionCall) {
		if (functionCall.getF().getArgs().size() > 0) {
			error("There's no support for function calls with arguments in the !title directive yet",
					MDDPackage.FUNCTION_CALL__F);
		}
	}

	/**
	 * Checks the from part of the MQL query.
	 * 
	 * @param from
	 */
	@Check
	public void checkValidFrom(final FromClassOrOuterQueryPath from) {

		String path = from.getPath();

		//we form the mql context
		MQLContext mqlContext = new MQLContext(utils.getLabels(from), getResourceDescriptions(from), from);

		//we validate the path
		String err = mqlValidator.validateFromPath(path, mqlContext);
		if (err != null)
			error(err, MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__PATH);

		// we validate the label
		err = mqlValidator.validateFromLabel(from.getName(), mqlContext);
		if (err != null)
			error(err, MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__NAME);

	}

	/**
	 * Checks the atom part of the MQL query.
	 * 
	 * @param atom
	 */
	@Check
	public void checkValidAtom(final Atom atom) {

		//we get the whole atom node
		CompositeNode node = NodeUtil.getNode(atom);
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
			MQLContext mqlContext = new MQLContext(utils.getLabels(atom), getResourceDescriptions(atom), atom);
			//add params to context
			mqlContext.setParams(utils.getParams(atom));

			//do the validation
			try {
				mqlValidator.validateQueryIdentifier(content, mqlContext, isFunction);
			} catch (ValidationException e) {
				error(e.getMessage(), MDDPackage.ATOM);
			}

		}

	}

	@Check
	public void checkRegexValidationRule(final RegexValidationRuleDeclaration regexRule) {
		if (!regexRule.getExp().startsWith("\"") || !regexRule.getExp().endsWith("\"")) {
			error("Regular expression must be e", MDDPackage.REGEX_VALIDATION_RULE_DECLARATION__EXP);

		} else {
			String expression = regexRule.getExp().substring(1).substring(0, regexRule.getExp().length() - 1);
			try {
				Pattern.compile(expression);
			} catch (PatternSyntaxException e) {
				error("Invalid regular expression: " + e.getDescription(),
						MDDPackage.REGEX_VALIDATION_RULE_DECLARATION__EXP);
			}
		}
	}

	/**
	 * Checks if the given string is valid name string. If there is an error,
	 * the error message will be returned, null otherwise.
	 * 
	 * @param name
	 * @return
	 */
	public static String checkValidName(String name) {
		for (int i = 0; i < name.length(); i++) {
			if (i == 0 && !Character.isJavaIdentifierStart(name.charAt(i)) || i > 0
					&& !Character.isJavaIdentifierPart(name.charAt(i))) {
				return "Invalid character \"" + name.charAt(i) + "\" in field name.";
			}
		}

		//TODO:make this work in plugin env
		//		if (ReservedKeywords.isReservedKeyword(field.getName())) {
		//			error("Field name (" + nm + ") cannot be one of the reserved keywords", MDDPackage.FIELD_DECLARATION__NAME);
		//		}

		return null;
	}

	//TODO: maybe add some checks on Native Validation rules (type check etc.)
}
