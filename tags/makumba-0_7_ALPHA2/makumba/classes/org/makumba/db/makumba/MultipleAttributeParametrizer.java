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
//  $Id: MultipleAttributeParametrizer.java 1141 2006-01-15 11:07:07Z cristian_bogdan $
//  $Name$
/////////////////////////////////////

package org.makumba.db.makumba;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.makumba.ProgrammerError;
import org.makumba.LogicException;
import org.makumba.commons.ArgumentReplacer;
import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;
import org.makumba.commons.RuntimeWrappedException;

/** Take care of multiple parameters, as a "decorator" of AttributeParametrizer which knows nothing about them */
public class MultipleAttributeParametrizer {
    // all argument names, multiple or not
    ArrayList<String> mixedArgumentNames = new ArrayList<String>();

    String baseOQL;

    NamedResources parametrizers;

    public String getTransformedQuery(Map args) throws LogicException {
        return getAttributeParametrizer(args).getTransformedQuery(args);
    }
    
    public Object[] getTransformedParams(Map args) throws LogicException{
        return getAttributeParametrizer(args).getTansformedParams(rewriteAttributes(args));
    }
    public MultipleAttributeParametrizer(String oql) {
        parametrizers = new NamedResources("JSP attribute parametrizer objects", parametrizerFactory);
        for (Iterator<String> e = new ArgumentReplacer(oql).getArgumentNames(); e.hasNext();)
            mixedArgumentNames.add((String)e.next());
        baseOQL = oql;
    }

    /** obtain the attribute parametrizer associuated to the length of the given attributes */
    public AttributeParametrizer getAttributeParametrizer(Map args) throws LogicException {
        try {
            return (AttributeParametrizer) parametrizers.getResource(args);
        } catch (RuntimeWrappedException e) {
            Throwable t = e.getCause();
            if (t instanceof LogicException)
                throw (LogicException) t;
            throw e;
        }
    }

    /**
     * a cache of attribute parametrizers with the key "length of each argument"
     */

    NamedResourceFactory parametrizerFactory = new NamedResourceFactory() {
        
        private static final long serialVersionUID = 1L;

        protected Object getHashObject(Object nm) throws Exception {
            StringBuffer sb = new StringBuffer();
            Map args = (Map) nm;
            for (Iterator<String> e = mixedArgumentNames.iterator(); e.hasNext();) {
                String name =  e.next();
                Object o = args.get(name);
                if (o instanceof List)
                    sb.append(((List) o).size());
                else
                    sb.append(1);
                sb.append(" ");
            }
            return sb.toString();
        }

        protected Object makeResource(Object nm, Object hashName) throws Exception {
            return new AttributeParametrizer(rewriteOQL((Map) nm));
        }
    };

    /** rewrite an OQL string to replace all multiple arguments $xxx with $xxx_1, $xxx_2, etc */
    public String rewriteOQL(Map args) throws LogicException {
        String workingOQL = baseOQL;

        for (Iterator<String> e = mixedArgumentNames.iterator(); e.hasNext();) {
            String name = e.next();
            Object o = args.get(name);
            if (o instanceof List)
                workingOQL = multiplyParameter(workingOQL, name, ((List) o).size());
        }
        return workingOQL;
    }

    /** rewrite an OQL $name with $name_1, $name_2, etc */
    public static String multiplyParameter(String oql, String name, int n) {
        StringBuffer sb = new StringBuffer();
        int i;
        String separator;

        while (true) {
            i = oql.indexOf("$" + name);
            if (i == -1) {
                sb.append(oql);
                return sb.toString();
            }
            sb.append(oql.substring(0, i));
            oql = oql.substring(i + name.length() + 1);
            separator = "";
            for (int j = 1; j <= n; j++) {
                sb.append(separator).append("$").append(name).append("_").append(j);
                separator = ",";
            }
        }
    }

    public Map<String, Object> rewriteAttributes(Map args) throws LogicException {
        Map<String, Object> ret = new HashMap<String, Object>();

        for (Iterator<String> e = mixedArgumentNames.iterator(); e.hasNext();) {
            String name = (String) e.next();
            Object o = args.get(name);
            if(o == null) throw new ProgrammerError("The argument '"+name+"' should not be null");
            if (o instanceof List) {
                List v = (List) o;
                for (int i = 1; i <= v.size(); i++)
                    ret.put(name + "_" + i, v.get(i - 1));
            } else
                ret.put(name, o);
        }
        return ret;
    }

}
