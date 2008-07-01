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

package org.makumba;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * Computes the version from SVN BaseURL tag. If used from a HEAD check-out, this will return "devel-<currentDate>" If
 * used from a TAG check-out, this will return the version of the tag, e.g. "0.7.1"
 * 
 * Note that in order to work, the file needs to have the HeadURL keyword enabled
 * 
 * @author Cristian Bogdan
 * @author Stefan Baebler
 * @author Frederik Habilis
 * @author Rudolf Mayer
 * @author Manuel Gay
 */
public class version {

    /** @see MakumbaSystem#getVersion() */
    static String getVersion() {
        String vs = "$HeadURL$";
        
        // HeadURL will return something like
        // https://makumba.svn.sourceforge.net/svnroot/makumba/trunk/makumba/classes/org/makumba/version.java for HEAD
        // https://makumba.svn.sourceforge.net/svnroot/makumba/tags/makumba-0_5_10_2/makumba/classes/org/makumba/version.java
        // for a tagged version

        String version = "1";
        vs = vs.substring("$HeadURL: https://makumba.svn.sourceforge.net/svnroot/makumba/".length());

        if (vs.startsWith("tags")) {
            vs = vs.substring("tags/".length());
            vs = vs.substring(0, vs.indexOf("/"));

            version = vs.substring(7);

            if (version.indexOf('-') >= 0)
                // we have something like "-0_5_10_2"
                version = version.substring(version.indexOf('-') + 1);

            if (version.length() > 2)
                // we have something like "0_5_10_2"
                version = version.replace('_', '.');

        } else {
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss"); // yyyy-MMM-dd HH:mm:ss
            Date buildDate = getBuildDate();
            if(buildDate != null) {
                version = "devel-" + df.format(getBuildDate());
            } else {
                version = "devel-DATE-NOT-FOUND";
            }
        }

        return version;
    }

    /** @return only the numeric version in dewey decimal format */
    @Deprecated
    static String getVersionDewey() {
        String vs = getVersion();
        return vs.substring(vs.indexOf('-') + 1, vs.length());
    }

    /** Reads a build date from properties file that was generated during compilation. */
    static final Date getBuildDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        Properties prop = new Properties();
        String filename = "org/makumba/versionBuildDate.properties";
        Date buildDate = null;

        try {
            prop.load(org.makumba.commons.ClassResource.get(filename).openStream());
            buildDate = df.parse(prop.getProperty("buildDate"), new java.text.ParsePosition(0));
        } catch (Exception e) {
            // TODO: throw an exception (needs to adapt some other classes / methods, as doStartTag in MakumbaInfoTag
            java.util.logging.Logger.getLogger("org.makumba." + "version").severe(
                "There was a problem reading the Makumba 'versionBuildDate.properties'. If you deleted the working directory of your web application, please make sure you reload the web application!");
        }
        return buildDate;
    }

    public static void main(String[] args) {
        System.out.println("name=Makumba");
        System.out.println("version=" + getVersion());
        // System.out.println("versionDewey=" + getVersionDewey());
        System.out.println("date=" + new java.util.Date());
        try {
            System.out.println("buildhost=" + (java.net.InetAddress.getLocalHost()).getHostName() + " ("
                    + (java.net.InetAddress.getLocalHost()).getHostAddress() + ")");
        } catch (Exception e) {
            System.out.println("buildhost=unknown.host");
        }
        System.out.println("java.vendor=" + java.lang.System.getProperty("java.vendor"));
        System.out.println("java.version=" + java.lang.System.getProperty("java.version"));
    }

    /** Fetches global SVN version with svnversion * */
    public static String getGlobalSVNRevision() {
        Writer out = new StringWriter();
        PrintWriter ps = new PrintWriter(out);
        Process p1;
        try {
            p1 = Runtime.getRuntime().exec("svnversion -n " + new java.io.File(".").getAbsolutePath(), null,
                new java.io.File(".").getAbsoluteFile());
        } catch (IOException e) {
            if (e.getMessage().indexOf("2") > 0) { // svnversion not found
                System.out.println("you need to have svn utils installed in order to retrieve the gloabl build version");
            }
            ps.println(e);
            return "";
        }

        final Process p = p1;
        final PrintWriter ps1 = ps;
        new Thread(new Runnable() {
            public void run() {
                flushTo(new BufferedReader(new InputStreamReader(p.getErrorStream()), 81960), ps1);
            }
        }).start();

        flushTo(new BufferedReader(new InputStreamReader(p.getInputStream()), 81960), ps);

        try {
            p.waitFor();
        } catch (InterruptedException e) {
            return "";
        }

        return out.toString();

    }

    public static void flushTo(BufferedReader r, PrintWriter o) {
        String s;
        try {
            while ((s = r.readLine()) != null)
                o.println(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
