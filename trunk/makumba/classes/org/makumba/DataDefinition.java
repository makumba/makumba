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
import java.util.Dictionary;
import java.util.Iterator;
import java.util.Vector;

import org.makumba.commons.StringUtils;

/**
 * Information about a makumba data definition as obtained from an MDD file or the structure of an OQL query result.
 * This class is provided for makumba programs to be able to introspect makumba data structures. Such introspection is
 * not needed usually, as the application programmer knows the makumba data structure.
 */
public interface DataDefinition {

    /** name of this data definition */
    public String getName();

    /** the names of the fields declared in this data definition, in the order of declaration */
    public java.util.Vector getFieldNames();

    /** the field with the respective name, null if such a field doesn't exist */
    public FieldDefinition getFieldDefinition(String name);

    /** the field with the respective index, null if such a field doesn't exist */
    public FieldDefinition getFieldDefinition(int n);

    /**
     * tells whether this data definition was generated temporarily to depict a query result as opposed to being read
     * from an MDD file
     */
    public boolean isTemporary();

    /** The title field indicated, or the default one */
    public String getTitleFieldName();

    /** The name of the index field (primary key), if any? */
    public String getIndexPointerFieldName();

    /** The name of the creation timestamp field, if any? */
    public String getCreationDateFieldName();

    /** The name of the modification timestamp field, if any? */
    public String getLastModificationDateFieldName();

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

    /** Checks whether all fieldnames exist in the database */
    public void checkFieldNames(Dictionary d);

    /** Indicates when the data definition was modified the last time */
    public long lastModified();

    /** Get the validation definition associated with this data definition. */
    public ValidationDefinition getValidationDefinition();

    /** Get all multiple-feld uniqueness definition. */
    public MultipleUniqueKeyDefinition[] getMultiFieldUniqueKeys();

    /** Add a multiple-feld uniqueness definition. */
    public void addMultiUniqueKey(MultipleUniqueKeyDefinition definition);

    /** Check whether this data definition has a multi-field uniqe key defined with the given fields. */
    public boolean hasMultiUniqueKey(String[] fieldNames);

    /** Gets all the fields that are references to other tables, i.e. pointers and some types of sets. */
    public Vector getReferenceFields();

    public QueryFragmentFunction getFunction(String name);

    public void addFunction(String name, QueryFragmentFunction function);

    class QueryFragmentFunction implements Serializable {
        private static final long serialVersionUID = 1L;

        private String name;

        private String queryFragment;

        private DataDefinition parameters;

        public QueryFragmentFunction(String name, String queryFragment, DataDefinition parameters) {
            super();
            this.name = name;
            this.queryFragment = queryFragment;
            this.parameters = parameters;
        }

        public String getName() {
            return name;
        }

        public DataDefinition getParameters() {
            return parameters;
        }

        public String getQueryFragment() {
            return queryFragment;
        }

        @Override
        public String toString() {
            String s = "";
            Vector fieldNames = getParameters().getFieldNames();
            for (Iterator iter = fieldNames.iterator(); iter.hasNext();) {
                String name = (String) iter.next();
                s += getParameters().getFieldDefinition(name).getType() + " " + name;
                if (iter.hasNext()) {
                    s += ", ";
                }
            }
            s += "";
            return "QueryFragment Function: " + getName() + "(" + s + ")" + " = " + queryFragment;
        }
    }

    /** Data structure holding the definition of a mult-field unique key. */
    class MultipleUniqueKeyDefinition implements Serializable {
        private static final long serialVersionUID = 1L;

        private String[] fields;

        private String line;

        /** indicates whether this key spans over subfields (internal or external sets, or pointer). */
        private boolean keyOverSubfield = false;

        public MultipleUniqueKeyDefinition(String[] fields, String line) {
            this.fields = fields;
            this.line = line;
        }

        public String[] getFields() {
            return fields;
        }

        public String getLine() {
            return line;
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
