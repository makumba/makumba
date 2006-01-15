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
 * @version $id
 */
public class HibernateSFManager {
    
    public static String findClassesRootFolder(String locatorSeed) {
        String rootFolder ="";
        try {
            rootFolder = new File(ClassResource.get(locatorSeed).getFile()).getParentFile().getCanonicalPath();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return rootFolder;
        
    }
    
    public static SessionFactory getSF(Vector dds, String locatorSeed, String cfgFilePath, String prefix) {
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
            MddToMapping xot = new MddToMapping(dds, cfg, org.makumba.HibernateSFManager.findClassesRootFolder(locatorSeed),prefix);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        SessionFactory sf = cfg.buildSessionFactory();
        SchemaUpdate schemaUpdate = new SchemaUpdate(cfg);
        schemaUpdate.execute(true, true);
        return sf;
    }

}
