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

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.makumba.DataDefinition;
import org.makumba.HibernateSFManager;
import org.makumba.db.hibernate.HibernateOqlAnalyzer;
import org.makumba.db.sql.SQLPointer;

public class HibernateQueryRunner extends AbstractQueryRunner {
    Session session;

    public Vector execute(String query, Object[] args, int offset, int limit) {
        session = HibernateSFManager.getSF().openSession();
        
        query = query.replaceAll("\\$", "\\:p"); // replace makumba style params to hibernate style params
        Query q = session.createQuery(query);

        q.setFirstResult(offset);
        if (limit != -1) { // limit parameter was specified
            q.setMaxResults(limit);
        }
        if (args != null) {
            // TODO: take out the && i < q.getNamedParameters().length [used for testing]
            for (int i = 0; i < args.length && i < q.getNamedParameters().length; i++) {
                String param = "p" + (i+1);
                if (args[i] instanceof Vector) {
                    q.setParameterList(param, (Collection) args[i]);
                    System.out.println("set COLLECTION param " + (i+1) + " ==> " + args[i]);
                } else if (args[i] instanceof Date) {
                    q.setParameter(param, args[i], Hibernate.DATE);
                } else if (args[i] instanceof Integer) {
                    q.setParameter(param, args[i], Hibernate.INTEGER );
                } else { // we have any param type (most likely String) 
                    q.setParameter("p" + (+1), args[i]);
                }                
            }
        }
        List list = q.list();
        Vector results = new Vector(list.size());
        HibernateOqlAnalyzer analyzer = HibernateOqlAnalyzer.getOqlAnalyzer(
                query, HibernateSFManager.getSF());
        DataDefinition dataDef = analyzer.getProjectionType();
        Object[] labels = dataDef.getFieldNames().toArray();
        int i = 1;
        for (Iterator iter = list.iterator(); iter.hasNext(); i++) {
            Object resultRow = iter.next();
            Dictionary dic = new Hashtable();
            if (resultRow instanceof Object[]) { // our query result has more than one field
                Object[] elements = (Object[]) resultRow;
                for (int j = 0; j < elements.length; j++) {
                    if (elements[j] != null) { // we add to the dictionary only fields with values in the DB
                        dic.put(labels[j], elements[j]);
                    }
                }
            } else { // the query result had just one field
                dic.put(labels[0], resultRow);
            }
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
        Object[] params1 = new Object[] { "Cristian", new Timestamp(cal.getTimeInMillis()), v, new Integer(1), new SQLPointer("general.Person", 151022406)  };
        qr.execute("SELECT p.id as ID, p.name as name, p.surname as surname, p.birthdate as date, p.T_shirt as shirtSize FROM general.Person p where p.name = $1 AND p.birthdate is not null AND p.birthdate > :p2 and p.T_shirt in :p3",
                        params1, 0, 20);
        Object[] params2 = new Object[] { new Double(2.0) };
        qr.execute("SELECT e.subject from general.archive.Email e WHERE e.spamLevel = :p1", params2, 0, -1);
    }
}
