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

package org.makumba.view.html;
import org.makumba.*;
import org.makumba.abstr.*;
import org.makumba.view.*;
import java.util.*;

public class RecordViewer extends RecordFormatter
{
  public RecordViewer(ComposedQuery q) { super(q); }
  public RecordViewer(RecordInfo ri, Hashtable h) { super(ri, h); }  

  protected String applyParameters(FieldFormatter ff, Dictionary formatParams, String s)
  {
    if(formatParams.get("urlEncode")!=null)
      return java.net.URLEncoder.encode(s);
    return s;
  }

}
