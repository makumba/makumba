package org.makumba.providers;

import java.util.LinkedHashMap;

import org.makumba.providers.bytecode.EntityClassGenerator;

/**
 * A Data Transfer Object that ensures decoupling between the {@link EntityClassGenerator} and the concrete
 * implementation of a given DataDefinition parser / provider. Later, this may become an interface which FieldDefinition
 * extends. For now we keep things simple and make a class that can be populated by any kind of MDD parser. It also
 * gives us a pretty good idea of the minimum set of field properties necessary for database persistence.
 * 
 * @author Manuel Gay
 * @version $Id: FieldData.java,v 1.1 May 30, 2010 6:50:13 PM manu Exp $
 */
public class FieldDataDTO {

    private final String name;

    private final int type;

    private final String description;

    private final String relatedTypeName;

    private final String setMappingColumnName;

    private final String mappingTableName;

    private final int characterLenght;

    // modifiers
    protected boolean fixed;

    protected boolean notNull;

    protected boolean notEmpty;

    protected boolean unique;

    // enum
    protected LinkedHashMap<Integer, String> intEnumValues = new LinkedHashMap<Integer, String>();

    protected LinkedHashMap<Integer, String> intEnumValuesDeprecated = new LinkedHashMap<Integer, String>();

    public FieldDataDTO(String name, int type, String description, String relatedTypeName, String mappingTable,
            String setMappingColumnName, int characterLength, boolean fixed, boolean notNull, boolean notEmpty,
            boolean unique, LinkedHashMap<Integer, String> intEnumValues,
            LinkedHashMap<Integer, String> intEnumValuesDeprecated) {
        super();
        this.name = name;
        this.type = type;
        this.description = description;
        this.relatedTypeName = relatedTypeName;
        this.mappingTableName = mappingTable;
        this.characterLenght = characterLength;
        this.setMappingColumnName = setMappingColumnName;
        this.fixed = fixed;
        this.notNull = notNull;
        this.notEmpty = notEmpty;
        this.unique = unique;
        this.intEnumValues = intEnumValues;
        this.intEnumValuesDeprecated = intEnumValuesDeprecated;
    }

    public boolean isFixed() {
        return fixed;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public boolean isNotEmpty() {
        return notEmpty;
    }

    public boolean isUnique() {
        return unique;
    }

    /**
     * Name of the field
     */
    public String getName() {
        return name;
    }

    /**
     * Type of the field as per FieldDefinition
     */
    public int getType() {
        return type;
    }

    /**
     * Name of the related type for relational fields (one-to-one, one-to-many, many-to-one, many-to-many)
     */
    public String getRelatedTypeName() {
        return relatedTypeName;
    }

    /**
     * Name of the mapping table for many-to-many relations (makumba external set).<br>
     * TODO: we should be able generate this as it follows simple naming rules, i.e. it could become a method of the
     * NameResolver.
     */
    public String getMappingTableName() {
        return mappingTableName;
    }

    /**
     * The name of the column which maps to the external set in the association table, i.e. Type2_PK_M in<br>
     * || Type1_PK || Type1_PK_M | Type2_PK_M || Type2_PK ||
     */
    public String getSetMappingColumnName() {
        return setMappingColumnName;
    }

    /**
     * Character length of a text/char field
     */
    public int getCharacterLenght() {
        return characterLenght;
    }

    public LinkedHashMap<Integer, String> getIntEnumValues() {
        return intEnumValues;
    }

    public LinkedHashMap<Integer, String> getIntEnumValuesDeprecated() {
        return intEnumValuesDeprecated;
    }

    public String getDescription() {
        return this.description;
    }

}
