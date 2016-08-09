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
//  $Id: AggregateAST.java 2046 2007-11-13 16:45:44Z manuel_gay $
//  $Name$
/////////////////////////////////////

package org.makumba.providers.query.oql;

import java.util.ArrayList;

import org.apache.commons.collections.CollectionUtils;
import org.makumba.commons.NameResolver;
import org.makumba.commons.StringUtils;

import antlr.collections.AST;

/**
 * Analyser for OQL Functions
 * 
 * @author Rudolf Mayer
 * @version $Id: FunctionAST.java,v 1.1 May 14, 2008 1:42:55 AM rudi Exp $
 */
public class FunctionAST extends OQLAST {
    /** Simple string-to-string functions with one argument. */
    public static String[] simpleStringFunctions = { "lower(", "upper(", "trim(", "rtrim(", "ltrim(" };

    /** int-to-string functions. */
    public static String[] intToStringFunctions = { "char(" };

    /** string-to-int functions. */
    public static String[] stringToIntFunctions = { "ascii(", "character_length(" };

    /** date-to-int functions. */
    public static String[] dateToIntFunctions = { "dayOfMonth(", "dayOfWeek(", "dayOfYear(", "month(", "hour(",
            "minute(", "second(" };

    /** date-to-String functions. */
    public static String[] dateToStringFunctions = { "monthName(", "dayName(" };

    public static String[] nonParametricDateFunctions = { "current_date", "current_time", "current_timestamp" };

    public static final String[] allNonParametricFunctions;
    
    public static final String[] allSingleParameterFunctions;

    /** All known functions. */
    public static final String[] allFunctions;

    static {
        ArrayList<String> allSingleParameter = new ArrayList<String>();
        CollectionUtils.addAll(allSingleParameter, simpleStringFunctions);
        CollectionUtils.addAll(allSingleParameter, intToStringFunctions);
        CollectionUtils.addAll(allSingleParameter, stringToIntFunctions);
        allSingleParameterFunctions = (String[]) allSingleParameter.toArray(new String[allSingleParameter.size()]);

        ArrayList<String> allNonParemtric = new ArrayList<String>();
        CollectionUtils.addAll(allNonParemtric, nonParametricDateFunctions);
        allNonParametricFunctions = (String[]) allNonParemtric.toArray(new String[allNonParemtric.size()]);
        
        ArrayList<String> all = new ArrayList<String>();
        CollectionUtils.addAll(all, allNonParametricFunctions);
        CollectionUtils.addAll(all, allSingleParameterFunctions);
        allFunctions = (String[]) all.toArray(new String[all.size()]);
    }

    private static final long serialVersionUID = 1L;

    OQLAST expr;

    public FunctionAST() {
    }

    public void setExpr(OQLAST e) {
        expr = e;
    }

    @Override
    public String writeInSQLQuery(NameResolver nr) {
        StringBuffer sb = new StringBuffer();
        sb.append(getText());
        if (expr != null) { // non-parametric functions don't have an expression
            sb.append(expr.writeInSQLQuery(nr));
            for (AST a = expr.getNextSibling(); !a.getText().equals(")"); a = a.getNextSibling())
                sb.append(((OQLAST) a).writeInSQLQuery(nr));
        }

        sb.append(")");
        return sb.toString();
    }

    @Override
    public Object getMakumbaType() throws antlr.RecognitionException {
        if (expr != null) { // functions with parameters
            Object o = expr.getMakumbaType();
            String os = o.toString();
            if (StringUtils.startsWith(getText(), simpleStringFunctions)) { // string functions
                if (os.startsWith("char") || os.startsWith("text")) {
                    return o;
                } else {
                    throw new antlr.SemanticException("cannot " + printOptions(simpleStringFunctions) + " a '" + os
                            + "' type");
                }
            } else if (StringUtils.startsWith(getText(), stringToIntFunctions)) { // string to int
                if (os.startsWith("char") || os.startsWith("text")) {
                    return "int";
                } else {
                    throw new antlr.SemanticException("cannot " + printOptions(stringToIntFunctions) + " a '" + os
                            + "' type");
                }
            } else if (StringUtils.startsWith(getText(), intToStringFunctions)) { // int to string
                if (os.startsWith("int")) {
                    return "char";
                } else {
                    throw new antlr.SemanticException("cannot " + printOptions(intToStringFunctions) + " a '" + os
                            + "' type");
                }
            } else if (StringUtils.startsWith(getText(), dateToIntFunctions)) { // date to int
                if (os.startsWith("date")) {
                    return "int";
                } else {
                    throw new antlr.SemanticException("cannot " + printOptions(dateToIntFunctions) + " a '" + os
                            + "' type");
                }
            } else if (StringUtils.startsWith(getText(), dateToStringFunctions)) { // date to string
                if (os.startsWith("date")) {
                    return "text";
                } else {
                    throw new antlr.SemanticException("cannot " + printOptions(dateToStringFunctions) + " a '" + os
                            + "' type");
                }
            }
        } else { // functions without paraemters
            if (StringUtils.startsWith(getText(), nonParametricDateFunctions)) { // non-parametric date
                return "date";
            } else {
                throw new antlr.SemanticException("non-parametric functions are: "
                        + StringUtils.toString(allNonParametricFunctions, false));
            }
        }

        throw new antlr.SemanticException("function expressions can be " + StringUtils.toString(allFunctions, false));
    }

    public static String printOptions(String[] functions) {
        String s = "";
        for (int i = 0; i < functions.length; i++) {
            s += functions[i] + ")";
            if (i < functions.length - 1) {
                s += " or ";
            }
        }
        return s;
    }

}
