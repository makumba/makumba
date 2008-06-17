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

import org.apache.commons.lang.StringUtils;

/** This exception occurs when an invalid value is passed to a field */
public class InvalidValueException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    protected String field;

    private FieldDefinition fieldDefinition;

    protected String shortMessage;

    public InvalidValueException(String message) {
        super(message);
    }

    public InvalidValueException(String field, String message) {
        super("Invalid value for " + field + ": " + message);
        this.field = field;
        this.shortMessage = message;
    }

    public InvalidValueException(FieldDefinition fi, String message) {
        this(fi.getDataDefinition().getName() + "#" + fi.getName(), message);
        this.fieldDefinition = fi;
        this.shortMessage = message;
    }

    /** form an exception message from the required type and the pointer that doesn't respect it */
    public InvalidValueException(FieldDefinition fi, Class requiredClass, Object value) {
        this(fi, "Required Java type:" + requiredClass.getName() + " ; given value: " + value + " of type "
                + value.getClass().getName());
        this.fieldDefinition = fi;
    }

    /** form an exception message from the required type and the pointer that doesn't respect it */
    public InvalidValueException(FieldDefinition fi, String requiredType, Pointer wrongPointer) {
        this(fi, "Required poiter type:" + requiredType + " ; given value: " + wrongPointer);
        this.fieldDefinition = fi;
    }

    /** form an exception message from the compared pointer and the pointer that doesn't match its type */
    public InvalidValueException(Pointer comparedPointer, Pointer wrongPointer) {
        super("Compared pointer: " + comparedPointer + " ; given value: " + wrongPointer);
    }

    public String getFieldName() {
        if (fieldDefinition != null) {
            return fieldDefinition.getName();
        } else if (field != null) {
            return field;
        } else {
            return null;
        }
    }

    public String getShortMessage() {
        if (StringUtils.isNotEmpty(shortMessage)) {
            return shortMessage;
        } else {
            return getMessage();
        }
    }
}
