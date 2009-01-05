// /////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003 http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
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

package org.makumba.commons.formatters;

import java.util.Dictionary;

public class intEnumFormatter extends FieldFormatter {
    static String[] params = { "default", "empty", "nullOption" };

    static String[][] paramValues = { null, null, null };

    private static final class SingletonHolder implements org.makumba.commons.SingletonHolder{
        static FieldFormatter singleton = new intEnumFormatter();
        
        public void release() {
            singleton = null;
        }

        public SingletonHolder() {
            org.makumba.commons.SingletonReleaser.register(this);
        }
    }

    private intEnumFormatter() {
    }

    public static FieldFormatter getInstance() {
        return SingletonHolder.singleton;
    }

    @Override
    public String formatNotNull(RecordFormatter rf, int fieldIndex, Object o, Dictionary<String, Object> formatParams) {
        return rf.dd.getFieldDefinition(fieldIndex).getNameFor(((Integer) o).intValue());
    }
}
