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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/** Error occured during data definition parsing. It can contain a number of errors occured at different lines during parsing */
public class DataDefinitionParseError extends MakumbaError
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
String typeName;
  String line;
  int column;

  public DataDefinitionParseError(){}

  /** Construct a message from the given explanation */
  public DataDefinitionParseError(String explanation){ super(explanation); }
 
  static String showTypeName(String typeName)
  {
    if(typeName.startsWith("temp"))
      return "";
    return typeName+":";
  }

  /** Construct a message for an error that is due to an IOException */
  public DataDefinitionParseError(String typeName, java.io.IOException e) 
  { 
    super(showTypeName(typeName)+e.toString());
    this.typeName= typeName;
  }

  /** Construct a message for a type */
  public DataDefinitionParseError(String typeName, String reason) 
  { 
    super(showTypeName(typeName)+reason);
    this.typeName= typeName;
  }
  
  /** Construct a message for a line */
  public DataDefinitionParseError(String typeName, String reason, String line)
  {
    super(showTypeName(typeName)+reason+"\n"+line);
    this.typeName= typeName;
    this.line=line;
  }

  /** Construct a message for a line and column */
  public DataDefinitionParseError(String typeName, String reason, String line, int column)
  {
    super(showTypeName(typeName)+reason+"\n"+line+"\n"+pointError(column));
    this.typeName= typeName;
    this.line=line;
    this.column=column;
  }

  /** return the type for which the error occured */
  public String getTypeName(){ return typeName; }
 
  /** put a marker for a given column */
  static public StringBuffer pointError(int column)
  {
    StringBuffer sb= new StringBuffer();
    for(int i=0; i< column; i++)
      sb.append(' ');
    return sb.append('^');
  }

  Vector components;
  Hashtable lines;

  /** tells whether this error is empty or contains sub-errors*/
  public boolean isSingle() { return components==null || components.isEmpty(); }
  
  /** add another error to the main error */
  public void add(DataDefinitionParseError e)
  {
    if(components==null)
      components= new Vector();

    components.addElement(e); 
    if(e.line!=null)
      {
	if(lines==null)lines= new Hashtable();
	lines.put(e.line, e);
      }
  }
  
  /** If the error is single, call the default action, else compose all components' messages */
  public String getMessage()
  {
    if(isSingle())
      return super.getMessage();

    StringBuffer sb= new StringBuffer();

    for(Enumeration e= components.elements(); e.hasMoreElements(); )
      sb.append('\n')
	.append(((DataDefinitionParseError)e.nextElement()).getMessage())
	.append('\n');
    return sb.toString();
  }
}
