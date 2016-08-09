package org.makumba.devel.eclipse.mdd.ui.project;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jst.j2ee.web.componentcore.util.WebArtifactEdit;
import org.eclipse.jst.j2ee.webapplication.Filter;
import org.eclipse.jst.j2ee.webapplication.FilterMapping;
import org.eclipse.jst.j2ee.webapplication.WebApp;
import org.eclipse.jst.j2ee.webapplication.WebapplicationFactory;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
import org.eclipse.wst.common.project.facet.core.IDelegate;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.xtext.builder.nature.ToggleXtextNatureAction;
import org.makumba.devel.eclipse.mdd.ui.internal.MDDActivator;
import org.osgi.framework.Bundle;

@SuppressWarnings("unchecked")
public class MakumbaFacetInstallDelegate implements IDelegate {

	private ToggleXtextNatureAction toggleNature = MDDActivator.getInstance()
			.getInjector("org.makumba.devel.eclipse.mdd.MDD").getInstance(ToggleXtextNatureAction.class);

	private static String[] libs = { "antlr-2.7.6.jar", "c3p0-0.9.1.2.jar", "commons-beanutils-1.7.0.jar",
			"commons-beanutils-core-1.8.0.jar", "commons-cli-1.2.jar", "commons-collections-3.2.jar",
			"commons-configuration-1.6.jar", "commons-digester-1.8.jar", "commons-io-1.1.jar", "commons-lang-2.3.jar",
			"commons-logging-1.1.1.jar", "hsqldb-1.8.0.7.jar", "makumba-0.8.2.7.2.jar", "mime-util-2.1.3.jar",
			"mysql-connector-java-5.1.11.jar" };

	public void execute(IProject project, IProjectFacetVersion facetVersion, Object config, IProgressMonitor monitor)
			throws CoreException {

		monitor.beginTask("Installing Makumba Facet", 2 + libs.length);

		boolean installLibs = true;
		boolean installExample = true;

		if (config != null && config instanceof FacetInstallActionConfigFactory) {
			installLibs = ((FacetInstallActionConfigFactory) config).isInstallLibs();
			installExample = ((FacetInstallActionConfigFactory) config).isInstallExample();
		}

		try {
			// Add Xtext nature
			if (!toggleNature.hasNature(project) && project.isAccessible()) {
				toggleNature.toggleNature(project);
			}

			final IVirtualComponent vc = ComponentCore.createComponent(project);
			final IVirtualFolder vf1 = vc.getRootFolder().getFolder("WEB-INF/");
			IFolder webLibFolder = (IFolder) vf1.getUnderlyingFolder();
			final IVirtualFolder vf2 = vc.getRootFolder().getFolder("WEB-INF/classes");
			IFolder srcFolder = (IFolder) vf2.getUnderlyingFolder();
			final IVirtualFolder vf3 = vc.getRootFolder();
			IFolder contentFolder = (IFolder) vf3.getUnderlyingFolder();

			//TODO: Integrate mavan

			try {
				final Bundle bundle = MDDActivator.getInstance().getBundle();
				if (installLibs) {
					if (!webLibFolder.getFolder("lib").exists()) {
						webLibFolder.getFolder("lib").create(true, true, monitor);
					}
					int i = 0;
					for (String lib : libs) {
						final InputStream in1 = FileLocator.openStream(bundle, new Path("res/lib/" + lib), false);
						if (!webLibFolder.getFile("lib/" + lib).exists()) {
							webLibFolder.getFile("lib/" + lib).create(in1, true, monitor);
						}
						monitor.worked(++i);
					}
				}
				final InputStream in2 = FileLocator.openStream(bundle, new Path("res/Makumba.conf"), false);
				if (!srcFolder.getFile("Makumba.conf").exists())
					srcFolder.getFile("Makumba.conf").create(in2, true, monitor);
				if (!srcFolder.getFolder("dataDefinitions").exists())
					srcFolder.getFolder("dataDefinitions").create(false, true, monitor);
				if (installExample) {
					final InputStream in3 = FileLocator.openStream(bundle, new Path("res/Person.mdd"), false);
					if (!srcFolder.getFile("dataDefinitions/Person.mdd").exists()) {
						srcFolder.getFile("dataDefinitions/Person.mdd").create(in3, false, monitor);
					}
					final InputStream in4 = FileLocator.openStream(bundle, new Path("res/index.jsp"), false);
					if (!contentFolder.getFile("index.jsp").exists()) {
						contentFolder.getFile("index.jsp").create(in4, false, monitor);
					}
				}
			} catch (IOException e) {

				//TODO: throw eception
				//throw new CoreException(MakumbaWebUIPlugin.createErrorStatus(e.getMessage(), e));
			}

			monitor.worked(libs.length + 1);
			//TODO: Maybe add other libs

			final WebArtifactEdit artifact = WebArtifactEdit.getWebArtifactEditForWrite(project);

			try {
				final WebApp root = artifact.getWebApp();

				final Filter filter = setFilter(root, "org.makumba.controller.http.ControllerFilter",
						"makumba_controller");

				//TODO: check that the filters don't already exist
				setFilterMapping(root, filter, "*.jsp");
				setFilterMapping(root, filter, "*.jspx");
				setFilterMapping(root, filter, "/mak-tools");
				setFilterMapping(root, filter, "/mak-tools/*");

				artifact.saveIfNecessary(null);
			} finally {
				artifact.dispose();
			}

			monitor.worked(2 + libs.length);

		} finally {
			monitor.done();
		}

	}

	/**
	 * Looks in {@link WebApp} if the {@link Filter} with the given classname
	 * already exists, if not it creates new with the given filtername and adds
	 * it to the webapp. Otherwise it returns the found filter.
	 * 
	 * @param webapp
	 * @param className
	 *            of the filter to set up
	 * @param filterName
	 *            to use if the filter doesn't already exist
	 * @return
	 */
	private final Filter setFilter(WebApp webapp, String className, String filterName) {

		for (Object f : webapp.getFilters()) {
			if (f instanceof Filter) {
				if (((Filter) f).getFilterClassName().equals(className)) {
					return (Filter) f;
				}

			}
		}
		final Filter filter = WebapplicationFactory.eINSTANCE.createFilter();
		filter.setName(filterName);
		filter.setFilterClassName(className);
		webapp.getFilters().add(filter);
		return filter;
	}

	/**
	 * Looks in {@link WebApp} if the {@link FilterMapping} for the given
	 * {@link Filter} and with given urlPatter already exists, if not it creates
	 * new and adds it to the webapp with the given. Otherwise it returns the
	 * found filter mapping.
	 * 
	 * @param webapp
	 * @param className
	 *            of the filter to set up
	 * @param filterName
	 *            to use if the filter doesn't already exist
	 * @return
	 */
	private final FilterMapping setFilterMapping(WebApp webapp, Filter filter, String urlPattern) {

		for (Object o : webapp.getFilterMappings()) {
			if (o instanceof FilterMapping) {
				FilterMapping filterMapping = (FilterMapping) o;
				if (filterMapping.getFilter().equals(filter) && filterMapping.getUrlPattern().equals(urlPattern))
					return filterMapping;
			}
		}
		final FilterMapping filterMapping = WebapplicationFactory.eINSTANCE.createFilterMapping();
		filterMapping.setFilter(filter);
		filterMapping.setUrlPattern(urlPattern);
		webapp.getFilterMappings().add(filterMapping);
		return filterMapping;
	}
}
