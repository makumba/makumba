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

package org.makumba.util;

/**
 * Keeps track of important points in a file. Syntax points are typically stored in a sorted set, and used for syntax
 * colouring. A syntax colourer would load the file, then go through the syntax points and change the coloring context
 * at each syntax point. This is an abstract class, but can be easily instantiated with an anonymous inner class like
 * this: SyntaxPoint comment=new SyntaxPoint(position) { public String getType(){ return "JSPcomment";}};
 * 
 * idea: the class can also find identifiers in the text and ask the owners of other enclosing syntax points (or the
 * owner of the whole text info) if they can account for them. For example in the case of a makumba JSP, a LIST tag can
 * account for a OQL label, an MDD field, a MDD name or a $attribute). If yes, syntax points are produced by the
 * respective entity, and presented in different colours or as links
 * 
 * idea: a syntax point walker that goes through a text and invokes methods of a syntax colourer like beginText(),
 * beginJSPComment, endJspComment, beginTag... etc
 * 
 * @author Cristian Bogdan
 * @version $Id$
 */
public abstract class SyntaxPoint implements Comparable {
    /**
     * Default constructor
     * 
     * @param position
     *            the position of this SyntaxPoint
     * @param begin
     *            is this SyntaxPoint the beginning
     */
    public SyntaxPoint(int position, boolean begin) {
        this.position = position;
        this.begin = begin;
    }

    /** Simpler constructor */
    public SyntaxPoint(int position) {
        this.position = position;
        this.begin = true;
    }

    /**
     * Gets the position in the file
     * 
     * @return the position in the file
     */
    public int getPosition() {
        return position;
    }

    /**
     * Gets the offset by which this SyntaxPoint has moved from the original position due to includes
     * 
     * @return the offset caused by inclusions
     */
    public int getIncludeOffset() {
        return offset;
    }

    public int getOriginalPosition() {
        return getPosition() - getIncludeOffset();
    }

    /**
     * Gets the line number in the file
     * 
     * @retun the line number in the file
     */
    public int getLine() {
        return line;
    }

    /**
     * Gets the column number in the file
     * 
     * @return the column number in the file
     */
    public int getColumn() {
        return column;
    }

    /**
     * This is a temporary workaround for '@included' files not having their syntaxpoints & offsets detected correctly.
     * 
     * @param lineLength
     *            the length of the line
     * @return the correct column
     */
    public int getOriginalColumn(int lineLength) {
        if (getColumn() > lineLength + 1) {
            return getColumn() - getIncludeOffset();
        } else {
            return getColumn();
        }
    }

    /**
     * Checks whether this this point is the beginning of the end of something. Ends are stored one position after the
     * last character of the entity so substring() works right away
     * 
     * @return <code>true</code> if this is a beginning, <code>false</code> otherwise
     */
    public boolean isBegin() {
        return begin;
    }

    /** The type of this point, will be defined by the subclass */
    public abstract String getType();

    /** Additional information which can be returned by subclasses */
    public Object getOtherInfo() {
        return null;
    }

    /** The file that contains this point */
    public java.io.File getFile() {
        return sourceFile.file;
    }

    /**
     * Makes an end for a given syntax point
     * 
     * @param begin
     *            the begin SyntaxPoint
     * @param position
     *            the position of the end
     */
    static public SyntaxPoint makeEnd(SyntaxPoint begin, int position) {
        return new End(begin, position);
    }

    /** The position in the file */
    int position;

    /** The offset sufferred due to includes */
    int offset = 0;

    /**
     * Adjusts the offset and position at inclusion of a file
     * 
     * @param delta
     *            the length of the included file
     */
    void moveByInclude(int delta) {
        position += delta;
        offset += delta;
    }

    /** Is this point a begin or an end of something (where applicable) */
    boolean begin;

    /** Redundant but useful data: line of the position in the text */
    int line;

    /** Redundant but useful data: column of the position in the text */
    int column;

    /** The file in which this syntax point was detected */
    SourceSyntaxPoints sourceFile;

    /**
     * Compares two objects, for sorting in the syntaxPoints collection
     * 
     * @param o
     *            object to be compared with
     * @return -1 if this is before, 1 if after
     */
    public int compareTo(Object o) {
        SyntaxPoint sp = (SyntaxPoint) o;
        int n = position - sp.position;

        if (n != 0) // order by position
            return n;

        if (begin == sp.begin) /*
                                 * two things begin at the same place? strange. but possible, e.g. lines can begin or
                                 * end at the same place where tags begin or end. at some point there should be a
                                 * special case here for lines begins and ends to be before, respectively after anything
                                 * else.
                                 */
        {
            if (sp.getType().equals("TextLine"))
                return sp.begin ? 1 : -1;
            if (getType().equals("TextLine"))
                return begin ? -1 : 1;
            return getType().compareTo(sp.getType()); // a random, really...
        }

        // the thing that begins must come after the thing that ends
        return begin ? 1 : -1;
    }

    /**
     * Performs a simple comparison, for hashing reasons
     * 
     * @param o
     *            the object to be compared with
     * @return <code>true</code> if the two objects are equal, <code>false</code> otherwise
     */
    public boolean equals(Object o) {
        SyntaxPoint sp = (SyntaxPoint) o;
        return sp != null && position == sp.position && begin == sp.begin && getType() == sp.getType();
    }

    /**
     * Generates the hashcode of the SyntaxPoint
     * 
     * @return The computed hashcode
     */
    public int hashCode() {
        return position * getType().hashCode() * (begin ? 1 : -1);
    }

    /**
     * Prints the position and type of the SyntaxPoint, for debugging
     * 
     * @return A String that can be used for debugging
     */
    public String toString() {
        return "" + position + ":" + (begin ? "<" + getType() : getType() + ">");
    }

    /**
     * A simple implementation: ends of strings marked by other syntax points
     * 
     * @author Cristian Bogdan
     */
    public static class End extends SyntaxPoint {
        SyntaxPoint _begin;

        /** constructor */
        public End(SyntaxPoint begin, int position) {
            super(position, false);
            this._begin = begin;
        }

        /** returns the begining */
        public Object getOtherInfo() {
            return _begin;
        }

        /** returns same type as the begining */
        public String getType() {
            return _begin.getType();
        }
    }

}