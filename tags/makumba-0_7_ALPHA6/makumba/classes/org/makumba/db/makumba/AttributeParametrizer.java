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
//  $Id: AttributeParametrizer.java 1141 2006-01-15 11:07:07Z cristian_bogdan $
//  $Name$
/////////////////////////////////////

package org.makumba.db.makumba;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.makumba.LogicException;
import org.makumba.commons.ArgumentReplacer;

/** Map $name to $n */
public class AttributeParametrizer {
    /** names of all arguments, to keep an order */
    List <String>argumentNames = new ArrayList<String>();

    String oql;

    public String getTransformedQuery(Map args) {
        return oql;
    }
    
    /** build a parametrizer from an OQL query, in the given database and with the given example arguments */
    public AttributeParametrizer(String oql) throws LogicException {
        ArgumentReplacer ar = new ArgumentReplacer(oql);

        for (Iterator<String> e = ar.getArgumentNames(); e.hasNext();)
            argumentNames.add(e.next());

        Map<String, Object> d = new HashMap<String, Object>();
        for (int i = 0; i < argumentNames.size(); i++)
            d.put(argumentNames.get(i), "$" + (i + 1));

        this.oql = ar.replaceValues(d);
    }
    

    /** execute the query */
    public Object[] getTansformedParams(Map<String, Object> a) {
        Object args[] = new Object[argumentNames.size()];
        for (int i = 0; i < args.length; i++)
            args[i] = a.get((String) argumentNames.get(i));
        return args;
    }

}
