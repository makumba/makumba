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

/** Computes the version from cvs Name tag. */
class version {

   /** @see MakumbaSystem.getVersion() */
   static String getVersion()
   {
	String version=("$Name$".substring(7,"$Name$".length()-2));
	if(version.indexOf('-')>0) version=version.substring(version.indexOf('-')+1);
	if(version.length()>2) version=version.replace('_','.');
	else 
	{
		//SimpleDateFormat formatter = new SimpleDateFormat ("yyyyMMddHHmmss");
 		//version="devel-"+formatter.format(new Date());
		version="development";
	}
	return version;
   }

    public static void main(String[] args) {

        System.out.println("name=Makumba"); 
        System.out.println("version="+getVersion()); 
        System.out.println("date="+new java.util.Date()); 
        try{
          System.out.println("buildhost="+(java.net.InetAddress.getLocalHost()).getHostName()+" ("+(java.net.InetAddress.getLocalHost()).getHostAddress()+")"); 
        } catch (Exception e) {
          System.out.println("buildhost=unknown.host"); 
        }
    }
}
