package org.makumba;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javassist.CannotCompileException;
import javassist.NotFoundException;

import javax.xml.transform.TransformerConfigurationException;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.makumba.db.hibernate.MddToClass;
import org.makumba.db.hibernate.MddToMapping;
import org.makumba.util.ClassResource;
import org.xml.sax.SAXException;

/**
 * 
 * @author manu
 * @author rudi
 * @version $Id$
 */
public class HibernateSFManager {

    //private static final String PREFIX = "makumbaGeneratedMappings";

    //private static final String SEED = "dataDefinitions";

    private static SessionFactory sessionFactory;

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

    public static SessionFactory getSF(Vector dds, String locatorSeed, String cfgFilePath) {
        if (sessionFactory == null) {
            Configuration cfg = new Configuration().configure(cfgFilePath);

            System.out.println(new java.util.Date());
            try {
                MddToClass jot = new MddToClass(dds, org.makumba.HibernateSFManager.findClassesRootFolder(locatorSeed));
            } catch (CannotCompileException e) {
                e.printStackTrace();
            } catch (NotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(new java.util.Date());

            try {
                MddToMapping xot = new MddToMapping(dds, cfg, org.makumba.HibernateSFManager
                        .findClassesRootFolder(cfg.getProperty("makumba.seed")), cfg.getProperty("makumba.prefix"));
      
            } catch (TransformerConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }
            System.out.println(cfg.getProperty("makumba.seed"));
            sessionFactory = cfg.buildSessionFactory();
            SchemaUpdate schemaUpdate = new SchemaUpdate(cfg);
            schemaUpdate.execute(true, true);
        }
        return sessionFactory;
    }

    public static SessionFactory getSF() {
        if (sessionFactory == null) {
            String configFile;
            if(MakumbaSystem.getDefaultDatabaseName() == null) {
                configFile = "org/makumba/db/hibernate/localhost_mysql_karambasmall.cfg.xml";
            } else {
                configFile = MakumbaSystem.getDefaultDatabaseName() + ".cfg.xml";
            }
            
            Vector dds = org.makumba.MakumbaSystem.mddsInDirectory(getConfiguration(configFile).getProperty("makumba.seed"));
            
            return getSF(dds, configFile, "org/makumba/db/hibernate/localhost_mysql_karambasmall.cfg.xml");
        }
        return sessionFactory;
    }

    /**
     * Returns session factory with a limited set of MDDs for testing purposes
     */
    public static SessionFactory getTestSF() {
        if (sessionFactory == null) {
            Vector dds = new Vector();
            dds.add("test.Person");
            //dds.add("test.Country");
            //dds.add("test.archive.Email");
            return getSF(dds, "test", "org/makumba/db/hibernate/localhost_mysql_karambasmall.cfg.xml");
        }
        return sessionFactory;
    }
    
    public static Configuration getConfiguration(String cfgFilePath) {
        Configuration cfg = new Configuration().configure(cfgFilePath);
        return cfg;
    }
    

}
