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

import java.util.Collections;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Iterator;

import java.util.regex.Pattern;
import java.util.regex.Matcher;


/** 
 * The collection of syntax points in a source file gathered from a source analysis.
 */
public class SourceSyntaxPoints
{
  TreeSet syntaxPoints= new TreeSet();
  ArrayList lineBeginnings= new ArrayList();
  String originalText;

  /** 
   * The constructor inserts syntax points (begin and end) for every line in a text. 
   * Most syntax colourers need to do specific operations at every line 
   */
  public SourceSyntaxPoints(String text)
  {
    this.originalText=text;
    int start=0;
    int line=1;
    
    int max= text.length();
    for(int i=0; i<max; i++)
      {
         // if found "\r\n" then treat together as one line break. 
	if(text.charAt(i)=='\r')
	  {
	    if(i+1 <max && text.charAt(i+1)=='\n')
	      i++;
	  }
	else if(text.charAt(i)!='\n')
	  continue;

         // found a linebreak
	addSyntaxPointsLine(start, i, "TextLine", new Integer(line));
	start=i+1;
	line++;
      }

    // treat the last line (not ending with '\n')
    if(start<max)
      addSyntaxPointsLine(start, max, "TextLine", new Integer(line));
  }


  /** get the text of the line n */
  public String getLineText(int n)
  {
      SyntaxPoint line=(SyntaxPoint)lineBeginnings.get(n-1);
      if(n==lineBeginnings.size())
	  return originalText.substring(line.position);
	
      SyntaxPoint nextline=(SyntaxPoint)lineBeginnings.get(n);
      
      return originalText.substring(line.position, nextline.position-1);
  }


  /** Replaces comments from a text by blanks, and stores syntax points. Comment is defined by a Pattern. 
   * @return The text with comments replaced by blanks, of equal length as the input.
   */
  String unComment(String content, Pattern commentPattern, String commentPointType)
  {
    Matcher m= commentPattern.matcher(content);
    int endOfLast=0;
    StringBuffer uncommentedContent= new StringBuffer();
    while(m.find())
      {
	uncommentedContent.append(content.substring(endOfLast, m.start()));
	for(int i=m.start(); i<m.end(); i++)
	  uncommentedContent.append(' ');
	endOfLast=m.end();
	org.makumba.MakumbaSystem.getMakumbaLogger("syntaxpoint.comment").fine("UNCOMMENT " +commentPointType+ " : " +m.group());
	addSyntaxPoints(m.start(), m.end(), commentPointType, null);
      }
    uncommentedContent.append(content.substring(endOfLast));
    return uncommentedContent.toString();
  }


  /** Creates a beginning and end syntaxPoint for a syntax entity, and adds these to the collection of points.
   * @param start  the starting position
   * @param end    the end position
   * @param type   String stating the type of syntax point
   * @param extra  any extra info (e.g. the object created at the syntax point 
   * @see   addSyntaxPointsCommon(int start, int end, String type, Object extra)
   */
  public SyntaxPoint.End addSyntaxPoints(int start, int end, String type, Object extra)
  {
    SyntaxPoint.End e= addSyntaxPointsCommon(start, end, type, extra);
    setLineAndColumn(e);
    setLineAndColumn((SyntaxPoint)e.getOtherInfo());
    return e;
  }


  /** Fills in the Line and Column for the given SyntaxPoint, based on the collection of lineBeginnings syntaxPoints. */
  void setLineAndColumn(SyntaxPoint point)
  {
    SyntaxPoint lineBegin= (SyntaxPoint)lineBeginnings.get((-1)*Collections.binarySearch(lineBeginnings, point)-2);
    point.line=lineBegin.line;
    point.column=point.position-lineBegin.position+1;
  }


  /** 
   * Creates begin- and end- syntaxpoints (but without setting the line and column fields) 
   * at given location and with given info, and adds them to the collection.
   * @return   the created <tt>SyntaxPoint.End</tt>
   * @see #addSyntaxPoints(int, int, String, Object)
   */
  SyntaxPoint.End addSyntaxPointsCommon(int start, int end, String type, Object extra)
  {
    // Java Note: defining these final variables, because "An inner class defined inside a method 
    // can still access all of the member variables of the outer class, but it can only 
    // access final variables of the method."
    final String type1=type;
    final Object extra1=extra;

    SyntaxPoint point=new SyntaxPoint(start){
        public String getType(){ return type1;}
        public Object getOtherInfo(){ return extra1;}	  
      };

    syntaxPoints.add(point);
    SyntaxPoint.End theEnd= (SyntaxPoint.End)SyntaxPoint.makeEnd(point, end);
    syntaxPoints.add(theEnd);
    return theEnd;
  }


   /** 
   * Creates begin- and end- syntaxpoints for a full line in text.
   * @return   the created <tt>SyntaxPoint.End</tt>
   */
  void addSyntaxPointsLine(int start, int end, String type, Object extra)
  {
    SyntaxPoint.End e= addSyntaxPointsCommon(start, end, type, extra);
    SyntaxPoint lineBegin= (SyntaxPoint)e.getOtherInfo();
    lineBegin.line= e.line= ((Integer)lineBegin.getOtherInfo()).intValue();
    lineBegin.column=1;
    e.column= end-start+1;
    lineBeginnings.add(lineBegin);
  }


  /**
   * Takes out pieces of text according to specified start and stop symbols, and sets SyntaxPoints around the removed substrings.
   * (2003-06-18) Function added for fix bug 465, but is not used now, as 465 was fixed with regex. Leaving it as it may be used in future.
   * @param start  String that indicates the start of a substring to remove, if it's not followed by any String in startNotFollowedBy array
   * @param end    String that indicates the end of a substring to remove, if it's not preceded by any String in the endNotPrecededBy array
   * @param syntaxPointType  the type of the syntaxPoints that are created to mark the removed substrings
   */
  void takeOut(StringBuffer sourceCode, String start, String[] startNotFollowedBy, String end, String[] endNotPrecededBy, String syntaxPointType)
  {
    int startPos, startPosInner, endPos=0, i;
    boolean wrongMatch;

    while(true){
       startPos = sourceCode.indexOf(start, endPos);
       if(startPos == -1) break; // no more substrings to remove!
       
       // found start symbol: check if not followed by any of forbidden follower-symbols.
       wrongMatch = false;
       startPosInner = startPos + start.length();
       for (i=0; startNotFollowedBy!=null && i<startNotFollowedBy.length; i++ ){
          //org.makumba.MakumbaSystem.getMakumbaLogger("syntaxpoint.comment").finest( "CHECKING forbidden follower " +startNotFollowedBy[i]+ " in " + sourceCode.substring(startPosInner, startPosInner +25) );          
          if( sourceCode.indexOf( startNotFollowedBy[i], startPosInner ) == startPosInner ) { 
             //org.makumba.MakumbaSystem.getMakumbaLogger("syntaxpoint.comment").finest( "FOUND forbidden follower " +startNotFollowedBy[i] );          
             wrongMatch=true; 
             break; 
          }
       }
       if (wrongMatch) { endPos = startPos+1; continue; } // move forward by 1, look for another start symbol. 

       // start symbol 'approved'; find end symbol
       wrongMatch=true; // looking for end match
       endPos = startPosInner; //endPos holds the position from where to start searching for the end symbol
       while(wrongMatch){
          endPos = sourceCode.indexOf(end, endPos); 
          if(endPos == -1)
            throw new RuntimeException("closure missing, how did this pass tomcat's parser???");

          // found end symbol: check if not preceded by any of forbidden preceding-symbols.
          wrongMatch=false;
          for (i=0; endNotPrecededBy != null && i<endNotPrecededBy.length; i++ ){
             if( sourceCode.substring( endPos-endNotPrecededBy[i].length(), endPos ).equals(endNotPrecededBy[i]) ) { 
                wrongMatch=true; 
                break; 
             }
          }
          if (wrongMatch) {endPos++; continue; } // move forward by 1, look for another end symbol (Java note: 'continue' will continue the inner loop!)
       }
       endPos+=end.length(); 

       // found both start and end now.
       org.makumba.MakumbaSystem.getMakumbaLogger("syntaxpoint.comment").fine( "REMOVE " +syntaxPointType+ " : " +sourceCode.substring(startPos, endPos) );
       for(i=startPos; i<endPos; i++) sourceCode.setCharAt(i, ' '); 

       // add a syntax point pair for the removed substring, with the type indicated by syntaxPointType
       addSyntaxPoints(startPos, endPos, syntaxPointType, null);
    }

  } //end function takeOut.

}// end class
