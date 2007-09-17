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

package org.makumba;

import java.util.Dictionary;
import java.util.Enumeration;

import org.makumba.util.StringUtils;

/** An insert in a certain type has violated a unique constraint */
public class NotUniqueError extends DBError {
    private static final long serialVersionUID = 1L;

    String type;

    Dictionary duplicates;

    public NotUniqueError(java.sql.SQLException se) {
        super("Not unique exception. " + se.getMessage());
    }

    /** Build a NotUniqueError for the given type, with the duplicated field names and values indicated as a Dictionary */
    public NotUniqueError(String type, Dictionary duplicates) {
        super(makeMessage(type, duplicates));
        this.type = type;
        this.duplicates = duplicates;
    }

    static String makeMessage(String type, Dictionary duplicates) {
        StringBuffer sb = new StringBuffer();
        String separator = "";
        for (Enumeration e = duplicates.keys(); e.hasMoreElements();) {
            Object field = e.nextElement();
            sb.append(separator);
            if (field instanceof String[]) { // multi-field uniquness error
                sb.append("The field-combination ").append(StringUtils.toString((String[]) field)).append(
                    " in the record ").append(type).append(
                    " only accepts unique values. There is however already an entry that has its values set to ").append(
                    StringUtils.toString((Object[]) duplicates.get(field))).append("!");
            } else { // other errors (should be all single fields
                sb.append("The field <").append(field).append("> in the record ").append(type).append(
                    " only accepts unique values. There is however already an entry that has its value set to '").append(
                    duplicates.get(field)).append("'!");
            }
            sb.append("\nPlease go back and correct the values in the form!");
            separator = "\n";
        }
        return sb.toString();
    }

    /** return the type where the duplicate was attempted */
    public String getType() {
        return type;
    }

    /** return the list of fields that were attempted be duplicated */
    public Enumeration getDuplicateFieldNames() {
        return duplicates.keys();
    }

    /** return the value that was attempted be duplicated for the given field */
    public Object getDuplicateForField(String field) {
        return duplicates.get(field);
    }
}
