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

import org.makumba.commons.formatters.FieldFormatter;
import org.makumba.commons.formatters.InvalidValueException;
import org.makumba.commons.formatters.RecordFormatter;

public class realEditor extends intEditor {

    static String[][] __paramValues = { null, null, null, null, new String[] { "spinner" }, null };

    @Override
    public String[][] getAcceptedValue() {
        return __paramValues;
    }

    private static final class SingletonHolder {
        static FieldEditor singleton = new realEditor();
    }

    private realEditor() {
    }

    public static FieldFormatter getInstance() {
        return SingletonHolder.singleton;
    }

    @Override
    public Object readFrom(RecordFormatter rf, int fieldIndex, org.makumba.commons.attributes.HttpParameters par,
            String suffix) {
        Object o = par.getParameter(getInputName(rf, fieldIndex, suffix));

        if (o instanceof java.util.Vector) {
            throw new InvalidValueException(rf.expr[fieldIndex], "multiple value not accepted for real: " + o);
        }
        return toReal(rf, fieldIndex, o);
    }

}
