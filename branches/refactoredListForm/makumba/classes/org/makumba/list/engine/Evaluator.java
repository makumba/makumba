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

package org.makumba.list.engine;

import javax.servlet.jsp.PageContext;


/** 
 * An evaluator using the EL engine to evaluate all occurences of #{...} in a string
 * @author Cristian Bogdan
 * @version $Id$
 */
public class Evaluator implements ComposedQuery.Evaluator {
    PageContext pc;

    public Evaluator(PageContext pc) {
        this.pc = pc;
    }
    
    public String evaluate(String s) {
        // FIXME: looking for #{....} may have to be rewritten
        StringBuffer ret = new StringBuffer();
        int begin, end;
        int last = 0;
        while (true) {
            begin = s.indexOf("#{", last);
            if (begin == -1)
                return ret.append(s.substring(last)).toString();
            ret.append(s.substring(last, begin));
            end = s.indexOf("}", begin + 2);
            if (end == -1)
                throw new org.makumba.ProgrammerError("unpaired #{ in " + s);
            try {
                ret.append(pc.getExpressionEvaluator().evaluate("$" + s.substring(begin + 1, end + 1), Object.class,
                        pc.getVariableResolver(), null)); // a:b() functions not supported yet
            } catch (javax.servlet.jsp.el.ELException ele) {
                throw new org.makumba.ProgrammerError(ele.toString());
            }
            last = end + 1;
        }
    }
}
