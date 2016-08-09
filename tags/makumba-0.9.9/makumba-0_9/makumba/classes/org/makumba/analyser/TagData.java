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

package org.makumba.analyser;

import java.io.Serializable;
import java.util.Map;

import org.makumba.analyser.engine.SyntaxPoint;

/**
 * A composite object passed to the analyzers.
 * 
 * @author Cristian Bogdan
 * @author Manuel Gay
 * @author Rudolf Mayer
 * @version $Id$
 */
public class TagData extends ElementData implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Name of the tag */
    public String name;

    /** Number of the node in the graph of tags of the page * */
    public int nodeNumber;

    /** Tag attributes */
    public Map<String, String> attributes;

    /** Tag object, if one is created by the analyzer */
    public AnalysableTag tagObject;

    /** The {@link TagData} of the tag closing element */
    public TagData closingTagData;

    public TagData(String name, SyntaxPoint start, SyntaxPoint end, Map<String, String> attributes) {
        this.name = name;
        this.sourceSyntaxPoints = start.getSourceSyntaxPoints();
        this.startLine = start.getLine();
        this.startColumn = start.getColumn();
        this.endLine = end.getLine();
        this.endColumn = end.getColumn();
        this.attributes = attributes;
    }

    public AnalysableTag getTagObject() {
        return tagObject;
    }

    @Override
    public String toString() {
        return "Tag " + name + " on " + getLocation() + ", attributes: " + attributes
                + (closingTagData != null ? " (ends on " + closingTagData.getLocation() + ")" : "");
    }

    /** Checks whether this {@link ElementData} is declared after the closing tag of the given {@link ElementData} */
    public boolean afterClosing(TagData el) {
        if (el.closingTagData != null) {
            return after(el.closingTagData);
        } else {
            return after(el);
        }
    }

    public void setClosingTagData(TagData closingTagData) {
        this.closingTagData = closingTagData;
    }
}