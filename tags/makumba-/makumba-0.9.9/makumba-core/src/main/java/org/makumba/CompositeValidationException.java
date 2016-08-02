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
import java.util.Collection;

import org.makumba.commons.SerializedGenericMultiValueMap;

/**
 * This class holds several {@link InvalidValueException} of the same form together.<br/>
 * When performing custom form validation in business logics, you should instantiate an instance of this class, and keep
 * on adding all {@link InvalidValueException} encountered. Finally, trigger the exception throwing by calling
 * {@link #throwCheck()}<br/>
 * 
 * @author Rudolf Mayer
 * @version $Id$
 */

public class CompositeValidationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private SerializedGenericMultiValueMap<InvalidValueException> exceptionsHash = new SerializedGenericMultiValueMap<InvalidValueException>();

    /** Creates an empty instance */
    public CompositeValidationException() {
    }

    /** Creates an instance and adds all exceptions from the given {@link Iterable} */
    public CompositeValidationException(Iterable<InvalidValueException> exceptions) {
        for (InvalidValueException e : exceptions) {
            addException(e);
        }
    }

    /** Creates an instance and adds all exceptions from the given array */
    public CompositeValidationException(InvalidValueException... exceptions) {
        for (InvalidValueException e : exceptions) {
            addException(e);
        }
    }

    public ArrayList<InvalidValueException> getExceptions() {
        return new ArrayList<InvalidValueException>(exceptionsHash.values());
    }

    /** Adds a new exception */
    public void addException(InvalidValueException e) {
        if (e.getFieldName() != null) {
            exceptionsHash.put(e.getFieldName(), e);
        } else {
            exceptionsHash.put("__makumba__unassigned__", e);
        }
    }

    /** Checks whether there are any exceptions gathered, and if so throws this {@link CompositeValidationException} */
    public boolean throwCheck() throws CompositeValidationException {
        if (exceptionsHash.size() > 0) {
            throw this;
        } else {
            return false;
        }
    }

    /** Prints the messages of all exceptions gathered */
    @Override
    public String toString() {
        StringBuilder message = new StringBuilder("\n");
        for (InvalidValueException e : exceptionsHash.values()) {
            message.append("<span class=\"invalidValueException\">").append(e.getMessage()).append("</span>");
            message.append("\n");
        }
        return message.toString();
    }

    /** returns the value of {@link #toString()} */
    @Override
    public String getMessage() {
        return toString();
    }

    /** Gets the exceptions gathered for a specific field name */
    public Collection<InvalidValueException> getExceptions(String fieldName) {
        return exceptionsHash.get(fieldName);
    }

}
