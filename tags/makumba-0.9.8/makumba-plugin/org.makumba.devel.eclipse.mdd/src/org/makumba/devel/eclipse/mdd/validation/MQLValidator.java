package org.makumba.devel.eclipse.mdd.validation;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.makumba.devel.eclipse.mdd.MDDUtils;
import org.makumba.devel.eclipse.mdd.MQLContext;
import org.makumba.devel.eclipse.mdd.MDD.DataDefinition;
import org.makumba.devel.eclipse.mdd.MDD.FieldDeclaration;

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
	public void validateQueryIdentifier(String ident, MQLContext context, final boolean isFunction)
			throws ValidationException {

		context.resolveQueryIdentifier(ident, resourceSet.get(), isFunction);

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
