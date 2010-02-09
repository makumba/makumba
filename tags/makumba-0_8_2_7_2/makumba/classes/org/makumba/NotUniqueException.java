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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Not-unique is a special case of an {@link InvalidValueException} - the value is syntactically correct, but is
 * restricted to only one usage. This exception can be used for single-field and multi-field uniqueness with the
 * respective constructors.
 * 
 * @author Rudolf Mayer
 * @version $Id$
 */
public class NotUniqueException extends InvalidValueException {
    private static final long serialVersionUID = 1L;
    
    private Map<String, String> fields;
    
    public NotUniqueException(String message) {
        super(message);
    }

    public NotUniqueException(String primaryField, String message) {
        super(message);
        this.field = primaryField;
    }

    /** Uniqueness violation for a single field. */
    public NotUniqueException(FieldDefinition fd, Object value) {
        super(fd, "Allows only unique values - an entry with the value " + getValueForMessage(fd, value)
                + " already exists!");
    }
    
    /** gets the field-value pairs for the conflicting fields **/
    public Map<String, String> getFields() {
        return this.fields;
    }
    
    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }

    private static Object getValueForMessage(FieldDefinition fd, Object value) {
        if (value != null && value.equals("")) {
            return "empty";
        }
        return "'" + value + "'";
    }

}
