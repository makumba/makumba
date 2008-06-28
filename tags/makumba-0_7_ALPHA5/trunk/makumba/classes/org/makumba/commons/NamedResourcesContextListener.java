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

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletContextEvent;

public class NamedResourcesContextListener implements javax.servlet.ServletContextListener {
    public NamedResourcesContextListener() {
        // this also ensures that the loadTime of MakumbaSystem is set
        java.util.logging.Logger.getLogger("org.makumba." + "system").info("loading makumba context listener");
    }

    public void contextInitialized(ServletContextEvent sce) {
    }

    public void contextDestroyed(ServletContextEvent sce) {
        java.util.logging.Logger.getLogger("org.makumba." + "system").info("destroying makumba caches");
        NamedResources.cleanup();
        JDBCUnload();
    }

    private void JDBCUnload() {
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        ArrayList<Driver> driversToUnload=new ArrayList<Driver>();
        while (drivers.hasMoreElements()) {
                Driver driver = drivers.nextElement();
                if (driver.getClass().getClassLoader().equals(getClass().getClassLoader())) {
                        driversToUnload.add(driver);
                }
        }
        for (Driver driver : driversToUnload) {
            try {
                DriverManager.deregisterDriver(driver);
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
    }
}
