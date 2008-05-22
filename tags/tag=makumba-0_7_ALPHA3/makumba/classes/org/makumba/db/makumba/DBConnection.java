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

package org.makumba.db.makumba;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.LogicException;
import org.makumba.Pointer;
import org.makumba.ProgrammerError;
import org.makumba.Transaction;
import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.db.TransactionImplementation;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.QueryProvider;
import org.makumba.providers.TransactionProviderInterface;

/**
 * This is the Makumba-specific implementation of a {@link Transaction}
 * 
 * @author Cristian Bogdan
 * @author Manuel Gay
 * @version $Id$
 */
public abstract class DBConnection extends TransactionImplementation {
   
    private QueryProvider qp;
    
    protected String dataSource;
    
    protected org.makumba.db.makumba.Database db;
    
    protected DBConnection(TransactionProviderInterface tp) {
        super(tp);
    }//for the wrapper
    
    public DBConnection(Database database, TransactionProviderInterface tp) {
        this(tp);
        this.db = database;
        this.ddp = DataDefinitionProvider.getInstance();
    }

    public DBConnection(Database database, String dataSource, TransactionProviderInterface tp) {
        this(database, tp);
        this.dataSource = dataSource;
        this.qp = QueryProvider.makeQueryRunner(getDataSource(), "oql");
    }

    public org.makumba.db.makumba.Database getHostDatabase() {
        return db;
    }

    /** Get the name of the database in the form host[_port]_dbprotocol_dbname */
    public String getName() {
        return db.getName();
    }

    Map<String, Pointer> locks = new HashMap<String, Pointer>(13);

    Hashtable<String, String> lockRecord = new Hashtable<String, String>(5);

    public void lock(String symbol) {
        lockRecord.clear();
        lockRecord.put("name", symbol);
        locks.put(symbol, insert("org.makumba.db.makumba.Lock", lockRecord));
    }

    public void unlock(String symbol) {
        Pointer p = (Pointer) locks.get(symbol);
        if (p == null)
            throw new ProgrammerError(symbol + " not locked in connection " + this);
        deleteLock(symbol);
    }

    protected void deleteLock(String symbol) {
        locks.remove(symbol);
        // we need to delete after the lock name instead of the pointer
        // in order not to produce deadlock
        delete("org.makumba.db.makumba.Lock l", "l.name=$1", symbol);
    }

    protected void unlockAll() {
        for (Iterator i = locks.keySet().iterator(); i.hasNext();) {
            deleteLock((String) i.next());
        }
    }

    protected StringBuffer writeReadQuery(Pointer p, Enumeration e) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ");
        String separator = "";
        while (e.hasMoreElements()) {
            Object o = e.nextElement();
            DataDefinition r = ddp.getDataDefinition(p.getType());
            if (!(o instanceof String))
                throw new org.makumba.NoSuchFieldException(r,
                        "Dictionaries passed to makumba DB operations should have String keys. Key <" + o
                                + "> is of type " + o.getClass() + r.getName());
            if (r.getFieldDefinition((String) o) == null)
                throw new org.makumba.NoSuchFieldException(r, (String) o);
            String s = (String) o;
            sb.append(separator).append("p.").append(s).append(" as ").append(s);
            separator = ",";
        }
        sb.append(" FROM " + p.getType() + " p WHERE p=$1");
        return sb;
    }
    
    protected Vector executeReadQuery(Pointer p, StringBuffer sb) {
        Object[] params = { p };
        Vector v = executeQuery(sb.toString(), params);
        return v;
    }

    /** insert a record */
    @Override
    public Pointer insert(String type, Dictionary data) {
        Table t = db.getTable(type);
        t.computeInsertHook();

        if (t.insertHook != null) {
            Hashtable<Object, Object> h = new Hashtable<Object, Object>();
            for (Enumeration e = data.keys(); e.hasMoreElements();) {
                Object k = e.nextElement();
                h.put(k, data.get(k));
            }
            data = h;
        }

        if (t.insertHook == null || t.insertHook.transform(data, this)) {
            return super.insert(type, data);
        }
        return null;
    }
 
    class QueryAndArgs{
        String query;
        Object[] args;
        protected Object[] getArgs() {
            return args;
        }
        protected String getQuery() {
            return query;
        }
        QueryAndArgs(String OQL, Object a){
            Map args1=paramsToMap(a);
            MultipleAttributeParametrizer mpa= ((MultipleAttributeParametrizer) queries.getResource(OQL));
            try {
                query=mpa.getTransformedQuery(args1);
                args= mpa.getTransformedParams(args1);
            } catch (LogicException e) {
                throw new RuntimeWrappedException(e);
            }


        }
    }
    /**
     * Execute a parametrized OQL query.
     * 
     * @return a Vector of Dictionaries
     */
    public java.util.Vector executeQuery(String OQL, Object args, int offset, int limit) {
        QueryAndArgs qa= new QueryAndArgs(OQL, args);
        Object[] k = { qa.getQuery(), "" };
        return ((Query) getHostDatabase().queries.getResource(k)).execute(qa.getArgs(), this, offset, limit);

    }
    NamedResources queries = new NamedResources("OQL query multiple-parametrizers", new NamedResourceFactory() {

        private static final long serialVersionUID = 1L;

        protected Object makeResource(Object nm, Object hashName) {

            return new MultipleAttributeParametrizer((String) nm);
        }
    });
    
    public int insertFromQuery(String type, String OQL, Object args) {
        QueryAndArgs qa= new QueryAndArgs(OQL, args);
        Object[] k = { qa.getQuery(), type };
        return ((Query) getHostDatabase().queries.getResource(k)).insert(qa.getArgs(), this);
    }

    public java.util.Vector executeQuery(String OQL, Object args) {
        return executeQuery(OQL, args, 0, -1);
    }
 
    static final String whereDelim=" ##### ";

    /**
     * Execute a parametrized update or delete. A null set means "delete"
     * 
     * @return a Vector of Dictionaries
     */
    @Override
    public int executeUpdate(String type, String set, String where, Object args) {
        if (set != null && set.trim().length() == 0) {
            throw new org.makumba.OQLParseError("Invalid empty update 'set' section in: UPDATE "+type+" SET (empty!) WHERE "+where);
        }

        if (where != null && where.trim().length() == 0) {
            where = null;
        }
        QueryAndArgs qa= new QueryAndArgs((set==null?"":set)+whereDelim+(where==null?"":where), args);
        Object[] multi = { type, qa.getQuery(), whereDelim };

        return ((Update) getHostDatabase().updates.getResource(multi)).execute(this, qa.getArgs());
    }
    
    public Query getQuery(String OQL) {
        Object[] k = { OQL, "" };
        return ((Query) getHostDatabase().queries.getResource(k));
    }
    
    @Override
    public String getNullConstant() {
        return "nil";
    }
    
    @Override
    public String getDataSource() {
        return this.dataSource;
    }
    
    // FIXME should be done at construction time, but due to nature of how DB is now it's not possible
    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }
}