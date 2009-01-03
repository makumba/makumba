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

import java.util.HashMap;
import java.util.Map;

import org.makumba.Attributes;
import org.makumba.Transaction;
import org.makumba.commons.attributes.RequestAttributes;
import org.makumba.db.TransactionImplementation;
import org.makumba.providers.TransactionProvider;

/**
 * A group of database connections, at most one per database name. They can be closed all at a time. This object is not
 * thread-safe. The typical use is database accesses made by a JSP page (which take place all in the same thread of the
 * servlet engine).
 * 
 * @author Cristian Bogdan
 */
public class DbConnectionProvider {
    
    private TransactionProvider tp;
    
    Map<String, Transaction> connections = new HashMap<String, Transaction>(7);

    private Attributes contextAttributes;

    public Transaction getConnectionTo(String dbname) {
        if(tp == null) {
            tp = TransactionProvider.getInstance();
        }
        
        Transaction db = connections.get(dbname);
        if (db == null) {
            connections.put(dbname, db = tp.getConnectionTo(dbname));
        }
        ((TransactionImplementation)db).setContext(contextAttributes);
        return db;
    }

    /** Close all connections. */
    public void close() {
        for (Transaction transaction : connections.values()) {
            (transaction).close();
        }
        connections.clear();
    }

    @Override
    protected void finalize() {
        close();
    }
    
    public void setTransactionProvider(TransactionProvider tp) {
        this.tp = tp;
    }
    
    public TransactionProvider getTransactionProvider() {
        if(tp == null) {
            tp = TransactionProvider.getInstance();
        }
        return tp;
    }

    public void setContext(Attributes attributes) {
        contextAttributes=attributes;
        
    }

}
