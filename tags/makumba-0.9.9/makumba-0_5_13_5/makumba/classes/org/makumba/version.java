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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/** Computes the version from cvs Name tag. */
class version {


   /** @see MakumbaSystem#getVersion() */
   static String getVersion()
   {
        String vs="$Name$";
        String version= vs.substring(7,vs.length()-2);
	if(version.indexOf('-')>0) version=version.substring(version.indexOf('-')+1);
	if(version.length()>2) version=version.replace('_','.');
	else 
	{
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
 		version="devel-"+df.format(getBuildDate());
 		//version="devel-"+getBuildDate();
		//version="development";
	}
	return version;
   }

   /** @return only the numeric version in dewey decimal format */
   static String getVersionDewey()
   {
	String vs=getVersion();
	return vs.substring(vs.indexOf('-')+1,vs.length());
   }

   /** Reads a build date from properties file that was generated during compilation. */
   static final Date getBuildDate()
   {
	SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
	Properties prop=new Properties(); 
	String filename = "org/makumba/versionBuildDate.properties";            
	Date buildDate=null;

	try{
	    prop.load(org.makumba.util.ClassResource.get(filename).openStream());
	    buildDate=df.parse(prop.getProperty("buildDate"),new java.text.ParsePosition(0));
	} catch (Exception e) { 
	  //some error handling here 
	  System.out.println(e);
	} 
	return buildDate;
   }

    public static void main(String[] args) {

        System.out.println("name=Makumba"); 
        System.out.println("version="+getVersion()); 
        System.out.println("versionDewey="+getVersionDewey()); 
        System.out.println("date="+new java.util.Date()); 
        try{
          System.out.println("buildhost="+(java.net.InetAddress.getLocalHost()).getHostName()+" ("+(java.net.InetAddress.getLocalHost()).getHostAddress()+")"); 
        } catch (Exception e) {
          System.out.println("buildhost=unknown.host"); 
        }
        System.out.println("java.vendor="+java.lang.System.getProperty("java.vendor")); 
        System.out.println("java.version="+java.lang.System.getProperty("java.version")); 
    }
}
