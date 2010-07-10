package org.makumba.providers.bytecode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaError;
import org.makumba.annotations.MessageType;
import org.makumba.commons.NameResolver;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.FieldDataDTO;

/**
 * Generator for entity classes using the JPA annotations.<br>
 * 
 * @author Manuel Gay
 * @version $Id: EntityClassGenerator.java,v 1.1 May 30, 2010 6:43:20 PM manu Exp $
 */
public class EntityClassGenerator {

    private static final String JOIN_TABLE = "javax.persistence.JoinTable";

    private static final String JOIN_COLUMN = "javax.persistence.JoinColumn";

    private static final String ID = "javax.persistence.Id";

    private static final String ONE_TO_MANY = "javax.persistence.OneToMany";

    private static final String MANY_TO_ONE = "javax.persistence.ManyToOne";

    private static final String MANY_TO_MANY = "javax.persistence.ManyToMany";

    private static final String COLUMN = "javax.persistence.Column";

    private static final String BASIC = "javax.persistence.Basic";

    private static final String LOB = "javax.persistence.Lob";

    private static final String MAKUMBA_ENUM = "org.makumba.annotations.MakumbaEnum";

    private static final String MAKUMBA_ENUM_ELEMENT = "org.makumba.annotations.E";

    private static final String DESCRIPTION = "org.makumba.annotations.Description";

    private static final String MESSAGE = "org.makumba.annotations.Message";

    private static final String MESSAGES = "org.makumba.annotations.Messages";

    private final String generatedClassesPath;

    private final AbstractClassWriter classWriter;

    private final List<String> entitiesDone = new ArrayList<String>();

    private final LinkedHashMap<String, Vector<FieldDataDTO>> entitiesToDo = new LinkedHashMap<String, Vector<FieldDataDTO>>();

    private final LinkedHashMap<String, FieldDataDTO> appendToClass = new LinkedHashMap<String, FieldDataDTO>();

    private final DataDefinitionProvider ddp = DataDefinitionProvider.getInstance();

    private final NameResolver nr;

    public EntityClassGenerator(Map<String, Vector<FieldDataDTO>> entities, String generationPath,
            AbstractClassWriter classWriter, NameResolver nr) {

        this.generatedClassesPath = generationPath;
        this.classWriter = classWriter;
        this.nr = nr;
        Vector<String> treatedEntities = new Vector<String>();

        // generate all types
        for (String type : entities.keySet()) {
            generateClass(type, entities.get(type));
            treatedEntities.add(className(type));
        }

        // generate all related types and sub-types (setComplex, ptrOne)
        while (!entitiesToDo.isEmpty()) {
            String firstEntityName = entitiesToDo.keySet().iterator().next();
            Vector<FieldDataDTO> firstEntity = entitiesToDo.remove(firstEntityName);
            String name = className(firstEntityName);
            if (!treatedEntities.contains(name)) {
                treatedEntities.add(name);
            }
            generateClass(firstEntityName, firstEntity);
        }

        for (String key : appendToClass.keySet()) {
            String entity = key.substring(0, key.indexOf("####"));
            FieldDataDTO f = appendToClass.get(key);
            String primaryKeyPropertyName = key.substring(key.lastIndexOf("####") + 4);
            ddp.initializeNameResolver(nr, entity);
            appendToClass(entity, f, primaryKeyPropertyName);
        }
    }

    /**
     * Generates a bytecode .class file for the given type
     **/
    public void generateClass(String entityName, Vector<FieldDataDTO> fields) {
        if (!entitiesDone.contains(entityName)) {
            entitiesDone.add(entityName);

            // checks if the class has to be generated
            if (classWriter.getLastGenerationTime(entityName, generatedClassesPath) > -1) {

                /*
                 * FIXME? With the current data structure we don't know the last modification date of the MDD
                 * 
                if (dd.lastModified() < checkFile.lastModified()) {
                    return;
                }
                */
            }

            Clazz clazz = classWriter.createClass(className(entityName));

            // write the @Entity annotation
            Vector<AbstractAnnotation> classAnnotations = new Vector<AbstractAnnotation>();
            AbstractAnnotation aa = classWriter.createAnnotation("javax.persistence.Entity").addAttribute("name",
                nr.resolveTypeName(entityName));
            classAnnotations.add(aa);
            classWriter.addClassAnnotations(clazz, classAnnotations);

            boolean skipToNext = false;
            String fieldName = null;
            String fieldType = null;
            String primaryKeyPropertyName = null; // keep this for other field mappings that may need it

            for (int i = 0; i < fields.size(); i++) {
                FieldDataDTO field = fields.get(i);

                // first switch
                // - generate the base fields
                // - register sub-tables and additional types that need mapping
                switch (field.getType()) {
                    case FieldDefinition._intEnum:
                    case FieldDefinition._int:
                        fieldType = "Integer";
                        break;
                    case FieldDefinition._real:
                        fieldType = "Double";
                        break;
                    case FieldDefinition._charEnum:
                    case FieldDefinition._char:
                        fieldType = "String";
                        break;
                    case FieldDefinition._dateModify:
                    case FieldDefinition._dateCreate:
                    case FieldDefinition._date:
                        fieldType = "java.util.Date";
                        break;
                    case FieldDefinition._ptr:
                    case FieldDefinition._ptrOne:
                        entitiesToDo.put(field.getRelatedTypeName(), ddp.getFieldDataDTOs(field.getRelatedTypeName()));
                        postponeFieldGeneration(entityName, field, primaryKeyPropertyName);
                        skipToNext = true;
                        break;
                    case FieldDefinition._ptrRel:
                        fieldType = field.getRelatedTypeName();
                        break;
                    case FieldDefinition._ptrIndex:
                        primaryKeyPropertyName = field.getName();
                        fieldName = "primaryKey";
                        fieldType = "Long";
                        break;
                    case FieldDefinition._text:
                        fieldType = "java.lang.String";
                        break;
                    case FieldDefinition._binary:
                        fieldType = "byte[]";
                        break;
                    case FieldDefinition._boolean:
                        fieldType = "java.lang.Boolean";
                        break;
                    case FieldDefinition._set:
                        fieldType = "java.util.Collection";
                        entitiesToDo.put(field.getRelatedTypeName(), ddp.getFieldDataDTOs(field.getRelatedTypeName()));
                        break;
                    case FieldDefinition._setComplex:
                    case FieldDefinition._setCharEnum:
                    case FieldDefinition._setIntEnum:
                        entitiesToDo.put(field.getRelatedTypeName(), ddp.getFieldDataDTOs(field.getRelatedTypeName()));
                        postponeFieldGeneration(entityName, field, primaryKeyPropertyName);
                        skipToNext = true;
                        break;
                    default:
                        try {
                            throw new Exception("Unmapped type: " + field.getName() + "-" + field.getType());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                }

                if (skipToNext) {
                    skipToNext = false;
                    continue; // skip loop
                }

                // convert the type and field names so that they can be used as Java identifiers
                fieldName = NameResolver.arrowToDoubleUnderscore(field.getName());
                fieldType = className(fieldType);

                classWriter.addField(clazz, fieldName, fieldType);

                // second switch - add the mapping meta-data
                Vector<AbstractAnnotation> a = generateAnnotations(entityName, fieldName, primaryKeyPropertyName, field);

                // add all annotations to the getter method
                classWriter.addMethodAnnotations(clazz, "get" + StringUtils.capitalize(fieldName), a);
            }

            classWriter.writeClass(clazz, generatedClassesPath);
        }
    }

    private Vector<AbstractAnnotation> generateAnnotations(String entityName, String name,
            String primaryKeyPropertyName, FieldDataDTO field) {
        Vector<AbstractAnnotation> a = new Vector<AbstractAnnotation>();
        AbstractAnnotation column = null;
        switch (field.getType()) {

            case FieldDefinition._int:
            case FieldDefinition._real:
            case FieldDefinition._charEnum:
            case FieldDefinition._dateModify:
            case FieldDefinition._dateCreate:
            case FieldDefinition._date:
            case FieldDefinition._boolean:
                column = addColumn(entityName, name, a);
                addModifiers(column, field);
                addMessages(a, field);
                addDescription(a, field);
                break;
            case FieldDefinition._intEnum:
                column = addColumn(entityName, name, a);
                addModifiers(column, field);
                addDescription(a, field);
                addMessages(a, field);
                addIntEnum(field, a);
                break;
            case FieldDefinition._char:
                column = addColumn(entityName, name, a).addAttribute("length", field.getCharacterLenght());
                addDescription(a, field);
                addMessages(a, field);
                addModifiers(column, field);
                break;
            case FieldDefinition._ptr:
                column = addColumn(entityName, name, a).addAttribute("cascade", "all");
                try {
                    addAnnotation(MANY_TO_ONE, a).addAttribute("targetEntity",
                        Class.forName(field.getRelatedTypeName()));
                } catch (ClassNotFoundException e) {
                    postponeFieldGeneration(entityName, field, primaryKeyPropertyName);
                }
                addModifiers(column, field);
                addMessages(a, field);
                addDescription(a, field);
                break;
            case FieldDefinition._ptrOne:
                column = addColumn(entityName, name, a).addAttribute("cascade", "all").addAttribute("unique", true);
                addModifiers(column, field);
                addDescription(a, field);
                addMessages(a, field);
                try {
                    addAnnotation(MANY_TO_ONE, a).addAttribute("targetEntity",
                        Class.forName(field.getRelatedTypeName()));
                } catch (ClassNotFoundException e) {
                    postponeFieldGeneration(entityName, field, primaryKeyPropertyName);
                }
                break;
            case FieldDefinition._ptrIndex:
                column = addAnnotation(ID, a).addAttribute("column", columnName(entityName, name));
                addAnnotation("javax.persistence.GeneratedValue", a).addAttribute("strategy",
                    javax.persistence.GenerationType.IDENTITY);
                addModifiers(column, field);
                addDescription(a, field);
                break;
            case FieldDefinition._text: // not sure the definition mapping will fly
                column = addColumn(entityName, name, a).addAttribute("columnDefinition", "longtext");
                addModifiers(column, field);
                addDescription(a, field);
                addMessages(a, field);
                addAnnotation(BASIC, a).addAttribute("fetch", javax.persistence.FetchType.LAZY);
                addAnnotation(LOB, a);
                break;
            case FieldDefinition._binary: // not sure the definition mapping will fly
                column = addColumn(entityName, name, a).addAttribute("columnDefinition", "longblob");
                addModifiers(column, field);
                addDescription(a, field);
                addMessages(a, field);
                addAnnotation(BASIC, a).addAttribute("fetch", javax.persistence.FetchType.LAZY);
                addAnnotation(LOB, a);
                break;
            case FieldDefinition._set:
                try {
                    addAnnotation(MANY_TO_MANY, a).addAttribute("mappedBy", "primaryKey").addAttribute("targetEntity",
                        Class.forName(field.getRelatedTypeName()));
                } catch (ClassNotFoundException e1) {
                    postponeFieldGeneration(entityName, field, primaryKeyPropertyName);
                }
                addDescription(a, field);

                // see http://java.sun.com/javaee/6/docs/api/javax/persistence/JoinTable.html
                AbstractAnnotation joinTable = addAnnotation(JOIN_TABLE, a).addAttribute("name",
                    tableName(field.getMappingTableName())).addAttribute("cascade", javax.persistence.CascadeType.ALL);

                // || Type1_PK || Type1_PK_M | Type2_PK_M || Type2_PK ||

                // this maps Type1_PK_M
                joinTable.addNestedAnnotation("joinColumns", JOIN_COLUMN).addAttribute("name", primaryKeyPropertyName).addAttribute(
                    "referencedColumnName", columnName(field.getMappingTableName(), primaryKeyPropertyName));

                // this maps Type2_PK_M
                joinTable.addNestedAnnotation("inverseJoinColumns", JOIN_COLUMN).addAttribute("name",
                    field.getSetMappingColumnName()).addAttribute("referencedColumnName",
                    columnName(field.getMappingTableName(), field.getSetMappingColumnName()));
                break;
            case FieldDefinition._setComplex:
            case FieldDefinition._setCharEnum:
                addInnerSet(entityName, primaryKeyPropertyName, field, a);
                addDescription(a, field);
                break;
            case FieldDefinition._setIntEnum:
                addInnerSet(entityName, primaryKeyPropertyName, field, a);
                addDescription(a, field);
                addIntEnum(field, a);
                break;
            case FieldDefinition._ptrRel:
                // reverse mapping from ManyToOne to the owning side (OneToMany)
                addAnnotation(MANY_TO_ONE, a);
                addAnnotation(JOIN_COLUMN, a).addAttribute("name", field.getName()).addAttribute(
                    "referencedColumnName", columnName(entityName, field.getName())).addAttribute("nullable", false);
                break;
        }
        return a;
    }

    private void addInnerSet(String entityName, String primaryKeyPropertyName, FieldDataDTO field,
            Vector<AbstractAnnotation> a) {
        try {
            addAnnotation(ONE_TO_MANY, a).addAttribute("cascade", javax.persistence.CascadeType.ALL).addAttribute(
                "mappedBy", "primaryKey").addAttribute("targetEntity",
                Class.forName(className(field.getRelatedTypeName())));
        } catch (ClassNotFoundException e) {
            postponeFieldGeneration(entityName, field, primaryKeyPropertyName);
        }
    }

    private void addIntEnum(FieldDataDTO field, Vector<AbstractAnnotation> a) {
        AbstractAnnotation enums = addAnnotation(MAKUMBA_ENUM, a);
        Vector<AbstractAnnotation> elements = new Vector<AbstractAnnotation>();
        for (Integer key : field.getIntEnumValues().keySet()) {
            AbstractAnnotation e = addAnnotation(MAKUMBA_ENUM_ELEMENT, elements);
            e.addAttribute("key", key);
            e.addAttribute("value", field.getIntEnumValues().get(key));
        }
        for (Integer key : field.getIntEnumValuesDeprecated().keySet()) {
            AbstractAnnotation e = addAnnotation(MAKUMBA_ENUM_ELEMENT, elements);
            e.addAttribute("key", key);
            e.addAttribute("value", field.getIntEnumValuesDeprecated().get(key));
            e.addAttribute("deprecated", true);
        }
        enums.addAttribute("value", elements);
    }

    private AbstractAnnotation addColumn(String entityName, String name, Vector<AbstractAnnotation> a) {
        return addAnnotation(COLUMN, a).addAttribute("name", columnName(entityName, name));
    }

    private void addDescription(Vector<AbstractAnnotation> a, FieldDataDTO field) {
        addAnnotation(DESCRIPTION, a).addAttribute("value", field.getDescription());
    }

    private void addModifiers(AbstractAnnotation aa, FieldDataDTO field) {
        if (field.isFixed()) {
            aa.addAttribute("updatable", false);
        }
        if (field.isNotNull()) {
            aa.addAttribute("nullable", false);
        }
        if (field.isUnique()) {
            aa.addAttribute("unique", true);
        }
    }

    private void addMessages(Vector<AbstractAnnotation> a, FieldDataDTO field) {
        if (field.getMessages().size() > 0) {
            AbstractAnnotation m = addAnnotation(MESSAGES, a);
            Vector<AbstractAnnotation> messages = new Vector<AbstractAnnotation>();
            for (MessageType t : field.getMessages().keySet()) {
                if (field.getMessages().get(t) != null) {
                    addAnnotation(MESSAGE, messages).addAttribute("type", t).addAttribute("message",
                        field.getMessages().get(t));
                }
            }
            m.addAttribute("value", messages);
        }

    }

    /**
     * recover by appending the field to the ones to be processed later on
     */
    private void postponeFieldGeneration(String entityName, FieldDataDTO field, String primaryKeyPropertyName) {
        appendToClass.put(entityName + "####" + field.getName() + "####" + primaryKeyPropertyName, field);
    }

    /**
     * Append a field to an existing class
     */
    public void appendToClass(String entityName, FieldDataDTO field, String primaryKeyPropertyName) {

        String type = null;
        String name = field.getName();
        switch (field.getType()) {
            case FieldDefinition._ptr:
            case FieldDefinition._ptrOne:
                type = className(field.getRelatedTypeName());
                break;
            case FieldDefinition._set:
            case FieldDefinition._setIntEnum:
            case FieldDefinition._setCharEnum:
            case FieldDefinition._setComplex:
                type = "java.util.Collection";
                break;
        }

        classWriter.appendField(className(entityName), name, type, generatedClassesPath);

        Vector<AbstractAnnotation> annotations = generateAnnotations(entityName, name, primaryKeyPropertyName, field);
        classWriter.appendAnnotations(className(entityName), "get" + StringUtils.capitalize(name), annotations,
            generatedClassesPath);

    }

    private AbstractAnnotation addAnnotation(String name, Vector<AbstractAnnotation> v) {
        AbstractAnnotation b = classWriter.createAnnotation(name);
        v.add(b);
        return b;
    }

    /**
     * Converts to the database-level column name
     */
    private String columnName(String entityName, String name) {
        ddp.initializeNameResolver(nr, entityName);
        String n = nr.resolveFieldName(entityName, name);
        if (n == null) {
            throw new MakumbaError("Fail with " + name);
        }
        return n;
    }

    /**
     * Converts to the database-level table name
     * 
     * @param entityName
     * @return
     */
    private String tableName(String entityName) {
        ddp.initializeNameResolver(nr, entityName);
        return nr.resolveTypeName(entityName);
    }

    /**
     * Converts to a valid Java class name
     * 
     * @param entityName
     * @return
     */
    private String className(String entityName) {
        return NameResolver.arrowToDoubleUnderscore(entityName);
    }

    public static void main(String... args) {
        DataDefinitionProvider ddp = DataDefinitionProvider.getInstance();
        Vector<String> dds = new Vector<String>();
        dds.add("test.Person");
        dds.add("test.Individual");
        ddp.generateEntityClasses(dds);
    }

}
