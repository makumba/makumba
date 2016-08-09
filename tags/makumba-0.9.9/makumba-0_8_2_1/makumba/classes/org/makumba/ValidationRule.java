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

import java.util.Collection;

/**
 * Defines the basics of a Makumba Validation rule.
 * 
 * @author Rudolf Mayer
 * @author Manuel Gay
 * @version $Id$
 */
public interface ValidationRule extends Comparable<ValidationRule> {
    /**
     * Perform the validation process.
     * 
     * @throws InvalidValueException
     *             if the validation fails
     */
    public boolean validate(Object value) throws InvalidValueException;

    /** Gets the error message that should be shown for this rule. */
    public String getErrorMessage();

    /** Gets the name of this rule. */
    public String getRuleName();

    /** Gets the {@link FieldDefinition} this rule applies to. */
    public FieldDefinition getFieldDefinition();
    
    /** Gets the arguments (field names) this rule applies to **/
    public Collection<String> getValidationRuleArguments();
    
    /** Gets the {@link DataDefinition} this rule applies to **/
    public DataDefinition getDataDefinition();
}
