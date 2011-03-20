package org.makumba.devel.eclipse.mdd;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Stack;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.makumba.devel.eclipse.mdd.MDD.DataDefinition;
import org.makumba.devel.eclipse.mdd.MDD.Declaration;
import org.makumba.devel.eclipse.mdd.MDD.FieldDeclaration;
import org.makumba.devel.eclipse.mdd.MDD.FieldType;
import org.makumba.devel.eclipse.mdd.MDD.FromClassOrOuterQueryPath;
import org.makumba.devel.eclipse.mdd.MDD.FromClause;
import org.makumba.devel.eclipse.mdd.MDD.FunctionArgumentBody;
import org.makumba.devel.eclipse.mdd.MDD.FunctionBody;
import org.makumba.devel.eclipse.mdd.MDD.FunctionDeclaration;
import org.makumba.devel.eclipse.mdd.MDD.IntEnum;
import org.makumba.devel.eclipse.mdd.MDD.MDDPackage;
import org.makumba.devel.eclipse.mdd.MDD.PointerType;
import org.makumba.devel.eclipse.mdd.MDD.SelectFrom;
import org.makumba.devel.eclipse.mdd.MDD.SetType;
import org.makumba.devel.eclipse.mdd.MDD.SubFieldDeclaration;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;

/**
 * @author filip
 * 
 */
public class MDDUtils {

	@Inject
	private IQualifiedNameProvider nameProvider;

	@Inject
	private IResourceDescriptions resourceDescriptions;

	public static Predicate<Declaration> PointerOrSetFilter = new Predicate<Declaration>() {
		public boolean apply(Declaration input) {
			if (input instanceof FieldDeclaration)
				return ((FieldDeclaration) input).getTypedef() instanceof SetType
						|| ((FieldDeclaration) input).getTypedef() instanceof PointerType;
			return false;
		}
	};

	public static Predicate<Declaration> NonPointerOrSetFiledFilter = new Predicate<Declaration>() {
		public boolean apply(Declaration input) {
			if (input instanceof FieldDeclaration)
				return !(((FieldDeclaration) input).getTypedef() instanceof SetType || ((FieldDeclaration) input)
						.getTypedef() instanceof PointerType);
			return false;
		}
	};

	public static Predicate<Declaration> NonSetComplex = new Predicate<Declaration>() {
		public boolean apply(Declaration input) {
			if (input instanceof FieldDeclaration) {
				if (((FieldDeclaration) input).getTypedef() instanceof SetType) {
					SetType type = (SetType) ((FieldDeclaration) input).getTypedef();
					return type.getRef() != null;
				}
				return true;

			}
			return false;
		}
	};

	public static Predicate<Declaration> Field = new Predicate<Declaration>() {
		public boolean apply(Declaration input) {
			return (input instanceof FieldDeclaration);
		}
	};

	public static Predicate<Declaration> NotFixedField = new Predicate<Declaration>() {
		public boolean apply(Declaration input) {
			return (input instanceof FieldDeclaration)
					&& ((((FieldDeclaration) input).getModifiers() == null) || !(((FieldDeclaration) input)
							.getModifiers().isFixed()));
		}
	};

	public static Predicate<Declaration> FieldOrFunction = new Predicate<Declaration>() {
		public boolean apply(Declaration input) {
			return (input instanceof FieldDeclaration) || (input instanceof FunctionDeclaration);
		}
	};

	public static Predicate<Declaration> EnumFields = new Predicate<Declaration>() {
		public boolean apply(Declaration input) {
			if (input instanceof FieldDeclaration) {
				FieldDeclaration field = (FieldDeclaration) input;
				return (getFieldType(field) instanceof IntEnum);
			}
			return false;
		}
	};

	public static Predicate<Declaration> SetComplex = new Predicate<Declaration>() {
		public boolean apply(Declaration input) {
			if (input instanceof FieldDeclaration && ((FieldDeclaration) input).getTypedef() instanceof SetType) {
				SetType type = (SetType) ((FieldDeclaration) input).getTypedef();
				return type.getRef() == null;
			}
			return false;
		}
	};

	public static Predicate<Declaration> PtrOne = new Predicate<Declaration>() {
		public boolean apply(Declaration input) {
			if (input instanceof FieldDeclaration && ((FieldDeclaration) input).getTypedef() instanceof PointerType) {
				PointerType type = (PointerType) ((FieldDeclaration) input).getTypedef();
				return type.getRef() == null;
			}
			return false;
		}
	};

	/**
	 * Returns the real {@link FieldType} even if it's a macro type defined with
	 * !type expression.
	 * 
	 * @param field
	 * @return
	 */
	public static FieldType getFieldType(FieldDeclaration field) {
		if (field.getTypedef().getTypeDec() != null) {
			return field.getTypedef().getTypeDec().getFieldType();
		}
		return field.getTypedef();
	}

	/**
	 * Gets all the {@link Declaration}s on the same lavel as the one passed as
	 * the parameter.
	 * 
	 * @param declaration
	 * @return
	 */
	public static Iterable<Declaration> getSiblingsOf(Declaration declaration) {
		Stack<FieldDeclaration> fieldsHierarchy = new Stack<FieldDeclaration>();
		Declaration current = declaration;
		while (current.eContainer() instanceof SubFieldDeclaration) {
			current = (SubFieldDeclaration) current.eContainer();
			fieldsHierarchy.push(((SubFieldDeclaration) current).getSubFieldOf());
		}

		Iterable<Declaration> declarations = ((DataDefinition) current.eContainer()).getD();
		while (!fieldsHierarchy.empty()) {
			FieldDeclaration field = fieldsHierarchy.pop();
			declarations = getChildrenOf(field, declarations);
		}
		return declarations;
	}

	/**
	 * Gets all the {@link Declaration}s that are {@link SubFieldDeclaration}s
	 * for the given {@link PointerType} or {@link SetType} field.
	 * 
	 * @param field
	 *            must be {@link PointerType} or {@link SetType} otherwise null
	 *            is returned
	 * @return {@link Declaration}s
	 */
	public static Iterable<Declaration> getChildrenOf(final FieldDeclaration field) {
		Iterable<Declaration> siblings = getSiblingsOf(field);
		return getChildrenOf(field, siblings);

	}

	/**
	 * Gets all the child declarations of the given {@link EObject}. Only
	 * {@link DataDefinition} and {@link SubFieldDeclaration} can have
	 * {@link Declaration}s.
	 * 
	 * @param either
	 *            {@link SubFieldDeclaration} or {@link DataDefinition} for
	 *            which to find declarations.
	 * @return found {@link Declaration}s or null if wrong param.
	 */
	public static Iterable<Declaration> getDeclarationsOf(EObject context) {
		Stack<FieldDeclaration> fieldsHierarchy = new Stack<FieldDeclaration>();
		EObject dataDefinition = context;
		if (context instanceof SubFieldDeclaration) {
			Declaration current = (Declaration) context;
			fieldsHierarchy.push(((SubFieldDeclaration) current).getSubFieldOf());
			while (current.eContainer() instanceof SubFieldDeclaration) {
				current = (SubFieldDeclaration) current.eContainer();
				fieldsHierarchy.push(((SubFieldDeclaration) current).getSubFieldOf());
			}
			dataDefinition = current.eContainer();
		}

		if (dataDefinition instanceof DataDefinition) {
			Iterable<Declaration> declarations = ((DataDefinition) dataDefinition).getD();
			while (!fieldsHierarchy.empty()) {
				FieldDeclaration field = fieldsHierarchy.pop();
				declarations = getChildrenOf(field, declarations);
			}
			return declarations;
		} else
			return Collections.emptySet();
	}

	/**
	 * It returns only {@link PointerType} or {@link SetType}
	 * {@link FieldDeclaration}s of a given {@link EObject}. Basically it's a
	 * filters {@link #getDeclarationsOf(EObject)} for pointer or sets.
	 * 
	 * @param context
	 * @return
	 */
	public static Iterable<FieldDeclaration> getPointerOrSetFieldsOf(EObject context) {
		Iterable<Declaration> declarations = getDeclarationsOf(context);
		return getPointerOrSetFields(declarations);
	}

	public static Iterable<FieldDeclaration> getPointerOrSetFields(Iterable<Declaration> declarations) {
		Iterable<FieldDeclaration> fields = Iterables.filter(declarations, FieldDeclaration.class);
		fields = Iterables.filter(fields, PointerOrSetFilter);
		return fields;
	}

	/**
	 * Returns all the {@link Declaration}s that the field points to. Field's
	 * {@link FieldType} can be either {@link PointerType} or {@link SetType},
	 * otherwise it will return empty set. The declarations are found either by
	 * following the {@link DataDefinition} refrence of the field, or looking
	 * for it's {@link SubFieldDeclaration}s.
	 * 
	 * 
	 * @param field
	 *            {@link FieldDeclaration} of either {@link PointerType} or
	 *            {@link SetType}
	 * @return {@link Declaration}s
	 */
	public static Iterable<Declaration> getPointedDeclarations(FieldDeclaration field) {
		if (field.getTypedef() instanceof PointerType) {
			if (((PointerType) field.getTypedef()).getRef() != null) {
				return ((PointerType) field.getTypedef()).getRef().getD();
			} else {
				return getChildrenOf(field);
			}
		} else if (field.getTypedef() instanceof SetType) {
			if (((SetType) field.getTypedef()).getRef() != null) {
				return ((SetType) field.getTypedef()).getRef().getD();
			} else {
				return getChildrenOf(field);
			}
		}
		return Collections.emptySet();

	}

	private static Iterable<Declaration> getChildrenOf(final FieldDeclaration targetField,
			final Iterable<Declaration> inDeclarations) {
		Iterable<SubFieldDeclaration> subfieldDeclarations = Iterables
				.filter(inDeclarations, SubFieldDeclaration.class);
		subfieldDeclarations = Iterables.filter(subfieldDeclarations, new Predicate<SubFieldDeclaration>() {
			public boolean apply(SubFieldDeclaration declaration) {
				return declaration.getSubFieldOf().equals(targetField);
			}
		});
		return Iterables.transform(subfieldDeclarations, new Function<SubFieldDeclaration, Declaration>() {
			public Declaration apply(SubFieldDeclaration from) {
				return from.getD();
			}
		});
	}

	public LinkedHashMap<String, String> getLabels(EObject object) {
		LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
		EObject current = object.eContainer();
		while (current != null && !(current instanceof FunctionBody)) { //go until end or functionbody
			if (current instanceof SelectFrom) //we are in select from section, we get the from and proccess it
				result.putAll(processFrom(((SelectFrom) current).getFrom(), object));
			current = current.eContainer();
		}
		EObject parent = object.eContainer();
		// we search for the containing datadefintion or subfield declaration so we can add this to the labels
		while (parent != null && !(parent instanceof SubFieldDeclaration) && !(parent instanceof DataDefinition)) {
			parent = parent.eContainer();
		}
		if (parent != null) { //if it was found, we add this
			String name = nameProvider.getQualifiedName(parent);
			//TODO: check if this referes subfielddeclaration as well or always only to MDD
			result.put("this", name);
		}
		return result;
	}

	public LinkedHashMap<String, String> processFrom(FromClause from, EObject stopAt) {
		LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
		if (from != null) {
			for (EObject o : from.eContents()) { //lets get all the contents of the fromclause (both range and joins)
				if (o instanceof FromClassOrOuterQueryPath) {//check if it's the right type TODO: maybe add others (InCollectionElementDeclaration)
					FromClassOrOuterQueryPath f = (FromClassOrOuterQueryPath) o;
					if (!f.equals(stopAt)) { //check that we didn't reach the stop from
						if (f.getName() != null && !f.getName().isEmpty() && f.getPath() != null
								&& !f.getPath().isEmpty()) { //if it has the path and the label
							result.put(f.getName(), f.getPath()); //add it to the label set
						}
					} else { //we reached the stop from, we are not interested in the others that follow it
						break;
					}
				}
			}
		}
		return result;

	}

	public Map<String, FunctionArgumentBody> getParams(EObject object) {
		Map<String, FunctionArgumentBody> result = new HashMap<String, FunctionArgumentBody>();
		EObject current = object;
		while (current != null && !(current instanceof FunctionDeclaration)) {
			current = current.eContainer();
		}
		if (current instanceof FunctionDeclaration) {
			for (FunctionArgumentBody arg : ((FunctionDeclaration) current).getArg().getF()) {
				//TODO: put the type as well
				result.put(arg.getName(), arg);
			}
		}
		return result;
	}

	/**
	 * 
	 * Test implementation
	 * 
	 * @param parent
	 * @return
	 */
	public Iterable<IEObjectDescription> getDeclarations(final IEObjectDescription parent) {
		IResourceDescription resource = resourceDescriptions.getResourceDescription(parent.getEObjectURI()
				.trimFragment());
		if (parent.getEClass().equals(MDDPackage.Literals.FIELD_DECLARATION)) {
			try {
				IReferenceDescription reference = Iterables.find(resource.getReferenceDescriptions(),
						new Predicate<IReferenceDescription>() {
							public boolean apply(IReferenceDescription input) {

								return input.getSourceEObjectUri().equals(parent.getEObjectURI());
							}
						});
				IEObjectDescription target = getIEObjectDescription(reference.getTargetEObjectUri());
				return getDeclarations(target);
			} catch (NoSuchElementException e) {
				//we didn't find reference, might be that it's just a setComplex
			}
		}
		return Iterables.filter(resource.getExportedObjects(MDDPackage.Literals.DECLARATION),
				new Predicate<IEObjectDescription>() {
					public boolean apply(IEObjectDescription input) {
						String name = input.getQualifiedName();
						if (name.startsWith(parent.getQualifiedName())) {
							name = name.substring(parent.getQualifiedName().length());
							if (name.matches("\\.\\w+")) {
								return true;
							}
						}
						return false;
					}

				});
	}

	/**
	 * 
	 * Test implementation
	 * 
	 * @param uri
	 * @return
	 */
	public IEObjectDescription getIEObjectDescription(final URI uri) {
		IResourceDescription resource = resourceDescriptions.getResourceDescription(uri);
		try {
			return Iterables.find(resource.getExportedObjects(), new Predicate<IEObjectDescription>() {
				public boolean apply(IEObjectDescription input) {

					return input.getEObjectURI().equals(uri);
				}
			});
		} catch (NoSuchElementException e) {
			//we didn't find it return null
		}
		return null;
	}

	public static IProject getProject(EObject object) {
		try {
			return ResourcesPlugin.getWorkspace().getRoot()
					.getFile(new Path(object.eResource().getURI().toPlatformString(true))).getProject();
		} catch (Exception e) {

		}
		return null;
	}

}
