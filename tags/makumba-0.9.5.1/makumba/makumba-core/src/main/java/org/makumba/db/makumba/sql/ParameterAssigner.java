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

import org.makumba.FieldDefinition;
import org.makumba.providers.ParameterTransformer;
import org.makumba.providers.QueryAnalysis;

/**
 * this class takes parameters passed to an OQL query and transmits them to the corresponding PreparedStatement. The
 * order in the two is different, because OQL parameters are numbered. Also, strict type checking is performed for the
 * parameters
 */
public class ParameterAssigner {
    TableManager paramHandler;

    org.makumba.db.makumba.Database db;

    QueryAnalysis qA;

    ParameterTransformer qG;

    ParameterAssigner(org.makumba.db.makumba.Database db, QueryAnalysis qA, ParameterTransformer qG) {
        this.qA = qA;
        this.qG = qG;
        this.db = db;
    }

    static final Object[] empty = new Object[0];

    public String assignParameters(PreparedStatement ps, Object[] args) throws SQLException {
        if (qG.getParameterCount() == 0) {
            return null;
        }

        if (qG.getParameterCount() > 0) {
            paramHandler = (TableManager) db.makePseudoTable(qG.getTransformedParameterTypes());
        }

        try {
            for (int i = 0; i < qG.getParameterCount(); i++) {
                FieldDefinition fd = qG.getTransformedParameterTypes().getFieldDefinition(i);
                if (fd == null) {
                    throw new IllegalStateException("No type assigned for param" + i + " of query " + qA.getQuery());
                }

                Object value = args[i];
                if (value == org.makumba.Pointer.Null) {
                    value = fd.getNull();
                }

                value = fd.checkValue(value);

                paramHandler.setUpdateArgument(fd.getName(), ps, i + 1, value);
            }

        } catch (ArrayIndexOutOfBoundsException ae) {
            throw new org.makumba.MakumbaError("wrong number of arguments to query ");
        }
        return null;
    }
}
