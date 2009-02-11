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

import java.util.Hashtable;
import java.util.Vector;

/**
 * This class provides basic support for definition parse errors.
 * 
 * @author Rudolf Mayer
 * @version $Id$
 */
public abstract class DefinitionParseError extends MakumbaError {

    private static final long serialVersionUID = 1L;

    /** put a marker for a given column */
    public static StringBuffer pointError(int column) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < column; i++) {
            sb.append(' ');
        }
        return sb.append('^');
    }

    protected static String showTypeName(String typeName) {
        if (typeName.startsWith("temp")) {
            return "";
        }
        return typeName + ":";
    }

    protected int column;

    protected Vector<DefinitionParseError> components;

    protected String line;

    protected Hashtable<String, DefinitionParseError> lines;

    protected String typeName;

    public DefinitionParseError() {
        super();
    }

    /** Construct a message for a line */
    public DefinitionParseError(String typeName, String reason, String line) {
        this(showTypeName(typeName) + reason + "\n" + line);
        this.typeName = typeName;
        this.line = line;
    }

    public DefinitionParseError(String explanation) {
        super(explanation);
    }

    public DefinitionParseError(Throwable reason) {
        super(reason);
    }

    public DefinitionParseError(Throwable reason, String expl) {
        super(reason, expl);
    }

    /** add another error to the main error */
    public void add(DefinitionParseError e) {
        if (components == null) {
            components = new Vector<DefinitionParseError>();
        }

        components.addElement(e);
        if (e.line != null) {
            if (lines == null) {
                lines = new Hashtable<String, DefinitionParseError>();
            }
            lines.put(e.line, e);
        }
    }

    /** If the error is single, call the default action, else compose all components' messages */
    @Override
    public String getMessage() {
        if (isSingle()) {
            return super.getMessage();
        }

        StringBuffer sb = new StringBuffer();

        for (DefinitionParseError definitionParseError : components) {
            sb.append('\n').append(definitionParseError.getMessage()).append('\n');
        }
        return sb.toString();
    }

    /** tells whether this error is empty or contains sub-errors */
    public boolean isSingle() {
        return components == null || components.isEmpty();
    }

    /** return the type for which the error occured */
    public String getTypeName() {
        return typeName;
    }

}