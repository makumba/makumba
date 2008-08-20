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
//  $Id: DataDefinitionParseError.java,v 2.3 2007/09/04 00:45:45 rosso_nero Exp $
//  $Name:$
/////////////////////////////////////

package org.makumba;

/**
 * Error occured during validation definition parsing. It can contain a number of errors occured at different lines
 * during parsing.
 * 
 * @author Rudolf Mayer
 * @version $Id: ValidationDefinitionParseError.java,v 1.1 Sep 16, 2007 11:10:50 PM rudi Exp $
 */
public class ValidationDefinitionParseError extends DefinitionParseError {

    private static final long serialVersionUID = 1L;

    public ValidationDefinitionParseError() {
        super();
    }

    /** Construct a message for a line */
    public ValidationDefinitionParseError(String typeName, String reason, String line) {
        super(typeName, reason, line);
    }
}
