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
//  $Id: intEditor.java 1749 2007-10-03 15:56:11Z manuel_gay $
//  $Name$
/////////////////////////////////////package org.makumba.forms.html;
package org.makumba.forms.html;

import java.util.Dictionary;

import org.makumba.commons.formatters.FieldFormatter;
import org.makumba.commons.formatters.InvalidValueException;
import org.makumba.commons.formatters.RecordFormatter;

/**
 * Boolean choice editor
 * 
 * TODO we should be able to choose what to display & select by default (Yes or No) and what is the text to display.
 * 
 * @author Manuel Gay
 * @version $Id: booleanEditor.java,v 1.1 May 11, 2008 9:22:57 PM manu Exp $
 */
public class booleanEditor extends choiceEditor {
    
    private static final class SingletonHolder {
        static final FieldEditor singleton = new booleanEditor();
    }

    /** Don't use this, use getInstance() */
    protected booleanEditor() {}

    public static FieldFormatter getInstance() {
        return SingletonHolder.singleton;
    }

    static String[] __params = { "default" };

    static String[][] __paramValues = { null };

    public String[] getAcceptedParams() {
        return __params;
    }

    public String[][] getAcceptedValue() {
        return __paramValues;
    }

    /** Formats the value to appear in an input statement. */
    public String formatValue(RecordFormatter rf, int fieldIndex, Object o, Dictionary formatParams) {

        String s = (o == null) ? null : (((Boolean)o) ? "Yes" : "No");
        return resetValueFormat(rf, fieldIndex, s, formatParams);
    }

    public Object readFrom(RecordFormatter rf, int fieldIndex, org.makumba.commons.attributes.HttpParameters par,
            String suffix) {
        Object o = par.getParameter(getInputName(rf, fieldIndex, suffix));

        if (o instanceof java.util.Vector) {
            throw new InvalidValueException(rf.expr[fieldIndex],
                    "multiple value not accepted for boolean: " + o);
        }
        return toBoolean(rf, fieldIndex, o);
    }

    @Override
    public String formatOptionTitle(RecordFormatter rf, int fieldIndex, Object options, int i) {
        return (i==0 ? "Yes" : "No");
    }

    @Override
    public String formatOptionValue(RecordFormatter rf, int fieldIndex, Object opts, int i, Object val) {
        return ((Boolean)val) ? "true" : "false";
    }

    @Override
    public String formatOptionValue(RecordFormatter rf, int fieldIndex, Object val) {
        return ((Boolean)val) ? "true" : "false";
    }

    @Override
    public int getDefaultSize(RecordFormatter rf, int fieldIndex) {
        return 1;
    }

    @Override
    public Object getOptionValue(RecordFormatter rf, int fieldIndex, Object options, int i) {
        return (i==0 ? true : false);
    }

    @Override
    public Object getOptions(RecordFormatter rf, int fieldIndex, Dictionary formatParams) {
        ChoiceSet c = new ChoiceSet();
        c.add("true", "Yes", true, false);
        c.add("false", "No", false, false);
        return c;
    }

    @Override
    public int getOptionsLength(RecordFormatter rf, int fieldIndex, Object opts) {
        return 2;
    }

    @Override
    public boolean isMultiple(RecordFormatter rf, int fieldIndex) {
        return false;
    }

    @Override
    public String getMultiple(RecordFormatter rf, int fieldIndex) {
        return "";
    }

}
