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
//  $Id: ConfigurationTest.java 5201 2010-05-23 15:39:32Z manuel_gay $
//  $Name$
/////////////////////////////////////

package org.makumba.test.component;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.makumba.ConfigurationError;
import org.makumba.MakumbaSystem;
import org.makumba.providers.TransactionProvider;

/**
 * Testing configuration related operations
 * 
 * @author Stefan Baebler
 */
public class ConfigurationTest extends TestCase {
    public ConfigurationTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(ConfigurationTest.class);
    }

    private TransactionProvider tp = TransactionProvider.getInstance();

    public void testBuildInfo() {
        System.out.println("\nTesting Makumba version: " + MakumbaSystem.getVersion() + "\n		   built on: "
                + MakumbaSystem.getBuildDate() + "\n	       using locale: " + MakumbaSystem.getLocale());
    }

    public void disabledTestNoDefaultDB() {
        try {
            String defaultDB = tp.getDefaultDataSourceName();
            fail("Should raise ConfigFileError, but found: " + defaultDB);
        } catch (ConfigurationError e) {
        }
    }

}
