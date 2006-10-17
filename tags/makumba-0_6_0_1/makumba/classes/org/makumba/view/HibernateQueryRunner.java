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

package org.makumba.view;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.Dictionary;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.hibernate.CacheMode;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.makumba.Attributes;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.HibernateSFManager;
import org.makumba.LogicException;
import org.makumba.MakumbaSystem;
import org.makumba.Pointer;
import org.makumba.ProgrammerError;
import org.makumba.db.hibernate.HibernatePointer;
import org.makumba.db.hibernate.hql.HqlAnalyzer;
import org.makumba.db.sql.SQLPointer;
import org.makumba.util.ArrayMap;

public class HibernateQueryRunner extends AbstractQueryRunner {
    Session session;

    Transaction transaction;

    public Vector execute(String query, Object[] args, int offset, int limit) {
        return null;
    }

    public Vector executeDirect(String query, Attributes a, int offset, int limit) throws LogicException {
        MakumbaSystem.getLogger("hibernate").info("Executing hibernate query " + query);

        HqlAnalyzer analyzer = MakumbaSystem.getHqlAnalyzer(query);
        DataDefinition dataDef = analyzer.getProjectionType();

        // check the query for correctness (we do not allow "select p from Person p", only "p.id")
        for (int i = 0; i < dataDef.getFieldNames().size(); i++) {
            FieldDefinition fd = dataDef.getFieldDefinition(i);
            if (fd.getType().equals("ptr")) { // we have a pointer
                if (!(fd.getDescription().equalsIgnoreCase("ID") || fd.getDescription().startsWith("hibernate_"))) {
                    throw new ProgrammerError("Invalid HQL query - you must not select the whole object '"
                            + fd.getDescription() + "' in the query'" + query + "'!\nYou have to select '"
                            + fd.getDescription() + ".id' instead.");
                }
            }
        }

        // FIXME: we might want to open the session in a constructor, to re-use it for more than one exection
        session = HibernateSFManager.getSF().openSession();
        session.setCacheMode(CacheMode.IGNORE);
        transaction = session.beginTransaction();
        Query q = session.createQuery(query);

        q.setCacheable(false); // we do not cache queries

        q.setFirstResult(offset);
        if (limit != -1) { // limit parameter was specified
            q.setMaxResults(limit);
        }
        if (a != null) {
            String[] queryParams = q.getNamedParameters();
            for (int i = 0; i < queryParams.length; i++) {
                if (a.getAttribute(queryParams[i]) instanceof Vector) {
                    q.setParameterList(queryParams[i], (Collection) a.getAttribute(queryParams[i]));
                } else if (a.getAttribute(queryParams[i]) instanceof Date) {
                    q.setParameter(queryParams[i], a.getAttribute(queryParams[i]), Hibernate.DATE);
                } else if (a.getAttribute(queryParams[i]) instanceof Integer) {
                    q.setParameter(queryParams[i], a.getAttribute(queryParams[i]), Hibernate.INTEGER);
                } else { // we have any param type (most likely String)
                    q.setParameter(queryParams[i], a.getAttribute(queryParams[i]));
                }
            }
        }

        // TODO: find a way to not fetch the results all by one, but row by row, to reduce the memory used in both the
        // list returned from the query and the Vector composed out of.
        // see also bug
        List list = q.list();
        Vector results = new Vector(list.size());

        Object[] projections = dataDef.getFieldNames().toArray();
        Dictionary keyIndex = new java.util.Hashtable(projections.length);
        for (int i = 0; i < projections.length; i++) {
            keyIndex.put(projections[i], new Integer(i));
        }

        int i = 1;
        for (Iterator iter = list.iterator(); iter.hasNext(); i++) {
            Object resultRow = iter.next();
            Object[] resultFields;
            if (!(resultRow instanceof Object[])) { // our query result has only one field
                resultFields = new Object[] { resultRow }; // we put it into an object[]
            } else { // our query had more results ==>
                resultFields = (Object[]) resultRow; // we had an object[] already
            }

            // process each field's result
            for (int j = 0; j < resultFields.length; j++) { // 
                if (resultFields[j] != null) { // we add to the dictionary only fields with values in the DB
                    FieldDefinition fd;
                    if ((fd = dataDef.getFieldDefinition(j)).getType().equals("ptr")) {
                        // we have a pointer
                        String ddName = fd.getPointedType().getName();
                        // FIXME: once we do not get dummy pointers from hibernate queries, take this out
                        if (resultFields[j] instanceof Pointer) { // we have a dummy pointer
                            resultFields[j] = new HibernatePointer(ddName, ((Pointer) resultFields[j]).getUid());
                        } else { // we have an integer
                            resultFields[j] = new HibernatePointer(ddName, ((Integer) resultFields[j]).intValue());
                        }
                    } else {
                        resultFields[j] = resultFields[j];
                    }
                }
            }
            Dictionary dic = new ArrayMap(keyIndex, resultFields);
            results.add(dic);
        }
        return results;
    }

    public void close() {
        if (session != null) {
            transaction.commit();
            session.close();
        }
    }

    /**
     * Method for testing the query runner outside a JSP
     */
    public static void main(String[] args) throws LogicException {
        HibernateSFManager.getSF(); // we use a simple configuration
        HibernateQueryRunner qr = new HibernateQueryRunner();
        Vector v = new Vector();
        v.add(new Integer(1));
        v.add(new Integer(2));
        v.add(new Integer(3));
        v.add(new Integer(4));
        Attributes params = qr.new MyAttributes();
        params.setAttribute("date", new Timestamp(new GregorianCalendar(1970, 1, 1).getTimeInMillis()));
        params.setAttribute("name", "Cristian");
        params.setAttribute("someInt", new Integer(1));
        params.setAttribute("someSet", v);
        params.setAttribute("generalPerson", new SQLPointer("general.Person", 151022406));
        params.setAttribute("someDouble", new Double(2.0));

        String query1 = "SELECT p.id as ID, p.name as name, p.surname as surname, p.birthdate as date, p.T_shirt as shirtSize FROM general.Person p where p.name = :name AND p.birthdate is not null AND p.birthdate > :date AND p.T_shirt = :someInt";
        String query2 = "SELECT p.id as ID, p.name as name, p.surname as surname, p.birthdate as date, p.T_shirt as shirtSize FROM general.Person p where p.name = :name AND p.birthdate is not null AND p.birthdate > :date and p.T_shirt in (:someSet) order by p.surname DESC";
        String query3 = "SELECT e.subject as subject, e.spamLevel AS spamLevel from general.archive.Email e WHERE e.spamLevel = :someDouble";

        String[] queries = new String[] { query1, query2, query3 };
        for (int i = 0; i < queries.length; i++) {
            System.out.println("Query " + queries[i] + " ==> \n"
                    + printQueryResults(qr.executeDirect(queries[i], params, 0, 50)) + "\n\n");
        }
    }

    public static String printQueryResults(Vector v) {
        String result = "";
        for (int i = 0; i < v.size(); i++) {
            result += "Row " + i + ":" + v.elementAt(i) + "\n";
        }
        return result;
    }

    class MyAttributes implements Attributes {
        Hashtable attr = new Hashtable();

        /**
         * @see org.makumba.Attributes#getAttribute(java.lang.String)
         */
        public Object getAttribute(String name) throws LogicException {
            return attr.get(name);
        }

        /**
         * @see org.makumba.Attributes#setAttribute(java.lang.String, java.lang.Object)
         */
        public Object setAttribute(String name, Object value) throws LogicException {
            if (value != null) {
                return attr.put(name, value);
            } else {
                throw new LogicException("No value for " + name);
            }
        }

        /**
         * @see org.makumba.Attributes#removeAttribute(java.lang.String)
         */
        public void removeAttribute(String name) throws LogicException {
            attr.remove(name);
        }

        /**
         * @see org.makumba.Attributes#hasAttribute(java.lang.String)
         */
        public boolean hasAttribute(String s) {
            return attr.get(s) != null;
        }
        
        /**
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return attr.toString();
        }
    }
}
