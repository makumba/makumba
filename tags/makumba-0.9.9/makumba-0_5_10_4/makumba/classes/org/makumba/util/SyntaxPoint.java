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

/** Keep track of important points in a file. Syntax points are typically stored in a sorted set, and used for syntax colouring. A syntax colourer would load the file, then go thru the syntax points and change the coloring context at each syntax point. 
 * This is an abstract class, but can be easily instantiated with an anonymous inner class like this:
 * 	SyntaxPoint comment=new SyntaxPoint(position)
 *	  { public String getType(){ return "JSPcomment";}};

 * idea: the class can also find identifiers in the text and ask the owners of other enclosing syntax points (or the owner of the whole text info) if they can account for them. e.g. in the case of a makumba JSP, a LIST tag can account for a OQL label, an MDD field, a MDD name or a $attribute). if yes, syntax points are produced by the respective entity, and presented in different colours or as links
 * idea: a syntax point walker that goes thru a text and invokes methods of a syntax colourer like beginText(), beginJSPComment, endJspComment, beginTag... etc
 * @author cristi
 */
public abstract class SyntaxPoint implements Comparable
{
  /** constructor */
  public SyntaxPoint(int position, boolean begin)
  { this.position=position; this.begin=begin; }
  /** simpler constructor */
  public SyntaxPoint(int position)
  { this.position=position; this.begin=true; }

  /** the position in the file  */
  public int getPosition(){return position; }

  /** the line number in the file  */
  public int getLine(){return line; }

  /** the column number in the file  */
  public int getColumn(){return column; }

  /** is this point a begin or an end of something? ends are stored one position after the last character of the entity so substring() works right away */
  public boolean isBegin() {return begin; }

  /** the type of this point, will be defined by the subclass */
  public abstract String getType();

  /** subclasses can return other info */
  public Object getOtherInfo(){ return null; }

  /** the file that contains this point */
  public java.io.File getFile(){ return sourceFile.file ; }

  /** make an end for a given syntax point */
  static public SyntaxPoint makeEnd(SyntaxPoint begin, int position)
  {
    return new End(begin, position);
  }

  /** the position in the file */
  int position;
  
  /** is this point a begin or an end of something (where applicable)*/
  boolean begin;
  
  /** redundant but useful data: line of the position in the text */
  int line;

  /** redundant but useful data: column of the position in the text */
  int column;

  /** the file in which this syntax point was detected */
  SourceSyntaxPoints sourceFile;

  /** for sorting in the syntaxPoints collection */
  public int compareTo(Object o)
  { 
    SyntaxPoint sp= (SyntaxPoint)o;
    int n=position-sp.position;

    if(n!=0)   // order by position
      return n;

    if(begin == sp.begin)  /* two things begin at the same place? strange. but possible, e.g. lines can begin or end at the same place where tags begin or end. at some point there should be a special case here for lines begins and ends to be before, respectively after anything else. */
      {
	if(sp.getType().equals("TextLine"))
	  return sp.begin?1:-1;
	if(getType().equals("TextLine"))
	  return begin?-1:1;
	return getType().compareTo(sp.getType());  // a random, really...
      }

    // the thing that begins must come after the thing that ends
    return begin?1:-1;
  }

  /** simple comparison, for hashing reasons */
  public boolean equals(Object o)
  {
    SyntaxPoint sp= (SyntaxPoint)o;
    return sp!=null && position==sp.position && begin==sp.begin && getType()==sp.getType();
  }
  /** for hashing*/
  public int hashCode() { return position*getType().hashCode()*(begin?1:-1); }
  /** for debugging */
  public String toString(){ return ""+position+":"+(begin?"<"+getType():getType()+">"); }

  /** a simple implementation: ends of strings marked by other syntax points */
  public static class End extends SyntaxPoint
  {
    SyntaxPoint begin;
    /** constructor */
    public End(SyntaxPoint begin, int position)
    {
      super(position, false);
      this.begin=begin;
    }
    /** returns the begining */
    public Object getOtherInfo(){ return begin; }
    /** returns same type as the begining */
    public String getType(){ return begin.getType(); }
  }


}



