package org.makumba.providers;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.makumba.FieldDefinition;
import org.makumba.FieldDefinition.FieldErrorMessageType;
import org.makumba.annotations.MessageType;
import org.makumba.providers.bytecode.EntityClassGenerator;
import org.makumba.providers.datadefinition.mdd.FieldDefinitionImpl;

/**
 * A Data Transfer Object that ensures decoupling between the {@link EntityClassGenerator} and the concrete
 * implementation of a given DataDefinition parser / provider. Later, this may become an interface which FieldDefinition
 * extends. For now we keep things simple and make a class that can be populated by any kind of MDD parser. It also
 * gives us a pretty good idea of the minimum set of field properties necessary for database persistence.
 * 
 * @author Manuel Bernhardt <manuel@makumba.org>
 * @version $Id$
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

    // messages
    protected HashMap<MessageType, String> messages = new HashMap<MessageType, String>();

    public FieldDataDTO(FieldDefinitionImpl fd) {
        this.name = fd.getName();
        this.type = fd.getIntegerType();
        this.description = fd.getDescription();
        this.relatedTypeName = fd.getIntegerType() == FieldDefinition._ptr
                || fd.getIntegerType() == FieldDefinition._ptrOne || fd.getIntegerType() == FieldDefinition._ptrRel
                || fd.isSetType() ? fd.getPointedType().getName() : null;
        this.mappingTableName = fd.isSetType() ? fd.getSubtable().getName() : null;
        this.setMappingColumnName = fd.getIntegerType() == FieldDefinition._set ? fd.getSubtable().getSetMemberFieldName()
                : null;
        this.characterLenght = fd.getIntegerType() == FieldDefinition._char ? fd.getWidth() : -1;
        this.fixed = fd.isFixed();
        this.notNull = fd.isNotNull();
        this.notEmpty = fd.isNotEmpty();
        this.unique = fd.isUnique();
        this.intEnumValues = fd.getIntEnumValues();
        this.intEnumValuesDeprecated = fd.getIntEnumValuesDeprecated();
        this.messages.put(MessageType.UNIQUE_ERROR, fd.getErrorMessage(FieldErrorMessageType.NOT_UNIQUE));
        this.messages.put(MessageType.NULLABLE_ERROR, fd.getErrorMessage(FieldErrorMessageType.NOT_NULL));
        this.messages.put(MessageType.INT_ERROR, fd.getErrorMessage(FieldErrorMessageType.NOT_INT));
        this.messages.put(MessageType.BOOLEAN_ERROR, fd.getErrorMessage(FieldErrorMessageType.NOT_BOOLEAN));
        this.messages.put(MessageType.NaN_ERROR, fd.getErrorMessage(FieldErrorMessageType.NOT_A_NUMBER));
        this.messages.put(MessageType.REAL_ERROR, fd.getErrorMessage(FieldErrorMessageType.NOT_REAL));
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

    public HashMap<MessageType, String> getMessages() {
        return messages;
    }

}
