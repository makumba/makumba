package org.makumba;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
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

    private static final String PREFIX = "makumbaGeneratedMappings";

    private static final String SEED = "SEED.txt";

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

    public static SessionFactory getSF(String cfgFilePath) {
        if (sessionFactory == null) {
            
            Configuration cfg = new Configuration().configure(cfgFilePath);
            String seed, prefix;
            if((seed = cfg.getProperty("makumba.seed")) == null)
                seed = SEED;
            String seedDir= findClassesRootFolder(seed);
            System.out.println("Generating classes under "+ seedDir);

            if((prefix = cfg.getProperty("makumba.prefix")) == null)
                prefix = PREFIX;

            System.out.println("Generating mappings under "+ seedDir+File.separator+prefix);
          
            String mddList;
            Vector dds;
            if((mddList = cfg.getProperty("makumba.mdd.list")) == null)
            {
                String mddRoot;
                if((mddRoot = cfg.getProperty("makumba.mdd.root")) == null)
                    mddRoot="dataDefinitions";
                System.out.println("Working with the MDDs under "+ mddRoot);
                dds= org.makumba.MakumbaSystem.mddsInDirectory(mddRoot);
            }else{
                dds= new Vector();
                System.out.println("Working with the MDDs "+ mddList);
                for(StringTokenizer st= new StringTokenizer(mddList, ","); st.hasMoreTokens();){
                    dds.addElement(st.nextToken().trim());
                }
            }
            
            System.out.println(new java.util.Date()+" generating classes");
            try {
                MddToClass jot = new MddToClass(dds, seedDir);
            } catch (CannotCompileException e) {
                e.printStackTrace();
            } catch (NotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(new java.util.Date()+" generating mappings");
            
            try {
                MddToMapping xot = new MddToMapping(dds, cfg, org.makumba.HibernateSFManager
                        .findClassesRootFolder(seed), prefix);
      
            } catch (TransformerConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }
            System.out.println(new java.util.Date()+" building session factory");
            sessionFactory = cfg.buildSessionFactory();
            
            if("true".equals(cfg.getProperty("makumba.schemaUpdate"))){
                System.out.println(new java.util.Date()+" peforming schema update");
                SchemaUpdate schemaUpdate = new SchemaUpdate(cfg);
                schemaUpdate.execute(true, true);
                System.out.println(new java.util.Date()+" finished");
            }else
                System.out.println(new java.util.Date()+" skipping schema update");
                
        }
        return sessionFactory;
    }

    public static synchronized SessionFactory getSF() {
        if (sessionFactory == null) {
            String configFile;
            if(MakumbaSystem.getDefaultDatabaseName() == null) {
                configFile = "default.cfg.xml";
            } else {
                configFile = MakumbaSystem.getDefaultDatabaseName() + ".cfg.xml";
            }
            System.out.println("Initializing configuration from "+configFile);
            return getSF(configFile);
        }
        return sessionFactory;
    }

    
    public static Configuration getConfiguration(String cfgFilePath) {
        Configuration cfg = new Configuration().configure(cfgFilePath);
        return cfg;
    }
    

}
