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
 * Information about a makumba validation definition. Implementations of this interface can obtain the validation
 * definition e.g. by parsing a validation definition file, or other means.
 * 
 * @author Rudolf Mayer
 * @version $Id$
 */
public interface ValidationDefinition {
    /** Name of this validation definition, normally the same */
    public String getName();

    /** Get all validation rules for the given field name. */
    public Collection<ValidationRule> getValidationRules(String fieldName);

    /** Get the validation rule with the given rule name. */
    public ValidationRule getValidationRule(String ruledName);

    /** Add a new rule for the given field. */
    public void addRule(String fieldName, ValidationRule rule);

    /** Add several rules for the given field. */
    public void addRule(String fieldName, Collection<ValidationRule> rules);

    /** Get the data definition associated with this validation definition. */
    public DataDefinition getDataDefinition();

    public boolean hasValidationRules();
    
}
