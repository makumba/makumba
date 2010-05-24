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
//  $Id: AllTests.java 5200 2010-05-23 15:35:50Z manuel_gay $
//  $Name$
/////////////////////////////////////

package org.makumba.test;

import org.makumba.test.component.ConcurrentTest;
import org.makumba.test.component.ConfigurationTest;
import org.makumba.test.component.MddTest;
import org.makumba.test.component.TableHibernateTest;
import org.makumba.test.component.TableTest;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.makumba.test.tags.FormsHQLTest;
import org.makumba.test.tags.FormsOQLTest;
import org.makumba.test.tags.ListHQLTest;
import org.makumba.test.tags.ListOQLTest;

/**
 * TestSuite that runs all the Makumba tests
 * 
 * @author Stefan Baebler
 * @author Manuel Gay
 */
public class AllTests {

    public static void main(String[] args) {
        System.out.println("Makumba test suite: Running all tests...");
        TestResult tr = junit.textui.TestRunner.run(suite());
        if (!tr.wasSuccessful()) {
            System.exit(1); // report an error
        }
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("All JUnit Tests for Makumba");
        suite.addTest(ConfigurationTest.suite());
        suite.addTest(MddTest.suite());
        suite.addTest(TableTest.suite());
        suite.addTest(TableHibernateTest.suite());
        suite.addTest(ListOQLTest.suite());
        suite.addTest(FormsOQLTest.suite());
        suite.addTest(ListHQLTest.suite());
        suite.addTest(FormsHQLTest.suite());
        suite.addTest(ConcurrentTest.suite());
        return suite;
    }
}
