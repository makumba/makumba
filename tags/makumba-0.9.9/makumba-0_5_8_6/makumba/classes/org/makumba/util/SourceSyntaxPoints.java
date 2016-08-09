package org.makumba.util;

import java.util.Collections;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Iterator;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/** The collection of syntax points gathered from a source analysis */
public class SourceSyntaxPoints
{
  TreeSet syntaxPoints= new TreeSet();
  ArrayList lineBeginings= new ArrayList();
    String originalText;

  /** at creation, inserts syntax points (begin and end) for every line in a text. Most syntax colourers need to do specific operations at every line */
  public SourceSyntaxPoints(String text)
  {
      originalText=text;
    int start=0;
    int line=1;
    
    int max= text.length();
    for(int i=0; i<max; i++)
      {
	if(text.charAt(i)=='\r')
	  {
	    if(i+1 <max && text.charAt(i+1)=='\n')
	      i++;
	  }
	else if(text.charAt(i)!='\n')
	  continue;
	addSyntaxPointsLine(start, i, "TextLine", new Integer(line));
	start=i+1;
	line++;
      }
    if(start<max)
      addSyntaxPointsLine(start, max, "TextLine", new Integer(line));
  }

  /** get the text of the line n */
  public String getLineText(int n)
  {
      SyntaxPoint line=(SyntaxPoint)lineBeginings.get(n-1);
      if(n==lineBeginings.size())
	  return originalText.substring(line.position);
	
      SyntaxPoint nextline=(SyntaxPoint)lineBeginings.get(n);
      
      return originalText.substring(line.position, nextline.position-1);
  }

  /** go thru the comments in a text, defined according to a pattern, return the text uncommented (for further processing) but of the same length by replacing every comment with whitespace, put the comments limits in the syntax point set */
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
	org.makumba.MakumbaSystem.getMakumbaLogger("syntaxpoint.comment").fine(m.group());
	addSyntaxPoints(m.start(), m.end(), commentPointType, null);
      }
    uncommentedContent.append(content.substring(endOfLast));
    return uncommentedContent.toString();
  }

  /** add a begining and end for a syntax entity 
   * @param syntaxPoints the collection of points
   * @param start the starting position
   * @param end the end position
   * @param type the type of syntax point
   * @param extra any extra info (e.g. the object created at the syntax point 
   */
  public SyntaxPoint.End addSyntaxPoints(int start, int end, String type, Object extra)
  {
    SyntaxPoint.End e= addSyntaxPointsCommon(start, end, type, extra);
    setLineAndColumn(e);
    setLineAndColumn((SyntaxPoint)e.getOtherInfo());
    return e;
  }

  void setLineAndColumn(SyntaxPoint point)
  {
    SyntaxPoint lineBegin= (SyntaxPoint)lineBeginings.get((-1)*Collections.binarySearch(lineBeginings, point)-2);
    point.line=lineBegin.line;
    point.column=point.position-lineBegin.position+1;
  }

		       
  SyntaxPoint.End addSyntaxPointsCommon(int start, int end, String type, Object extra)
  {
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

  void addSyntaxPointsLine(int start, int end, String type, Object extra)
  {
    SyntaxPoint.End e= addSyntaxPointsCommon(start, end, type, extra);
    SyntaxPoint lineBegin= (SyntaxPoint)e.getOtherInfo();
    lineBegin.line= e.line= ((Integer)lineBegin.getOtherInfo()).intValue();
    lineBegin.column=1;
    e.column= end-start+1;
    lineBeginings.add(lineBegin);
  }

}
