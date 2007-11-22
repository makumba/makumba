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

package org.makumba.list.tags;

import java.util.Vector;

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;


/**
 * Extra information for the attributes
 * @author Cristian Bogdan
 *
 */
public class AttrTEI extends TagExtraInfo {
    public VariableInfo[] getVariableInfo(TagData data) {
        Vector v = new Vector();

        String var = data.getAttributeString("var");
        if (var != null)
            v.addElement(new VariableInfo(var, "java.lang.Object", true, VariableInfo.AT_BEGIN));

        var = data.getAttributeString("exceptionVar");
        if (var != null)
            v.addElement(new VariableInfo(var, "java.lang.Throwable", true, VariableInfo.AT_BEGIN));

        return CountTEI.vector2VarInfo(v);
    }
}
