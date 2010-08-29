package org.makumba.devel.eclipse.mdd.validation;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.makumba.devel.eclipse.mdd.MDDUtils;
import org.makumba.devel.eclipse.mdd.MQLContext;
import org.makumba.devel.eclipse.mdd.MDD.DataDefinition;
import org.makumba.devel.eclipse.mdd.MDD.Declaration;
import org.makumba.devel.eclipse.mdd.MDD.FieldDeclaration;
import org.makumba.devel.eclipse.mdd.MDD.FunctionDeclaration;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class MQLValidator {

	@Inject
	private Provider<ResourceSet> resourceSet;

	/**
	 * Validates the from path of the MQL query.
	 * 
	 * @param path
	 *            to validate
	 * @param context
	 *            {@link MQLContext} of the query
	 * @return
	 */
	public String validateFromPath(String path, MQLContext context) {
		String passed = "";
		path = context.resolvePath(path);

		IEObjectDescription ddDescription = context.getDataDefinition(path);
		//IEObjectDescription current = ddDescription;
		if (ddDescription != null) {
			passed = path.substring(0, ddDescription.getName().length());
			path = path.substring(ddDescription.getName().length());
			if (!path.isEmpty()) { //we still have some path after data type
				if (path.charAt(0) != '.') {
					return "Found '" + path + "' where '.' was expected";

				}
				path = path.substring(1);
				EcoreUtil2.resolveAll(resourceSet.get());
				DataDefinition dd = (DataDefinition) EcoreUtil2.resolve((ddDescription.getEObjectOrProxy()),
						resourceSet.get());

				Iterable<FieldDeclaration> fields = MDDUtils.getPointerOrSetFields(dd.getD());
				String[] segments = path.split("\\.");
				for (final String segment : segments) {
					try {
						FieldDeclaration field = Iterables.find(fields, new Predicate<FieldDeclaration>() {
							public boolean apply(FieldDeclaration input) {
								return input.getName().equals(segment);
							}
						});
						fields = Iterables.filter(MDDUtils.getPointedDeclarations(field), FieldDeclaration.class);
						passed += "." + segment;
					} catch (Exception e) {
						return "No such field '" + segment + "' in '" + passed;
					}
				}
			}
		} else {
			return "Unknown Data Type at the begining of: " + path;
		}
		return null;
	}

	/**
	 * Validates the identifiers in the MQL query.
	 * 
	 * @param ident
	 *            to validate
	 * @param context
	 *            {@link MQLContext} of the query
	 * @param isFunction
	 *            true if the identifier should be a function, false otherwise
	 * @return
	 */
	public String validateQueryIdentifier(String ident, MQLContext context, final boolean isFunction) {
		if (!ident.contains(".")) {
			if (isFunction) {
				if (context.isFunctionName(ident))
					return null;
				if (context.containsLabel("this")) {
					return validateQueryIdentifier(context.resolveLabel("this") + "." + ident, context, true);
				}
				return "Unknown function:" + ident;
			} else {
				if (context.containsParam(ident)) {
					return null; //it's a function paramater
				}
				if (context.containsLabel(ident)) {
					return null; //it's a label
				}
				if (context.containsLabel("this")) {
					return validateQueryIdentifier(context.resolveLabel("this") + "." + ident, context, false);
				}
				return "Unkonwn value:" + ident;
			}
		} else {
			//			String label = context.resolveLabel(ident.substring(0, ident.indexOf(".")));
			//			if (label.equals(ident.substring(0, ident.indexOf(".")))) {
			//				return "Unknown label:" + label;
			//			}
			//			ident = ident.substring(ident.indexOf("."));

			String path = context.resolvePath(ident);
			IEObjectDescription ddDescription = context.getDataDefinition(path);
			//IEObjectDescription current = ddDescription;
			if (ddDescription != null) {
				String passed = path.substring(0, ddDescription.getName().length());
				path = path.substring(ddDescription.getName().length());
				if (!path.isEmpty()) { //we still have some path after data type
					if (path.charAt(0) != '.') {
						return "Found '" + path + "' where '.' was expected";

					}
					path = path.substring(1);
					EcoreUtil2.resolveAll(resourceSet.get());
					DataDefinition dd = (DataDefinition) EcoreUtil2.resolve((ddDescription.getEObjectOrProxy()),
							resourceSet.get());

					Iterable<Declaration> declarations = dd.getD();
					String[] segments = path.split("\\.");
					for (int i = 0; i < segments.length; i++) {//we search all the segments
						final String segment = segments[i];
						if (i < segments.length - 1) { //we are not at the last segment so it has to be a point or set
							try {
								Iterable<FieldDeclaration> fields = Iterables.filter(declarations,
										FieldDeclaration.class);
								FieldDeclaration field = Iterables.find(fields, new Predicate<FieldDeclaration>() {
									public boolean apply(FieldDeclaration input) {
										return input.getName().equals(segment);
									}
								});
								passed += "." + segment;
								declarations = MDDUtils.getPointedDeclarations(field);
							} catch (Exception e) { //in the middle of the path we have a non existing field
								return "No such field '" + segment + "' in '" + passed;
							}
						} else { //we are at the last segment we check if it is an existing function or field
							try {
								Iterables.find(declarations, new Predicate<Declaration>() {
									public boolean apply(Declaration input) {
										if (!isFunction && (input instanceof FieldDeclaration))
											return ((FieldDeclaration) input).getName().equals(segment);
										if (isFunction && (input instanceof FunctionDeclaration))
											return ((FunctionDeclaration) input).getName().equals(segment);
										return false;
									}
								});
								passed += "." + segment;
							} catch (Exception e) { //at the end of the path we have a non existing field
								return "No such " + (isFunction ? "function" : "field") + " '" + segment + "' in '"
										+ passed;
							}
						}
					}
				}
			} else {
				return "Unknown Data Type at the begining of: " + path;
			}
		}
		return null;

	}

	/**
	 * Check if the from label is unique.
	 * 
	 * @param label
	 * @param context
	 *            {@link MQLContext} of the query
	 * @return
	 */
	public String validateFromLabel(String label, MQLContext context) {
		//we check for duplicate labels
		if (context.containsLabel(label)) {
			return "Duplicated label: " + label;
		}
		return null;
	}

}
