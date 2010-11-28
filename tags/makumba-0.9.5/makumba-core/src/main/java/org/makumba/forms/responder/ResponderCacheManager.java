package org.makumba.forms.responder;

import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;
import org.makumba.controller.Logic;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.Hashtable;
import java.util.logging.Level;

/**
 * This class is handling the caching mechanism of the Responder. There are two caches: one memory cache and one cache
 * on the disk. In case of server failure, responder objects are serialized on the disk so they can be retrieved again
 * after restart and form data is not lost.
 * 
 * @author Manuel Bernhardt <manuel@makumba.org>
 * @version $Id: ResponderCacheManager.java,v 1.1 03.10.2007 09:54:18 Manuel Exp $
 */
public class ResponderCacheManager {

    private static class SingletonHolder implements org.makumba.commons.SingletonHolder {
        private static ResponderCacheManager singleton = new ResponderCacheManager();

        public void release() {
            singleton = null;
        }

        public SingletonHolder() {
            org.makumba.commons.SingletonReleaser.register(this);
        }

    }

    private ResponderFactory factory;

    public static ResponderCacheManager getInstance() {
        return SingletonHolder.singleton;
    }

    static Hashtable<Integer, Object> indexedCache = new Hashtable<Integer, Object>();

    /** The directory where makumba will store to and load responders from. */
    public static String makumbaResponderBaseDirectory;

    static String validResponderFilename(int responderValue) {
        return new String(makumbaResponderBaseDirectory + "/") + String.valueOf(responderValue).replaceAll("-", "_");
    }
    
    public String getResponderBaseDirectory() {
        return makumbaResponderBaseDirectory;
    }

    /** Sets the responder working directory from the "javax.servlet.context.tempdir" variable. */
    public void setResponderWorkingDir(HttpServletRequest request) {
        // set the correct working directory for the responders
        if (makumbaResponderBaseDirectory == null) {
            Object tempDir = request.getSession().getServletContext().getAttribute("javax.servlet.context.tempdir");
            String contextPath = request.getContextPath();
            setResponderWorkingDir(tempDir, contextPath);
        }
    }

    /** Sets the responder working directory from the given temp directory and context path. */
    public void setResponderWorkingDir(Object tempDir, String contextPath) {
        java.util.logging.Logger.getLogger("org.makumba.controller").info(
            "had an empty responder dir - working dir ==> " + tempDir);
        String baseDir = tempDir + System.getProperty("file.separator") + "makumba-responders"
                + System.getProperty("file.separator");
        makumbaResponderBaseDirectory = baseDir + contextPath;
        if (!new File(makumbaResponderBaseDirectory).exists()) {
            new File(baseDir).mkdir();
            new File(makumbaResponderBaseDirectory).mkdir();
        }
        java.util.logging.Logger.getLogger("org.makumba.controller").info("base dir: " + makumbaResponderBaseDirectory);
    }

    public void setResponderWorkingDir(String path) {
        makumbaResponderBaseDirectory = path;
    }

    /**
     * Fetches a responder from the cache. If the memory cache is empty we try to get the responder from the disk.
     * 
     * @param code
     *            the responder code
     * @param suffix
     *            the responder suffix
     * @param parentSuffix
     *            the suffix of the parent responder (of the parent form)
     * @return the cached responder object, if any could be found
     */
    Responder getResponder(String code, String suffix, String parentSuffix) {
        Integer i = new Integer(Integer.parseInt(code));
        Responder fr = (Responder) indexedCache.get(i);
        String fileName = validResponderFilename(i.intValue());

        // responder check
        if (fr == null) { // we do not have responder in cache --> try to get it from disk
            ObjectInputStream objectIn = null;
            try {
                objectIn = new ObjectInputStream(new FileInputStream(fileName));
                fr = (Responder) objectIn.readObject();
                fr.postDeserializaton();
                fr.controller = Logic.getController(fr.controllerClassname);
            } catch (UnsupportedClassVersionError e) {
                // if we try to read a responder that was written with a different version of the responder class
                // we delete it, and throw an exception
                java.util.logging.Logger.getLogger("org.makumba.controller").log(Level.SEVERE,
                    "Error while trying to check for responder on the HDD: could not read from file " + fileName, e);
                new File(fileName).delete();
                throw new org.makumba.MakumbaError(
                        "Responder cannot be re-used due to Makumba version change! Please reload this page.");
            } catch (InvalidClassException e) {
                // same as above
                java.util.logging.Logger.getLogger("org.makumba.controller").log(Level.SEVERE,
                    "Error while trying to check for responder on the HDD: could not read from file " + fileName, e);
                new File(fileName).delete();
                throw new org.makumba.MakumbaError(
                        "Responder cannot be re-used due to Makumba version change! Please reload this page.");
            } catch (IOException e) {
                java.util.logging.Logger.getLogger("org.makumba.controller").log(Level.SEVERE,
                    "Error while trying to check for responder on the HDD: could not read from file " + fileName, e);
            } catch (ClassNotFoundException e) {
                java.util.logging.Logger.getLogger("org.makumba.controller").log(Level.SEVERE,
                    "Error while trying to check for responder on the HDD: class not found: " + fileName, e);
            } finally {
                if (objectIn != null) {
                    try {
                        objectIn.close();
                    } catch (IOException e1) {
                    }
                }
            }
            if (fr == null) { // we did not find the responder on the disk
                throw new org.makumba.MakumbaError(
                        "Responder cannot be found, probably due to server restart. Please reload this page.");
            }
        }
        // end responder check
        return fr;
    }

    /**
     * We cache the responder in the memory because if two forms are the same (often the case in multiple forms), their
     * responder looks exactly the same.
     */
    static NamedResources cache = new NamedResources("controller.responders", new NamedResourceFactory() {

        private static final long serialVersionUID = 1L;

        @Override
        public Object getHashObject(Object o) {
            return ((Responder) o).responderKey();
        }

        @Override
        public Object makeResource(Object name, Object hashName) {
            Responder f = (Responder) name;
            f.identity = hashName.hashCode();

            if (indexedCache.get(new Integer(f.identity)) == null) { // responder not in cache
                f.saveResponderToDisc();
            }
            indexedCache.put(new Integer(f.identity), name);

            return name;
        }
    });

    public ResponderFactory getFactory() {
        return factory;
    }

    public void setFactory(ResponderFactory factory) {
        this.factory = factory;
    }

}
