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

package org.makumba.commons;

import java.util.HashMap;
import java.util.Properties;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaError;

/**
 * This class provides utility methods to convert names from MDD types into their name in the data source. It also takes
 * into account properties passed in the database configuration. TODO document these properties
 * 
 * @author Manuel Bernhardt <manuel@makumba.org>
 * @author Cristian Bogdan
 * @version $Id$
 */
public class NameResolver {

    private Properties config;

    protected HashMap<String, HashMap<String, String>> fieldDBNames = new HashMap<String, HashMap<String, String>>();

    public NameResolver() {
        this(null);
    }

    public NameResolver(Properties configurationProperties) {
        this.config = configurationProperties;
        if (config == null) {
            config = new Properties();
        }
    }

    public NameResolver(Properties configurationProperties, DataDefinition dd) {
        this(configurationProperties);
        makeTypeCache(dd);

    }

    public String getKey() {
        return getClass().getName() + config;
    }

    public static class Resolvable {
        DataDefinition dd;

        String field;

        public String resolve(NameResolver nr) {
            if (field != null) {
                return nr.resolveFieldName(dd, field);
            } else {
                return nr.resolveTypeName(dd.getName());
            }
        }

        @Override
        public String toString() {
            return field;
        }
    }

    /**
     * Resolves the database level name for a type, based on Makumba business rules and specific configuration done by
     * the user.
     * 
     * @param typeName
     *            the name of the type to resolve
     * @return the database level name for this type
     */
    public String resolveTypeName(String typeName) {
        return getTableNameFromConfig(config, typeName);
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
    public String resolveFieldName(DataDefinition dd, String fieldName) {
        HashMap<String, String> resolvedCache = fieldDBNames.get(dd.getName());
        if (resolvedCache == null) {
            resolvedCache = makeTypeCache(dd);
        }
        return resolvedCache.get(fieldName);
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
    public String resolveFieldName(String typeName, String fieldName) {
        HashMap<String, String> resolvedCache = fieldDBNames.get(typeName);
        if (resolvedCache == null) {
            throw new MakumbaError("No cache for name resolution of type '" + typeName
                    + "' found. Please report to developers.");
        }
        return resolvedCache.get(fieldName);

    }

    /**
     * Gets the database-level name of a table with a certain abstract name. This just replaces strange signs like ., ->
     * ( ) with underscores. Old names are lowercased.
     * 
     * @param name
     *            the name of the type, e.g general.Person
     * @return the name the type should have in the source
     */
    private String getTypeNameInSource(String name) {

        boolean addUnderscore = true;

        String s = config.getProperty("addUnderscore");
        if (s != null) {
            addUnderscore = s.equals("true");
        }

        if (!addUnderscore) {
            name = "." + name.toLowerCase(); // OLDSUPPORT
        }
        // "/general/Person"->"_general_person"
        name = name.replace('.', '_').replace('(', '_').replace(')', '_').replace('>', '_').replace('-', '_');
        name = name + (addUnderscore ? "_" : "");

        // if the name is too long, we replace the end with a hash

        if (name.length() <= getMaxTableNameLength()) {
            return name;
        } else // compose "startingpartoflongnam___HASH"
        {
            String hash = Integer.toString(name.hashCode(), Character.MAX_RADIX).replace('-', '_');
            String shortname = name.substring(0, getMaxTableNameLength() - 3 - hash.length());
            return shortname + "___" + hash;
        }
    }

    /**
     * Finds the shortest possible table name, according to what is defined in the configuration with rule and table:
     * general.Person = gp general.Person->fields will create table _gp__fields_ instead of _general_Person__fields_ as
     * it did before
     * 
     * @param config
     *            the configuration {@link Properties}
     * @param dd
     *            the {@link DataDefinition} corresponding to the type
     * @return the name of the table as specified in the configuration
     */
    public String getTableNameFromConfig(Properties config, String typeName) {
        String tbname = config.getProperty(typeName);

        if (tbname == null) {
            String key = org.makumba.db.makumba.Database.findConfig(config, typeName);
            String shortname = typeName;
            if (key != null) {
                shortname = config.getProperty(key) + typeName.substring(key.length());
            }

            tbname = getTypeNameInSource(shortname);
        } else if (tbname.indexOf('.') != -1) {
            tbname = getTypeNameInSource(tbname);
        }

        return tbname;
    }

    /**
     * Gets the database-level name of a field with the given abstract name. This simply returns the same name, but it
     * can be otherwise for certain more restrictive SQL engines. old names have first letter lowercased.
     * 
     * @param field
     *            the name of the field to resolve
     * @return the name of the field as it should be in the source
     */
    private String getFieldNameInSource(String field) {
        boolean addUnderscore = true;

        String s = config.getProperty("addUnderscore");
        if (s != null) {
            addUnderscore = s.equals("true");
        }

        String name = field;

        if (!addUnderscore && !s.startsWith("TS_")) {
            // lowercase
            name = name.substring(0, 1).toLowerCase() + name.substring(1);
        }
        name = name.replace('.', '_'); // should be tirrelevant for field names,
        // OLDSUPPORT?
        name = name + (addUnderscore ? "_" : "");
        if (name.length() <= getMaxFieldNameLength()) {
            return name;
        } else // compose "startingpartoflongnam___HASH"
        {
            String hash = Integer.toString(name.hashCode(), Character.MAX_RADIX).replace('-', '_');
            String shortname = name.substring(0, getMaxFieldNameLength() - 3 - hash.length());
            return shortname + "___" + hash;
        }
    }

    /**
     * Checks if the given database fieldname-s actually exist in the database
     * 
     * @param name
     *            the name of the field
     * @param dd
     *            the {@link DataDefinition} corresponding to the type we want to check
     * @return <code>true</code> if such a field already exists, <code>false</code> otherwise
     */
    private static boolean checkDuplicateFieldName(String name, DataDefinition dd, HashMap<String, String> resolvedCache) {
        for (FieldDefinition fd : dd.getFieldDefinitions()) {
            if (fd.getType().startsWith("set")) {
                continue;
            }
            if (resolvedCache.get(fd.getName()) != null
                    && resolvedCache.get(fd.getName()).toLowerCase().equals(name.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Initializes the NameResolver for the given type
     */
    public void initializeType(DataDefinition dd) {
        makeTypeCache(dd);
    }

    /**
     * Creates a cache containing the resolved field names for the given type.<br>
     * FIXME: 2 threads may be doing this at the same time, but they'll achieve the same result so that's OK
     * 
     * @param dd
     *            the {@link DataDefinition} corresponding to the type
     * @return a {@link HashMap} containing the resolved names for each field
     */
    private HashMap<String, String> makeTypeCache(DataDefinition dd) {
        HashMap<String, String> resolvedCache;
        resolvedCache = new HashMap<String, String>();

        for (FieldDefinition fd : dd.getFieldDefinitions()) {

            if (fd.getType().startsWith("set")) {
                continue;
            }

            String resolved = config.getProperty(getTableNameFromConfig(config, dd.getName()) + "->" + fd.getName());

            if (resolved == null) {
                resolved = checkReserved(getFieldNameInSource(fd.getName()));
                while (checkDuplicateFieldName(resolved, dd, resolvedCache)) {
                    resolved = resolved + "_";
                }
            }
            resolvedCache.put(fd.getName(), resolved);
        }
        fieldDBNames.put(dd.getName(), resolvedCache);
        return resolvedCache;
    }

    private int getMaxTableNameLength() {
        return 64;
    }

    private int getMaxFieldNameLength() {
        return 64;
    }

    public static String dotToUnderscore(String name) {
        return name.replace('.', '_');
    }

    public static String arrowToDoubleUnderscore(String name) {
        return name.replaceAll("->", "__");
    }

    public static String checkReserved(String name) {
        // check if this is a java reserved keyword, not to annoy the class generator
        if (ReservedKeywords.getReservedKeywords().contains(name)) {
            return arrowToDoubleUnderscore(name + "_");
        }
        return arrowToDoubleUnderscore(name);
    }

    public String mddToSQLName(String name) {
        name = dotToUnderscore(name);
        name = arrowToDoubleUnderscore(name);
        return name + "_";
    }

}
