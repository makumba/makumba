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

package org.makumba.abstr.printer;
import org.makumba.DataDefinition;
import org.makumba.MakumbaSystem;

/** This class just prepares a more complex toString. The toString is still
  (like in the generic RecordHandler) a concatenation of the FieldHandlers toString, but those FieldHandlers are different in this handler family. The FieldHandlers are built according to rules in org.makumba/abstr/printer/redirectPrinter.properties :
<pre>
ptrIndex=Field
dateCreate=Field
dateModify=Field
date=Field
text=Field
int=Field

#not needed, but anyway
char= char          

#not needed, but anyway
charEnum= charEnum

#not needed, but anyway
intEnum= intEnum

#not needed, but anyway
ptr=ptr

#not needed, but anyway
ptrRel=ptrRel

ptrOne=subtable
set=subtable
setcharEnum=subtable
setintEnum=subtable
setComplex=subtable
</pre>
* @see org.makumba.abstr.printer.FieldPrinter
* @see org.makumba.abstr.printer.charPrinter
* @see org.makumba.abstr.printer.charEnumPrinter
* @see org.makumba.abstr.printer.intEnumPrinter
* @see org.makumba.abstr.printer.ptrPrinter
* @see org.makumba.abstr.printer.ptrRelPrinter
* @see org.makumba.abstr.printer.subtablePrinter
 */
public class RecordPrinter extends org.makumba.abstr.RecordHandler
{
  String separator= super.toStringSeparator();
    
  public RecordPrinter(String path)  
  {
    this(MakumbaSystem.getDataDefinition(path));
  }

  public RecordPrinter(DataDefinition ri)
  {
    super(ri);
    separator= System.getProperty("line.separator") +((org.makumba.abstr.RecordInfo)ri).fieldPrefix();
  }

  protected String toStringBefore() 
  { return getDataDefinition().getName()+" title: "+getDataDefinition().getTitleFieldName()+separator; }
  
  protected String toStringSeparator() { return separator; }
  
  public static void main(String [] argv) 
  {
    try{
      if(argv.length==0)
	return;
      System.out.println(new RecordPrinter(argv[0]));  
    }
    catch(org.makumba.MakumbaError e){System.err.println(e.getMessage()); }
    
  }
}
