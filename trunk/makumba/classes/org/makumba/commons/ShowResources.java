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

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;

/**
 * @author
 * @version $Id$
 */
public class ShowResources {

    public static void main(String[] args) throws Exception {
        List<URL> resources = listResources(ShowResources.class.getClassLoader());

        if (args.length == 0) {
            for (URL url : resources) {
                System.out.println((url).toExternalForm());
            }
        } else {
            for (URL url2 : resources) {
                URL url = url2;
                String urlString = url.toExternalForm();
                for (String element : args) {
                    if (urlString.indexOf(element) != -1) {
                        System.out.println("Found resource: " + urlString);
                        System.out.println("First few chars: " + readAFewChars(url));
                        System.out.println();
                        break;
                    }
                }
            }
        }
    }

    private static List<URL> listResources(ClassLoader cl) throws IOException, MalformedURLException {
        List<URL> resources = new ArrayList<URL>();
        while (cl != null) {
            if (cl instanceof URLClassLoader) {
                URLClassLoader ucl = (URLClassLoader) cl;
                URL[] urls = ucl.getURLs();
                for (URL url : urls) {
                    if (url.getFile().endsWith(".jar")) {
                        listJarResources(new URL("jar:" + url.toExternalForm() + "!/"), resources);
                    } else if (url.getProtocol().equals("file")) {
                        File file = new File(url.getFile());
                        if (file.isDirectory()) {
                            listDirResources(file, resources);
                        }
                    }
                }
            }
            cl = cl.getParent();
        }
        return resources;
    }

    private static void listDirResources(File dir, List<URL> resources) throws MalformedURLException {
        File[] files = dir.listFiles();
        for (File file : files) {
            resources.add(file.toURL());
            if (file.isDirectory()) {
                listDirResources(file, resources);
            }
        }
    }

    private static void listJarResources(URL jarUrl, List<URL> resources) throws IOException, MalformedURLException {
        JarURLConnection jarConnection = (JarURLConnection) jarUrl.openConnection();

        for (Enumeration<JarEntry> entries = jarConnection.getJarFile().entries(); entries.hasMoreElements();) {
            JarEntry entry = entries.nextElement();
            resources.add(new URL(jarUrl, entry.getName()));
        }
    }

    private static String readAFewChars(URL url) throws IOException {
        StringBuffer buf = new StringBuffer(10);
        Reader reader = new InputStreamReader(url.openStream());
        for (int i = 0; i < 10; i++) {
            int c = reader.read();
            if (c == -1) {
                break;
            }
            buf.append((char) c);
        }
        reader.close();
        return buf.toString();
    }

}
