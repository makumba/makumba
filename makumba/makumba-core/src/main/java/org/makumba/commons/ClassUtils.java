package org.makumba.commons;

import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.logging.Logger;

public class ClassUtils {

    public static void printClassSourceInformation(Logger logger, String classType, Class<?> cls) {
        String noSuccess = classType + " source determination unsuccessful";
        try {
            ProtectionDomain pDomain = cls.getProtectionDomain();
            if (pDomain != null) {
                CodeSource cSource = pDomain.getCodeSource();
                if (cSource != null) {
                    URL loc = cSource.getLocation();
                    if (loc != null) {
                        logger.info(classType + " was loaded from: " + loc + ".");
                    } else {
                        logger.info("Couldn't retrieve location from CodeSource " + cSource + ", ProtectionDomain "
                                + pDomain + ". " + noSuccess);
                    }
                } else {
                    logger.info("Couldn't retrieve CodeSource from ProtectionDomain " + pDomain + ". " + noSuccess);
                }
            } else {
                logger.info("Couldn't retrieve ProtectionDomain for Factory" + ". " + noSuccess);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
