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

import java.util.Dictionary;
import java.util.Vector;

import org.makumba.NullObject;
import org.makumba.commons.formatters.FieldFormatter;
import org.makumba.commons.formatters.RecordFormatter;

public class setintEnumEditor extends setcharEnumEditor {

    private static final class SingletonHolder implements org.makumba.commons.SingletonHolder {
        static FieldEditor singleton = new setintEnumEditor();

        @Override
        public void release() {
            singleton = null;
        }

        public SingletonHolder() {
            org.makumba.commons.SingletonReleaser.register(this);
        }
    }

    private setintEnumEditor() {
    }

    public static FieldFormatter getInstance() {
        return SingletonHolder.singleton;
    }

    @Override
    public Object getOptions(RecordFormatter rf, int fieldIndex, Dictionary<String, Object> formatParams) {
        ChoiceSet c = (ChoiceSet) formatParams.get(ChoiceSet.PARAMNAME);
        if (c != null) {
            return c;
        }
        return super.getOptions(rf, fieldIndex, formatParams);
    }

    @Override
    public Object getOptionValue1(RecordFormatter rf, int fieldIndex, Object options, int i) {
        if (options != null) {
            Object ret = ((ChoiceSet) options).get(i).getValue();
            if (ret == null || ret instanceof NullObject) {
                return "";
            }
            return ret;
        }
        return new Integer(rf.dd.getFieldDefinition(fieldIndex).getIntAt(i));
    }

    @Override
    public Object readFrom(RecordFormatter rf, int fieldIndex, org.makumba.commons.attributes.HttpParameters par,
            String suffix) {
        Object o = par.getParameter(getInputName(rf, fieldIndex, suffix));

        if (o == null || o == org.makumba.Pointer.NullSet || "".equals(o)) {
            return new Vector<Object>();
        }
        if (o instanceof Vector) {
            @SuppressWarnings("unchecked")
            Vector<Object> v = (Vector<Object>) o;
            cleanEmpty(v);

            for (int i = 0; i < v.size(); i++) {
                v.setElementAt(toInt(rf, fieldIndex, v.elementAt(i)), i);
            }
            return v;
        }
        return toInt(rf, fieldIndex, o);
    }
}
