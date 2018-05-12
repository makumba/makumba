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

//TODO extra comments about changes from refactoring

package org.makumba;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.makumba.commons.StringUtils;

/**
 * Information about a makumba data definition as obtained from an MDD file or the structure of an MQL query result.
 * This class is provided for makumba programs to be able to introspect makumba data structures. Such introspection is
 * not needed usually, as the application programmer knows the makumba data structure.
 */
public interface DataDefinition {

    String createName = "TS_create";

    String modifyName = "TS_modify";

    /** name of this data definition */
    public String getName();

    /** the names of the fields declared in this data definition, in the order of declaration */
    @Deprecated
    public java.util.Vector<String> getFieldNames();

    /** The FieldDefinitions in this data definition, as immutable list */
    public List<FieldDefinition> getFieldDefinitions();

    /** the field with the respective name, null if such a field doesn't exist */
    public FieldDefinition getFieldDefinition(String name);

    /** the field with the respective index, null if such a field doesn't exist */
    public FieldDefinition getFieldDefinition(int n);

    /** Returns a field definition that is either contained in this data definition, or in a pointed type. */
    public FieldDefinition getFieldOrPointedFieldDefinition(String name);

    /** Returns a query function that is either contained in this data definition, or in a pointed type. */
    public QueryFragmentFunction getFunctionOrPointedFunction(String name);

    /**
     * tells whether this data definition was generated temporarily to depict a query result as opposed to being read
     * from an MDD file
     */
    public boolean isTemporary();

    /** The title field indicated, or the default one */
    public String getTitleFieldName();

    /** The name of the index field (primary key), if any? */
    public String getIndexPointerFieldName();

    // TODO the two methods beneath are actually not needed but not using them
    // somehow breaks the TableManager

    /** The name of the creation timestamp field, if any? */
    public String getCreationDateFieldName();

    /** The name of the modification timestamp field, if any? */
    public String getLastModificationDateFieldName();

    public QueryFragmentFunctions getFunctions();

    /**
     * If this type is the data pointed to by a 1-1 pointer or subset, return the field definition in the main record,
     * otherwise return null
     */
    public FieldDefinition getParentField();

    /** The name of the set member (Pointer, Character or Integer), for set subtypes */
    public String getSetMemberFieldName();

    /** The name of the pointer to the main table, for set and internal set subtypes */
    public String getSetOwnerFieldName();

    /** Add a new field definition. Works only for temporary data definitions */
    public void addField(FieldDefinition fd);

    /** Indicates when the data definition was modified the last time */
    public long lastModified();

    /** Get all multiple-field uniqueness definition. */
    public MultipleUniqueKeyDefinition[] getMultiFieldUniqueKeys();

    /** Add a multiple-field uniqueness definition. */
    public void addMultiUniqueKey(MultipleUniqueKeyDefinition definition);

    /** Check whether this data definition has a multi-field uniqe key defined with the given fields. */
    public boolean hasMultiUniqueKey(String[] fieldNames);

    /** Data structure holding the definition of a mult-field unique key. */
    class MultipleUniqueKeyDefinition implements Serializable {
        private static final long serialVersionUID = 1L;

        private String[] fields;

        private String line;

        private String errorMessage;

        /** indicates whether this key spans over subfields (internal or external sets, or pointer). */
        private boolean keyOverSubfield = false;

        public MultipleUniqueKeyDefinition(String[] fields, String line, String errorMessage) {
            this(fields, errorMessage);
            this.line = line;
        }

        public MultipleUniqueKeyDefinition(String[] fields, String errorMessage) {
            this.fields = fields;
            this.errorMessage = errorMessage;
        }

        public String[] getFields() {
            return fields;
        }

        public String getLine() {
            return line;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public String getErrorMessage(Object[] values) {
            if (org.apache.commons.lang.StringUtils.isNotBlank(errorMessage)) {
                return errorMessage;
            } else {
                return "The field-combination " + Arrays.toString(fields)
                        + " allows only unique values - an entry with the values " + Arrays.toString(values)
                        + " already exists!";
            }
        }

        public void setKeyOverSubfield(boolean keyOverSubfield) {
            this.keyOverSubfield = keyOverSubfield;
        }

        public boolean isKeyOverSubfield() {
            return keyOverSubfield;
        }

        @Override
        public String toString() {
            return "Multi-field unique key over: " + StringUtils.toString(fields);
        }

    }

}
