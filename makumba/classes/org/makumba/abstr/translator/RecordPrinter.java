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

package org.makumba.abstr.translator;
import org.makumba.abstr.RecordInfo;

/** This class translates a Data Definition to a new language (such as a new version of the Data Definition)
 */
public class RecordPrinter extends org.makumba.abstr.RecordHandler
{
  String separator= super.toStringSeparator();


  public RecordPrinter(String path)  
  {
    this(RecordInfo.getRecordInfo(path));
  }

  public RecordPrinter(RecordInfo ri)
  {
    super(ri);
    separator= System.getProperty("line.separator")+ri.fieldPrefix();
  }

  protected String toStringBefore()
  { return ""; }

  protected String toStringSeparator() { return ""; }

  public static void main(String [] argv)
  {
    if(argv.length==0)
      return;
    System.out.println(new RecordPrinter(argv[0]));
  }
}
