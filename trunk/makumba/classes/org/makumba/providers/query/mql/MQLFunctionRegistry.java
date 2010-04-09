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

package org.makumba.providers.query.mql;

import java.util.ArrayList;

/**
 * This class provides all known MQL / SQL functions.
 * 
 * @author Rudolf Mayer
 * @version $Id$
 */
public class MQLFunctionRegistry {

    private static ArrayList<MQLFunctionDefinition> mqlFunctions = new ArrayList<MQLFunctionDefinition>();

    static {
        initMQLFunctions();
    }

    public static MQLFunctionDefinition findMQLFunction(String functionName) {
        for (MQLFunctionDefinition function : mqlFunctions) {
            if (function.getName().equals(functionName)) {
                return function;
            }
        }
        return null;
    }

    static void initMQLFunctions() {
        //
        // String FUNCTIONS
        //
        // simple string-to-string functions
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.stringToStringFunction("lower"));
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.stringToStringFunction("upper"));
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.stringToStringFunction("trim"));
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.stringToStringFunction("rtrim"));
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.stringToStringFunction("ltrim"));
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.stringToStringFunction("reverse"));

        // to-string functions with more arguments
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.toStringFunction("concat",
            MQLFunctionArgument.multipleArgument("char[255]")));
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.toStringFunction("concat_ws",
            new MQLFunctionArgument("char[255]"), MQLFunctionArgument.multipleArgument("char[255]")));
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.toStringFunction("substring",
            new MQLFunctionArgument("char[255]"), new MQLFunctionArgument("int"),
            MQLFunctionArgument.optionalArgument("int")));
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.toStringFunction("replace", "char[255]", "char[255]"));

        // simple string-to-int functions
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.stringToIntFunction("ascii"));
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.stringToIntFunction("character_length"));

        // simple int-to-string functions
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.intToStringFunction("format"));
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.intToStringFunction("char"));

        //
        // DATE FUNCTIONS
        //
        // simple date-to-int functions
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.dateToIntFunction("dayOfMonth"));
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.dateToIntFunction("dayOfWeek"));
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.dateToIntFunction("week"));
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.dateToIntFunction("weekday"));
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.dateToIntFunction("dayOfYear"));
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.dateToIntFunction("year"));
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.dateToIntFunction("month"));
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.dateToIntFunction("hour"));
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.dateToIntFunction("minute"));
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.dateToIntFunction("second"));
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.dateToIntFunction("microsecond"));
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.dateToIntFunction("quarter"));
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.dateToIntFunction("to_days"));

        // date-to-int functions with more arguments
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.toIntFunction("datediff", "date", "date"));

        // to-int functions with more arguments
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.toIntFunction("mod", "int", "int"));
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.toIntFunction("extract", "char[255]", "date"));

        // simple date-to-string functions
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.dateToStringFunction("monthName"));
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.dateToStringFunction("dayName"));

        // simple date-to-date functions
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.dateToDateFunction("last_day"));

        // to-date functions with no arguments
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.toDateFunction("current_date"));
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.toDateFunction("current_time"));
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.toDateFunction("current_timestamp"));
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.toDateFunction("now"));

        // to-date functions with more arguments
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.toDateFunction("date_add", "char[255]", "date"));
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.toDateFunction("date_sub", "char[255]", "date"));
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.toDateFunction("makedate", "date", "date"));
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.toDateFunction("maketime", "date", "date", "date"));

        // int-to-date functions with more arguments
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.intToDateFunction("from_days"));

        // to-real functions with no arguments
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.toRealFunction("rand"));
        MQLFunctionRegistry.mqlFunctions.add(MQLFunctionDefinition.intToRealFunction("rand"));
    }
}
