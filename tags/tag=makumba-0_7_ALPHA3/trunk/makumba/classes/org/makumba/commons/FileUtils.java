package org.makumba.commons;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Gathers file related utilits.
 * 
 * @author Rudolf Mayer
 * @version $Id: FileUtils.java,v 1.1 May 17, 2008 3:34:49 PM rudi Exp $
 */
public class FileUtils {
    public static final Logger logger = Logger.getLogger("org.makumba.fileutils");

    /**
     * Return all files that match the given filter, in any subdir of the given root directory.
     */
    public static ArrayList<String> getAllFilesInDirectory(String root, String[] skipPaths, FileFilter filter) {
        File f = new File(root);
        ArrayList<String> allFiles = new ArrayList<String>();
        FileUtils.processFilesInDirectory(root, skipPaths, f, allFiles, filter);
        return allFiles;
    }

    /**
     * Process files in one directory.
     */
    public static void processFilesInDirectory(String root, String[] skipPaths, File f, ArrayList<String> allFiles,
            FileFilter filter) {
        if (!f.exists()) {
            logger.warning("Couldn't read files of directory " + f.getAbsolutePath() + ": file does not exist");
            return;
        }
        final File[] fileList = f.listFiles(filter);
        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].isDirectory()) {
                processFilesInDirectory(root, skipPaths, fileList[i], allFiles, filter);
            } else {
                try {
                    String fileName = fileList[i].getCanonicalPath().substring(root.length());
                    if (StringUtils.startsWith(fileName, skipPaths)) {
                        logger.info("Skipping file " + fileName + ", indicated in skip-list "
                                + Arrays.toString(skipPaths));
                    } else {
                        allFiles.add(fileName);
                    }
                } catch (IOException e) {
                    logger.warning("Could not compute canonical path for " + fileList[i].getAbsolutePath());
                }
            }
        }
    }

}
