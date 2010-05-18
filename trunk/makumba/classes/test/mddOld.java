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

package test;

import java.util.Enumeration;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.makumba.DataDefinitionNotFoundError;
import org.makumba.DataDefinitionParseError;
import org.makumba.providers.Configuration;
import org.makumba.providers.DataDefinitionProvider;

/**
 * Testing mdd handling & parsing, for the old RecordInfo-based MDD parser. In order to run this test you need to make
 * sure that "dataDefinitionProvider=recordinfo" is set in the Makumba.conf file (dataSource section).
 * 
 * @author Stefan Baebler
 */
public class mddOld extends TestCase {

    private DataDefinitionProvider ddp = DataDefinitionProvider.getInstance();

    public mddOld(String name) {
        super(name);
        Configuration.setPropery("dataSourceConfig", "dataDefinitionProvider",
            Configuration.RECORDINFO_DATADEFINITIONPROVIDER);

    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(mddOld.class);
    }

    public void testMdd() {
        ddp.getDataDefinition("test.Person");
        ddp.getDataDefinition("test.Person.address.sth");
    }

    public void testNonexistingMdd() {
        try {
            ddp.getDataDefinition("test.brokenMdds.NonexistingMdd");
            fail("Should raise DataDefinitionNotFoundError");
        } catch (DataDefinitionNotFoundError e) {
        }
    }

    public void testWronglyCapitalizedMdd() {
        final String osName = System.getProperty("os.name");
        if (osName.toLowerCase().startsWith("windows")) {
            // This test can't be performed on a Windows platform! Windows does not support capitalization in file name.
            System.out.println("\n\nOperating System is "
                    + osName
                    + ", skipping 'testWronglyCapitalizedMdd' test (Windows does not support capitalization in file name).\n");
        } else {
            try {
                ddp.getDataDefinition("test.person");
                fail("Should raise DataDefinitionNotFoundError");
            } catch (DataDefinitionNotFoundError e) {
            }
        }
    }

    public void testAllValidMdds() {
        String base = "test/validMdds/";
        Vector<String> mdds = ddp.getDataDefinitionsInLocation(base);

        // we have to collect all errors if we want to run tests on all
        // MDDs in directory instead of stopping at first fail()ure.
        Vector<String> errors = new Vector<String>();
        for (Enumeration<String> e = mdds.elements(); e.hasMoreElements();) {
            String mdd = (String) e.nextElement();
            try {
                ddp.getDataDefinition("test.validMdds." + mdd);
            } catch (DataDefinitionParseError ex) {
                errors.add("\n ." + (errors.size() + 1) + ") Error reported in valid MDD <" + mdd + ">:\n" + ex);
                // ex.printStackTrace();
            }
        }
        if (errors.size() > 0)
            fail("\n  Tested " + mdds.size() + " valid MDDs, but found " + errors.size() + " problems: "
                    + errors.toString());
    }

    public void testIfAllBrokenMddsThrowErrors() {
        String base = "test/brokenMdds/";
        Vector<String> mdds = ddp.getDataDefinitionsInLocation(base);

        // we have to collect all errors if we want to run tests on all
        // MDDs in directory instead of stoping at first fail()ure.
        Vector<String> errors = new Vector<String>();
        for (Enumeration<String> e = mdds.elements(); e.hasMoreElements();) {
            DataDefinitionParseError expected = new DataDefinitionParseError();
            DataDefinitionParseError actual = expected;
            String mdd = (String) e.nextElement();
            try {
                ddp.getDataDefinition("test.brokenMdds." + mdd);
            } catch (DataDefinitionParseError thrown) {
                actual = thrown;
            }

            if (expected == actual)
                errors.add("\n ." + (errors.size() + 1) + ") Error report missing from broken MDD <" + mdd + "> ");
            if (!expected.getClass().equals(actual.getClass()))
                errors.add("\n ." + (errors.size() + 1) + ") MDD " + mdd + " threw <" + actual.getClass()
                        + "> instead of expected <" + expected.getClass() + ">");
        }
        if (errors.size() > 0)
            fail("\n  Tested " + mdds.size() + " broken MDDs, but " + errors.size() + " reported wrong/no error: "
                    + errors.toString());
    }

}
