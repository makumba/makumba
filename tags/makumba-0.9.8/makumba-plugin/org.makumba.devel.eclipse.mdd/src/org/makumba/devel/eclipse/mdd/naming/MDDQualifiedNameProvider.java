package org.makumba.devel.eclipse.mdd.naming;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.xtext.naming.DefaultDeclarativeQualifiedNameProvider;
import org.makumba.devel.eclipse.mdd.MDD.DataDefinition;
import org.makumba.devel.eclipse.mdd.MDD.SubFieldDeclaration;

public class MDDQualifiedNameProvider extends DefaultDeclarativeQualifiedNameProvider {

	/**
	 * Returns a qualified name for the data definition. A qualified name is the
	 * data type name. It is computed starting from dataDefinitons folder if one
	 * exists on the classpath or classpath root otherwise.
	 * 
	 * Example for a data definition stored in
	 * dataDefinitions/general/Person.mdd, the qualified name would be
	 * general.Person.
	 * 
	 * 
	 * @param data
	 *            {@link DataDefinition} for which to compute the name
	 * @return
	 */
	public static String qualifiedName(DataDefinition data) {
		String mddname = "";
		// check if it is a platform resource
		if (!data.eIsProxy() && data.eResource().getURI().isPlatformResource()) {
			String URIpath = data.eResource().getURI().toPlatformString(true);
			IFile file = WorkspaceSynchronizer.getFile(data.eResource());
			// get the resource uri
			URIpath = data.eResource().getURI().trimFileExtension().toPlatformString(true);
			IProject project = file.getProject();
			try {// check if it is a java project
				if (project.getNature(JavaCore.NATURE_ID) != null) {
					IJavaProject jp = JavaCore.create(project);
					try {// find the projects source foulder
						IClasspathEntry[] cps = jp.getRawClasspath();
						for (IClasspathEntry cp : cps) {
							if (cp.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
								if (URIpath.startsWith(cp.getPath().toString())) { //if the resource is in the source folder
									mddname = URIpath.substring(cp.getPath().toString().length() + 1); //compute the remaining path when source folder is removed
									if (mddname.startsWith("dataDefinitions")) // check if it starts with dataDefinitions folder and remove it
										mddname = mddname.substring("dataDefinitions".length() + 1);
									break;
								}

							}
						}
					} catch (JavaModelException e) {
						e.printStackTrace();
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		} else if (!data.eIsProxy() && data.eResource().getURI().isFile()) { // if it's a simple file resource (no platform, usually meaning we're not in eclipse workespace
			String URIpath = data.eResource().getURI().trimFileExtension().toFileString();
			Enumeration<URL> classpathUrls;
			try {// find the datadefintions on the classpath
				classpathUrls = ClassLoader.getSystemResources("dataDefinitions/");
				while (classpathUrls.hasMoreElements()) {
					String cp = classpathUrls.nextElement().getPath();
					if (URIpath.startsWith(cp)) { //check if the resource starts with it
						mddname = URIpath.substring(cp.length());
						break;
					}
				}
				if (mddname.isEmpty()) {
					classpathUrls = ClassLoader.getSystemResources("");
					while (classpathUrls.hasMoreElements()) {
						String cp = classpathUrls.nextElement().getPath();
						if (URIpath.startsWith(cp)) {
							mddname = URIpath.substring(cp.length());
							break;
						}
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//data.eResource().getURI().resolve(base)
		if (!mddname.isEmpty()) {
			mddname = mddname.replaceAll("/", ".");
			return mddname;
		}
		return null;

	}

	/**
	 * Computes and returns the qualified name for the
	 * {@link SubFieldDeclaration}. The name is computed by concatenating parent
	 * names separated by the dot.
	 * 
	 * For example, a qualified name for an MDD declaration
	 * <code>field1->field2-></code> would be "field1.field2".
	 * 
	 * @param subfield
	 * @return
	 */
	public String qualifiedName(SubFieldDeclaration subfield) {
		String parent = getQualifiedName(subfield.eContainer());
		String name = subfield.getSubFieldOf().getName();
		if (parent != null && !parent.isEmpty()) {
			return parent + "." + name;
		}
		return name;
	}

}
