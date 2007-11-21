package org.makumba.commons;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import org.makumba.DataDefinition;
import org.makumba.db.makumba.sql.Database;

/**
 * This class provides utility methods to convert names from MDD types into their name in the data source. It also takes
 * into account properties passed in the database configuration.
 * 
 * TODO document these properties
 * 
 * @author Manuel Gay
 * @author Cristian Bogdan
 * @version $Id: DataSource.java,v 1.1 08.11.2007 05:42:48 Manuel Exp $
 */
public class NameResolver {

    private Properties config;

    protected HashMap<String, HashMap<String, String>> fieldDBNames = new HashMap<String, HashMap<String, String>>();

    public NameResolver() {

    }

    public NameResolver(Properties configurationProperties) {
        this.config = configurationProperties;
    }

    
    /**
     * Resolves the database level name for a type, based on Makumba business rules and specific configuration done by the user.
     * @param dd the {@link DataDefinition} corresponding to the type to resolve
     * @return the database level name for this type
     */
    public String resolveTypeName(DataDefinition dd) {
        return getTableNameFromConfig(config, dd);
    }
    
    /**
     * Resolves the database level name for a field, based on Makumba business rules and specific configuration done by the user.
     * @param dd the {@link DataDefinition} corresponding to the type of the field to resolve
     * @param fieldName the name of the field to resolve
     * @return the database level name for this field
     */
    public String resolveFieldName(DataDefinition dd, String fieldName) {
        HashMap<String, String> resolvedCache = fieldDBNames.get(dd.getName());
        if (resolvedCache == null)
            resolvedCache = makeTypeCache(dd);
        return resolvedCache.get(fieldName);
    }
    
    /**
     * Gets the database-level name of a table with a certain abstract name.
     * This just replaces strange signs like ., -> ( ) with underscores. Old names are lowercased.
     * 
     * @param name the name of the type, e.g general.Person
     * @return the name the type should have in the source
     */
    private String getTypeNameInSource(String name) {

        boolean addUnderscore = true;

        String s = config.getProperty("addUnderscore");
        if (s != null)
            addUnderscore = s.equals("true");

        if (!addUnderscore)
            name = ("." + name.toLowerCase()); // OLDSUPPORT
        // "/general/Person"->"_general_person"
        name = name.replace('.', '_').replace('(', '_').replace(')', '_').replace('>', '_').replace('-', '_');
        name = name + (addUnderscore ? "_" : "");

        // if the name is too long, we replace the end with a hash
        
        if (name.length() <= getMaxTableNameLength())
            return name;
        else // compose "startingpartoflongnam___HASH"
        {
            String hash = Integer.toString(name.hashCode(), Character.MAX_RADIX).replace('-', '_');
            String shortname = name.substring(0, getMaxTableNameLength() - 3 - hash.length());
            return (shortname + "___" + hash);
        }
    }
    
    /**
     *  Finds the shortest possible table name, according to what is defined in the configuration with rule and table:
     *  general.Person = gp
     *  
     *  general.Person->fields will create table _gp__fields_ instead of _general_Person__fields_ as it did before
     *  
     *  @param config the configuration {@link Properties}
     *  @param dd the {@link DataDefinition} corresponding to the type
     *  @return the name of the table as specified in the configuration
     */
    public String getTableNameFromConfig(Properties config, DataDefinition dd) {
        String tbname = config.getProperty(dd.getName());

        if (tbname == null) {
            String key = Database.findConfig(config, dd.getName());
            String shortname = dd.getName();
            if (key != null)
                shortname = config.getProperty(key) + dd.getName().substring(key.length());

            tbname = getTypeNameInSource(shortname);
        } else if (tbname.indexOf('.') != -1)
            tbname = getTypeNameInSource(tbname);

        return tbname;
    }

    /**
     * Gets the database-level name of a field with the given abstract name. This simply returns the same name, but it
     * can be otherwise for certain more restrictive SQL engines. old names have first letter lowercased.
     * 
     * @param field the name of the field to resolve
     * @return the name of the field as it should be in the source
     */
    private String getFieldNameInSource(String field) {
        boolean addUnderscore = true;

        String s = config.getProperty("addUnderscore");
        if (s != null)
            addUnderscore = s.equals("true");

        String name = field;

        if (!addUnderscore && !s.startsWith("TS_")) // make it start with
            // lowercase
            name = name.substring(0, 1).toLowerCase() + name.substring(1);
        name = name.replace('.', '_'); // should be tirrelevant for field names,
        // OLDSUPPORT?
        name = name + (addUnderscore ? "_" : "");
        if (name.length() <= getMaxFieldNameLength())
            return name;
        else // compose "startingpartoflongnam___HASH"
        {
            String hash = Integer.toString(name.hashCode(), Character.MAX_RADIX).replace('-', '_');
            String shortname = name.substring(0, getMaxFieldNameLength() - 3 - hash.length());
            return (shortname + "___" + hash);
        }
    }

    /**
     * Checks if the given database fieldname-s actually exist in the database
     * 
     * @param name
     *            the name of the field
     * @param dd
     *            the {@link DataDefinition} corresponding to the type we want to check
     *            
     * @return <code>true</code> if such a field already exists, <code>false</code> otherwise
     */
    private boolean checkDuplicateFieldName(String name, DataDefinition dd) {
        for (Enumeration e = dd.getFieldNames().elements(); e.hasMoreElements();) {
            String fieldName = (String) e.nextElement();
            if (dd.getFieldDefinition(fieldName).getType().startsWith("set"))
                continue;
            if (fieldDBNames.get(dd.getName()).get(fieldName) != null
                    && fieldDBNames.get(dd.getName()).get(fieldName).toLowerCase().equals(name.toLowerCase()))
                return true;
        }
        return false;
    }

    /**
     * Creates a cache containing the resolved field names for the given type.
     * 
     * @param dd the {@link DataDefinition} corresponding to the type
     * @return a {@link HashMap} containing the resolved names for each field
     */
    private HashMap<String, String> makeTypeCache(DataDefinition dd) {
        HashMap<String, String> resolvedCache;
        resolvedCache = new HashMap<String, String>();
        fieldDBNames.put(dd.getName(), resolvedCache);

        for (Enumeration e = dd.getFieldNames().elements(); e.hasMoreElements();) {

            String name = (String) e.nextElement();
            
            if (dd.getFieldDefinition(name).getType().startsWith("set"))
                continue;

            
            String resolved = config.getProperty(getTableNameFromConfig(config, dd) + "#" + name);
            if (resolved == null) {
                resolved = checkReserved(getFieldNameInSource(name));
                while (checkDuplicateFieldName(resolved, dd))
                    resolved = resolved + "_";
            }
            resolvedCache.put(name, resolved);

        }
        return resolvedCache;
    }

    private int getMaxTableNameLength() {
        return 64;
    }

    private int getMaxFieldNameLength() {
        return 64;
    }
    
    
    public String dotToUnderscore(String name) {
        return name.replaceAll("\\.", "_");
    }

    public String arrowToDot(String name) {
        return name.replaceAll("->", ".");
    }

    public String arrowToDoubleDot(String name) {
        return name.replaceAll("->", "..");
    }
    public String arrowToDoubleUnderscore(String name){
        return name.replaceAll("->", "__");        
    }
    
    public String checkReserved(String name){
        // check if this is a java reserved keyword, not to annoy the class generator
        if(ReservedKeywords.getReservedKeywords().contains(name))
            return arrowToDoubleUnderscore(name+"_");
        return arrowToDoubleUnderscore(name);
    }
    
    public String mddToSQLName(String name) {
        name = dotToUnderscore(name);
        name = arrowToDoubleUnderscore(name);
        return name + "_";
    }
    
    
    
}
