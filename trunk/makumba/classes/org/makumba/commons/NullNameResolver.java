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
//  $Id: NameResolver.java 5282 2010-06-02 14:43:04Z manuel_gay $
//  $Name$
/////////////////////////////////////

package org.makumba.commons;

import org.makumba.DataDefinition;

/**
 * This class provides utility methods to convert names from MDD types into their name in the data source. It also takes
 * into account properties passed in the database configuration. TODO document these properties
 * 
 * @author Manuel Gay
 * @author Cristian Bogdan
 * @version $Id: NameResolver.java 5282 2010-06-02 14:43:04Z manuel_gay $
 */
public class NullNameResolver extends NameResolver {
    /**
     * Resolves the database level name for a type, based on Makumba business rules and specific configuration done by
     * the user.
     * 
     * @param typeName
     *            the name of the type to resolve
     * @return the database level name for this type
     */
    @Override
    public String resolveTypeName(String typeName) {
        return arrowToDoubleUnderscore(typeName);
    }

    /**
     * Resolves the database level name for a field, based on Makumba business rules and specific configuration done by
     * the user.
     * 
     * @param dd
     *            the {@link DataDefinition} corresponding to the type of the field to resolve
     * @param fieldName
     *            the name of the field to resolve
     * @return the database level name for this field
     */
    @Override
    public String resolveFieldName(DataDefinition dd, String fieldName) {
        return fieldName;
    }

    /**
     * Resolves the database level for a field, given a type name. Use this method only if you previously initialized
     * all types you need resolution for via {@link #initializeType(DataDefinition)}
     * 
     * @param typeName
     *            the name of the type
     * @param fieldName
     *            the name of the field to resolve
     * @return the database level name for this field
     */
    @Override
    public String resolveFieldName(String typeName, String fieldName) {
        return fieldName;
    }

    /**
     * Initializes the NameResolver for the given type
     */
    @Override
    public void initializeType(DataDefinition dd) {
    }

}
