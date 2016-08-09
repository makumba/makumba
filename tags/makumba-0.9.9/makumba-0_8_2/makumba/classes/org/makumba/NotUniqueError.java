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

import org.makumba.db.makumba.sql.SQLUpdate;

/**
 * An insert in a certain type has violated a unique constraint.<br>
 * Note: this class has mostly been replaced by {@link NotUniqueException} in combination with
 * {@link CompositeValidationException}, which has more or less the same functionality as this class used to have, but
 * is ready for form annotation. This class is used onyl in
 * {@link SQLUpdate#execute(org.makumba.db.DBConnection, Object[])}, usage there should also be stopped.
 */
public class NotUniqueError extends DBError {
    private static final long serialVersionUID = 1L;

    public NotUniqueError(java.sql.SQLException se) {
        super("Not unique exception. " + se.getMessage());
    }

}
