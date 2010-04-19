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
import org.makumba.commons.NameResolver.TextList;

/**
 * This class represents an MQL function, with it's name, return type and required arguments. This definition is then
 * used to check for correct use of the MQL functions, and set the return type, types of parameters.
 * 
 * @author Rudolf Mayer
 * @version $Id: MQLFunction.java,v 1.1 Dec 20, 2008 1:19:31 AM rudi Exp $
 */
public class MQLFunctionDefinition {

    protected int argumentCount = 0;

    private static MQLFunctionArgument[] makeStandardArguments(String[] args) {
        MQLFunctionArgument[] arguments = new MQLFunctionArgument[args.length];
        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = new MQLFunctionArgument(args[i]);
        }
        return arguments;
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

    public String getSQLCommand() {
        return getName();
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
    public TextList render(List<TextList> args) {
        TextList textList = new TextList();
        textList.append(getSQLCommand() + "(");
        for (int i = 0; i < args.size(); i++) {
            textList.append(args.get(i));
            if (i + 1 < args.size()) {
                textList.append(",");
            }
        }
        textList.append(")");
        return textList;
    }

}

class SQLDialectFunction extends MQLFunctionDefinition {
    protected String sqlCommand;

    public SQLDialectFunction(String name, String sqlCommand, String returnType, MQLFunctionArgument... arguments) {
        super(name, returnType, arguments);
        this.sqlCommand = sqlCommand;
    }

    @Override
    public String getSQLCommand() {
        return sqlCommand;
    }
}

class DateArithmeticFunction extends SQLDialectFunction {
    protected DateArithmeticFunction(String name, String sqlCommand) {
        super(name, sqlCommand, "date", new MQLFunctionArgument("date"), new MQLFunctionArgument("date"),
                new MQLFunctionArgument("char", true, false));
    }

    @Override
    public TextList render(List<TextList> args) {
        // FIXME: this is mysql specific; other dialects should be supported
        TextList textList = new TextList();
        textList.append(getSQLCommand() + "(");
        if (args.size() == 2) {
            textList.append(args.get(0)).append(", INTERVAL ").append(args.get(1)).append(" second");
        } else if (args.size() == 3) {
            String timeUnit = args.get(2).toString().trim();
            if (timeUnit.startsWith("'")) {
                timeUnit = timeUnit.substring(1);
            }
            if (timeUnit.endsWith("'")) {
                timeUnit = timeUnit.substring(0, timeUnit.length() - 1);
            }
            textList.append(args.get(0)).append(", INTERVAL ").append(args.get(1)).append(" ").append(timeUnit);
        } else { // doesn't happen
            throw throwUnexpectedArguments(args.size());
        }
        textList.append(")");
        return textList;
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

    public TextList render(List<TextList> args) {
        // FIXME: MySQL specific
        return super.render(args);
    }
}
