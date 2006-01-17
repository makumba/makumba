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
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.makumba.DataDefinition;
import org.makumba.HibernateSFManager;
import org.makumba.Pointer;
import org.makumba.db.hibernate.HibernatePointer;
import org.makumba.db.hibernate.hql.HqlAnalyzer;
import org.makumba.db.sql.SQLPointer;
import org.makumba.util.ArrayMap;

public class HibernateQueryRunner extends AbstractQueryRunner {
    Session session;

    public Vector execute(String query, Object[] args, int offset, int limit) {
        query = query.replaceAll("\\$", "\\:p"); // replace makumba style params to hibernate style params

        HqlAnalyzer analyzer = new HqlAnalyzer(query);

        session = HibernateSFManager.getSF().openSession();

        Query q = session.createQuery(query);

        q.setFirstResult(offset);
        if (limit != -1) { // limit parameter was specified
            q.setMaxResults(limit);
        }
        if (args != null) {
            // TODO: take out the && i < q.getNamedParameters().length [used for testing]
            for (int i = 0; i < args.length && i < q.getNamedParameters().length; i++) {
                String param = "p" + (i + 1);
                if (args[i] instanceof Vector) {
                    q.setParameterList(param, (Collection) args[i]);
                } else if (args[i] instanceof Date) {
                    q.setParameter(param, args[i], Hibernate.DATE);
                } else if (args[i] instanceof Integer) {
                    q.setParameter(param, args[i], Hibernate.INTEGER);
                } else { // we have any param type (most likely String)
                    q.setParameter("p" + (+1), args[i]);
                }
            }
        }
        List list = q.list();
        Vector results = new Vector(list.size());

        DataDefinition dataDef = analyzer.getProjectionType();
        
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
            } else {  // our query had more results ==>
                resultFields = (Object[]) resultRow; // we had an object[] already
            }
            
            // process each field's result
            for (int j = 0; j < resultFields.length; j++) { // 
                if (resultFields[j] != null) { // we add to the dictionary only fields with values in the DB
                    if (analyzer.getProjectionType().getFieldDefinition(j).getType().startsWith("ptr")) {                        
                        // we have a pointer
                        String ddName = analyzer.getProjectionType().getFieldDefinition(j).getPointedType().getName();
                        Pointer pointer = new HibernatePointer(ddName, ((Integer) resultFields[j]).intValue());
                        resultFields[j] = pointer;
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
        session.close();
    }

    /**
     * Method for testing the query runner outside a JSP
     */
    public static void main(String[] args) {
        HibernateQueryRunner qr = new HibernateQueryRunner();
        GregorianCalendar cal = new GregorianCalendar(1984, 1, 1);
        Vector v = new Vector();
        v.add(new Integer(1));
        v.add(new Integer(2));
        Object[] params1 = new Object[] { "Cristian", new Timestamp(cal.getTimeInMillis()), v, new Integer(1),
                new SQLPointer("general.Person", 151022406) };
        Object[] params2 = new Object[] { "Cristian", new Timestamp(cal.getTimeInMillis()), new Integer(1),
                new Integer(2) };
        Object[] params3 = new Object[] { new Double(2.0) };

        String query1 = "SELECT p.id as ID, p.name as name, p.surname as surname, p.birthdate as date, p.T_shirt as shirtSize FROM general.Person p where p.name = $1 AND p.birthdate is not null AND p.birthdate > :p2 and p.T_shirt in (:p3)";
        String query2 = "SELECT p.id as ID, p.name as name, p.surname as surname, p.birthdate as date, p.T_shirt as shirtSize FROM general.Person p where p.name = $1 AND p.birthdate is not null AND p.birthdate > :p2 and p.T_shirt in (:p3, :p4)";
        String query3 = "SELECT e.subject as subject, e.spamLevel AS spamLevel from general.archive.Email e WHERE e.spamLevel = :p1";

        String[] queries = new String[] { query1, query2, query3 };
        Object[] params = new Object[] { params1, params2, params3 };
        for (int i = 0; i < Math.min(queries.length, params.length); i++) {
            System.out.println("Query" + queries[i] + " ==> \n"
                    + printQueryResults(qr.execute(queries[i], (Object[]) params[i], 0, 100)) + "\n\n");
        }
    }

    public static String printQueryResults(Vector v) {
        String result = "";
        for (int i = 0; i < v.size(); i++) {
            result += "Row " + i + ":" + v.elementAt(i) + "\n";
        }
        return result;
    }
}
