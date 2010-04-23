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

package org.makumba.forms.html;

import java.util.Collection;
import java.util.Vector;

import org.makumba.commons.attributes.HttpParameters;
import org.makumba.commons.formatters.FieldFormatter;
import org.makumba.commons.formatters.InvalidValueException;
import org.makumba.commons.formatters.RecordFormatter;

public class intEnumEditor extends charEnumEditor {

    private static final class SingletonHolder implements org.makumba.commons.SingletonHolder {
        static FieldEditor singleton = new intEnumEditor();
        
        public void release() {
            singleton = null;
        }

        public SingletonHolder() {
            org.makumba.commons.SingletonReleaser.register(this);
        }
    }

    private intEnumEditor() {
    }

    public static FieldFormatter getInstance() {
        return SingletonHolder.singleton;
    }

    @Override
    public Object getOptionValue(RecordFormatter rf, int fieldIndex, Object options, int i) {
        if (nullOption != null) {
            if (i == 0) {
                return "";
            } else {
                i -= 1;
            }
        }
        return new Integer(rf.dd.getFieldDefinition(fieldIndex).getIntAt(i));
    }

    @Override
    public Object readFrom(RecordFormatter rf, int fieldIndex, HttpParameters par, String suffix) {
        Object o = par.getParameter(getInputName(rf, fieldIndex, suffix));
        // DB level should complain in this case:
        // if(o==null && isNotNull())
        // { throw new InvalidValueException(this, "null value not allowed for a
        // not null field"); }
        if (o instanceof java.util.Vector) {
            throw new InvalidValueException(rf.expr[fieldIndex], "multiple value not accepted for integer: " + o);
        }
        return toInt(rf, fieldIndex, o);
    }

    /** allowMultipleValues indicates that we still require an integer type, but we accept multiple values */
    @Override
    public Object readFrom(RecordFormatter rf, int fieldIndex, HttpParameters p, String suffix,
            boolean allowMultipleValues) {

        if (!allowMultipleValues) {
            return readFrom(rf, fieldIndex, p, suffix);
        }
        
        Object o = p.getParameter(getInputName(rf, fieldIndex, suffix));
        if (o instanceof Collection<?>) {
            // convert the generic collection to a collection of integer
            // thereby also checking that each member is actually an integer
            Collection<?> values = (Collection<?>) o;
            Vector<Integer> convertedValues = new Vector<Integer>(values.size());
            for (Object object : values) {
                convertedValues.add(toInt(rf, fieldIndex, object));
            }
            return convertedValues;
        } else {
            return readFrom(rf, fieldIndex, p, suffix);
        }
    }
}
