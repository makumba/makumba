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
//  $Id: OQLAnalyzer.java,v 2.1 2007/07/18 16:04:57 manuel_gay Exp $
//  $Name:  $
/////////////////////////////////////

package org.makumba.providers;

import java.util.Map;

import org.makumba.DataDefinition;

/**
 * This interface describes the result of a query analysis.
 * 
 * @author Cristian Bogdan
 * @author Manuel Gay
 * 
 * @since 0.5.5.10
 * @version $Id: OQLAnalyzer.java,v 2.1 2007/07/18 16:04:57 manuel_gay Exp $
 */
public interface QueryAnalysis {
    /**
     * Gets the original OQL query that is analyzed by this object
     * 
     * @deprecated Use {@link #getQuery()} instead
     */
    String getOQL();

    
    /**
     * Gets the original query that is analyzed by this object
     */
    String getQuery();
    
    /**
     * Gets the pre-processed query
     * @return
     */
    String getPreProcessedQuery(String query);

    /**
     * Gets the type of the fields between SELECT and FROM
     * 
     * @return A DataDefinition containing in the first field the type and name of the first QL projection, the second
     *         field the type and name of the second QL projection $2 etc.
     */
    DataDefinition getProjectionType();
    
    /**
     * Gets the type of the fields between FROM and WHERE
     * @return A DataDefinition containing in the first field the type and name of the first label, the second
     *         field the type and name of the second label $2 etc.
     */
    Map<String, DataDefinition> getLabelTypes();

    /**
     * Gets the type of a label used within the query
     * @param labelName the name of the label
     * @return The type of the label as declared in the FROM part of the query
     */
    DataDefinition getLabelType(String labelName);

    /**
     * Gets the types of the query parameters, as resulted from the query analysis.
     * 
     * @return A DataDefinition containing in the first field the type of the QL parameter $1, the second field the
     *         type of the QL parameter $2 etc
     */
    DataDefinition getParameterTypes();

    /**
     * Gets the total number of parameters in the query; like $1, $2 etc. Note that if for example. $1 appears twice it will
     * be counted twice.
     * 
     * @see #parameterAt(int)
     */
    int parameterNumber();

    /**
     * Gets the number of the parameter mentioned at the position indicated by the given index. query parameters may not
     * get mentioned in the order of their $number, for example $1 may not appear first in the query, $2 may not appear second
     * in the query, etc.
     * 
     * @see #parameterNumber()
     */
    public int parameterAt(int index);
}
