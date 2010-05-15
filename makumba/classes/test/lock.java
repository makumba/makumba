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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.makumba.Transaction;
import org.makumba.providers.TransactionProvider;

/**
 * Testing locking related operations run "ant test.lock" from a number of consoles to test locks
 * 
 * @author cristi
 */
public class lock extends TestCase {

    public lock(String name) {
        super(name);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(lock.class);
    }

    Transaction db;

    @Override
    public void setUp() {
        TransactionProvider tp = TransactionProvider.getInstance();
        db = tp.getConnectionTo(tp.getDefaultDataSourceName());
    }

    @Override
    public void tearDown() {
        db.close();
    }

    public void testLock() {
        System.out.println("locking");
        db.lock("something");
        System.out.println("waiting");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
        }
        System.out.println("closing");
    }
}
