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

package org.makumba.db.makumba.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.makumba.FieldDefinition;
import org.makumba.InvalidValueException;
import org.makumba.Pointer;
import org.makumba.providers.QueryAnalysis;
import org.makumba.providers.SQLQueryGenerator;

/**
 * this class takes parameters passed to an OQL query and transmits them to the corresponding PreparedStatement. The
 * order in the two is different, because OQL parameters are numbered. Also, strict type checking is performed for the
 * parameters
 */
public class ParameterAssigner {
    TableManager paramHandler;
    org.makumba.db.makumba.Database db;
    QueryAnalysis qA;
    SQLQueryGenerator qG;

    ParameterAssigner(org.makumba.db.makumba.Database db, QueryAnalysis qA, SQLQueryGenerator qG) {
        this.qA = qA;
        this.qG = qG;
        this.db = db;
    }

    static final Object[] empty = new Object[0];

    public String assignParameters(PreparedStatement ps, Object[] args) throws SQLException {
        if (qG.getSQLArgumentNumber() == 0) {
            return null;
        }
        
        if (qG.getSQLArgumentNumber() > 0) {
            paramHandler = (TableManager) db.makePseudoTable(qG.getSQLQueryArgumentTypes());
        }

        
        try {
            Hashtable<String, Integer> correct = new Hashtable<String, Integer>();
            Hashtable<String, InvalidValueException> errors = new Hashtable<String, InvalidValueException>();
            for (int i = 0; i < qG.getSQLArgumentNumber(); i++) {
                FieldDefinition fd = qG.getSQLQueryArgumentTypes().getFieldDefinition(i);
                if (fd == null) {
                    throw new IllegalStateException("No type assigned for param" + i + " of query " + qA.getQuery());
                }

                String spara = "$" + i;
                Object value = args[i];
                if (value == Pointer.Null) {
                    value = fd.getNull();
                }
                try {
                    value = fd.checkValue(value);
                } catch (InvalidValueException e) {
                    // we have a wrong value, we pass something instead and we remember that there is a problem.
                    // if there is no correct value for this argument, we'll throw an exception later
                    if (correct.get(spara) == null) {
                        errors.put(spara, e);
                    }
                    paramHandler.setNullArgument(fd.getName(), ps, i + 1);
                    continue;
                }
                correct.put(spara, i);
                errors.remove(spara);

                paramHandler.setUpdateArgument(fd.getName(), ps, i + 1, value);
            }
            if (errors.size() > 0) {
                String s = "";
                for (Enumeration<String> e = errors.keys(); e.hasMoreElements();) {
                    Object o = e.nextElement();
                    s += "\nargument: " + o + "; exception:\n" + errors.get(o);
                }
                return s;
            }
        } catch (ArrayIndexOutOfBoundsException ae) {
            throw new org.makumba.MakumbaError("wrong number of arguments to query ");
        }
        return null;
    }
}
