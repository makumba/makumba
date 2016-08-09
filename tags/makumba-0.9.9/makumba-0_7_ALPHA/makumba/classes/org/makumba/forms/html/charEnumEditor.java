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
import org.makumba.commons.formatters.RecordFormatter;

public class charEnumEditor extends choiceEditor {
    private static final class SingletonHolder {
        static final FieldEditor singleton = new charEnumEditor();
    }

    /** Don't use this, use getInstance() */
    protected charEnumEditor() {
    }

    public static FieldFormatter getInstance() {
        return SingletonHolder.singleton;
    }

    public Object getOptions(RecordFormatter rf, int fieldIndex, java.util.Dictionary fP) {
        return null;
    }

    public int getOptionsLength(RecordFormatter rf, int fieldIndex, Object opts) {
        int enumeratorSize = rf.dd.getFieldDefinition(fieldIndex).getEnumeratorSize();
        if (nullOption != null) {
            return enumeratorSize + 1;
        } else {
            return enumeratorSize;
        }
    }

    public Object getOptionValue(RecordFormatter rf, int fieldIndex, Object options, int i) {
        if (nullOption != null) {
            if (i == 0) {
                return null;
            } else {
                i -= 1;
            }
        }
        return rf.dd.getFieldDefinition(fieldIndex).getStringAt(i);
    }

    public String formatOptionValue(RecordFormatter rf, int fieldIndex, Object val) {
        return val.toString();
    }

    public String formatOptionValue(RecordFormatter rf, int fieldIndex, Object opts, int i, Object val) {
        return val.toString();
    }

    public String formatOptionTitle(RecordFormatter rf, int fieldIndex, Object options, int i) {
        if (nullOption != null) {
            if (i == 0) {
                return nullOption;
            } else {
                i -= 1;
            }
        }
        return rf.dd.getFieldDefinition(fieldIndex).getNameAt(i);
    }

    public String getMultiple(RecordFormatter rf, int fieldIndex) {
        return "";
    }

    public boolean isMultiple(RecordFormatter rf, int fieldIndex) {
        return false;
    }

    public int getDefaultSize(RecordFormatter rf, int fieldIndex) {
        return 1;
    }

}
