package org.makumba;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import javassist.CannotCompileException;
import javassist.NotFoundException;

import javax.xml.transform.TransformerConfigurationException;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.makumba.commons.ClassResource;
import org.makumba.commons.NameResolver;
import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;
import org.makumba.db.hibernate.MddToClass;
import org.makumba.db.hibernate.MddToMapping;
import org.makumba.providers.TransactionProvider;
import org.xml.sax.SAXException;

/**
 * Hibernate Session Factory Manager: handles the configuration and generation of the Hibernate sessionFactory used by
 * Makumba.<br>
 * Additional settings can be provided in the configuration file (databaseName.cfg.xml), such as:
 * <ul>
 * <li>makumba.seed: the name of the seed file, used to locate the root folder of where makumba should place the
 * generated mappings and classes</li>
 * <li>makumba.prefix: the prefix of all the makumba generated mappings (i.e. the name of the folder where they will be
 * stored, also used as package name)</li>
 * <li>makumba.mdd.root: the name of the folder in which the MDDs of the webapp are located. If none is provided, the
 * default value is "dataDefinitions".</li>
 * <li>makumba.mdd.list: a comma-separated list of MDDs that should be used. If none is provided, makumba will use all
 * the MDDs in the root folder, if there are.</li>
 * <li>makumba.mdd.additionalList: a comma-separated list of MDDs that should be used in addition to all MDDs found in
 * the root folder. if none is provided, makumba will use all the MDDs in the root folder, if there are.</li>
 * <li>makumba.schemaUpdate: indicates whether makumba should do a schema update with the session factory.</li>
 * </ul>
 * <br>
 * Additionaly, it is possible to:
 * <ul>
 * <li>Add external mapping resources by providing a Vector containing the relative path to those resources (as one
 * would do with Hibernate's Configuration.addResource()) using the setExternalConfigurationResources() method</li>
 * <li>Tell Makumba to use an already existing session factory using the setHibernateSessionFactory() method</li>
 * </ul>
 * 
 * @author Manuel Gay
 * @author Rudolf Mayer
 * @author Cristian Bogdan
 * @version $Id$
 */
public class HibernateSFManager {

    private static final String PREFIX = "makumbaGeneratedMappings";

    private static final String SEED = "SEED.txt";

    private static Vector<String> externalConfigurationResources = new Vector<String>();

    private static Configuration configuredConfiguration;

    private static SessionFactory sf;
    
    private static Vector<String> generatedClasses;

    public static String findClassesRootFolder(String locatorSeed) {
        String rootFolder = "";
        try {
            rootFolder = new File(ClassResource.get(locatorSeed).getFile()).getParentFile().getCanonicalPath();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return rootFolder;
    }

    public static int sessionFactories = NamedResources.makeStaticCache("hibernate session factory",
        new NamedResourceFactory() {

            private static final long serialVersionUID = 1L;

            protected Object makeResource(Object nm, Object hashName) throws Exception {
                return makeSF((String) nm);
            }
        }, false);

    public static SessionFactory getSF(String cfgFilePath) {
        return (SessionFactory) NamedResources.getStaticCache(sessionFactories).getResource(cfgFilePath);
    }

    private static SessionFactory makeSF(String cfgFilePath) {
        if (sf != null) {
            return sf;
        }

        cfgFilePath += ".cfg.xml";

        Configuration cfg = new Configuration().configure(cfgFilePath);

        for (String res : externalConfigurationResources) {
            cfg.addResource(res);
        }

        String seed, prefix;
        if ((seed = cfg.getProperty("makumba.seed")) == null)
            seed = SEED;
        String seedDir = findClassesRootFolder(seed);
        java.util.logging.Logger.getLogger("org.makumba." + "hibernate.sf").info("Generating classes under " + seedDir);

        if ((prefix = cfg.getProperty("makumba.prefix")) == null)
            prefix = PREFIX;

        java.util.logging.Logger.getLogger("org.makumba." + "hibernate.sf").info(
            "Generating mappings under " + seedDir + File.separator + prefix);

        String mddList;
        Vector<String> dds = new Vector<String>();
        
        if ((mddList = cfg.getProperty("makumba.mdd.list")) != null) {
            dds = new Vector<String>();
            java.util.logging.Logger.getLogger("org.makumba." + "hibernate.sf").info("Working with the MDDs " + mddList);
            for (StringTokenizer st = new StringTokenizer(mddList, ","); st.hasMoreTokens();) {
                dds.addElement(st.nextToken().trim());
            }

        } else if ((mddList = cfg.getProperty("makumba.mdd.additionalList")) != null) {
            dds = getDefaultMDDs(cfg);
            java.util.logging.Logger.getLogger("org.makumba." + "hibernate.sf").info(
                "Working with additional MDDs " + mddList);
            for (StringTokenizer st = new StringTokenizer(mddList, ","); st.hasMoreTokens();) {
                dds.addElement(st.nextToken().trim());
            }

        } else {
            dds = getDefaultMDDs(cfg);
        }

        // internal makumba MDDs are there by default
        dds.add("org.makumba.controller.ErrorLog");
        dds.add("org.makumba.controller.MultipleSubmit");

// please uncomment these if you think they are needed. I think they should only take effect in developer environments
//        dds.add("org.makumba.devel.relations.Relation");
//        dds.add("org.makumba.devel.relations.RelationOrigin");
//        dds.add("org.makumba.devel.relations.WebappDatabase");
        
        java.util.logging.Logger.getLogger("org.makumba." + "hibernate.sf").info("Generating classes");

        // FIXME this is an ugly workaround for the current state of the code. there should be only ONE config file, not
        // two
        String databaseProperties = cfgFilePath.substring(0, cfgFilePath.indexOf(".cfg.xml")) + ".properties";
        Properties p = new Properties();
        try {
            p.load(org.makumba.commons.ClassResource.get(databaseProperties).openStream());
        } catch (Exception e) {
            throw new org.makumba.ConfigFileError(databaseProperties);
        }

        NameResolver nr = new NameResolver(p);

        try {
            MddToClass jot = new MddToClass(dds, seedDir, nr);
        } catch (CannotCompileException e) {
            e.printStackTrace();
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        java.util.logging.Logger.getLogger("org.makumba." + "hibernate.sf").info("Generating mappings");

        try {
            MddToMapping xot = new MddToMapping(dds, cfg, org.makumba.HibernateSFManager.findClassesRootFolder(seed),
                    prefix, nr);

        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        java.util.logging.Logger.getLogger("org.makumba." + "hibernate.sf").info("building session factory");
        SessionFactory sessionFactory = cfg.buildSessionFactory();

        if ("true".equals(cfg.getProperty("makumba.schemaUpdate"))) {
            // if(!schemaUpd)
            // throw new ProgrammerError("Hibernate schema update must be authorized, remove it from cfg.xml!");
            java.util.logging.Logger.getLogger("org.makumba." + "hibernate.sf").info("Peforming schema update");
            SchemaUpdate schemaUpdate = new SchemaUpdate(cfg);
            schemaUpdate.execute(true, true);
            java.util.logging.Logger.getLogger("org.makumba." + "hibernate.sf").info("Schema update finished");
        } else
            java.util.logging.Logger.getLogger("org.makumba." + "hibernate.sf").info("skipping schema update");

        configuredConfiguration = cfg;
        
        generatedClasses = dds;
        java.util.logging.Logger.getLogger("org.makumba." + "hibernate.sf").info("Generated the classes " + dds);

        return sessionFactory;
    }

    private static Vector<String> getDefaultMDDs(Configuration cfg) {
        Vector<String> dds;
        String mddRoot;
        if ((mddRoot = cfg.getProperty("makumba.mdd.root")) == null)
            mddRoot = "dataDefinitions";
        java.util.logging.Logger.getLogger("org.makumba." + "hibernate.sf").info(
            "Working with the MDDs under " + mddRoot);
        dds = org.makumba.MakumbaSystem.mddsInDirectory(mddRoot);
        return dds;
    }

    public static synchronized SessionFactory getSF() {
        String configFile;
        String defaultDataSourceName = TransactionProvider.getInstance().getDefaultDataSourceName();
        if (defaultDataSourceName == null) {
            configFile = "default";
        } else {
            configFile = defaultDataSourceName;
        }
        java.util.logging.Logger.getLogger("org.makumba." + "hibernate.sf").info(
            "Initializing configuration from " + configFile);
        return getSF(configFile);
    }

    public static Configuration getConfiguration(String cfgFilePath) {
        Configuration cfg = new Configuration().configure(cfgFilePath);
        return cfg;
    }

    /**
     * Gets the Hibernate {@link Configuration} that was used to create the {@link SessionFactory}
     * @return
     */
    public static Configuration getConfiguredConfiguration() {
        return configuredConfiguration;
    }

    /**
     * Sets additional resources to be included in the Configuration and used at {@link SessionFactory} creation time.
     * @param resources a Vector of String containing the relative path to the additional mapping resources
     */
    public static void setExternalConfigurationResources(Vector<String> resources) {
        externalConfigurationResources = resources;
    }

    /**
     * Sets the {@link SessionFactory} Makumba should use. In this case Makumba won't generate any mappings.
     * @param sessionFactory the instance of the Hibernate {@link SessionFactory} Makumba should use
     */
    public static void setHibernateSessionFactory(SessionFactory sessionFactory) {
        sf = sessionFactory;
    }
    
    public static String getFullyQualifiedName(String className) {
        return (String) configuredConfiguration.getImports().get(className);
    }
    
    public static Vector<String> getGeneratedClasses() {
        return generatedClasses;
    }

}
