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

import java.util.Vector;

import org.makumba.Transaction;

public abstract class AbstractQueryRunner {
    public static AbstractQueryRunner makeQueryRunner(String db, ComposedQuery cq){
        if(!cq.useHibernate)
            return new MakumbaQueryRunner(db);
        return null;
    }
    public abstract Vector execute(String query, Object[] args, int offset, int limit);
    public abstract void close();
}

class MakumbaQueryRunner extends AbstractQueryRunner{
    //      org.makumba.controller.http.RequestAttributes.getConnectionProvider
    //((javax.servlet.http.HttpServletRequest)pageContext.getRequest()).

    Transaction tr;
    MakumbaQueryRunner(String db){ tr= org.makumba.MakumbaSystem.getConnectionTo(db); }
    
    public Vector execute(String query, Object[] args, int offset, int limit){ return tr.executeQuery(query, args, offset, limit); }
    public void close(){ tr.close(); }
}
