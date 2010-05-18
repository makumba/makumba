///////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003  http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//  -------------
//  $Id$
//  $Name$
/////////////////////////////////////

package org.makumba.commons;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * Gathers file related utilits.
 * 
 * @author Rudolf Mayer
 * @version $Id$
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

    /** Gets an input stream from a file-system file or JAR file */
    public static InputStream getInputStream(URL url) throws IOException {
        if (!url.toExternalForm().startsWith("jar:")) {
            return url.openStream();
        } else {
            final JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
            return jar.getInputStream(((JarURLConnection) url.openConnection()).getJarEntry());
        }
    }

}
