package org.makumba.forms.responder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;
import org.makumba.controller.Logic;
import org.makumba.controller.http.ControllerFilter;

/**
 * This class is handling the caching mechanism of the Responder. There are two caches: one memory cache and one cache
 * on the disk. In case of server failure, responder objects are serialized on the disk so they can be retrieved again
 * after restart and form data is not lost.
 * 
 * @author Manuel Gay
 * @version $Id: ResponderCacheManager.java,v 1.1 03.10.2007 09:54:18 Manuel Exp $
 */
public class ResponderCacheManager {

    static Hashtable<Integer, Object> indexedCache = new Hashtable<Integer, Object>();

    public static String makumbaResponderBaseDirectory;

    static String validResponderFilename(int responderValue) {
        return new String(ResponderCacheManager.makumbaResponderBaseDirectory + "/")
                + String.valueOf(responderValue).replaceAll("-", "_");
    }

    public static void setResponderWorkingDir(HttpServletRequest request) {
        // set the correct working directory for the responders
        if (ResponderCacheManager.makumbaResponderBaseDirectory == null) {
            System.out.println("had an empty responder dir - working dir ==> "
                    + request.getSession().getServletContext().getAttribute("javax.servlet.context.tempdir"));
            String baseDir = request.getSession().getServletContext().getAttribute("javax.servlet.context.tempdir")
                    + System.getProperty("file.separator") + "makumba-responders"
                    + System.getProperty("file.separator");
            ResponderCacheManager.makumbaResponderBaseDirectory = baseDir + request.getContextPath();
            if (!new File(ResponderCacheManager.makumbaResponderBaseDirectory).exists()) {
                new File(baseDir).mkdir();
                new File(ResponderCacheManager.makumbaResponderBaseDirectory).mkdir();
            }
            System.out.println("base dir: " + ResponderCacheManager.makumbaResponderBaseDirectory);
        }
    }

    /**
     * Returns the first responder object found fitting the request. It can be used to retrieve information
     * about the form which is valid for all nested forms, and is used e.g. in {@link ControllerFilter} to find out the
     * value of {@link #getReloadFormOnError()}.
     * @param req the current request
     * @return the first responder fitting the request.
     */
    public static Responder getFirstResponder(ServletRequest req) {
        Iterator responderCodes = Responder.getResponderCodes((HttpServletRequest) req);
        if (responderCodes.hasNext()) {
            String code = (String) responderCodes.next();
            String suffix = "";
            String parentSuffix = null;
            int n = code.indexOf(Responder.suffixSeparator);
            if (n != -1) {
                suffix = code.substring(n);
                parentSuffix = "";
                n = suffix.indexOf(Responder.suffixSeparator, 1);
                if (n != -1) {
                    parentSuffix = suffix.substring(n);
                    suffix = suffix.substring(0, n);
                }
            }
            return ResponderCacheManager.getResponder(code, suffix, parentSuffix);
        } else {
            return null;
        }
    }

    /**
     * Fetches a responder from the cache. If the memory cache is empty we try to get the responder from the disk.
     * @param code the responder code
     * @param suffix the responder suffix
     * @param parentSuffix the suffix of the parent responder (of the parent form)
     * @return the cached responder object, if any could be found
     */
    static Responder getResponder(String code, String suffix, String parentSuffix) {
        Integer i = new Integer(Integer.parseInt(code));
        Responder fr = ((Responder) ResponderCacheManager.indexedCache.get(i));
        String fileName = ResponderCacheManager.validResponderFilename(i.intValue());

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
                java.util.logging.Logger.getLogger("org.makumba." + "controller").log(Level.SEVERE,
                    "Error while trying to check for responder on the HDD: could not read from file " + fileName, e);
                new File(fileName).delete();
                throw new org.makumba.MakumbaError(
                        "Responder cannot be re-used due to Makumba version change! Please reload this page.");
            } catch (InvalidClassException e) {
                // same as above
                java.util.logging.Logger.getLogger("org.makumba." + "controller").log(Level.SEVERE,
                    "Error while trying to check for responder on the HDD: could not read from file " + fileName, e);
                new File(fileName).delete();
                throw new org.makumba.MakumbaError(
                        "Responder cannot be re-used due to Makumba version change! Please reload this page.");
            } catch (IOException e) {
                java.util.logging.Logger.getLogger("org.makumba." + "controller").log(Level.SEVERE,
                    "Error while trying to check for responder on the HDD: could not read from file " + fileName, e);
            } catch (ClassNotFoundException e) {
                java.util.logging.Logger.getLogger("org.makumba." + "controller").log(Level.SEVERE,
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

        public Object getHashObject(Object o) {
            return ((Responder) o).responderKey();
        }

        public Object makeResource(Object name, Object hashName) {
            Responder f = (Responder) name;
            f.identity = hashName.hashCode();

            String fileName = validResponderFilename(f.identity);

            if (indexedCache.get(new Integer(f.identity)) == null) { // responder not in cache
                try {
                    if (!new File(fileName).exists()) { // file does not exist
                        f.controllerClassname = f.controller.getClass().getName();
                        ObjectOutputStream objectOut = new ObjectOutputStream(new FileOutputStream(fileName));
                        objectOut.writeObject(f); // we write the responder to disk
                        objectOut.close();
                    }
                } catch (IOException e) {
                    java.util.logging.Logger.getLogger("org.makumba." + "controller").log(Level.SEVERE,
                        "Error while trying to check for responder on the HDD: could not read from file " + fileName, e);
                }
            }
            indexedCache.put(new Integer(f.identity), name);

            return name;
        }
    });

}
