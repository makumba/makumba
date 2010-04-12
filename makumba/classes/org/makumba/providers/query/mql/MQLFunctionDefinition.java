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
//  $Id: FieldCursor.java 1707 2007-09-28 15:35:48Z manuel_gay $
//  $Name$
/////////////////////////////////////

package org.makumba.providers.query.mql;

import java.util.List;

import org.makumba.OQLParseError;
import org.makumba.commons.StringUtils;

/**
 * This class represents an MQL function, with it's name, return type and required arguments. This definition is then
 * used to check for correct use of the MQL functions, and set the return type, types of parameters.
 * 
 * @author Rudolf Mayer
 * @version $Id: MQLFunction.java,v 1.1 Dec 20, 2008 1:19:31 AM rudi Exp $
 */
public class MQLFunctionDefinition {

    protected int argumentCount = 0;

    public static MQLFunctionDefinition dateToDateFunction(String name) {
        return new MQLFunctionDefinition(name, "date", "date");
    }

    public static MQLFunctionDefinition dateToIntFunction(String name) {
        return new MQLFunctionDefinition(name, "int", "date");
    }

    public static MQLFunctionDefinition dateToStringFunction(String name) {
        return new MQLFunctionDefinition(name, "char[255]", "date");
    }

    public static MQLFunctionDefinition intToDateFunction(String name) {
        return new MQLFunctionDefinition(name, "date", "int");
    }

    public static MQLFunctionDefinition intToStringFunction(String name) {
        return new MQLFunctionDefinition(name, "char[255]", "int");
    }

    private static MQLFunctionArgument[] makeStandardArguments(String[] args) {
        MQLFunctionArgument[] arguments = new MQLFunctionArgument[args.length];
        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = new MQLFunctionArgument(args[i]);
        }
        return arguments;
    }

    public static MQLFunctionDefinition stringToIntFunction(String name) {
        return new MQLFunctionDefinition(name, "int", "char[255]");
    }

    public static MQLFunctionDefinition stringToStringFunction(String name) {
        return new MQLFunctionDefinition(name, "char[255]", "char[255]");
    }

    public static MQLFunctionDefinition toDateFunction(String name) {
        return new MQLFunctionDefinition(name, "date", new String[] {});
    }

    public static MQLFunctionDefinition toDateFunction(String name, MQLFunctionArgument... arguments) {
        return new MQLFunctionDefinition(name, "date", arguments);
    }

    public static MQLFunctionDefinition toDateFunction(String name, String... arguments) {
        return new MQLFunctionDefinition(name, "date", arguments);
    }

    public static MQLFunctionDefinition toIntFunction(String name, MQLFunctionArgument... arguments) {
        return new MQLFunctionDefinition(name, "int", arguments);
    }

    public static MQLFunctionDefinition toIntFunction(String name, String... arguments) {
        return new MQLFunctionDefinition(name, "int", arguments);
    }

    public static MQLFunctionDefinition toStringFunction(String name, MQLFunctionArgument... arguments) {
        return new MQLFunctionDefinition(name, "char[255]", arguments);
    }

    public static MQLFunctionDefinition toStringFunction(String name, String... arguments) {
        return new MQLFunctionDefinition(name, "char[255]", arguments);
    }

    public static MQLFunctionDefinition toRealFunction(String name) {
        return new MQLFunctionDefinition(name, "real", new String[] {});
    }

    public static MQLFunctionDefinition intToRealFunction(String name) {
        return new MQLFunctionDefinition(name, "real", "int");
    }

    private MQLFunctionArgument[] arguments;

    private String name;

    private String returnType;

    public MQLFunctionDefinition(String name, String returnType, MQLFunctionArgument... arguments) {
        this.name = name;
        this.returnType = returnType;
        this.arguments = arguments;
    }

    public MQLFunctionDefinition(String name, String returnType, String... arguments) {
        this(name, returnType, makeStandardArguments(arguments));
    }

    public MQLFunctionArgument[] getArguments() {
        return arguments;
    }

    public String getName() {
        return name;
    }

    public String getReturnType() {
        return returnType;
    }

    @Override
    public String toString() {
        StringBuilder params = new StringBuilder(name).append("(");
        for (int i = 0; arguments != null && i < arguments.length; i++) {
            params.append(arguments[i].getTypeNice());
            if (i + 1 < arguments.length) {
                if (arguments[i + 1].isOptional()) {
                    params.append(" [");
                }
                params.append(", ");
            }
            if (arguments[i].isMultiple()) {
                params.append("+");
            }
            if (arguments[i].isOptional()) {
                params.append("]");
            }
        }
        return params.append(") => ").append(getReturnType()).toString();
    }

    public OQLParseError throwUnexpectedArguments(int argumentCount) {
        return new OQLParseError("Unexpected argument count (" + argumentCount + ") while translating function " + this);
    }

    /**
     * This default implementation just renders the function by concatenating the function name and all arguments; for
     * functions that need to modify the name, the argument order, number of arguments, etc., this method provides an
     * entry point to rewrite it (possibly in a specific SQL dialect).
     */
    public String render(List<String> args) {
        StringBuilder buf = new StringBuilder();
        buf.append(getName() + "(");
        buf.append(StringUtils.concatAsString(args, ", "));
        buf.append(')');
        return buf.toString();
    }

}

class DateArithmeticFunction extends MQLFunctionDefinition {
    protected String sqlName;

    protected DateArithmeticFunction(String name, String sqlName) {
        super(name, "date", new MQLFunctionArgument("date"), new MQLFunctionArgument("date"), new MQLFunctionArgument(
                "char", true, false));
        this.sqlName = sqlName;
    }

    @Override
    public String render(List<String> args) {
        // FIXME: this is mysql specific; other dialects should be supported
        StringBuilder buf = new StringBuilder();
        buf.append(sqlName + "(");
        if (args.size() == 2) {
            buf.append(args.get(0)).append(", INTERVAL ").append(args.get(1)).append(" second");
        } else if (args.size() == 3) {
            String timeUnit = args.get(2).trim();
            if (timeUnit.startsWith("'")) {
                timeUnit = timeUnit.substring(1);
            }
            if (timeUnit.endsWith("'")) {
                timeUnit = timeUnit.substring(0, timeUnit.length() - 1);
            }
            buf.append(args.get(0)).append(", INTERVAL ").append(args.get(1)).append(" ").append(timeUnit);
        } else { // doesn't happen
            throw throwUnexpectedArguments(args.size());
        }
        buf.append(')');
        return buf.toString();
    }
}

class DateAddFunction extends DateArithmeticFunction {
    public DateAddFunction() {
        super("dateAdd", "date_add");
    }
}

class DateSubFunction extends DateArithmeticFunction {
    public DateSubFunction() {
        super("dateSub", "date_sub");
    }
}

class NowFunction extends MQLFunctionDefinition {
    public NowFunction() {
        super("now", "date", new String[] {});
    }

    public String render(java.util.List<String> args) {
        // FIXME: MySQL specific
        return super.render(args);
    }
}
