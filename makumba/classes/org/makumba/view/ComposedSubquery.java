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

package org.makumba.view;
import java.util.*;

/** a subquery of a composed query */
public class ComposedSubquery extends ComposedQuery
{
  /** the enclosing query */
  ComposedQuery superQuery;

  /** make a subquery of the indicated query, from the given sections */
  public ComposedSubquery(String[] subsections, ComposedQuery cq) 
  { 
    super(subsections);
    superQuery=cq; 
    superQuery.addSubquery(this);
    derivedSections= new String[4];
    derivedSections[FROM]=superQuery.derivedSections[FROM];
    if(sections[FROM]!=null)
      derivedSections[FROM]+=","+sections[FROM];
    concat(derivedSections, superQuery.derivedSections, sections, WHERE, " AND ", true);
    //    concat(derivedSections, superQuery.derivedSections, sections, GROUPBY, ",", false);
    String gpb= sections[GROUPBY];
    if(gpb!=null)
      derivedSections[GROUPBY]=gpb;

    //    concat(derivedSections, superQuery.derivedSections, sections, "ORDERBY", ",");
    String order=sections[ORDERBY];
    if(order!=null)
      derivedSections[ORDERBY]=order;
  }

  /** concatenate sections on the given index, with the given separator */
  static void concat(String[] result, String[] h1, String[] h2, int what, String sep, boolean paran)
  {
    String lp="";
    String rp="";
    if(paran)
      { lp="("; rp=")"; }
    String s1=h1[what];
    String s2=h2[what];
    if(s1!=null && s1.trim().length()==0)
      s1=null;

    if(s2!=null && s2.trim().length()==0)
      s2=null;

    if(s1==null && s2!=null)
      {
	result[what]= s2;
	return; 
      }
    if(s2==null &&  s1!=null)
      {
	result[what]= s1;
	return;
      }
    if(s2!=null &&  s1!=null)
      result[what]=lp+s1+rp+sep+lp+s2+rp;
  }

  /** initialize the keysets by adding the superquery's previousKeyset to its keyset */
  protected void initKeysets()
  {
    previousKeyset=(Vector)superQuery.previousKeyset.clone();
    keyset=(Vector)superQuery.keyset.clone();
    keysetLabels= (Vector)superQuery.keysetLabels.clone();

    for(Enumeration e= keysetLabels.elements(); e.hasMoreElements(); )
      addProjection((String)e.nextElement());
    previousKeyset.addElement(superQuery.keyset);
  }

}
