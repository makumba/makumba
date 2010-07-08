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
        mqlFunctions.add(stringToStringFunction("lower"));
        mqlFunctions.add(stringToStringFunction("upper"));
        mqlFunctions.add(stringToStringFunction("trim"));
        mqlFunctions.add(stringToStringFunction("rtrim"));
        mqlFunctions.add(stringToStringFunction("ltrim"));
        mqlFunctions.add(stringToStringFunction("reverse"));

        // to-string functions with more arguments
        mqlFunctions.add(toStringFunction("concat", makMultipleArgument("char[255]")));
        mqlFunctions.add(toStringFunction("concat_ws", new MQLFunctionArgument("char[255]"),
            makMultipleArgument("char[255]")));
        mqlFunctions.add(toStringFunction("substring", new MQLFunctionArgument("char[255]"), new MQLFunctionArgument(
                "int"), MQLFunctionRegistry.makeOptionalArgument("int")));
        String[] arguments = { "char[255]", "char[255]" };
        mqlFunctions.add(new MQLFunctionDefinition("replace", "char[255]", arguments));

        // simple string-to-int functions
        mqlFunctions.add(new MQLFunctionDefinition("ascii", "int", "char[255]"));
        mqlFunctions.add(new MQLFunctionDefinition("character_length", "int", "char[255]"));
        mqlFunctions.add(new MQLFunctionDefinition("length", "int", "char[255]"));

        // to-int functions with more arguments
        mqlFunctions.add(new MQLFunctionDefinition("locate", "int", "char[255]", "char[255]"));

        // simple int-to-string functions
        mqlFunctions.add(new MQLFunctionDefinition("format", "char[255]", "int"));
        mqlFunctions.add(new MQLFunctionDefinition("char", "char[255]", "int"));

        //
        // DATE FUNCTIONS
        //
        // simple date-to-int functions
        mqlFunctions.add(dateToIntFunction("dayOfMonth"));
        mqlFunctions.add(dateToIntFunction("dayOfWeek"));
        mqlFunctions.add(dateToIntFunction("week"));
        mqlFunctions.add(dateToIntFunction("weekday"));
        mqlFunctions.add(dateToIntFunction("dayOfYear"));
        mqlFunctions.add(dateToIntFunction("year"));
        mqlFunctions.add(dateToIntFunction("month"));
        mqlFunctions.add(dateToIntFunction("hour"));
        mqlFunctions.add(dateToIntFunction("minute"));
        mqlFunctions.add(dateToIntFunction("second"));
        mqlFunctions.add(dateToIntFunction("microsecond"));
        mqlFunctions.add(dateToIntFunction("quarter"));
        mqlFunctions.add(dateToIntFunction("to_days"));

        // simple string-to-date functions
        mqlFunctions.add(new MQLFunctionDefinition("str_to_date", "date", "char[255]", "char[255]"));

        // date-to-int functions with more arguments
        mqlFunctions.add(toIntFunction("datediff", "date", "date"));

        // to-int functions with more arguments
        mqlFunctions.add(toIntFunction("mod", "int", "int"));
        mqlFunctions.add(toIntFunction("extract", "char[255]", "date"));

        // simple date-to-string functions
        mqlFunctions.add(new MQLFunctionDefinition("monthName", "char[255]", "date"));
        mqlFunctions.add(new MQLFunctionDefinition("dayName", "char[255]", "date"));

        // simple date-to-date functions
        mqlFunctions.add(new MQLFunctionDefinition("last_day", "date", "date"));

        // to-date functions with no arguments
        mqlFunctions.add(new NowFunction());
        // FIXME: the functions below are MySQL specific, and are deprecated; should be removed at some later time
        mqlFunctions.add(toDateFunction("current_date"));
        mqlFunctions.add(toDateFunction("current_time"));
        mqlFunctions.add(toDateFunction("current_timestamp"));

        // to-date functions with more arguments
        mqlFunctions.add(new DateAddFunction());
        mqlFunctions.add(new DateSubFunction());
        mqlFunctions.add(new MQLFunctionDefinition("makedate", "date", new String[] { "date", "date" }));
        mqlFunctions.add(new MQLFunctionDefinition("maketime", "date", new String[] { "date", "date", "date" }));

        // int-to-date functions with more arguments
        mqlFunctions.add(new MQLFunctionDefinition("from_days", "date", "int"));

        // FIXME: those functions should also work for (int, int+)->int, (real, real+)->real, and other types
        mqlFunctions.add(new MQLFunctionDefinition("least", "date", new MQLFunctionArgument("date"),
                new MQLFunctionArgument("date", false, true)));
        mqlFunctions.add(new MQLFunctionDefinition("greatest", "date", new MQLFunctionArgument("date"),
                new MQLFunctionArgument("date", false, true)));

        //
        // REAL FUNCTIONS
        //
        // to-real functions with no arguments
        mqlFunctions.add(new MQLFunctionDefinition("rand", "real", new String[] {}));
        mqlFunctions.add(new MQLFunctionDefinition("rand", "real", "int"));
    }

    private static MQLFunctionDefinition dateToIntFunction(String name) {
        return new MQLFunctionDefinition(name, "int", "date");
    }

    private static MQLFunctionDefinition stringToStringFunction(String name) {
        return new MQLFunctionDefinition(name, "char[255]", "char[255]");
    }

    private static MQLFunctionDefinition toDateFunction(String name) {
        return new MQLFunctionDefinition(name, "date", new String[] {});
    }

    private static MQLFunctionDefinition toIntFunction(String name, String... arguments) {
        return new MQLFunctionDefinition(name, "int", arguments);
    }

    private static MQLFunctionDefinition toStringFunction(String name, MQLFunctionArgument... arguments) {
        return new MQLFunctionDefinition(name, "char[255]", arguments);
    }

    private static MQLFunctionArgument makMultipleArgument(String type) {
        return new MQLFunctionArgument(type, false, true);
    }

    private static MQLFunctionArgument makeOptionalArgument(String type) {
        return new MQLFunctionArgument(type, true, false);
    }

}
