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

import java.util.Vector;

import org.makumba.commons.formatters.FieldFormatter;
import org.makumba.commons.formatters.RecordFormatter;
import org.makumba.forms.tags.SearchFieldTag;

public class setEditor extends ptrEditor {

    static String[] _params = { "default", "empty", "type", "size", "labelSeparator", "elementSeparator", "nullOption",
            "forceInputStyle" };

    static String[][] _paramValues = { null, null, { "hidden", "radio", "checkbox", "tickbox", "seteditor" }, null, null, null,
            null, SearchFieldTag.allowedSelectTypes };

    @Override
    public String[] getAcceptedParams() {
        return _params;
    }

    @Override
    public String[][] getAcceptedValue() {
        return _paramValues;
    }

    private static final class SingletonHolder implements org.makumba.commons.SingletonHolder {
        static FieldEditor singleton = new setEditor();
        
        public void release() {
            singleton = null;
        }

        public SingletonHolder() {
            org.makumba.commons.SingletonReleaser.register(this);
        }
    }

    private setEditor() {
    }

    public static FieldFormatter getInstance() {
        return SingletonHolder.singleton;
    }

    @Override
    public boolean isMultiple(RecordFormatter rf, int fieldIndex) {
        return true;
    }

    @Override
    public int getDefaultSize(RecordFormatter rf, int fieldIndex) {
        return 10;
    }

    @Override
    public Object readFrom(RecordFormatter rf, int fieldIndex, org.makumba.commons.attributes.HttpParameters p,
            String suffix) {
        Object o = super.readFrom(rf, fieldIndex, p, suffix);
        if (o == null) {
            return new Vector();
        }

        /* we remove all nulls from the input */
        if (o instanceof Vector) {
            for (java.util.Iterator i = ((Vector) o).iterator(); i.hasNext();) {
                if ("".equals(i.next())) {
                    i.remove();
                }
            }
        }
        return o;
    }
    
}
