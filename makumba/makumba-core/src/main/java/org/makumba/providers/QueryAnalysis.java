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
//  $Name:  $
/////////////////////////////////////

package org.makumba.providers;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.makumba.DataDefinition;

import antlr.collections.AST;

/**
 * This interface describes the result of a query analysis.
 * 
 * @author Cristian Bogdan
 * @author Manuel Bernhardt <manuel@makumba.org>
 * @since 0.5.5.10
 * @version $Id$
 */
public interface QueryAnalysis {
    /**
     * Gets the original query that is analyzed by this object
     */
    String getQuery();

    /**
     * Gets the type of the fields between SELECT and FROM
     * 
     * @return A DataDefinition containing in the first field the type and name of the first QL projection, the second
     *         field the type and name of the second QL projection $2 etc.
     */
    DataDefinition getProjectionType();

    /**
     * Gets the type of the fields between FROM and WHERE
     * 
     * @return A DataDefinition containing in the first field the type and name of the first label, the second field the
     *         type and name of the second label $2 etc.
     */
    Map<String, DataDefinition> getLabelTypes();

    /**
     * Gets the type of a label used within the query FIXME: remove, inline everywhere as getLabelTypes().get(labelName)
     * for that to work, OQL and MQL need to put their aliases also in the Map returned by getLabelTypes() HQL does not
     * support aliases in the first place
     * 
     * @param labelName
     *            the name of the label
     * @return The type of the label as declared in the FROM part of the query
     */
    DataDefinition getLabelType(String labelName);

    public QueryParameters getQueryParameters();

    /**
     * Get the warnings resulted from query analysis
     * 
     * @return a collection of warnings, as strings
     */
    public Collection<String> getWarnings();

    /**
     * return the pass1 (syntax) AST tree
     * 
     * @return
     */
    AST getPass1Tree();

    /**
     * @return a map with all paths label[.field[.field]...] in the query
     */
    List<String> getPaths();
}
